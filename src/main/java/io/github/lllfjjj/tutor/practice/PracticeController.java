package io.github.lllfjjj.tutor.practice;

import jakarta.servlet.http.HttpServletResponse;
import io.github.lllfjjj.tutor.catalog.Catalog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
class PracticeController {
	private final Catalog catalog;
	private final PracticeService practice;

	PracticeController(Catalog catalog, PracticeService practice) {
		this.catalog = catalog;
		this.practice = practice;
	}

	@PostMapping("/lessons/{lessonId}/attempts")
	ModelAndView submit(@PathVariable String lessonId, @RequestParam MultiValueMap<String, String> selections,
						HttpServletResponse response) {
		try {
			long attemptId = practice.submit(lessonId, selections);
			var redirect = new RedirectView("/attempts/" + attemptId, true);
			redirect.setStatusCode(HttpStatus.SEE_OTHER);
			return new ModelAndView(redirect);
		} catch (PracticeService.InvalidSubmission error) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return new ModelAndView("lesson")
					.addObject("page", catalog.lesson(lessonId))
					.addObject("selections", selections.toSingleValueMap())
					.addObject("formError", "请为每题选择一个有效选项，再提交。");
		}
	}

	@GetMapping("/attempts/{attemptId}")
	String result(@PathVariable long attemptId, Model model) {
		model.addAttribute("result", practice.result(attemptId));
		return "result";
	}
}
