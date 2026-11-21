package io.github.lllfjjj.tutor.catalog;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
class Lesson {
	@Id String id;
	@Enumerated(EnumType.STRING) Track track;
	int position;
	String title;
	String objective;
	String body;
	String example;
}
