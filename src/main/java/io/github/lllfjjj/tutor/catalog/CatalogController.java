package io.github.lllfjjj.tutor.catalog;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class CatalogController {
	private final Catalog catalog;

	CatalogController(Catalog catalog) {
		this.catalog = catalog;
	}

	@GetMapping("/")
	String home(Model model) {
		model.addAttribute("routes", catalog.routes());
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
		return new LessonResponse(lesson.title(), lesson.objective(), false);
	}

	record LessonResponse(String title, String objective, boolean completed) {}
}
