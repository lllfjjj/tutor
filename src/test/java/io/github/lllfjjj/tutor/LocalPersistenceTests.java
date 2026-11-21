package io.github.lllfjjj.tutor;

import java.nio.file.Path;

import io.github.lllfjjj.tutor.practice.LearningProgress;
import io.github.lllfjjj.tutor.practice.PracticeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.util.LinkedMultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

class LocalPersistenceTests {
	@Test
	void reopensStudyHistoryFromTheSameFile(@TempDir Path temporaryDirectory) {
		String jdbcUrl = "jdbc:h2:file:" + temporaryDirectory.resolve("tutor").toAbsolutePath();
		var answers = new LinkedMultiValueMap<String, String>();
		answers.add("answer-7", "A");
		answers.add("answer-8", "A");
		answers.add("answer-9", "C");

		long attemptId;
		try (var first = new SpringApplicationBuilder(TutorApplication.class).web(WebApplicationType.NONE)
				.run("--spring.datasource.url=" + jdbcUrl)) {
			attemptId = first.getBean(PracticeService.class).submit("java-core", answers);
		}
		assertThat(temporaryDirectory.resolve("tutor.mv.db")).exists();
		try (var second = new SpringApplicationBuilder(TutorApplication.class).web(WebApplicationType.NONE)
				.run("--spring.datasource.url=" + jdbcUrl)) {
			var result = second.getBean(PracticeService.class).result(attemptId);
			assertThat(result.score()).isEqualTo(2);
			assertThat(result.lesson().id()).isEqualTo("java-core");
			var progress = second.getBean(LearningProgress.class);
			assertThat(progress.overview().completed()).isOne();
			assertThat(progress.mistakes()).singleElement().satisfies(mistake ->
					assertThat(mistake.attemptId()).isEqualTo(attemptId));
		}
	}
}
