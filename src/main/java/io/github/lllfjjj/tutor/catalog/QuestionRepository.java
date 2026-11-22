package io.github.lllfjjj.tutor.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

interface QuestionRepository extends Repository<Question, Long> {
	List<Question> findByLessonIdOrderById(String lessonId);

	@Query("""
			select count(question.id) from Question question join Lesson lesson on lesson.id = question.lessonId
			where lesson.questionBank = true
			""")
	long bankQuestionCount();
}
