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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:progress")
@AutoConfigureMockMvc
class LearningProgressTests {
	@Autowired MockMvc mvc;
	@Autowired JdbcTemplate jdbc;

	@Test
	void latestAnswerControlsReviewButRepeatsDoNotInflateProgress() throws Exception {
		mvc.perform(get("/mistakes")).andExpect(status().isOk())
				.andExpect(content().string(containsString("暂无错题")));
		mvc.perform(get("/progress")).andExpect(status().isOk())
				.andExpect(content().string(containsString("0 / 6")));

		String first = submitJava("A");
		mvc.perform(get("/mistakes")).andExpect(status().isOk())
				.andExpect(content().string(containsString("LessonKey.equals")))
				.andExpect(content().string(containsString("href=\"" + first + "#question-7\"")));
		mvc.perform(get("/api/lessons/java-core")).andExpect(jsonPath("$.completed").value(true));
		mvc.perform(get("/api/lessons/spring-backend")).andExpect(jsonPath("$.completed").value(false));

		submitJava("B");
		mvc.perform(get("/mistakes")).andExpect(status().isOk())
				.andExpect(content().string(containsString("暂无错题")))
				.andExpect(content().string(not(containsString("LessonKey.equals"))));
		mvc.perform(get("/progress")).andExpect(content().string(containsString("1 / 6")));
		mvc.perform(get(first)).andExpect(status().isOk())
				.andExpect(content().string(containsString("2 / 3")))
				.andExpect(content().string(containsString("id=\"question-7\"")));

		String third = submitJava("C");
		mvc.perform(get("/mistakes")).andExpect(content().string(containsString(third)))
				.andExpect(content().string(not(containsString("href=\"" + first + "#"))));
		mvc.perform(get("/progress")).andExpect(content().string(containsString("1 / 6")));
		assertThat(jdbc.queryForObject("select count(*) from attempt where lesson_id = 'java-core'", Integer.class))
				.isEqualTo(3);
	}

	private String submitJava(String firstAnswer) throws Exception {
		return mvc.perform(post("/lessons/java-core/attempts").param("answer-7", firstAnswer)
						.param("answer-8", "A").param("answer-9", "C"))
				.andExpect(status().isSeeOther()).andReturn().getResponse().getRedirectedUrl();
	}
}
