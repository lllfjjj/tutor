package io.github.lllfjjj.tutor.catalog;

import java.util.List;
import org.springframework.data.repository.Repository;

interface QuestionRepository extends Repository<Question, Long> {
	List<Question> findByLessonIdOrderById(String lessonId);
}
