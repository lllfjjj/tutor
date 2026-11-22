package io.github.lllfjjj.tutor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:question_bank")
@AutoConfigureMockMvc
class QuestionBankTests {
	@Autowired MockMvc mvc;
	@Autowired JdbcTemplate jdbc;

	@Test
	void seedsOneHundredFortyFourUniqueSourcedQuestionsInEighteenSets() {
		assertThat(jdbc.queryForList("""
				select count(*) from question join lesson on lesson.id = question.lesson_id
				where is_question_bank group by track
				""", Integer.class)).containsExactly(48, 48, 48);
		assertThat(jdbc.queryForList("""
				select count(*) from question join lesson on lesson.id = question.lesson_id
				where is_question_bank group by lesson_id
				""", Integer.class)).hasSize(18).containsOnly(8);
		assertThat(jdbc.queryForObject("""
				select count(distinct prompt) from question join lesson on lesson.id = question.lesson_id
				where is_question_bank
				  and source_type is not null and length(trim(source_title)) > 0
				  and source_url like 'https://%' and reference_url like 'https://%'
				  and length(trim(explanation)) > 0 and option_a <> option_b and option_a <> option_c and option_b <> option_c
				""", Integer.class)).isEqualTo(144);
	}

	@Test
	@Transactional
	void filtersByTrackAndCaseInsensitiveQuestionTextWithoutExposingSolutions() throws Exception {
		var javaSet = jdbc.queryForMap("select id, title from lesson where position = 202");
		var frontendSet = jdbc.queryForMap("select id, title from lesson where position = 101");
		mvc.perform(get("/question-bank"))
				.andExpect(status().isOk()).andExpect(content().string(containsString("144")))
				.andExpect(content().string(containsString(javaSet.get("TITLE").toString())));
		jdbc.update("update question set prompt = 'INDEX SEARCH FIXTURE %' where id = 209");
		jdbc.update("update question set prompt = 'INDEX SEARCH FIXTURE' where id = 101");
		mvc.perform(get("/question-bank").param("q", "iNdEx SeArCh FiXtUrE"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(javaSet.get("TITLE").toString())))
				.andExpect(content().string(containsString(frontendSet.get("TITLE").toString())));
		mvc.perform(get("/question-bank").param("track", "JAVA").param("q", "iNdEx SeArCh FiXtUrE"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(javaSet.get("TITLE").toString())))
				.andExpect(content().string(not(containsString(frontendSet.get("TITLE").toString()))));
		mvc.perform(get("/question-bank").param("q", "zz-no-matching-question"))
				.andExpect(status().isOk()).andExpect(content().string(containsString("没有找到符合条件的专题")));
		mvc.perform(get("/question-bank").param("q", "FiXtUrE %"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(javaSet.get("TITLE").toString())))
				.andExpect(content().string(not(containsString(frontendSet.get("TITLE").toString()))));
		mvc.perform(get("/question-bank").param("track", "UNKNOWN")).andExpect(status().isBadRequest());
		String lessonId = javaSet.get("ID").toString();
		String explanation = jdbc.queryForObject("select explanation from question where id = 209", String.class);
		mvc.perform(get("/lessons/" + lessonId)).andExpect(status().isOk())
				.andExpect(content().string(containsString("name=\"answer-209\"")))
				.andExpect(content().string(not(containsString(explanation))))
				.andExpect(content().string(not(containsString("技术依据"))));
	}

	@Test
	void submitsAFullSetAndTracksItSeparatelyFromCoreLessons() throws Exception {
		String lessonId = jdbc.queryForObject("select id from lesson where position = 201", String.class);
		var keys = jdbc.queryForList("select id, correct_option from question where lesson_id = ? order by id", lessonId);
		var first = post("/lessons/{id}/attempts", lessonId);
		var retry = post("/lessons/{id}/attempts", lessonId);
		for (var key : keys) {
			String name = "answer-" + key.get("ID");
			String correct = key.get("CORRECT_OPTION").toString();
			first.param(name, key == keys.getFirst() ? (correct.equals("A") ? "B" : "A") : correct);
			retry.param(name, correct);
		}
		String attempt = mvc.perform(first).andExpect(status().isSeeOther()).andReturn().getResponse().getRedirectedUrl();
		mvc.perform(get(attempt)).andExpect(content().string(containsString("7 / 8")));
		mvc.perform(get("/mistakes")).andExpect(status().isOk())
				.andExpect(content().string(containsString(attempt + "#question-" + keys.getFirst().get("ID"))));
		String result = mvc.perform(retry).andExpect(status().isSeeOther()).andReturn().getResponse().getRedirectedUrl();
		mvc.perform(get(result)).andExpect(status().isOk())
				.andExpect(content().string(containsString("8 / 8")))
				.andExpect(content().string(containsString("考点参考")))
				.andExpect(content().string(containsString("技术依据")));
		mvc.perform(get("/question-bank")).andExpect(content().string(containsString("已完成")));
		mvc.perform(get("/api/lessons/" + lessonId)).andExpect(jsonPath("$.completed").value(true));
		mvc.perform(get("/mistakes")).andExpect(content().string(containsString("暂无错题")));
		mvc.perform(get("/progress")).andExpect(content().string(containsString("0 / 6")))
				.andExpect(content().string(containsString("1 / 18")));
	}
}
