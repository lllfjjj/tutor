package io.github.lllfjjj.tutor.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

interface LessonRepository extends Repository<Lesson, String> {
	Optional<Lesson> findById(String id);
	List<Lesson> findAllByOrderByPosition();
}
