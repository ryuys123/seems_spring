package com.test.seems.user.model.service;

import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import com.test.seems.user.model.dto.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    @Autowired
    private final UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder bcryptPasswordEncoder;

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

    @Transactional
    public User insertUser(User user) {
        // ✅ 회원가입 시 비밀번호를 BCrypt로 해싱
        if (user.getUserPwd() != null && !isBCryptHash(user.getUserPwd())) {
            String hashedPassword = bcryptPasswordEncoder.encode(user.getUserPwd());
            user.setUserPwd(hashedPassword);
            log.info("회원가입 - 비밀번호 해싱 완료: userId={}", user.getUserId());
        }
        
        //jpa 제공 메소드 사용
        //save(entity) : entity => 실패하면 null 리턴
        return userRepository.save(user.toEntity()).toDto();
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

    public int selectSearchUserIdCount(String keyword) {
        return (int)userRepository.countByUserId(keyword);
    }

    public int selectSearchUserNameCount(String keyword) {
        return (int)userRepository.countByUserNameContaining(keyword);
    }

    public int selectSearchCreatedAtCount(java.util.Date begin, java.util.Date end) {
        return (int) userRepository.countByCreatedAtBetween(begin, end);
    }

    public int selectSearchStatusCount(int keyword) {
        return (int)userRepository.countByStatus(keyword);
    }


    public ArrayList<User> selectSearchUserId(String keyword, Pageable pageable) {
        return toList(userRepository.findByUserIdEquals(keyword, pageable));
    }

    public ArrayList<User> selectSearchUserName(String keyword, Pageable pageable) {
        return toList(userRepository.findByUserNameContaining(keyword, pageable));
    }

    public ArrayList<User> selectSearchCreatedAt(java.util.Date begin, java.util.Date end, Pageable pageable) {
        return toList(userRepository.findByCreatedAtBetween(begin, end, pageable));
    }

    public ArrayList<User> selectSearchStatus(int keyword, Pageable pageable) {
        return toList(userRepository.findByStatus(keyword, pageable));
    }

    // BCrypt 해싱 형태 확인 메서드
    private boolean isBCryptHash(String password) {
        return password != null && password.startsWith("$2a$");
    }

}
