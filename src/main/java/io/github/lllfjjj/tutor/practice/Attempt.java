package io.github.lllfjjj.tutor.practice;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;

@Entity
class Attempt {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
	String lessonId;
	@ElementCollection
	@CollectionTable(name = "attempt_answer", joinColumns = @JoinColumn(name = "attempt_id"))
	@OrderBy("questionId ASC")
	List<AttemptAnswer> answers = new ArrayList<>();

	protected Attempt() {}

	Attempt(String lessonId, List<AttemptAnswer> answers) {
		this.lessonId = lessonId;
		this.answers.addAll(answers);
	}
}
