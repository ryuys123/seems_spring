package com.test.seems.log.model.service;

import com.test.seems.log.jpa.entity.LogEntity;
import com.test.seems.log.jpa.repository.LogRepository;
import com.test.seems.log.model.dto.Log;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    @Autowired
    private final LogRepository logRepository;

    public long selectListCount() {
        //jpa 제공 메소드 사용
        //count() : long
        return logRepository.count();
    }

    public ArrayList<Log> selectList(Pageable pageable) {
        //jpa 가 제공하는 메소드 사용
        //findAll(pageable) : Page<entity>
        Page<LogEntity> page = logRepository.findAll(pageable);
        ArrayList<Log> list = new ArrayList<>();
        for (LogEntity log : page) {
            list.add(log.toDto());
        }
        return list;
    }


    //검색 관련 (메소드 추가) ---------------------------------------------------------------

    private ArrayList<Log> toList(List<LogEntity> list) {
        ArrayList<Log> logs = new ArrayList<>();
        for (LogEntity entity : list) {
            logs.add(entity.toDto());
        }
        return logs;
    }

    public int selectSearchLogNameCount(String action) {
		/* sql :
		* 	select count(*) from notice
			where title like '%' || #{ keyword } || '%'
		* */
        return logRepository.countByActionContainingIgnoreCase(action);
    }


    public int selectSearchSeverityCount(String severity) {
		/* sql :
		* 	select count(*) from notice
			where noticecontent like '%' || #{ keyword } || '%'
		* */
        return logRepository.countBySeverityContainingIgnoreCase(severity);
    }


    public int selectSearchCreatedAtCount(LocalDateTime begin, LocalDateTime end) {
		/* sql :
		* 	select count(*) from notice
			where noticedate between #{ begin } and #{ end }
		* */
        return logRepository.countByCreatedAtBetween(begin, end);
    }


    public ArrayList<Log> selectSearchLogName(String action, Pageable pageable) {
		/* sql :
			select *
			from (select rownum rnum, noticeno, title, noticedate, noticewriter, noticecontent,
						original_filepath, rename_filepath, importance, imp_end_date, readcount
				  from (select * from notice
						where title like '%' || #{ keyword } || '%'
						order by importance desc, noticedate desc, noticeno desc))
			where rnum between #{ startRow } and #{ endRow }
		* */
        return toList(logRepository.findByActionContainingIgnoreCase(action, pageable));
    }


    public ArrayList<Log> selectSearchSeverity(String severity, Pageable pageable) {
		/* sql :
		* 	select *
			from (select rownum rnum, noticeno, title, noticedate, noticewriter, noticecontent,
						original_filepath, rename_filepath, importance, imp_end_date, readcount
				  from (select * from notice
						where noticecontent like '%' || #{ keyword } || '%'
						order by importance desc, noticedate desc, noticeno desc))
			where rnum between #{ startRow } and #{ endRow }
		* */
        return toList(logRepository.findBySeverityContainingIgnoreCase(severity, pageable));
    }


    public ArrayList<Log> selectSearchCreatedAt(LocalDateTime begin, LocalDateTime  end, Pageable pageable) {
		/* sql :
		* 	select *
			from (select rownum rnum, noticeno, title, noticedate, noticewriter, noticecontent,
						original_filepath, rename_filepath, importance, imp_end_date, readcount
				  from (select * from notice
						where noticedate between #{ begin } and #{ end }
						order by importance desc, noticedate desc, noticeno desc))
			where rnum between #{ startRow } and #{ endRow }
		* */
        return toList(logRepository.findByCreatedAtBetween(
                begin, end, pageable));
    }



    // 로그 저장
    @Transactional
    public void saveLog(Log log) {

        LogEntity entity = LogEntity.builder()
                .userId(log.getUserId())
                .action(log.getAction())
                .severity(log.getSeverity())
                .beforeData(log.getBeforeData())
                .afterData(log.getAfterData())
                .createdAt(log.getCreatedAt() != null ? log.getCreatedAt() : LocalDateTime.now())
                .build();

        logRepository.save(entity);
    }    }
