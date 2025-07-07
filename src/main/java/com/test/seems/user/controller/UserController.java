package com.test.seems.user.controller;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.test.seems.common.Paging;
import com.test.seems.common.Search;
import com.test.seems.user.model.dto.User;
import com.test.seems.user.model.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Slf4j
@RestController
@CrossOrigin
public class UserController {

    // 서비스 모델과 연결 처리 (의존성 주입, 자동 연결 처리)
    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bcryptPasswordEncoder;

    // 요청 받아서 서비스 모델쪽으로 전달정보 넘기고 결과받는 메소드 ------------------------------------

    @PostMapping( "/idchk")
    public String dupCheckUserIdMethod(@RequestParam("userId") String userId) {
        // 방식1, ResponseBody 에 담아서 문자열 내보내기 방식 사용함 (반환자료형이 String 임)
        boolean result = userService.selectCheckId(userId);
        return (result == true) ? "ok" : "dup";
    }

    // 회원 가입 요청 처리용 메소드 (파일 첨부 기능이 있는 form 전송일 때 처리 방식) => 첨부파일은 별도로 전송받도록 처리함
    // 서버상의 파일 저장 폴더 지정을 위해서 request 객체가 필요함
    // 업로드되는 파일은 따로 전송받음 => multipart 방식으로 전송옴 => 스프링이 제공하는 MutipartFile 클래스 사용해서 받음
    // 비밀번호(패스워드) 암호화 처리 기능 추가
    @PostMapping("/signup")
    public ResponseEntity<String> userInsertMethod(
            @ModelAttribute User user,
            HttpServletRequest request,
            @RequestParam("profileImage") MultipartFile ufile) {
        log.info("/signup : " + user);

        // 패스워드 암호화 처리
        String encodePwd = bcryptPasswordEncoder.encode(user.getPasswordHash());
        log.info("암호화된 패스워드 : " + encodePwd);
        user.setPasswordHash(encodePwd);

        // 사진 파일 첨부가 있을 경우, 저장 폴더 지정 ---------------------------------------------
        String savePath = request.getSession().getServletContext().getRealPath("resources/photoFiles");
        // 서버 엔진이 구동하는 웹애플리케이션(Context)의 루트(webapp) 아래의 "resources/photoFiles" 까지의 경로
        // 정보를 저장함
        log.info("savePath : " + savePath);

        // 첨부파일이 있다면 지정 폴더에 저장 처리 ----------------------------
        if (!ufile.isEmpty()) {
            // 전송온 파일 이름 추출함
            String originalFilename = ufile.getOriginalFilename();
            // 여러 회원이 업로드한 사진파일명이 중복될 경우를 대비해서 저장파일명 이름 바꾸기함
            // 바꿀 파일이름은 개발자가 정함
            // userId 가 기본키(primary key)이므로 중복이 안됨 => userId_profileImage.확장자 형태로 정해봄
            String renameFilename = user.getUserId() + "_" + originalFilename;

            // 저장 폴더에 저장 처리
            if (originalFilename != null && originalFilename.length() > 0) {
                try {
                    ufile.transferTo(new File(savePath + "\\" + renameFilename));
                } catch (Exception e) {
                    // 첨부파일 저장시 에러 발생
                    e.printStackTrace(); // 개발자가 볼 정보
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("첨부파일 업로드 실패.");
                }
            } // if

            // 파일 업로드 정상 처리되었다면
            user.setProfileImage(renameFilename); // db 테이블에는 변경된 파일명이 기록 저장됨
        } // 첨부파일이 있다면 --------------------------------------------------------

        // 서비스 모델의 메소드 실행 요청하고 결과받기
        if (userService.insertUser(user) != null) { // 회원 가입 성공시
            return new ResponseEntity<>("signup ok", HttpStatus.OK);
        } else { // 회원 가입 실패시
            return new ResponseEntity<>("signup fail", HttpStatus.BAD_REQUEST);
        }
    }

    // '내 정보 보기' 요청 처리용 메소드
    /*
     * 컨트롤러 메소드에서 뷰리졸버로 리턴하는 타입은 String (뷰파일명 리턴시), ModelAndView 를 사용할 수 있음 클라이언트가
     * 보낸 데이터 추출은 String 변수 = request.getParameter("전송이름"); 스프링에서는 전송값 추출을 위한 위의 구문과
     * 동일한 동작을 수행하는 어노테이션 제공하고 있음
     *
     * @RequestParam("전송이름) 자료형 변수명 == request.getParameter("전송이름") 과 같음 이 어노테이션은
     * 메소드 () 안에 사용함
     */
    @GetMapping("/myinfo") // 전송방식 get 임
    public String userDetailMethod(@RequestParam("userId") String userId, Model model) {
        log.info("myinfo.do : " + userId);

        // 서비스 모델로 아이디 전달해서, 회원 정보 조회한 결과 리턴받기
        User user = userService.selectUser(userId);

        // 리턴받은 결과를 가지고 성공 또는 실패 페이지 내보내기
        if (user != null) { // 조회 성공시
            // 첨부된 사진파일이 있다면, 원래 파일명으로 변경해서 전달함
            String originalFilename = null;
            if (user.getProfileImage() != null) {
                // 아이디_파일명.확장자 => 파일명.확장자 로 바꿈
                originalFilename = user.getProfileImage().substring(user.getProfileImage().indexOf('_') + 1);
                log.info("사진파일명 확인 : " + user.getProfileImage() + ", " + originalFilename);
            }

            model.addAttribute("user", user);
            model.addAttribute("ofile", originalFilename);

            return "user/infoPage";
        } else { // 조회 실패시
            model.addAttribute("message", userId + " 에 대한 회원 정보 조회 실패! 아이디를 다시 확인하세요.");
            return "common/error";
        }
    }

        // 회원 정보 수정 처리용 메소드
        @PostMapping(value = "/uupdate")
        public String userUpdateMethod (User user, Model model, HttpServletRequest request,
                @RequestParam("profileImage") MultipartFile ufile, @RequestParam("originalPwd") String originalPasswordHash,
                @RequestParam("ofile") String originalFilename){
            log.info("uupdate.do : " + user);

            // 암호가 전송이 왔다면 (새로운 암호가 전송온 경우임)
            if (user.getPasswordHash() != null && user.getPasswordHash().length() > 0) {
                // 패스워드 암호화 처리함
                user.setPasswordHash(this.bcryptPasswordEncoder.encode(user.getPasswordHash()));
                log.info("변경된 암호 확인 : " + user.getPasswordHash() + ", length : " + user.getPasswordHash().length());
            } else { // 새로운 암호가 전송오지 않았다면, 현재 member.userPwd = null 임 => 쿼리문에 적용되면 기존 암호 지워짐
                user.setPasswordHash(originalPasswordHash); // 원래 패스워드 기록함
            }

            // 사진 파일 첨부가 있을 경우, 저장 폴더 지정 ---------------------------------------------
            String savePath = request.getSession().getServletContext().getRealPath("resources/photoFiles");
            // 서버 엔진이 구동하는 웹애플리케이션(Context)의 루트(webapp) 아래의 "resources/photoFiles" 까지의 경로
            // 정보를 저장함
            log.info("savePath : " + savePath);

            // 수정된 첨부파일이 있다면 지정 폴더에 저장 처리 ----------------------------
            if (!ufile.isEmpty()) {
                // 전송온 파일 이름 추출함
                String profileImage = ufile.getOriginalFilename();

                // 이전 첨부파일명과 새로 첨부된 파일명이 다른지 확인
                if (!profileImage.equals(originalFilename)) {

                    // 여러 회원이 업로드한 사진파일명이 중복될 경우를 대비해서 저장파일명 이름 바꾸기함
                    // 바꿀 파일이름은 개발자가 정함
                    // userId 가 기본키(primary key)이므로 중복이 안됨 => userId_profileImage.확장자 형태로 정해봄
                    String renameFileName = user.getUserId() + "_" + profileImage;

                    // 저장 폴더에 저장 처리
                    if (profileImage != null && profileImage.length() > 0) {
                        try {
                            ufile.transferTo(new File(savePath + "\\" + renameFileName));
                        } catch (Exception e) {
                            // 첨부파일 저장시 에러 발생
                            e.printStackTrace(); // 개발자가 볼 정보
                            model.addAttribute("message", "첨부파일 업로드 실패!");
                            return "common/error";
                        }
                    } // if

                    // 파일 업로드 정상 처리되었다면
                    user.setProfileImage(renameFileName); // db 테이블에는 변경된 파일명이 기록 저장됨
                } // 첨부파일이 있고, 파일명이 다르다면 --------------------------------------------------------
            } else { // 새로운 첨부파일이 없다면
                // 기존 파일명을 member 에 다시 저장함
                user.setProfileImage(user.getUserId() + "_" + originalFilename);
            }

            // 서비스 모델의 메소드 실행 요청하고 결과받기
            if (userService.updateUser(user) != null) { // 회원 정보 수정 성공시
                // 컨트롤러 메소드에서 다른 컨트롤러 메소드를 실행시킬 경우
                return "redirect:main.do";
            } else { // 회원 정보 수정 실패시
                model.addAttribute("message", "회원 정보 수정 실패! 확인하고 다시 가입해 주세요.");
                return "common/error";
            }
        }

    //회원 탈퇴 (삭제) 처리용 메소드
    @DeleteMapping("/udelete")
    public String userDeleteMethod(@RequestParam("userId") String userId, Model model) {
        if(userService.deleteUser(userId) > 0) {
            // 회원 탈퇴 성공시 자동 로그아웃 처리해야 함
            return "redirect:logout.do";
        }else {
            model.addAttribute("message", userId + "님의 회원 탈퇴 실패! 관리자에게 문의하세요");
            return "common/error";
        }
    }

    // 관리자용 기능 *********************************************************

    // 회원 목록 보기 요청 처리용 (페이징 처리 포함)
    @GetMapping("/ulist")
    public ModelAndView userListMethod(ModelAndView mv,
                                         @RequestParam(name="page", required=false) String page,
                                         @RequestParam(name="limit", required=false) String slimit) {
        // page : 목록 출력 페이지, limit : 한 페이지에 출력할 목록 갯수

        // 페이징 처리
        int currentPage = 1;
        if (page != null) {
            currentPage = Integer.parseInt(page);
        }

        // 한 페이지에 출력할 목록 갯수 기본 10개로 지정함
        int limit = 10;
        if (slimit != null) {
            limit = Integer.parseInt(slimit);
        }

        // 총 목록 갯수 조회해서, 총 페이지 수 계산함
        int listCount = (int)userService.selectListCount();
        // 페이지 관련 항목들 계산 처리
        Paging paging = new Paging(listCount, limit, currentPage, "ulist.do");
        paging.calculate();

        Pageable pageable = PageRequest.of(currentPage-1, limit,
                Sort.by(Sort.Direction.DESC, "createdat"));

        //서비스 모델로 페이징 적용된 목록 조회 요청하고 결과받기
        ArrayList<User> list = userService.selectList(pageable);

        if(list != null && list.size() > 0) {  //조회 성공시
            //ModelAndView : Model + View
            mv.addObject("list", list);  //request.setAttribute("list", list) 와 같음
            mv.addObject("paging", paging);

            mv.setViewName("user/userListView");
        } else {  //조회 실패시
            mv.addObject("message", currentPage + "페이지에 출력할 회원 조회 실패!");
            mv.setViewName("common/error");
        }

        return mv;
    }

    //회원 로그인 제한/허용 처리용 메소드
    @PostMapping("/ustatus")
    public String changeStatusMethod(User user, Model model) {
        if(userService.updateStatus(user) > 0) {
            return "redirect:ulist.do";
        }else {
            model.addAttribute("message", "로그인 제한/허용 처리 오류 발생");
            return "common/error";
        }
    }

    //관리자용 검색 기능 요청 처리용 메소드 (만약, 전송방식을 GET, POST 둘 다 사용할 수 있게 한다면)
    @RequestMapping(value="usearch.do", method= {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView userSearchMethod(HttpServletRequest request, ModelAndView mv) {
        //전송 온 값 꺼내기
        String action = request.getParameter("action");
        String keyword = request.getParameter("keyword");
        String begin = request.getParameter("begin");
        String end = request.getParameter("end");

        Search search = new Search();

        if(action.equals("ucreatedat")) {
            if(begin != null && end != null) {
                search.setBegin(Date.valueOf(begin));
                search.setEnd(Date.valueOf(end));
            }
        } else if(keyword != null) {
            if(action.equals("ustatus")) {
                search.setAge(Integer.parseInt(keyword));
            }else {
                search.setKeyword(keyword);
            }
        }

        //검색 결과에 대한 페이징 처리
        int currentPage = 1;
        if(request.getParameter("page") != null) {
            currentPage = Integer.parseInt(request.getParameter("page"));
        }

        //한 페이지에 출력할 목록 갯수 지정
        int limit = 10;
        if(request.getParameter("limit") != null) {
            limit = Integer.parseInt(request.getParameter("limit"));
        }

        //총 페이지수 계산을 위해 검색 결과가 적용된 총 목록 갯수 조회
        int listCount = 0;
        switch(action) {
            case "uid":	listCount = userService.selectSearchUserIdCount(keyword);		break;
            case "uname":	listCount = userService.selectSearchUserNameCount(keyword);		break;
            case "ucreatedat":	listCount = userService.selectSearchCreatedAtCount(search.getBegin(), search.getEnd());	break;
            case "ustatus":	listCount = userService.selectSearchStatusCount(Integer.parseInt(keyword));	break;
        }

        //페이징 계산 처리
        Paging paging = new Paging(listCount, limit, currentPage, "msearch.do");
        paging.calculate();

        Pageable pageable = PageRequest.of(currentPage - 1, limit, Sort.Direction.DESC, "enrollDate");

        ArrayList<User> list = null;
        switch(action) {
            case "uid":	list = userService.selectSearchUserId(keyword, pageable);		break;
            case "uname":	list = userService.selectSearchUserName(keyword, pageable);		break;
            case "ucreatedat":	list = userService.selectSearchCreatedAt(search.getBegin(), search.getEnd(), pageable);	break;
            case "ustatus":	list = userService.selectSearchStatus(Integer.parseInt(keyword), pageable);	break;
        }

        //조회 결과 성공 또는 실패에 따라 뷰페이지 내보내기
        if(list != null && list.size() > 0) {
            mv.addObject("list", list);
            mv.addObject("paging", paging);
            mv.addObject("action", action);

            if(keyword != null) {
                mv.addObject("keyword", keyword);
            }else {
                mv.addObject("begin", begin);
                mv.addObject("end", end);
            }

            mv.setViewName("user/userListView");
        }else {
            mv.addObject("message", "회원 관리 검색 결과가 존재하지 않습니다.");
            mv.setViewName("common/error");
        }

        return mv;
    }


} // class
