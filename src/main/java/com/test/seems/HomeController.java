package com.test.seems;

import com.test.seems.user.model.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

//RestController 에서는 뷰 페이지 내보내기용 요청을 처리할 수 없음
//  => 요청과 응답에 json or xml 구조의 자원(dto)을 직렬화해서 주고 받고 하는 api 구조임
//jsp 뷰페이지 내보내기용은 모두 HomeController 로 옮김
@Slf4j
@RequiredArgsConstructor
@Controller  //jsp 페이지 내보내기용 *.do 요청은 Controller 에서만 처리 가능함
@CrossOrigin
public class HomeController {
//
	@Autowired
	private UserService userService;
//
//	@Autowired
//	private NoticeService noticeService;
//
//	@Autowired
//	private FaqService faqService;
//
//	@Autowired
//	private ReplyService replyService;




	// 수정 필요: / 경로를 로그인 페이지로 리다이렉트
	@GetMapping("/")
	public ModelAndView home(ModelAndView mv) {
		mv.setViewName("/login");
		return mv;
	}

	// user ------------------------------------------------------------------

}