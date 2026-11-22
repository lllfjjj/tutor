package io.github.lllfjjj.tutor.catalog;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Catalog {
	private final LessonRepository lessons;
	private final QuestionRepository questions;

	Catalog(LessonRepository lessons, QuestionRepository questions) {
		this.lessons = lessons;
		this.questions = questions;
	}

	public List<Route> routes() {
		var cards = lessons.findByQuestionBankFalseOrderByPosition().stream().map(Catalog::card).toList();
		return Arrays.stream(Track.values())
				.map(track -> new Route(track, cards.stream().filter(card -> card.track() == track).toList()))
				.toList();
	}

	public List<LessonCard> allLessonCards() {
		return lessons.findAllByOrderByPosition().stream().map(Catalog::card).toList();
	}

	public List<String> bankIds() {
		return lessons.bankIds();
	}

	public List<BankSet> bankSets(Track track, String query) {
		String pattern = query.isEmpty() ? "" : "(?iu)" + Pattern.quote(query);
		return lessons.bankSets(track, pattern).stream()
				.map(row -> new BankSet(new LessonCard(row.getId(), row.getTrack(), row.getTitle(), row.getObjective(), true),
						row.getQuestionCount()))
				.toList();
	}

	public long bankQuestionCount() {
		return questions.bankQuestionCount();
	}

	public LessonCard summary(String lessonId) {
		return card(requireLesson(lessonId));
	}

	public LessonPage lesson(String lessonId) {
		var lesson = requireLesson(lessonId);
		var prompts = questions.findByLessonIdOrderById(lessonId).stream()
				.map(Catalog::prompt)
				.toList();
		return new LessonPage(card(lesson), lesson.body, lesson.example, prompts);
	}

	public List<GradingQuestion> gradingQuestions(String lessonId) {
		requireLesson(lessonId);
		return questions.findByLessonIdOrderById(lessonId).stream()
				.map(question -> new GradingQuestion(prompt(question), question.correctOption,
						question.explanation, question.sourceType == null ? null : new Source(
						SourceType.valueOf(question.sourceType), question.sourceTitle,
						question.sourceUrl, question.referenceUrl)))
				.toList();
	}

	private Lesson requireLesson(String lessonId) {
		return lessons.findById(lessonId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到这节课程"));
	}

	private static LessonCard card(Lesson lesson) {
		return new LessonCard(lesson.id, lesson.track, lesson.title, lesson.objective, lesson.questionBank);
	}

	private static Prompt prompt(Question question) {
		return new Prompt(question.id, question.prompt, List.of(
				new Option("A", question.optionA), new Option("B", question.optionB),
				new Option("C", question.optionC)));
	}

	public enum SourceType {
		OFFICIAL_GUIDE("官方指南"), CANDIDATE_ACCOUNT("公开面经"), COMMUNITY("社区整理");

		public final String label;

		SourceType(String label) {
			this.label = label;
		}
	}

	public record Route(Track track, List<LessonCard> lessons) {}
	public record LessonCard(String id, Track track, String title, String objective, boolean questionBank) {}
	public record BankSet(LessonCard lesson, long questionCount) {}
	public record LessonPage(LessonCard lesson, String body, String example, List<Prompt> questions) {}
	public record Prompt(long id, String text, List<Option> options) {}
	public record Option(String key, String label) {}
	public record GradingQuestion(Prompt prompt, String correctOption, String explanation, Source source) {
		public String optionLabel(String key) {
			return prompt.options().stream().filter(option -> option.key().equals(key))
					.findFirst().orElseThrow().label();
		}
	}
	public record Source(SourceType type, String title, String url, String referenceUrl) {}
}
