package com.test.seems.notice.controller;

import java.io.File;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aj.org.objectweb.asm.commons.TryCatchBlockSorter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.test.seems.common.FileNameChange;
import com.test.seems.common.Paging;
import com.test.seems.common.Search;
import com.test.seems.notice.model.dto.Notice;
import com.test.seems.notice.model.service.NoticeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j   // log 객체 선언임, 별도의 로그 객체 생성구문 필요없음, 레퍼런스는 log 임
@RequiredArgsConstructor
@RestController
//@RequestMapping("/notice")
@CrossOrigin   //다른 url port 에서 오는 요청을 처리하기 위함 (리액트 port 3000번, 리액트에서 부트(8080)로 요청함)
public class NoticeController {

	private final NoticeService noticeService;  //@RequiredArgsConstructor 로 해결

	@Value("${file.upload-dir}")
	private String uploadDir;

	// 요청 처리하는 메소드 (db 까지 연결되는 요청) -----------------------------------

	@GetMapping("/notice/ntop3")
	public ResponseEntity<List<Notice>> noticeNewTop3Method() {
		// 서비스 모델로 top3 결과 요청
		List<Notice> list = noticeService.selectTop3();
		log.info("ntop3 list : {}", list);
		return ResponseEntity.ok(list);
	}

	// paging 과 list 둘 다 뷰페이지로 전달하려면, ResponseEntity 리턴해야 함
	@GetMapping("/notice")
	@ResponseBody  // ResponseEntity<String> 인 경우는 생략해도 됨
	public ResponseEntity<Map<String, Object>> noticeListMethod(
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

		int listCount = noticeService.selectListCount();
		Paging paging = new Paging(listCount, limit, currentPage, "/notice");
		paging.calculate();

		//JPA 가 제공하는 Pageable 객체 생성
		Pageable pageable = PageRequest.of(currentPage - 1, limit, Sort.Direction.DESC, "noticeNo");

		// 서비스 모델로 페이징 적용된 목록 조회 요청하고 결과받기
		ArrayList<Notice> list = noticeService.selectList(pageable);

		Map<String, Object> map = new HashMap<>();
		if (list != null && list.size() > 0) {
			map.put("list", list);
			map.put("paging", paging);

			return ResponseEntity.ok(map);
		}else{
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
		}
	}

	//공지글 상세보기 요청 처리용 : SELECT 쿼리문 실행 요청임 => GetMapping 임
	@GetMapping("/notice/detail/{noticeNo}")
	public ResponseEntity<Notice> noticeDetailMethod(
			@PathVariable int noticeNo,
			@RequestParam(name = "increase", defaultValue = "true") boolean increase) {

		log.info("/notice/detail 요청 : " + noticeNo + ", increase = " + increase);

		//조회수 1증가 처리
		// 조회수 증가 여부를 쿼리파라미터로 제어
		if (increase) {
			noticeService.updateAddReadCount(noticeNo);
		}
		// 정보 불러오기
		Notice notice = noticeService.selectNotice(noticeNo);


		return notice != null ? new ResponseEntity<>(notice, HttpStatus.OK): new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}


	@GetMapping("/notice/nfdown")
	public ResponseEntity<Resource> fileDownMethod(
			@RequestParam("ofile") String originalFileName,
			@RequestParam("rfile") String renameFileName) {
		log.info("/notice/nfdown : " + originalFileName + ", " + renameFileName);
		String savePath = uploadDir + "/notice";
		log.info(savePath);
		//Path 객체 생성
		Path path = Paths.get(savePath).toAbsolutePath().normalize();

		//Resource 객체 생성
		Resource resource = null;
		try {
			resource = new UrlResource(path.toUri() + "/" + renameFileName);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		// 파일 이름 설정 (한글 파일명 때문에 인코딩 처리)
		String encodedFileName = originalFileName != null ? originalFileName : renameFileName;
		try {
			encodedFileName = URLEncoder.encode(encodedFileName, "UTF-8")
					.replaceAll("\\+", "%20");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		// Content-Disposition 해더 설정
		String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"";

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", contentDisposition)
				.body(resource);
	}

	// dml ****************************************************

	// 새 공지글 등록 요청 처리용 (파일 업로드 기능 포함)
	// insert 쿼리문 실행 요청임 => 전송방식 POST 임 => @PostMapping 지정해야 함
	// "/admin/**" 으로 보안설정을 따로 하고 싶다면, 클래스 위의 @RequestMapping 사용하면 안됨
	@PostMapping(value = "/admin/notice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, Object>> noticeInsertMethod(
			@ModelAttribute Notice notice,
			@RequestParam(name="ofile", required=false) MultipartFile mfile	) {
		log.info("/admin/notice : " + notice);

		Map<String, Object> map = new HashMap<>();

		//공지사항 첨부파일 저장 폴더를 경로 저장 (application.properties 에 경로 설정 추가)
		String savePath = uploadDir + "/notice";
		log.info("savePath : " + savePath);

		// 저장 폴더 없으면 생성
		File dir = new File(savePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		//첨부파일이 있을 때
		if (mfile != null && !mfile.isEmpty()) {
			// 전송온 파일이름 추출함
			String fileName = mfile.getOriginalFilename();
			String renameFileName = null;

			//저장 폴더에는 변경된 파일이름을 파일을 저장 처리함
			//바꿀 파일명 : 년월일시분초.확장자
			if (fileName != null && fileName.length() > 0) {
				renameFileName = FileNameChange.change(fileName, "yyyyMMddHHmmss");
				log.info("변경된 첨부 파일명 확인 : " + renameFileName);

				try {
					//저장 폴더에 바뀐 파일명으로 파일 저장하기
					mfile.transferTo(new File(savePath + "\\" + renameFileName));
				} catch (Exception e) {
					e.printStackTrace();
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
				}
			} //파일명 바꾸어 저장하기

			//notice 객체에 첨부파일 정보 저장하기
			notice.setOriginalFilePath(fileName);
			notice.setRenameFilePath(renameFileName);
		} //첨부파일 있을 때

		//새로 등록할 공지글 번호는 현재 마지막 등록글 번호에 + 1 한 값으로 저장 처리함
		notice.setNoticeNo(noticeService.selectLast().getNoticeNo() + 1);

		if (noticeService.insertNotice(notice) > 0) {
			map.put("status", "success");
			map.put("message", "새 공지 등록 성공!");
			return ResponseEntity.status(HttpStatus.CREATED).body(map);
		} else {
			map.put("status", "fail");
			map.put("message", "DB 등록 실패");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
		}

	}  // insertNotice closed

	// 공지글 삭제 요청 처리용 : delete 쿼리문 실행 요청임 => 전송방식 delete => @DeleteMapping
	@DeleteMapping("/admin/notice/{noticeNo}")
	public ResponseEntity<String> noticeDeleteMethod(
			@PathVariable int noticeNo,
			@RequestParam(name="rfile", required=false) String renameFileName) {
		if(noticeService.deleteNotice(noticeNo) > 0) {  // 공지글 삭제 성공시
			//공지글 삭제 성공시 저장 폴더에 있는 첨부파일도 삭제 처리함
			if(renameFileName != null && renameFileName.length() > 0) {
				// 공지사항 첨부파일 저장 폴더 경로 지정
				String savePath = uploadDir + "/notice";
				// 저장 폴더에서 파일 삭제함
				new File(savePath + "\\" + renameFileName).delete();
			}

			return ResponseEntity.ok("삭제 성공!");  //ResponseEntity<String>
		} else {  // 공지글 삭제 실패시
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("공지글 삭제 실패!");
		}
	}

	// 공지글 수정 요청 처리용 (파일 업로드 기능 포함)
	// update 쿼리문 실행 요청임 => 전송방식은 PUT 임 => @PutMapping 으로 지정해야 함
	@PutMapping(value="/admin/notice/{noticeNo}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> noticeUpdateMethod(
			@PathVariable int noticeNo,
			@ModelAttribute Notice notice,
			@RequestParam(name="deleteFlag", required=false) String delFlag,
			@RequestParam(name="upfile", required=false) MultipartFile mfile) {
		log.info("noticeUpdateMethod : " + notice);

		// 첨부파일 관련 변경 사항 처리
		// 공지사항 첨부파일 저장 폴더 경로 지정
		String savePath = uploadDir + "/notice";

		// 저장 폴더 없으면 생성
		File dir = new File(savePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// 1. 원래 첨부파일이 있는데, '파일삭제'를 체크한 경우
		//    또는 원래 첨부파일이 있는데 새로운 첨부파일로 변경 업로드된 경우
		//	=> 이전 파일 정보 삭제함
		if(notice.getOriginalFilePath() != null
				&& ((delFlag != null && delFlag.equals("yes")) || (mfile != null && !mfile.isEmpty()))) {
			//저장 폴더에서 이전 파일은 삭제함
			new File(savePath + "\\" + notice.getRenameFilePath()).delete();
			// notice 안의 파일 정보도 삭제함
			notice.setOriginalFilePath(null);
			notice.setRenameFilePath(null);
		}

		//2. 첨부파일이 있을 때 (변경 또는 추가)
		if(mfile != null && !mfile.isEmpty()) {
			// 전송온 파일이름 추출함
			String fileName = mfile.getOriginalFilename();
			String renameFileName = null;

			//저장 폴더에는 변경된 파일이름을 파일을 저장 처리함
			//바꿀 파일명 : 년월일시분초.확장자
			if(fileName != null && fileName.length() > 0) {
				renameFileName = FileNameChange.change(fileName, "yyyyMMddHHmmss");
				log.info("변경된 첨부 파일명 확인 : " + renameFileName);

				try {
					//저장 폴더에 바뀐 파일명으로 파일 저장하기
					mfile.transferTo(new File(savePath + "\\" + renameFileName));
				} catch (Exception e) {
					e.printStackTrace();
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패!");
				}
			} //파일명 바꾸어 저장하기

			//notice 객체에 첨부파일 정보 저장하기
			notice.setOriginalFilePath(fileName);
			notice.setRenameFilePath(renameFileName);
		} //첨부파일 있을 때

		if(noticeService.updateNotice(notice) > 0) {
			// 공지글 수정 성공시, 관리자 상세보기 페이지로 이동 처리
			return ResponseEntity.ok("공지 수정 성공");
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("공지 수정 실패!");
		}
	}

	// 공지글 검색 관련 **********************************************

	// 공지사항 제목 검색 목록보기 요청 처리용 (페이징 처리 : 한 페이지에 10개씩 출력 처리)
	@GetMapping("/notice/search/title")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> searchNoticeByTitle(
			@RequestParam("action") String action,
			@RequestParam("keyword") String keyword,
			@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			@RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
		log.info("/notice/search/title : " + keyword);
		int listCount = noticeService.selectSearchTitleCount(keyword);

		Paging paging = new Paging(listCount, limit, page, "/notice/search/title");
		paging.calculate();

		Pageable pageable = PageRequest.of(page - 1, limit, Sort.Direction.DESC, "noticeNo");
		ArrayList<Notice> list = noticeService.selectSearchTitle(keyword, pageable);

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

	// 공지사항 내용 검색 목록보기 요청 처리용 (페이징 처리 : 한 페이지에 10개씩 출력 처리)
	@GetMapping("/notice/search/content")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> noticeSearchContentMethod(
			@RequestParam("action") String action,
			@RequestParam("keyword") String keyword,
			@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			@RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
		log.info("/notice/search/content : " + keyword);
		int listCount = noticeService.selectSearchContentCount(keyword);

		Paging paging = new Paging(listCount, limit, page, "/notice/search/content");
		paging.calculate();

		Pageable pageable = PageRequest.of(page - 1, limit, Sort.Direction.DESC, "noticeNo");
		ArrayList<Notice> list = noticeService.selectSearchContent(keyword, pageable);

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

	// 공지사항 등록날짜 검색 목록보기 요청 처리용 (페이징 처리 : 한 페이지에 10개씩 출력 처리)
	@GetMapping("/notice/search/date")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> noticeSearchDateMethod(
			Search search,
			@RequestParam("action") String action,
			@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			@RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
		log.info("/notice/search/date : " + search.getBegin() + "-" + search.getEnd());

		int listCount = noticeService.selectSearchDateCount(search.getBegin().toLocalDate(), search.getEnd().toLocalDate());

		Paging paging = new Paging(listCount, limit, page, "/notice/search/date");
		paging.calculate();

		Pageable pageable = PageRequest.of(page - 1, limit, Sort.Direction.DESC, "noticeNo");
		ArrayList<Notice> list = noticeService.selectSearchDate(search.getBegin().toLocalDate(), search.getEnd().toLocalDate(), pageable);

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

}
