package io.github.lllfjjj.tutor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:practice")
@AutoConfigureMockMvc
class PracticeTests {
	@Autowired MockMvc mvc;
	@Autowired JdbcTemplate jdbc;

	@Test
	void gradesPersistsAndRedirectsToReadOnlyResultWithSources() throws Exception {
		var submitted = mvc.perform(submit("java-core").param("answer-7", "A")
						.param("answer-8", "A").param("answer-9", "C"))
				.andExpect(status().isSeeOther())
				.andExpect(header().string("Location", matchesPattern("/attempts/[0-9]+")))
				.andReturn();
		String location = submitted.getResponse().getRedirectedUrl();
		long attemptId = Long.parseLong(location.substring("/attempts/".length()));
		for (int refresh = 0; refresh < 2; refresh++) {
			mvc.perform(get(location)).andExpect(status().isOk())
					.andExpect(content().string(containsString("2 / 3")))
					.andExpect(content().string(containsString("考点参考")))
					.andExpect(content().string(containsString("技术依据")))
					.andExpect(content().string(containsString("公开面经")))
					.andExpect(content().string(containsString("社区整理")));
		}
		assertThat(jdbc.queryForObject("select count(*) from attempt where id = ?", Integer.class, attemptId)).isOne();
		assertThat(jdbc.queryForObject("select count(*) from attempt_answer where attempt_id = ?", Integer.class, attemptId)).isEqualTo(3);
	}

	@Test
	void rejectsMissingDuplicateIllegalAndForeignOptionsWithoutSaving() throws Exception {
		mvc.perform(submit("html-css").param("answer-1", "B").param("answer-2", "A"))
				.andExpect(status().isBadRequest()).andExpect(content().string(containsString("每题选择一个有效选项")));
		mvc.perform(submit("html-css").param("answer-1", "B", "C").param("answer-2", "A").param("answer-3", "C"))
				.andExpect(status().isBadRequest());
		mvc.perform(submit("html-css").param("answer-1", "D").param("answer-2", "A").param("answer-3", "C"))
				.andExpect(status().isBadRequest());
		mvc.perform(submit("html-css").param("answer-1", "B").param("answer-2", "A").param("answer-9", "C"))
				.andExpect(status().isBadRequest());
		assertThat(jdbc.queryForObject("select count(*) from attempt where lesson_id = 'html-css'", Integer.class)).isZero();
	}

	@Test
	void unknownLessonAndAttemptReturnNotFound() throws Exception {
		mvc.perform(submit("missing")).andExpect(status().isNotFound());
		mvc.perform(get("/attempts/999999")).andExpect(status().isNotFound());
	}

	@Test
	void rejectsCrossOriginBrowserSubmitBeforeWritingHistory() throws Exception {
		mvc.perform(submit("dom-events").header("Origin", "http://localhost:3000")
						.param("answer-4", "A").param("answer-5", "B").param("answer-6", "C"))
				.andExpect(status().isForbidden());
		mvc.perform(submit("dom-events").header("Sec-Fetch-Site", "cross-site")
						.param("answer-4", "A").param("answer-5", "B").param("answer-6", "C"))
				.andExpect(status().isForbidden());
		assertThat(jdbc.queryForObject("select count(*) from attempt where lesson_id = 'dom-events'", Integer.class)).isZero();
		mvc.perform(submit("dom-events").header("Origin", "http://localhost")
						.param("answer-4", "A").param("answer-5", "B").param("answer-6", "C"))
				.andExpect(status().isSeeOther());
	}

	private static MockHttpServletRequestBuilder submit(String lesson) {
		return post("/lessons/{lessonId}/attempts", lesson);
	}
}
