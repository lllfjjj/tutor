package io.github.lllfjjj.tutor.practice;

import jakarta.persistence.Embeddable;

@Embeddable
class AttemptAnswer {
	long questionId;
	String selectedOption;
	boolean correct;

	protected AttemptAnswer() {}

	AttemptAnswer(long questionId, String selectedOption, boolean correct) {
		this.questionId = questionId;
		this.selectedOption = selectedOption;
		this.correct = correct;
	}
}
