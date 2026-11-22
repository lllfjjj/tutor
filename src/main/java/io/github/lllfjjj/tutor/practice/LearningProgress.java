package io.github.lllfjjj.tutor.practice;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.lllfjjj.tutor.catalog.Catalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LearningProgress {
	private final Catalog catalog;
	private final AttemptRepository attempts;

	LearningProgress(Catalog catalog, AttemptRepository attempts) {
		this.catalog = catalog;
		this.attempts = attempts;
	}

	public boolean completed(String lessonId) {
		return attempts.existsByLessonId(lessonId);
	}

	public Set<String> completedIds() {
		return Set.copyOf(attempts.completedLessonIds());
	}

	public Progress overview() {
		var completedIds = completedIds();
		var routes = catalog.routes().stream().map(route -> new RouteStatus(route, completedIds)).toList();
		long completed = routes.stream().mapToLong(RouteStatus::completed).sum();
		long total = routes.stream().mapToLong(route -> route.route().lessons().size()).sum();
		var bankIds = catalog.bankIds();
		long bankCompleted = bankIds.stream().filter(completedIds::contains).count();
		return new Progress(routes, completed, total, total == 0 ? 0 : Math.round(completed * 100.0 / total),
				bankCompleted, bankIds.size());
	}

	public List<Mistake> mistakes() {
		Map<String, Catalog.LessonCard> lessons = catalog.allLessonCards().stream()
				.collect(Collectors.toMap(Catalog.LessonCard::id, Function.identity()));
		return attempts.latestMistakes().stream()
				.map(item -> new Mistake(lessons.get(item.getLessonId()), item.getQuestionId(),
						item.getAttemptId(), item.getPrompt()))
				.toList();
	}

	public record RouteStatus(Catalog.Route route, Set<String> completedIds) {
		public long completed() {
			return route.lessons().stream().filter(lesson -> completedIds.contains(lesson.id())).count();
		}
	}

	public record Progress(List<RouteStatus> routes, long completed, long total, long percent,
						   long bankCompleted, long bankTotal) {}
	public record Mistake(Catalog.LessonCard lesson, long questionId, long attemptId, String prompt) {}
}
