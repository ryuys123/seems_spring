package com.test.seems.face.model.service;

import com.test.seems.face.jpa.entity.FaceLoginEntity;
import com.test.seems.face.jpa.repository.FaceLoginRepository;
import com.test.seems.face.model.dto.FaceLoginRequest;
import com.test.seems.face.model.dto.FaceLoginResponse;
import com.test.seems.face.model.dto.FaceRegistrationRequest;
import com.test.seems.face.model.dto.FaceRegistrationResponse;
import com.test.seems.face.util.FaceRecognitionUtil;
import com.test.seems.security.jwt.JWTUtil;
import com.test.seems.security.jwt.jpa.entity.RefreshToken;
import com.test.seems.security.jwt.jpa.repository.RefreshRepository;
import com.test.seems.user.jpa.entity.UserEntity;
import com.test.seems.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
     * 페이스 로그인 수행
     */
    @Transactional
    public FaceLoginResponse login(FaceLoginRequest request) {
        try {
            log.info("페이스 로그인 시작");
            
            // 1. 얼굴 인식 수행
            String recognizedUserId = faceRecognitionUtil.recognizeFace(request.getFaceImageData());
            
            if (recognizedUserId == null) {
                log.warn("얼굴 인식 실패");
                return FaceLoginResponse.failure("등록되지 않은 얼굴입니다.");
            }
            
            // 2. 사용자 정보 조회
            Optional<UserEntity> userOpt = userRepository.findById(recognizedUserId);
            if (userOpt.isEmpty()) {
                log.warn("사용자 정보를 찾을 수 없습니다: {}", recognizedUserId);
                return FaceLoginResponse.failure("사용자 정보를 찾을 수 없습니다.");
            }
            
            UserEntity user = userOpt.get();
            
            // 3. 페이스 로그인 활성화 상태 확인
            if (!isFaceLoginEnabled(user.getUserId())) {
                log.warn("페이스 로그인이 비활성화된 사용자: {}", user.getUserId());
                return FaceLoginResponse.failure("페이스 로그인이 비활성화되어 있습니다.");
            }
            
            // 4. 토큰 발급
            String accessToken = jwtUtil.createFaceJwt("face", user.getUserId(), 600000L);
            String refreshToken = jwtUtil.createFaceJwt("face", user.getUserId(), 86400000L);
            
            // 5. RefreshToken 저장
            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(user.getUserId())
                    .tokenValue(refreshToken)
                    .build();
            refreshRepository.save(refreshTokenEntity);
            
            // 6. 페이스 이름 조회
            String faceName = request.getFaceName();
            if (faceName == null) {
                List<FaceLoginEntity> faceLogins = faceLoginRepository.findByUserId(user.getUserId());
                if (!faceLogins.isEmpty()) {
                    faceName = faceLogins.get(0).getFaceName();
                }
            }
            
            log.info("페이스 로그인 성공: 사용자 {}, 페이스 {}", user.getUserId(), faceName);
            
            return FaceLoginResponse.success(
                user.getUserId(),
                user.getUserName(),
                accessToken,
                refreshToken,
                faceName
            );
            
        } catch (Exception e) {
            log.error("페이스 로그인 중 오류 발생", e);
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
            // 2. DeepFace 서비스에 얼굴 등록
            boolean deepfaceSuccess = faceRecognitionUtil.registerFace(
                user.getUserId(),
                request.getFaceImageData(),
                request.getFaceName()
            );
            if (!deepfaceSuccess) {
                log.error("DeepFace 얼굴 등록 실패: userId={}, faceName={}", user.getUserId(), request.getFaceName());
                return FaceRegistrationResponse.failure("얼굴 등록에 실패했습니다.");
            }
            // 3. DB에 페이스 로그인 정보 저장
            FaceLoginEntity faceLogin = FaceLoginEntity.builder()
                    .userId(user.getUserId())
                    .faceImagePath("deepface_registered") // DeepFace 서비스에서 관리
                    .faceName(request.getFaceName())
                    .createdBy(user.getUserName())
                    .build();
            FaceLoginEntity savedFaceLogin = faceLoginRepository.save(faceLogin);
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
}