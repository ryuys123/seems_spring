package com.test.seems;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.test.seems.faq.model.dto.Faq;
import com.test.seems.reply.model.dto.Reply;
import com.test.seems.faq.model.service.FaqService;
import com.test.seems.reply.model.service.ReplyService;
import com.test.seems.user.model.service.UserService;
import com.test.seems.notice.model.dto.Notice;
import com.test.seems.notice.model.service.NoticeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

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


	//common/main.jsp 를 내보내기 위한 요청 메소드
	//스프링부트에서는 jsp 파일을 뷰파일로 지정시에는 반드시 ModelAndView 리턴 형태로 사용해야 함
	@RequestMapping(value = "/main", method = RequestMethod.GET)
	public ModelAndView home(ModelAndView mv) {
		mv.setViewName("common/main");  //내보낼 뷰파일명
		return mv;
	}

	@RequestMapping("main.do")
	public ModelAndView forwardMain(ModelAndView mv) {
		mv.setViewName("common/main");  //내보낼 뷰파일명
		return mv;
	}

	// user ------------------------------------------------------------------

	// 로그인 페이지 내보내기용
	@RequestMapping("loginPage.do") // 뷰페이지에서 사용하는 요청이름 등록용 어노테이션임, Mapping Handler 가 관리함
	public ModelAndView moveLoginPage(ModelAndView mv) {
		mv.setViewName("user/loginPage"); // 뷰리졸버로 리턴됨 >> 뷰리졸버가 지정된 폴더에서 해당 jsp 파일 찾아서 클라이언트로 전송함
		return mv;
	}

	// 회원가입 페이지 내보내기용
	@RequestMapping("enrollPage.do")
	public ModelAndView moveEnrollPage(ModelAndView mv) {
		mv.setViewName("user/enrollPage");
		return mv;
	}

	@GetMapping("/")
	public ResponseEntity<Map<String, Object>> home() {
		Map<String, Object> response = new HashMap<>();
		response.put("message", "Spring Boot API 서버가 정상적으로 실행 중입니다!");
		response.put("status", "success");
		response.put("timestamp", System.currentTimeMillis());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/test")
	public ResponseEntity<Map<String, Object>> test() {
		Map<String, Object> response = new HashMap<>();
		response.put("message", "React와 Spring Boot 연동 테스트 성공!");
		response.put("data", "이 메시지가 보이면 연동이 정상입니다.");
		response.put("status", "success");
		return ResponseEntity.ok(response);
	}
}