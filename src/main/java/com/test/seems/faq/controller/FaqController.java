package com.test.seems.faq.controller;

import com.test.seems.common.Paging;
import com.test.seems.common.Search;
import com.test.seems.faq.model.dto.Faq;
import com.test.seems.faq.model.service.FaqService;
import com.test.seems.notice.model.dto.Notice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j   // log 객체 선언임, 별도의 로그 객체 생성구문 필요없음, 레퍼런스는 log 임
@RequiredArgsConstructor
@RestController
//@RequestMapping("/Faq")
@CrossOrigin   //다른 url port 에서 오는 요청을 처리하기 위함 (리액트 port 3000번, 리액트에서 부트(8080)로 요청함)
public class FaqController {

    private final FaqService FaqService;  //@RequiredArgsConstructor 로 해결

    // 요청 처리하는 메소드 (db 까지 연결되는 요청) -----------------------------------


    // paging 과 list 둘 다 뷰페이지로 전달하려면, ResponseEntity 리턴해야 함
    @GetMapping("/faq")
    @ResponseBody  // ResponseEntity<String> 인 경우는 생략해도 됨
    public ResponseEntity<Map<String, Object>> FaqListMethod(
            @RequestParam(name = "page", required = false) String page,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "limit", required = false) String slimit) {
        // 페이징 처리
        try {
            int currentPage = 1;
            if (page != null) {
                currentPage = Integer.parseInt(page);
            }

            // 한 페이지에 출력할 목록 갯수 기본 10개로 지정함
            int limit = 10;
            if (slimit != null) {
                limit = Integer.parseInt(slimit);
            }

            int listCount = FaqService.selectListCount();
            Paging paging = new Paging(listCount, limit, currentPage, "/faq");
            paging.calculate();

            //JPA 가 제공하는 Pageable 객체 생성
            Pageable pageable = PageRequest.of(currentPage - 1, limit, Sort.Direction.DESC, "faqNo");

            // 서비스 모델로 페이징 적용된 목록 조회 요청하고 결과받기 (일반사용자용)
            ArrayList<Faq> list;
            System.out.println("📌 현재 role 값: " + role);

            if ("ADMIN".equalsIgnoreCase(role)) {
                list = FaqService.selectListForAdmin(pageable); // ✅ 관리자용
            } else {
                list = FaqService.selectListByUserid(userId, pageable); // ✅ 일반 사용자용
            }

            Map<String, Object> map = new HashMap<>();
            map.put("list", list);
            map.put("paging", paging);
            System.out.println("조회한문의글갯수 : " + list.stream().count());

            return ResponseEntity.ok(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
//
//    // 내 FAQ 목록 조회 요청 처리용
//    @GetMapping("/faq/my")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> getMyFaqs(
//            @RequestParam(name = "userid", required = true) String userid,
//            @RequestParam(name = "page", required = false) String page,
//            @RequestParam(name = "limit", required = false) String slimit) {
//        log.info("/faq/my 요청 : userid={}", userid);
//
//        try {
//            int currentPage = 1;
//            if (page != null) {
//                currentPage = Integer.parseInt(page);
//            }
//
//            int limit = 10;
//            if (slimit != null) {
//                limit = Integer.parseInt(slimit);
//            }
//
//            // 사용자별 FAQ 목록 조회
//            ArrayList<Faq> list = FaqService.selectListByUserid(userid, currentPage, limit);
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("list", list);
//            map.put("userid", userid);
//            map.put("currentPage", currentPage);
//            map.put("limit", limit);
//
//            log.info("내 FAQ 목록 조회 성공: {}개", list.size());
//            return ResponseEntity.ok(map);
//        } catch (Exception e) {
//            log.error("내 FAQ 목록 조회 실패: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }


    //Faq글 상세보기 요청 처리용 : SELECT 쿼리문 실행 요청임 => GetMapping 임
    @GetMapping("/faq/detail/{faqNo}")
    public ResponseEntity<Faq> FaqDetailMethod(
            @PathVariable int faqNo){
        log.info("/Faq/detail 요청 : " + faqNo);

        // 정보 불러오기
        Faq Faq = FaqService.selectFaq(faqNo);

        return Faq != null ? new ResponseEntity<>(Faq, HttpStatus.OK): new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }



    // dml ****************************************************

    // 새 Faq 등록 요청 처리용 
    // insert 쿼리문 실행 요청임 => 전송방식 POST 임 => @PostMapping 지정해야 함
    @PostMapping(value = "/faq", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> FaqInsertMethod(
            @ModelAttribute Faq Faq) {
        log.info("/faq : " + Faq);

        Map<String, Object> map = new HashMap<>();

        //새로 등록할 Faq글 번호는 현재 마지막 등록글 번호에 + 1 한 값으로 저장 처리함
        Faq.setFaqNo(FaqService.selectLast().getFaqNo() + 1);

        if (FaqService.insertFaq(Faq) > 0) {
            map.put("status", "success");
            map.put("message", "새 Faq 등록 성공!");
            return ResponseEntity.status(HttpStatus.CREATED).body(map);
        } else {
            map.put("status", "fail");
            map.put("message", "DB 등록 실패");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
        }

    }  // insertFaq closed

    // Faq글 삭제 요청 처리용 : delete 쿼리문 실행 요청임 => 전송방식 delete => @DeleteMapping
    @DeleteMapping("/faq/{faqNo}")
    public ResponseEntity<String> FaqDeleteMethod(
            @PathVariable int faqNo,
            @RequestParam(name="rfile", required=false) String renameFileName) {
        if(FaqService.deleteFaq(faqNo) > 0) {
            return ResponseEntity.ok("삭제 성공!");  //ResponseEntity<String>
        } else {  // Faq글 삭제 실패시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Faq글 삭제 실패!");
        }
    }

    // Faq글 수정 요청 처리용 (파일 업로드 기능 포함)
    // update 쿼리문 실행 요청임 => 전송방식은 PUT 임 => @PutMapping 으로 지정해야 함
    @PutMapping(value="/faq/{faqNo}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> FaqUpdateMethod(
            @PathVariable int faqNo,
            @ModelAttribute Faq faq,
            @RequestParam(name="deleteFlag", required=false) String delFlag){
        log.info("FaqUpdateMethod : " + faq);

        if(FaqService.updateFaq(faq) > 0) {
            // Faq글 수정 성공시, 관리자 상세보기 페이지로 이동 처리
            return ResponseEntity.ok("Faq 수정 성공");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Faq 수정 실패!");
        }
    }

    // 상담 종료 처리
    @PutMapping("/faq/detail/{faqNo}/close")
    public ResponseEntity<Void> closeFaq(@PathVariable int faqNo) {
        try {
            FaqService.updateFaqStatusOnly(faqNo, "CLOSED");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/faq/auto-close")
    public ResponseEntity<Void> autoCloseFaqs() {
        FaqService.autoCloseFaqs();
        return ResponseEntity.ok().build();
    }

    // 인증 없이 접근 가능한 GET 방식 테스트용 엔드포인트
    @GetMapping("/faq/auto-close-test")
    public ResponseEntity<String> testAutoCloseFaqs() {
        FaqService.autoCloseFaqs();
        return ResponseEntity.ok("자동 종료 테스트 완료!");
    }


    // FAQ글 검색 관련 (관리자용) **********************************************************

    // FAQ 제목 검색 목록보기 요청 처리용 (페이징 처리 : 한 페이지에 10개씩 출력 처리)
    @GetMapping("/faq/search/title")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchNoticeByTitle(
            @RequestParam("action") String action,
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        log.info("/faq/search/title : " + keyword);
        int listCount = FaqService.selectSearchTitleCount(keyword);

        Paging paging = new Paging(listCount, limit, page, "/faq/search/title");
        paging.calculate();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.Direction.DESC, "faqNo");
        ArrayList<Faq> list = FaqService.selectSearchTitle(keyword, pageable);

        Map<String, Object> result = new HashMap<>();

        if (list != null && !list.isEmpty()) {
            result.put("list", list);
            result.put("paging", paging);
            result.put("action", action);
            result.put("keyword", keyword);
            return ResponseEntity.ok(result);
        } else {
            result.put("error", "검색 결과가 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    // FAQ 내용 검색 목록보기 요청 처리용 (페이징 처리 : 한 페이지에 10개씩 출력 처리)
    @GetMapping("/faq/search/content")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> faqSearchContentMethod(
            @RequestParam("action") String action,
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        log.info("/faq/search/content : " + keyword);
        int listCount = FaqService.selectSearchContentCount(keyword);

        Paging paging = new Paging(listCount, limit, page, "/faq/search/content");
        paging.calculate();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.Direction.DESC, "faqNo");
        ArrayList<Faq> list = FaqService.selectSearchContent(keyword, pageable);

        Map<String, Object> result = new HashMap<>();

        if (list != null && !list.isEmpty()) {
            result.put("list", list);
            result.put("paging", paging);
            result.put("action", action);
            result.put("keyword", keyword);
            return ResponseEntity.ok(result);
        } else {
            result.put("error", "검색 결과가 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    // FAQ 등록날짜 검색 목록보기 요청 처리용 (페이징 처리 : 한 페이지에 10개씩 출력 처리)
    @GetMapping("/faq/search/date")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> faqSearchDateMethod(
            Search search,
            @RequestParam("action") String action,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        log.info("/faq/search/date : " + search.getBegin() + "-" + search.getEnd());

        int listCount = FaqService.selectSearchDateCount(search.getBegin().toLocalDate(), search.getEnd().toLocalDate());

        Paging paging = new Paging(listCount, limit, page, "/faq/search/date");
        paging.calculate();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.Direction.DESC, "faqNo");
        ArrayList<Faq> list = FaqService.selectSearchDate(search.getBegin().toLocalDate(), search.getEnd().toLocalDate(), pageable);

        Map<String, Object> result = new HashMap<>();

        if (list != null && !list.isEmpty()) {
            result.put("list", list);
            result.put("paging", paging);
            result.put("action", action);
            result.put("begin", search.getBegin());
            result.put("end", search.getEnd());
            return ResponseEntity.ok(result);
        } else {
            result.put("error", "검색 결과가 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    // FAQ 답변상태 검색 목록보기 요청 처리용 (페이징 처리 : 한 페이지에 10개씩 출력 처리)
    @GetMapping("/faq/search/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> faqSearchStatusMethod(
            @RequestParam("action") String action,
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        log.info("/faq/search/status : " + keyword);
        int listCount = FaqService.selectSearchStatusCount(keyword);

        Paging paging = new Paging(listCount, limit, page, "/faq/search/status");
        paging.calculate();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.Direction.DESC, "faqNo");
        ArrayList<Faq> list = FaqService.selectSearchStatus(keyword, pageable);

        Map<String, Object> result = new HashMap<>();

        if (list != null && !list.isEmpty()) {
            result.put("list", list);
            result.put("paging", paging);
            result.put("action", action);
            result.put("keyword", keyword);
            return ResponseEntity.ok(result);
        } else {
            result.put("error", "검색 결과가 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

}
