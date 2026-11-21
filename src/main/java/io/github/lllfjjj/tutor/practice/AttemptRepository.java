package io.github.lllfjjj.tutor.practice;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

interface AttemptRepository extends Repository<Attempt, Long> {
	Attempt save(Attempt attempt);
	Optional<Attempt> findById(Long id);

	boolean existsByLessonId(String lessonId);

	@Query("select distinct attempt.lessonId from Attempt attempt")
	List<String> completedLessonIds();

	@Query(value = """
			select attempt.lesson_id as "lessonId", answer.question_id as "questionId",
			       answer.attempt_id as "attemptId", question.prompt as "prompt"
			from attempt_answer answer
			join attempt on attempt.id = answer.attempt_id
			join question on question.id = answer.question_id
			where not answer.correct and not exists (
			    select 1 from attempt_answer newer
			    where newer.question_id = answer.question_id and newer.attempt_id > answer.attempt_id
			)
			order by answer.attempt_id desc, answer.question_id
			""", nativeQuery = true)
	List<LatestMistake> latestMistakes();

	interface LatestMistake {
		String getLessonId();
		long getQuestionId();
		long getAttemptId();
		String getPrompt();
	}
}
