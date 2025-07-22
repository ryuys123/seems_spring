package com.test.seems.user.controller;

import com.test.seems.common.Paging;
import com.test.seems.common.Search;
import com.test.seems.user.model.dto.User;
import com.test.seems.user.model.dto.UserInfoResponse;
import com.test.seems.user.model.dto.UserDeleteRequest;
import com.test.seems.user.model.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin		//리액트 애플리케이션 (포트가 다름)의 요청을 처리하기 위함
public class UserController {

    /**
     * 마이페이지 사용자 정보 API (/user/info)
     */
    @GetMapping("/user/info")
    public ResponseEntity<UserInfoResponse> getUserInfo(Authentication authentication) {
        String userId = authentication.getName(); // 인증 방식에 따라 다를 수 있음
        UserInfoResponse userInfo = userService.getUserInfoByUserId(userId);
        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 마이페이지 사용자 정보 수정 API (/user/info)
     */
    @PutMapping("/user/info")
    public ResponseEntity<?> updateUserInfo(@RequestBody UserInfoResponse req, Authentication authentication) {
        String userId = authentication.getName();
        
        try {
            boolean result = userService.updateUserInfo(userId, req);
            if (result) {
                // 비밀번호 변경이 포함된 경우와 일반 정보 수정을 구분
                boolean isPasswordChange = req.getCurrentPassword() != null && 
                                         req.getNewPassword() != null && 
                                         req.getConfirmPassword() != null;
                
                if (isPasswordChange) {
                    return ResponseEntity.ok().body("비밀번호가 성공적으로 변경되었습니다.");
                } else {
                    return ResponseEntity.ok().body("프로필이 성공적으로 수정되었습니다.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("수정 실패");
            }
        } catch (Exception e) {
            log.error("사용자 정보 수정 중 오류 발생: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * 비밀번호 확인 API (회원 탈퇴용)
     */
    @PostMapping("/user/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody Map<String, String> request, Authentication authentication) {
        String userId = authentication.getName();
        String password = request.get("password");
        
        if (password == null || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호를 입력해주세요.");
        }
        
        try {
            boolean isValid = userService.verifyUserPassword(userId, password);
            if (isValid) {
                return ResponseEntity.ok().body("비밀번호 확인이 완료되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
            }
        } catch (Exception e) {
            log.error("비밀번호 확인 중 오류 발생: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * 회원 탈퇴 API
     */
    @DeleteMapping("/user/account")
    public ResponseEntity<?> deleteAccount(@RequestBody UserDeleteRequest request, Authentication authentication) {
        String userId = authentication.getName();
        
        try {
            boolean result = false;
            
            if ("normal".equals(request.getUserType())) {
                // 일반 로그인 사용자 - 비밀번호 확인 후 탈퇴
                if (request.getPassword() == null || request.getPassword().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호를 입력해주세요.");
                }
                result = userService.deleteUserWithPasswordVerification(userId, request.getPassword());
            } else if ("social".equals(request.getUserType())) {
                // 소셜 로그인 사용자 - 비밀번호 확인 없이 탈퇴
                result = userService.deleteSocialUser(userId);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("올바르지 않은 사용자 타입입니다.");
            }
            
            if (result) {
                log.info("회원 탈퇴 성공: userId={}, userType={}", userId, request.getUserType());
                return ResponseEntity.ok().body("회원 탈퇴가 완료되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원 탈퇴에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 서비스 모델과 연결 처리 (의존성 주입, 자동 연결 처리)
    private final UserService userService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    //관련 객체 생성 관련 config 파일을 만들고 @Bean 으로 등록 처리하고 사용
    //config.EncoderConfig
    private final BCryptPasswordEncoder bcryptPasswordEncoder;

    // 요청 받아서 서비스 모델쪽으로 전달정보 넘기고 결과받는 메소드 ------------------------------------

    @PostMapping( "user/idchk")
    public String dupCheckUserIdMethod(@RequestParam("userId") String userId) {
        // 방식1, ResponseBody 에 담아서 문자열 내보내기 방식 사용함 (반환자료형이 String 임)
        boolean result = userService.selectCheckId(userId);
        return (result == false) ? "ok" : "dup";
    }

    // 회원 가입 요청 처리용 메소드 (파일 첨부 기능이 있는 form 전송일 때 처리 방식) => 첨부파일은 별도로 전송받도록 처리함
    // 서버상의 파일 저장 폴더 지정을 위해서 request 객체가 필요함
    // 업로드되는 파일은 따로 전송받음 => multipart 방식으로 전송옴 => 스프링이 제공하는 MutipartFile 클래스 사용해서 받음
    // 비밀번호(패스워드) 암호화 처리 기능 추가
    @PostMapping("user/signup")
    public ResponseEntity userInsertMethod(
            @ModelAttribute User user,
            @RequestParam(name="profileImage", required = false) MultipartFile ufile) {
        log.info("/user/signup : " + user);

        // 패스워드 암호화 처리
//			String encodePwd = bcryptPasswordEncoder.encode(member.getUserPwd());
//			member.setUserPwd(encodePwd);
        user.setUserPwd(bcryptPasswordEncoder.encode(user.getUserPwd()));
        log.info("after encode : " + user.getUserPwd() + ", length : " + user.getUserPwd().length());

        // 회원가입시 사진 파일첨부가 있을 경우, 저장 폴더 경로 지정 -----------------------------------
        String savePath = uploadDir + "/photo";
        log.info("savePath : " + savePath);

        File directory = new File(savePath);
        if(!directory.exists()){
            directory.mkdirs();
        }

        // 사진 첨부파일이 있다면
        if (ufile != null && !ufile.isEmpty()) {
            // 전송온 파일 이름 추출함
            String fileName = ufile.getOriginalFilename();
            // 여러 회원이 업로드한 사진파일명이 중복될 경우를 대비해서 저장파일명 이름바꾸기함
            // 바꿀 파일이름은 개발자가 정함
            // userId 가 기본키이므로 중복이 안됨 => userId_filename 저장형태로 정해봄
            String renameFileName = user.getUserId() + "_" + fileName;

            // 저장 폴더에 저장 처리
            if (fileName != null && fileName.length() > 0) {
                try {
                    // mfile.transferTo(new File(savePath + "\\" + fileName));
                    // 저장시 바뀐 이름으로 저장 처리함
                    ufile.transferTo(new File(savePath, renameFileName));
                } catch (Exception e) {
                    // 첨부파일 저장시 에러 발생
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
            }

            // 파일 업로드 정상 처리되었다면
            // user.setProfileImage(fileName); //db 저장시에는 원래 이름으로 기록함
            user.setProfileImage(renameFileName); // db 저장시에는 변경된 이름으로 기록함
        } // 첨부파일이 있을 때

        //가입정보 추가 입력 처리
        user.setStatus(1);
        user.setAdminYn("N");
        log.info("userInsertMethod : " + user);

        try {
            userService.insertUser(user);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            log.error("회원가입 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 실패");
        }


    }

    /**
     * 페이스 등록이 포함된 회원가입
     */
    @PostMapping("api/face-signup")
    public ResponseEntity<?> userInsertWithFaceMethod(
            @ModelAttribute User user,
            @RequestParam(name="profileImage", required = false) MultipartFile ufile,
            @RequestParam(name="faceImageData", required = false) String faceImageData,
            @RequestParam(name="faceName", required = false) String faceName) {
        log.info("/api/face-signup : " + user);

        // 패스워드 암호화 처리
        user.setUserPwd(bcryptPasswordEncoder.encode(user.getUserPwd()));
        log.info("after encode : " + user.getUserPwd() + ", length : " + user.getUserPwd().length());

        // 회원가입시 사진 파일첨부가 있을 경우, 저장 폴더 경로 지정
        String savePath = uploadDir + "/photo";
        log.info("savePath : " + savePath);

        File directory = new File(savePath);
        if(!directory.exists()){
            directory.mkdirs();
        }

        // 사진 첨부파일이 있다면
        if (ufile != null && !ufile.isEmpty()) {
            String fileName = ufile.getOriginalFilename();
            String renameFileName = user.getUserId() + "_" + fileName;

            if (fileName != null && fileName.length() > 0) {
                try {
                    ufile.transferTo(new File(savePath, renameFileName));
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
            }
            user.setProfileImage(renameFileName);
        }

        //가입정보 추가 입력 처리
        user.setStatus(1);
        user.setAdminYn("N");
        user.setFaceLoginEnabled(false); // 기본값은 비활성화
        log.info("userInsertWithFaceMethod : " + user);

        try {
            User savedUser = userService.insertUser(user);

            // 페이스 등록이 요청된 경우
            if (savedUser != null && faceImageData != null && faceName != null) {
                // FaceLoginService를 통한 페이스 등록
                // 이 부분은 별도 API로 처리하는 것이 더 적절할 수 있습니다.
                log.info("회원가입 시 페이스 등록 요청: 사용자 {}, 페이스 {}", savedUser.getUserId(), faceName);
            }

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            log.error("페이스 등록 포함 회원가입 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 실패");
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
    @GetMapping("user/myinfo") // 전송방식 get 임
    public ResponseEntity<?> userDetailMethod(@RequestParam("userId") String userId) {
        log.info("/user/myinfo : " + userId);

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

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("user", user);
            responseBody.put("photoFileName", originalFilename);

            return ResponseEntity.ok(responseBody);  // 200 OK + JSON 응답

        } else { // 조회 실패시
            Map<String, String> errorBody = new HashMap<>();
            errorBody.put("message", userId + " 에 대한 회원 정보 조회 실패! 아이디를 다시 확인하세요.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        }
    }

    // 회원 정보 수정 처리용 메소드
    @PostMapping(value = "user/update/{userId}")
    public ResponseEntity<?> userUpdateMethod (
            @PathVariable String userId,
            @ModelAttribute User user,
            @RequestParam(name="profileImage", required = false) MultipartFile ufile,
            @RequestParam(name="originalPwd", required = false) String originalUserPwd,
            @RequestParam(name="ofile", required = false) String originalFilename){
        log.info("/user/update : " + user);

        // 암호가 전송이 왔다면 (새로운 암호가 전송온 경우임)
        if (user.getUserPwd() != null && user.getUserPwd().length() > 0) {
            // 패스워드 암호화 처리함
            user.setUserPwd(this.bcryptPasswordEncoder.encode(user.getUserPwd()));
            log.info("변경된 암호 확인 : " + user.getUserPwd() + ", length : " + user.getUserPwd().length());
        } else { // 새로운 암호가 전송오지 않았다면, 현재 member.userPwd = null 임 => 쿼리문에 적용되면 기존 암호 지워짐
            user.setUserPwd(originalUserPwd); // 원래 패스워드 기록함
        }

        // 사진 파일 첨부가 있을 경우, 저장 폴더 지정 ---------------------------------------------
        String savePath = uploadDir + "/photo";
        log.info("savePath : " + savePath);

        // 수정된 첨부파일이 있다면 지정 폴더에 저장 처리 ----------------------------
        if (!ufile.isEmpty() && ufile != null) {
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
                        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(HttpStatus.OK);
        } else { // 회원 정보 수정 실패시
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //회원 탈퇴 (삭제) 처리용 메소드
    @DeleteMapping("/user/delete/{userId}")
    public ResponseEntity<?> userDeleteMethod(@RequestParam("userId") String userId) {
        if(userService.deleteUser(userId) > 0) {
            // 회원 탈퇴 성공시 자동 로그아웃 처리해야 함
            return new ResponseEntity<>(HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 관리자용 기능 *********************************************************

    // 회원 목록 보기 요청 처리용 (페이징 처리 포함)
    @GetMapping("/admin/ulist")
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
    @PostMapping("admin/ustatus")
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
