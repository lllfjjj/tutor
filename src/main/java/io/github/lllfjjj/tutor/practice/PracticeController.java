package io.github.lllfjjj.tutor.practice;

import jakarta.servlet.http.HttpServletRequest;
import io.github.lllfjjj.tutor.catalog.Catalog;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
class PracticeController {
	private final Catalog catalog;
	private final PracticeService practice;
	private final LearningProgress progress;

	PracticeController(Catalog catalog, PracticeService practice, LearningProgress progress) {
		this.catalog = catalog;
		this.practice = practice;
		this.progress = progress;
	}

	@PostMapping("/lessons/{lessonId}/attempts")
	ModelAndView submit(@PathVariable String lessonId, @RequestParam MultiValueMap<String, String> selections,
						HttpServletRequest request) {
		requireLocalForm(request);
		try {
			long attemptId = practice.submit(lessonId, selections);
			var redirect = new RedirectView("/attempts/" + attemptId, true);
			redirect.setStatusCode(HttpStatus.SEE_OTHER);
			return new ModelAndView(redirect);
		} catch (PracticeService.InvalidSubmission error) {
			var view = new ModelAndView("lesson")
					.addObject("page", catalog.lesson(lessonId))
					.addObject("selections", selections.toSingleValueMap())
					.addObject("formError", "请为每题选择一个有效选项，再提交。");
			view.setStatus(HttpStatus.BAD_REQUEST);
			return view;
		}
	}

	private static void requireLocalForm(HttpServletRequest request) {
		String origin = request.getHeader(HttpHeaders.ORIGIN);
		String fetchSite = request.getHeader("Sec-Fetch-Site");
		int port = request.getServerPort();
		boolean defaultPort = port == ("https".equals(request.getScheme()) ? 443 : 80);
		String expectedOrigin = request.getScheme() + "://" + request.getServerName()
				+ (defaultPort ? "" : ":" + port);
		if ((origin != null && !origin.equalsIgnoreCase(expectedOrigin))
				|| ("cross-site".equals(fetchSite) || "same-site".equals(fetchSite))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "请从 Tutor 的课程页面提交练习");
		}
	}

	@GetMapping("/attempts/{attemptId}")
	String result(@PathVariable long attemptId, Model model) {
		model.addAttribute("result", practice.result(attemptId));
		return "result";
	}

	@GetMapping("/mistakes")
	String mistakes(Model model) {
		model.addAttribute("mistakes", progress.mistakes());
		return "mistakes";
	}

	@GetMapping("/progress")
	String progress(Model model) {
		model.addAttribute("progress", progress.overview());
		return "progress";
	}
}
