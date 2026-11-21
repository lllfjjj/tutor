package io.github.lllfjjj.tutor.catalog;

import io.github.lllfjjj.tutor.practice.LearningProgress;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class CatalogController {
	private final Catalog catalog;
	private final LearningProgress progress;

	CatalogController(Catalog catalog, LearningProgress progress) {
		this.catalog = catalog;
		this.progress = progress;
	}

	@GetMapping("/")
	String home(Model model) {
		model.addAttribute("routes", catalog.routes());
		model.addAttribute("completedIds", progress.completedIds());
		return "home";
	}

	@GetMapping("/lessons/{lessonId}")
	String lesson(@PathVariable String lessonId, Model model) {
		model.addAttribute("page", catalog.lesson(lessonId));
		return "lesson";
	}

	@GetMapping("/api/lessons/{lessonId}")
	@ResponseBody
	LessonResponse api(@PathVariable String lessonId) {
		var lesson = catalog.summary(lessonId);
		return new LessonResponse(lesson.title(), lesson.objective(), progress.completed(lesson.id()));
	}

	record LessonResponse(String title, String objective, boolean completed) {}
}
