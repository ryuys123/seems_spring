package com.test.seems.log.controller;

import com.test.seems.common.Paging;
import com.test.seems.common.Search;
import com.test.seems.log.model.dto.Log;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.test.seems.log.model.service.LogService;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class LogController {
    private final LogService logService;

    // 로그 목록 보기 요청 처리용 (페이징 처리 포함)
    @GetMapping("/log")
    @ResponseBody  // ResponseEntity<String> 인 경우는 생략해도 됨
    public ResponseEntity<Map<String, Object>> logListMethod(
            @RequestParam(name = "page", required = false) String page,
            @RequestParam(name = "limit", required = false) String slimit) {
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

        int listCount = Math.toIntExact(logService.selectListCount());
        Paging paging = new Paging((int) listCount, limit, currentPage, "/admin/ulist");
        paging.calculate();

        //JPA 가 제공하는 Pageable 객체 생성
        Pageable pageable = PageRequest.of(currentPage - 1, limit, Sort.Direction.DESC, "logId");

        // 서비스 모델로 페이징 적용된 목록 조회 요청하고 결과받기
        ArrayList<Log> list = logService.selectList(pageable);

        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("paging", paging);

        return ResponseEntity.ok(map);
    }

    //관리자용 검색 기능 요청 처리용 메소드 (만약, 전송방식을 GET, POST 둘 다 사용할 수 있게 한다면)
    // 사용자 이름, 아이디 검색 목록보기 요청 처리용 (페이징 처리 : 한 페이지에 10개씩 출력 처리)
    @GetMapping("/log/search/activity")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchAction(
            @RequestParam("activity") String action,
//            @RequestParam("keyword") String keyword,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        log.info("/admin/search/action : " + action);
        int listCount = logService.selectSearchLogNameCount(action);

        Paging paging = new Paging(listCount, limit, page, "/admin/search/action");
        paging.calculate();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.Direction.DESC, "logId");
        ArrayList<Log> list = logService.selectSearchLogName(action, pageable);

        Map<String, Object> result = new HashMap<>();

        if (list != null && !list.isEmpty()) {
            result.put("list", list);
            result.put("paging", paging);
            result.put("action", action);
//            result.put("keyword", action);
            return ResponseEntity.ok(result);
        } else {
            result.put("error", "검색 결과가 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    // 사용자 상태 검색 목록보기 요청 처리용 (페이징 처리 : 한 페이지에 10개씩 출력 처리)
    @GetMapping("/log/search/severity")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logSearchSeverity(
            @RequestParam("severity") String severity,
//            @RequestParam("status") int status,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        log.info("/admin/search/severity : " + severity);
        int listCount = logService.selectSearchSeverityCount(severity);

        Paging paging = new Paging(listCount, limit, page, "/admin/search/status");
        paging.calculate();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.Direction.DESC, "logId");
        ArrayList<Log> list = logService.selectSearchSeverity(severity, pageable);

        Map<String, Object> result = new HashMap<>();

        if (list != null && !list.isEmpty()) {
            result.put("list", list);
            result.put("paging", paging);
//            result.put("action", action);
            result.put("severity", severity);
            return ResponseEntity.ok(result);
        } else {
            result.put("error", "검색 결과가 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    // 사용자 가입날짜 검색 목록보기 요청 처리용 (페이징 처리 : 한 페이지에 10개씩 출력 처리)
    @GetMapping("/log/search/createdAt")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logSearchDateMethod(
            @RequestParam("begin") String beginStr,
            @RequestParam("end") String endStr,
            @RequestParam("action") String action,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {

        // ✅ 시분초 포함된 문자열 → LocalDateTime으로 파싱
        LocalDateTime begin = LocalDateTime.parse(beginStr);
        LocalDateTime end = LocalDateTime.parse(endStr);

        log.info("검색 요청 begin: {}", begin);
        log.info("검색 요청 end: {}", end);

        int listCount = logService.selectSearchCreatedAtCount(begin, end);

        Paging paging = new Paging(listCount, limit, page, "/admin/search/date");
        paging.calculate();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.Direction.DESC, "logId");
        ArrayList<Log> list = logService.selectSearchCreatedAt(begin, end, pageable);

        Map<String, Object> result = new HashMap<>();
        if (list != null && !list.isEmpty()) {
            result.put("list", list);
            result.put("paging", paging);
            result.put("action", action);
            result.put("begin", begin);
            result.put("end", end);
            return ResponseEntity.ok(result);
        } else {
            result.put("error", "검색 결과가 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }
}
