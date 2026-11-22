package io.github.lllfjjj.tutor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:catalog")
@AutoConfigureMockMvc
class CatalogTests {
	@Autowired MockMvc mvc;
	@Autowired JdbcTemplate jdbc;

	@Test
	void seedsThreeTracksSixLessonsAndEighteenQuestions() {
		assertThat(jdbc.queryForObject("select count(distinct track) from lesson", Integer.class)).isEqualTo(3);
		assertThat(jdbc.queryForObject("select count(*) from lesson where not is_question_bank", Integer.class)).isEqualTo(6);
		assertThat(jdbc.queryForList("""
				select count(*) from question join lesson on lesson.id = question.lesson_id
				where not is_question_bank group by lesson_id
				""", Integer.class))
				.containsExactly(3, 3, 3, 3, 3, 3);
		assertThat(jdbc.queryForObject("""
				select count(*) from question join lesson on lesson.id = question.lesson_id
				where track = 'JAVA' and not is_question_bank and source_type is not null and source_title <> ''
				  and source_url like 'https://%' and reference_url like 'https://%'
				""", Integer.class)).isEqualTo(6);
	}

	@Test
	void lessonAndApiNeverExposeSolutions() throws Exception {
		String explanation = jdbc.queryForObject("select explanation from question where id = 7", String.class);
		mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("前后端联调")));
		mvc.perform(get("/lessons/java-core")).andExpect(status().isOk())
				.andExpect(content().string(containsString("equals")))
				.andExpect(content().string(not(containsString(explanation))))
				.andExpect(content().string(not(containsString("考点参考"))));
		mvc.perform(get("/api/lessons/java-core")).andExpect(status().isOk())
				.andExpect(content().json("""
						{"title":"Java 基础","objective":"理解对象相等、集合选择与资源关闭。","completed":false}
						""", org.springframework.test.json.JsonCompareMode.STRICT));
		mvc.perform(get("/lessons/missing")).andExpect(status().isNotFound());
		mvc.perform(get("/api/lessons/missing")).andExpect(status().isNotFound());
	}
}
