package io.github.lllfjjj.tutor.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

interface LessonRepository extends Repository<Lesson, String> {
	Optional<Lesson> findById(String id);
	List<Lesson> findAllByOrderByPosition();
	List<Lesson> findByQuestionBankFalseOrderByPosition();

	@Query("""
			select lesson.id from Lesson lesson where lesson.questionBank = true
			and exists (select question.id from Question question where question.lessonId = lesson.id)
			""")
	List<String> bankIds();

	@Query("""
			select lesson.id as id, lesson.track as track, lesson.title as title, lesson.objective as objective,
			       count(question.id) as questionCount
			from Lesson lesson join Question question on question.lessonId = lesson.id
			where lesson.questionBank = true and (:track is null or lesson.track = :track)
			  and (:pattern = '' or function(regexp_like as Boolean, lesson.title, :pattern) = true
			    or function(regexp_like as Boolean, lesson.objective, :pattern) = true
			    or exists (select candidate.id from Question candidate
			               where candidate.lessonId = lesson.id and function(regexp_like as Boolean, candidate.prompt, :pattern) = true))
			group by lesson.id, lesson.track, lesson.title, lesson.objective, lesson.position
			order by lesson.position
			""")
	List<BankRow> bankSets(Track track, String pattern);

	interface BankRow {
		String getId();
		Track getTrack();
		String getTitle();
		String getObjective();
		long getQuestionCount();
	}
}
