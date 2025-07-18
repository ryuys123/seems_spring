package com.test.seems.face.model.service;

import com.test.seems.face.jpa.entity.FaceLoginEntity;
import com.test.seems.face.jpa.repository.FaceLoginRepository;
import com.test.seems.face.model.dto.*;
import com.test.seems.face.util.FaceRecognitionUtil;
import com.test.seems.security.jwt.JWTUtil;
import com.test.seems.security.jwt.jpa.repository.RefreshRepository;
import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaceLoginService {
    
    private final FaceRecognitionUtil faceRecognitionUtil;
    private final FaceLoginRepository faceLoginRepository;
    private final UserRepository userRepository;
    private final RefreshRepository refreshRepository;
    private final JWTUtil jwtUtil;
    
    /**
     * 페이스 로그인
     */
    @Transactional
    public FaceLoginResponse faceLogin(FaceLoginRequest request) {
        try {
            log.info("페이스 로그인 시도: 얼굴 인식 시작");
            
            // 1. DeepFace 서비스에서 얼굴 인식하여 사용자 ID 찾기
            log.info("DeepFace 서비스에서 얼굴 인식 시작");
            
            // React 호환을 위해 두 필드 모두 확인
            String imageData = request.getFaceImageData() != null ? request.getFaceImageData() : request.getImageData();
            log.info("페이스 로그인 요청 데이터 - faceImageData 길이: {}, imageData 길이: {}, 최종 사용할 데이터 길이: {}", 
                    request.getFaceImageData() != null ? request.getFaceImageData().length() : "null",
                    request.getImageData() != null ? request.getImageData().length() : "null",
                    imageData != null ? imageData.length() : "null");
            
            if (imageData == null) {
                log.error("페이스 로그인 실패: 이미지 데이터가 없습니다.");
                return FaceLoginResponse.failure("얼굴 이미지 데이터가 필요합니다.");
            }
            
            String recognizedUserId = faceRecognitionUtil.recognizeFace(imageData);
            
            if (recognizedUserId == null) {
                log.warn("DeepFace 서비스에서 얼굴 인식 실패");
                return FaceLoginResponse.failure("등록되지 않은 얼굴입니다. 먼저 페이스 등록을 해주세요.");
            }
            
            log.info("DeepFace 서비스에서 얼굴 인식 성공: userId={}", recognizedUserId);
            
            // 2. 사용자 정보 확인
            Optional<UserEntity> userOpt = userRepository.findById(recognizedUserId);
            if (userOpt.isEmpty()) {
                log.warn("인식된 사용자 정보를 찾을 수 없음: {}", recognizedUserId);
                return FaceLoginResponse.failure("사용자 정보를 찾을 수 없습니다.");
            }
            
            UserEntity user = userOpt.get();
            if (!user.getFaceLoginEnabled()) {
                log.warn("페이스 로그인이 비활성화된 사용자: {}", recognizedUserId);
                return FaceLoginResponse.failure("페이스 로그인이 비활성화되었습니다.");
            }
            
            // 3. JWT 토큰 생성
            String accessToken = jwtUtil.createFaceJwt("access", user.getUserId(), 600000L); // 10분
            String refreshToken = jwtUtil.createFaceJwt("refresh", user.getUserId(), 86400000L); // 24시간
            
            // 4. Refresh 토큰 저장 (임시로 주석 처리)
            // refreshService.saveRefreshToken(user.getUserId(), refreshToken, 86400000L);
            
            // 5. 페이스 로그인 정보 업데이트
            List<FaceLoginEntity> faceLogins = faceLoginRepository.findByUserId(recognizedUserId);
            if (!faceLogins.isEmpty()) {
                FaceLoginEntity faceLogin = faceLogins.get(0);
                faceLogin.setLastUsedAt(LocalDateTime.now());
                faceLoginRepository.save(faceLogin);
                log.info("페이스 로그인 정보 업데이트: userId={}", recognizedUserId);
            }
            
            log.info("페이스 로그인 성공: userId={}", recognizedUserId);
            return FaceLoginResponse.success(
                user.getUserId(), 
                user.getUserName(), 
                accessToken, 
                refreshToken, 
                faceLogins.isEmpty() ? "정면" : faceLogins.get(0).getFaceName()
            );
            
        } catch (Exception e) {
            log.error("페이스 로그인 중 오류 발생: error={}", e.getMessage(), e);
            return FaceLoginResponse.failure("페이스 로그인 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 페이스 등록
     */
    @Transactional
    public FaceRegistrationResponse registerFace(FaceRegistrationRequest request) {
        try {
            log.info("페이스 등록 시작: 사용자 {}", request.getUserId());
            
            // 1. 사용자 정보 조회
            Optional<UserEntity> userOpt = userRepository.findById(request.getUserId());
            if (userOpt.isEmpty()) {
                log.error("페이스 등록 실패(사용자 없음): userId={}", request.getUserId());
                return FaceRegistrationResponse.failure("사용자 정보를 찾을 수 없습니다.");
            }
            UserEntity user = userOpt.get();
            
            // 2. 사용자당 하나의 얼굴만 등록 가능하도록 체크
            List<FaceLoginEntity> existingFaces = faceLoginRepository.findByUserId(request.getUserId());
            if (!existingFaces.isEmpty()) {
                log.warn("이미 등록된 얼굴이 있는 사용자: userId={}", request.getUserId());
                return FaceRegistrationResponse.failure("이미 등록된 얼굴이 있습니다. 한 계정당 하나의 얼굴만 등록 가능합니다.");
            }
            
            // 3. 얼굴 중복 등록 체크 (다른 계정에서 이미 등록된 얼굴인지 확인)
            log.info("얼굴 중복 등록 체크 시작");
            String existingUserId = faceRecognitionUtil.checkFaceDuplicate(request.getFaceImageData());
            if (existingUserId != null && !existingUserId.equals(request.getUserId())) {
                log.warn("이미 다른 계정에 등록된 얼굴: 기존 사용자={}, 요청 사용자={}", existingUserId, request.getUserId());
                return FaceRegistrationResponse.failure("이미 다른 계정에 등록된 얼굴입니다. 다른 얼굴로 등록해주세요.");
            }
            
            // 4. DeepFace 서비스에 얼굴 등록
            boolean deepfaceSuccess = faceRecognitionUtil.registerFace(
                user.getUserId(),
                request.getFaceImageData(),
                request.getFaceName()
            );
            if (!deepfaceSuccess) {
                log.error("DeepFace 얼굴 등록 실패: userId={}, faceName={}", user.getUserId(), request.getFaceName());
                return FaceRegistrationResponse.failure("얼굴 등록에 실패했습니다.");
            }
            
            // 5. DB에 페이스 로그인 정보 저장
            FaceLoginEntity faceLogin = FaceLoginEntity.builder()
                    .userId(user.getUserId())
                    .faceImagePath("deepface_registered") // DeepFace 서비스에서 관리
                    .faceName(request.getFaceName())
                    .createdBy(user.getUserName())
                    .build();
            FaceLoginEntity savedFaceLogin = faceLoginRepository.save(faceLogin);
            
            // 6. 사용자의 페이스 로그인 활성화
            user.setFaceLoginEnabled(true);
            userRepository.save(user);
            
            log.info("페이스 등록 성공: userId={}, faceName={}", user.getUserId(), request.getFaceName());
            return FaceRegistrationResponse.success(savedFaceLogin.getFaceLoginId(), request.getFaceName());
            
        } catch (Exception e) {
            log.error("페이스 등록 예외: userId={}, faceName={}, error={}", request.getUserId(), request.getFaceName(), e.getMessage());
            return FaceRegistrationResponse.failure("페이스 등록 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 페이스 삭제
     */
    @Transactional
    public boolean deleteFace(String userId, String faceName) {
        try {
            log.info("페이스 삭제 시작: 사용자 {}, 페이스 {}", userId, faceName);
            
            // 1. DeepFace 서비스에서 얼굴 삭제
            boolean deepfaceSuccess = faceRecognitionUtil.deleteFace(userId, faceName);
            
            if (!deepfaceSuccess) {
                log.warn("DeepFace 서비스 얼굴 삭제 실패");
                return false;
            }
            
            // 2. DB에서 페이스 로그인 정보 삭제
            Optional<FaceLoginEntity> faceLoginOpt = faceLoginRepository.findByUserIdAndFaceName(userId, faceName);
            if (faceLoginOpt.isPresent()) {
                faceLoginRepository.delete(faceLoginOpt.get());
            }
            
            log.info("페이스 삭제 성공: 사용자 {}, 페이스 {}", userId, faceName);
            return true;
            
        } catch (Exception e) {
            log.error("페이스 삭제 중 오류 발생", e);
            return false;
        }
    }
    
    /**
     * 사용자의 등록된 페이스 목록 조회
     */
    public List<FaceLoginEntity> getUserFaces(String userId) {
        return faceLoginRepository.findByUserId(userId);
    }
    
    /**
     * 페이스 로그인 활성화 상태 확인
     */
    public boolean isFaceLoginEnabled(String userId) {
        // 사용자가 등록된 페이스가 있는지 확인
        long faceCount = faceLoginRepository.countByUserId(userId);
        return faceCount > 0;
    }
    
    /**
     * 페이스 로그인 활성화/비활성화
     */
    @Transactional
    public boolean toggleFaceLogin(String userId, boolean enabled) {
        try {
            if (enabled) {
                // 활성화는 이미 등록된 페이스가 있으면 자동으로 활성화됨
                return isFaceLoginEnabled(userId);
            } else {
                // 비활성화는 모든 페이스 삭제
                List<FaceLoginEntity> userFaces = faceLoginRepository.findByUserId(userId);
                for (FaceLoginEntity face : userFaces) {
                    deleteFace(userId, face.getFaceName());
                }
                return true;
            }
        } catch (Exception e) {
            log.error("페이스 로그인 상태 변경 중 오류 발생", e);
            return false;
        }
    }
    
    /**
     * 페이스 회원가입 (사용자 생성 + 페이스 등록)
     */
    @Transactional
    public FaceSignupResponse faceSignup(FaceSignupRequest request) {
        try {
            log.info("페이스 회원가입 시작: 사용자 {}", request.getUserId());
            
            // 1. 사용자 ID 중복 확인
            Optional<UserEntity> existingUser = userRepository.findById(request.getUserId());
            if (existingUser.isPresent()) {
                log.warn("이미 존재하는 사용자 ID: {}", request.getUserId());
                return FaceSignupResponse.failure("이미 존재하는 사용자 ID입니다.");
            }
            
            // 2. 사용자 정보 생성
            UserEntity newUser = UserEntity.builder()
                    .userId(request.getUserId())
                    .userName(request.getUsername())
                    .phone(request.getPhone())
                    .userPwd(request.getPassword()) // 실제로는 해시화해야 함
                    .faceLoginEnabled(true) // 페이스 로그인 활성화
                    .status(1) // 활성 상태
                    .adminYn("N") // 일반 사용자
                    .createdAt(new java.util.Date()) // 생성 시간 설정
                    .updatedAt(new java.util.Date()) // 수정 시간 설정
                    .build();
            
            UserEntity savedUser = userRepository.save(newUser);
            log.info("사용자 정보 저장 성공: userId={}, userName={}, phone={}, faceLoginEnabled={}", 
                savedUser.getUserId(), savedUser.getUserName(), savedUser.getPhone(), savedUser.getFaceLoginEnabled());
            
            // 3. 얼굴 중복 등록 체크 (다른 계정에서 이미 등록된 얼굴인지 확인)
            log.info("얼굴 중복 등록 체크 시작");
            
            // React 호환을 위해 두 필드 모두 확인
            String imageData = request.getFaceImageData() != null ? request.getFaceImageData() : request.getImageData();
            log.info("페이스 회원가입 요청 데이터 - faceImageData 길이: {}, imageData 길이: {}, 최종 사용할 데이터 길이: {}", 
                    request.getFaceImageData() != null ? request.getFaceImageData().length() : "null",
                    request.getImageData() != null ? request.getImageData().length() : "null",
                    imageData != null ? imageData.length() : "null");
            
            if (imageData == null) {
                log.error("페이스 회원가입 실패: 이미지 데이터가 없습니다.");
                return FaceSignupResponse.failure("얼굴 이미지 데이터가 필요합니다.");
            }
            
            String existingUserId = faceRecognitionUtil.checkFaceDuplicate(imageData);
            if (existingUserId != null) {
                log.warn("이미 다른 계정에 등록된 얼굴: 기존 사용자={}, 요청 사용자={}", existingUserId, request.getUserId());
                return FaceSignupResponse.failure("이미 다른 계정에 등록된 얼굴입니다. 다른 얼굴로 등록해주세요.");
            }
            
            // 4. DeepFace 서비스에 얼굴 등록 요청
            log.info("DeepFace 서비스 호출 시작: userId={}", request.getUserId());
            boolean deepfaceSuccess = faceRecognitionUtil.registerFace(
                    request.getUserId(), 
                    imageData, 
                    "정면"
            );
            
            if (!deepfaceSuccess) {
                log.error("DeepFace 서비스에서 얼굴 등록 실패: userId={}", request.getUserId());
                return FaceSignupResponse.failure("얼굴 인식 등록에 실패했습니다. 다시 시도해주세요.");
            }
            
            log.info("DeepFace 서비스에서 얼굴 등록 성공: userId={}", request.getUserId());
            
            // 4. 페이스 로그인 정보 생성 및 저장
            FaceLoginEntity faceLoginEntity = FaceLoginEntity.builder()
                    .userId(request.getUserId())
                    .faceIdHash("temp_hash_" + request.getUserId() + "_" + System.currentTimeMillis()) // 임시 해시값
                    .faceImagePath("/faces/" + request.getUserId() + "_" + System.currentTimeMillis() + ".jpg") // 파일 경로만 저장
                    .faceName("정면") // 기본값
                    .isActive(deepfaceSuccess) // DeepFace 등록 성공 여부에 따라 활성화
                    .createdBy(request.getUserId())
                    .build();
            
            FaceLoginEntity savedFaceLogin = faceLoginRepository.save(faceLoginEntity);
            log.info("페이스 로그인 정보 저장 성공: faceLoginId={}, userId={}, isActive={}", 
                    savedFaceLogin.getFaceLoginId(), savedFaceLogin.getUserId(), savedFaceLogin.getIsActive());
            
            // 5. DB 저장 확인
            Optional<UserEntity> verifyUser = userRepository.findById(savedUser.getUserId());
            if (verifyUser.isPresent()) {
                UserEntity verifiedUser = verifyUser.get();
                log.info("DB 저장 확인 - 사용자: userId={}, faceLoginEnabled={}", 
                    verifiedUser.getUserId(), verifiedUser.getFaceLoginEnabled());
            } else {
                log.error("DB 저장 확인 실패 - 사용자를 찾을 수 없음: {}", savedUser.getUserId());
            }
            
            List<FaceLoginEntity> verifyFaces = faceLoginRepository.findByUserId(savedUser.getUserId());
            log.info("DB 저장 확인 - 페이스 로그인 정보: count={}", verifyFaces.size());
            
            log.info("페이스 회원가입 완료: userId={}, faceLoginId={}", savedUser.getUserId(), savedFaceLogin.getFaceLoginId());
            
            return FaceSignupResponse.success(
                savedUser.getUserId(),
                savedUser.getUserName(),
                savedFaceLogin.getFaceLoginId()
            );
            
        } catch (Exception e) {
            log.error("페이스 회원가입 중 오류 발생: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            return FaceSignupResponse.failure("페이스 회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}