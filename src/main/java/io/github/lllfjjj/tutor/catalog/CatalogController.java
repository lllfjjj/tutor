package io.github.lllfjjj.tutor.catalog;

import io.github.lllfjjj.tutor.practice.LearningProgress;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

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
		model.addAttribute("bankQuestionCount", catalog.bankQuestionCount());
		return "home";
	}

	@GetMapping("/question-bank")
	String questionBank(@RequestParam(required = false) Track track,
						@RequestParam(defaultValue = "") String q, Model model) {
		String keyword = q.strip();
		if (keyword.length() > 80) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "关键词最多 80 个字符");
		}
		model.addAttribute("tracks", Track.values());
		model.addAttribute("selectedTrack", track);
		model.addAttribute("query", keyword);
		model.addAttribute("sets", catalog.bankSets(track, keyword));
		model.addAttribute("bankQuestionCount", catalog.bankQuestionCount());
		model.addAttribute("completedIds", progress.completedIds());
		return "question-bank";
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
