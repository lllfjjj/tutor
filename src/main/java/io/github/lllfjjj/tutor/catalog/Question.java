package io.github.lllfjjj.tutor.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
class Question {
	@Id Long id;
	String lessonId;
	String prompt;
	@Column(name = "option_a") String optionA;
	@Column(name = "option_b") String optionB;
	@Column(name = "option_c") String optionC;
	String correctOption;
	String explanation;
	String sourceType;
	String sourceTitle;
	String sourceUrl;
	String referenceUrl;
}
