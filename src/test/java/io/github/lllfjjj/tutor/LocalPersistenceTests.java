package io.github.lllfjjj.tutor;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.lllfjjj.tutor.catalog.Catalog;
import io.github.lllfjjj.tutor.practice.LearningProgress;
import io.github.lllfjjj.tutor.practice.PracticeService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.util.LinkedMultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

class LocalPersistenceTests {
	@Test
	void upgradesVersionThreeDatabaseWithoutReplacingHistory(@TempDir Path temporaryDirectory) throws SQLException {
		String jdbcUrl = "jdbc:h2:file:" + temporaryDirectory.resolve("legacy").toAbsolutePath();
		Flyway.configure().dataSource(jdbcUrl, "sa", "").placeholderReplacement(false).target("3").load().migrate();
		try (var connection = DriverManager.getConnection(jdbcUrl, "sa", "");
			 var statement = connection.createStatement()) {
			statement.executeUpdate("insert into attempt (id, lesson_id) values (42, 'java-core')");
			statement.executeUpdate("""
					insert into attempt_answer values (42, 7, 'A', false), (42, 8, 'A', true), (42, 9, 'C', true)
					""");
		}
		try (var upgraded = new SpringApplicationBuilder(TutorApplication.class).web(WebApplicationType.NONE)
				.run("--spring.datasource.url=" + jdbcUrl)) {
			assertThat(upgraded.getBean(Catalog.class).bankQuestionCount()).isEqualTo(144);
			var result = upgraded.getBean(PracticeService.class).result(42);
			assertThat(result.score()).isEqualTo(2);
			assertThat(result.answers()).hasSize(3);
			var progress = upgraded.getBean(LearningProgress.class);
			assertThat(progress.overview().completed()).isOne();
			assertThat(progress.overview().bankCompleted()).isZero();
			assertThat(progress.mistakes()).singleElement().satisfies(mistake ->
					assertThat(mistake.attemptId()).isEqualTo(42));
		}
	}

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
