package io.github.lllfjjj.tutor.practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.lllfjjj.tutor.catalog.Catalog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PracticeService {
	private final Catalog catalog;
	private final AttemptRepository attempts;

	PracticeService(Catalog catalog, AttemptRepository attempts) {
		this.catalog = catalog;
		this.attempts = attempts;
	}

	@Transactional
	public long submit(String lessonId, MultiValueMap<String, String> selections) {
		var questions = catalog.gradingQuestions(lessonId);
		if (questions.isEmpty() || selections.size() != questions.size()) {
			throw new InvalidSubmission();
		}
		var answers = new ArrayList<AttemptAnswer>();
		for (var question : questions) {
			long questionId = question.prompt().id();
			var selected = selections.get("answer-" + questionId);
			if (selected == null || selected.size() != 1
					|| question.prompt().options().stream().noneMatch(option -> option.key().equals(selected.getFirst()))) {
				throw new InvalidSubmission();
			}
			String choice = selected.getFirst();
			answers.add(new AttemptAnswer(questionId, choice, question.correctOption().equals(choice)));
		}
		return attempts.save(new Attempt(lessonId, answers)).id;
	}

	@Transactional(readOnly = true)
	public Result result(long attemptId) {
		var attempt = attempts.findById(attemptId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到这次作答"));
		var questions = catalog.gradingQuestions(attempt.lessonId).stream()
				.collect(Collectors.toMap(question -> question.prompt().id(), Function.identity()));
		var answers = attempt.answers.stream().map(answer -> {
			var question = questions.get(answer.questionId);
			return new AnswerResult(answer.questionId, question.prompt().text(), answer.selectedOption,
					question.optionLabel(answer.selectedOption), question.correctOption(),
					question.optionLabel(question.correctOption()), answer.correct, question.explanation(), question.source());
		}).toList();
		return new Result(catalog.summary(attempt.lessonId), answers.stream().filter(AnswerResult::correct).count(), answers);
	}

	static final class InvalidSubmission extends RuntimeException {}

	public record Result(Catalog.LessonCard lesson, long score, List<AnswerResult> answers) {}
	public record AnswerResult(long questionId, String prompt, String selectedKey, String selectedLabel, String correctKey,
							   String correctLabel, boolean correct, String explanation, Catalog.Source source) {}
}
