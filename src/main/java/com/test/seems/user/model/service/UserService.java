package com.test.seems.user.model.service;

import com.test.seems.notice.model.dto.Notice;
import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import com.test.seems.user.model.dto.User;
import com.test.seems.user.model.dto.UserInfoResponse;
import com.test.seems.quest.jpa.repository.UserPointsRepository; // quest 패키지의 UserPointsRepository 임포트
import com.test.seems.quest.jpa.entity.UserPointsEntity; // quest 패키지의 UserPointsEntity 임포트

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final UserPointsRepository userPointsRepository; // quest 패키지의 UserPointsRepository 주입

    @Autowired
    private BCryptPasswordEncoder bcryptPasswordEncoder;

    public User findByUserId(String userId) {
        return userRepository.findById(userId)
                .map(UserEntity::toDto)
                .orElse(null);
    }

    public boolean selectCheckId(String userId) {
        //기존 가입회원과 아이디 중복 검사용
        //jpa 제공 메소드 사용
        //existsById(@id 로 등록된 프로퍼티) : boolean
        //존재하면 true, 없으면 false 반환됨
        return userRepository.existsById(userId);
    }

    // 내정보 조회
    public User selectUser(String userId) {
        UserEntity entity = userRepository.findByUserId(userId);
        return entity != null ? entity.toDto() : null;
    }

    // 마이페이지용 사용자 정보 반환
    public UserInfoResponse getUserInfoByUserId(String userId) {
        UserEntity entity = userRepository.findByUserId(userId);
        if (entity == null) return null;
        
        // 페이스 연동 상태 확인
        Boolean faceLinked = entity.getFaceLoginEnabled();
        
        return new UserInfoResponse(
                entity.getUserName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getProfileImage(),
                entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null,
                entity.getStatus(),
                faceLinked
        );
    }

    // 마이페이지 사용자 정보 수정
    public boolean updateUserInfo(String userId, com.test.seems.user.model.dto.UserInfoResponse req) {
        UserEntity entity = userRepository.findByUserId(userId);
        if (entity == null) return false;
        
        log.info("업데이트 전 사용자 정보: userName={}, email={}, phone={}, profileImage={}", 
                entity.getUserName(), entity.getEmail(), entity.getPhone(), entity.getProfileImage());
        
        // 기본 정보 업데이트 - NULL 값 처리 개선
        entity.setUserName(req.getUserName() != null && !req.getUserName().trim().isEmpty() ? req.getUserName() : entity.getUserName());
        entity.setEmail(req.getEmail() != null && !req.getEmail().trim().isEmpty() ? req.getEmail() : entity.getEmail());
        entity.setPhone(req.getPhone() != null && !req.getPhone().trim().isEmpty() ? req.getPhone() : entity.getPhone());
        
        // 프로필 이미지가 새로 업로드된 경우에만 업데이트
        if (req.getProfileImage() != null && !req.getProfileImage().trim().isEmpty()) {
            entity.setProfileImage(req.getProfileImage());
            log.info("프로필 이미지 업데이트: {}", req.getProfileImage());
        } else {
            log.info("프로필 이미지 업데이트 없음, 기존 값 유지: {}", entity.getProfileImage());
        }
        
        entity.setUpdatedAt(new java.util.Date());
        
        // 비밀번호 변경 처리
        if (req.getCurrentPassword() != null && req.getNewPassword() != null && req.getConfirmPassword() != null) {
            // 현재 비밀번호 확인
            if (!bcryptPasswordEncoder.matches(req.getCurrentPassword(), entity.getUserPwd())) {
                log.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치: userId={}", userId);
                return false;
            }
            
            // 새 비밀번호와 확인 비밀번호 일치 확인
            if (!req.getNewPassword().equals(req.getConfirmPassword())) {
                log.warn("비밀번호 변경 실패 - 새 비밀번호 불일치: userId={}", userId);
                return false;
            }
            
            // 새 비밀번호 길이 확인
            if (req.getNewPassword().length() < 6) {
                log.warn("비밀번호 변경 실패 - 비밀번호 길이 부족: userId={}", userId);
                return false;
            }
            
            // 새 비밀번호 해싱 및 저장
            String hashedNewPassword = bcryptPasswordEncoder.encode(req.getNewPassword());
            entity.setUserPwd(hashedNewPassword);
            log.info("비밀번호 변경 성공: userId={}", userId);
        }
        
        userRepository.save(entity);
        
        log.info("업데이트 후 사용자 정보: userName={}, email={}, phone={}, profileImage={}", 
                entity.getUserName(), entity.getEmail(), entity.getPhone(), entity.getProfileImage());
        
        return true;
    }

    @Transactional
    public User insertUser(User user) {
        // 회원가입 시 비밀번호를 BCrypt로 해싱
        if (user.getUserPwd() != null && !isBCryptHash(user.getUserPwd())) {
            String hashedPassword = bcryptPasswordEncoder.encode(user.getUserPwd());
            user.setUserPwd(hashedPassword);
            log.info("회원가입 - 비밀번호 해싱 완료: userId={}", user.getUserId());
        }

        //jpa 제공 메소드 사용
        //save(entity) : entity => 실패하면 null 리턴
        User savedUser = userRepository.save(user.toEntity()).toDto();

        // TB_USER_POINTS에 초기 포인트(0) 삽입 (quest 모듈의 UserPointsEntity 사용)
        if (savedUser != null) {
            UserPointsEntity userPointsEntity = UserPointsEntity.builder()
                    .userId(savedUser.getUserId())
                    .points(0) // 초기 포인트 0으로 설정
                    .build();
            userPointsRepository.save(userPointsEntity);
            log.info("TB_USER_POINTS에 초기 포인트 삽입 완료: userId={}, points={}", savedUser.getUserId(), 0);
        }
        return savedUser;
    }

    @Transactional
    public User updateUser(User user) {
        // ✅ 비밀번호 변경 시에도 해싱 처리
        UserEntity existingUser = userRepository.findByUserId(user.getUserId());
        if (existingUser != null && user.getUserPwd() != null &&
                !isBCryptHash(user.getUserPwd()) &&
                !user.getUserPwd().equals(existingUser.getUserPwd())) {
            String hashedPassword = bcryptPasswordEncoder.encode(user.getUserPwd());
            user.setUserPwd(hashedPassword);
            log.info("비밀번호 변경 - 해싱 완료: userId={}", user.getUserId());
        }

        //jpa 제공 메소드 사용
        return userRepository.save(user.toEntity()).toDto();
    }

    @Transactional
    public int deleteUser(String userId) {
        //jpa 제공 메소드 사용
        //deleteById(@id 프로퍼티) : void => 실패하면 에러 발생함
        try {
            userRepository.deleteById(userId);
            return 1;
        } catch (Exception e) {
            log.error(e.getMessage());
            return 0;
        }
    }

    // 관리자용 ------------------------------------------------------

    public long selectListCount() {
        //jpa 제공 메소드 사용
        //count() : long
        return userRepository.count();
    }

    public ArrayList<User> selectList(Pageable pageable) {
        //jpa 가 제공하는 메소드 사용
        //findAll(pageable) : Page<entity>
        Page<UserEntity> page = userRepository.findAll(pageable);
        ArrayList<User> list = new ArrayList<>();
        for (UserEntity user : page) {
            list.add(user.toDto());
        }
        return list;
    }

    @Transactional
    public int updateStatus(User user) {
        //jpa 가 제공하는 save() 사용해도 됨 (주의 : 수정할 데이터와 이전 기록 데이터 모두 저장되어 있어야 함)
        //원하는 항목만 변경하기 위해 메소드 추가했음
        return userRepository.modifyUserStatus(user.getUserId(), user.getStatus());
    }

    //검색 관련 (메소드 추가) ---------------------------------------------------------------

    // 검색 결과 List<UserEntity> => List<User> 또는 ArrayList<User> 변환 필요함
    private ArrayList<User> toList(List<UserEntity> list) {
        ArrayList<User> users = new ArrayList<>();
        for (UserEntity entity : list) {
            users.add(entity.toDto());
        }
        return users;
    }

    public int selectSearchUserNameCount(String keyword) {
		/* sql :
		* 	select count(*) from notice
			where title like '%' || #{ keyword } || '%'
		* */
        return userRepository.countByKeyword(keyword);
    }


    public int selectSearchStatusCount(int status) {
		/* sql :
		* 	select count(*) from notice
			where noticecontent like '%' || #{ keyword } || '%'
		* */
        return userRepository.countByStatus(status);
    }


    public int selectSearchCreatedAtCount(Date begin, Date end) {
		/* sql :
		* 	select count(*) from notice
			where noticedate between #{ begin } and #{ end }
		* */
        return userRepository.countByCreatedAtBetween(begin, end);
    }


    public ArrayList<User> selectSearchUserName(String keyword, Pageable pageable) {
		/* sql :
			select *
			from (select rownum rnum, noticeno, title, noticedate, noticewriter, noticecontent,
						original_filepath, rename_filepath, importance, imp_end_date, readcount
				  from (select * from notice
						where title like '%' || #{ keyword } || '%'
						order by importance desc, noticedate desc, noticeno desc))
			where rnum between #{ startRow } and #{ endRow }
		* */
        return toList(userRepository.findByKeyword(keyword, pageable));
    }


    public ArrayList<User> selectSearchStatus(int status, Pageable pageable) {
		/* sql :
		* 	select *
			from (select rownum rnum, noticeno, title, noticedate, noticewriter, noticecontent,
						original_filepath, rename_filepath, importance, imp_end_date, readcount
				  from (select * from notice
						where noticecontent like '%' || #{ keyword } || '%'
						order by importance desc, noticedate desc, noticeno desc))
			where rnum between #{ startRow } and #{ endRow }
		* */
        return toList(userRepository.findByStatusEquals(status, pageable));
    }


    public ArrayList<User> selectSearchCreatedAt(Date begin, Date end, Pageable pageable) {
		/* sql :
		* 	select *
			from (select rownum rnum, noticeno, title, noticedate, noticewriter, noticecontent,
						original_filepath, rename_filepath, importance, imp_end_date, readcount
				  from (select * from notice
						where noticedate between #{ begin } and #{ end }
						order by importance desc, noticedate desc, noticeno desc))
			where rnum between #{ startRow } and #{ endRow }
		* */
        return toList(userRepository.findByCreatedAtBetween(
                begin, end, pageable));
    }

    // BCrypt 해싱 형태 확인 메서드
    private boolean isBCryptHash(String password) {
        return password != null && password.startsWith("$2a$");
    }

    /**
     * 회원가입 시 페이스 등록
     */
    @Transactional
    public User insertUserWithFace(User user, String faceImageData, String faceName) {
        // 1. 기본 회원가입 처리
        User savedUser = insertUser(user);

        if (savedUser != null && faceImageData != null && faceName != null) {
            // 2. 페이스 등록 처리
            try {
                // FaceLoginService를 통한 페이스 등록
                // 이 부분은 컨트롤러에서 처리하는 것이 더 적절할 수 있습니다.
                log.info("회원가입 시 페이스 등록 요청: 사용자 {}, 페이스 {}", savedUser.getUserId(), faceName);
            } catch (Exception e) {
                log.error("회원가입 시 페이스 등록 실패: {}", e.getMessage());
            }
        }

        return savedUser;
    }

    /**
     * 페이스 로그인 활성화 상태 업데이트
     */
    @Transactional
    public boolean updateFaceLoginEnabled(String userId, boolean enabled) {
        try {
            UserEntity user = userRepository.findByUserId(userId);
            if (user != null) {
                user.setFaceLoginEnabled(enabled);
                userRepository.save(user);
                log.info("페이스 로그인 상태 변경: 사용자 {}, 활성화: {}", userId, enabled);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("페이스 로그인 상태 변경 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 비밀번호 확인 (회원 탈퇴용)
     */
    public boolean verifyUserPassword(String userId, String password) {
        try {
            UserEntity user = userRepository.findByUserId(userId);
            if (user == null) {
                log.warn("비밀번호 확인 실패 - 사용자 없음: userId={}", userId);
                return false;
            }
            
            boolean isValid = bcryptPasswordEncoder.matches(password, user.getUserPwd());
            if (isValid) {
                log.info("비밀번호 확인 성공: userId={}", userId);
            } else {
                log.warn("비밀번호 확인 실패 - 비밀번호 불일치: userId={}", userId);
            }
            return isValid;
        } catch (Exception e) {
            log.error("비밀번호 확인 중 오류 발생: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 회원 탈퇴 (비밀번호 확인 후)
     */
    @Transactional
    public boolean deleteUserWithPasswordVerification(String userId, String password) {
        try {
            // 1. 사용자 존재 확인
            UserEntity user = userRepository.findByUserId(userId);
            if (user == null) {
                log.warn("회원 탈퇴 실패 - 사용자 없음: userId={}", userId);
                return false;
            }
            
            // 2. 비밀번호 확인 (일반 로그인 사용자의 경우)
            if (password != null && !password.isEmpty()) {
                if (!bcryptPasswordEncoder.matches(password, user.getUserPwd())) {
                    log.warn("회원 탈퇴 실패 - 비밀번호 불일치: userId={}", userId);
                    return false;
                }
            }
            
            // 3. 회원 삭제
            userRepository.deleteById(userId);
            log.info("회원 탈퇴 완료: userId={}", userId);
            return true;
            
        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 소셜 로그인 회원 탈퇴
     */
    @Transactional
    public boolean deleteSocialUser(String userId) {
        try {
            // 1. 소셜 연동 정보 먼저 삭제
            // socialLoginRepository.deleteByUser_UserId(userId); // socialLoginRepository가 없으므로 주석 처리
            // 2. 사용자 정보 삭제
            userRepository.deleteById(userId);
            log.info("소셜 회원 탈퇴 완료: userId={}", userId);
            return true;
        } catch (Exception e) {
            log.error("소셜 회원 탈퇴 중 오류 발생: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }

}
