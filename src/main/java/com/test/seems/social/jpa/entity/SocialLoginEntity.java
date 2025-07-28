package com.test.seems.social.jpa.entity;

import com.test.seems.user.jpa.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "TB_USER_SOCIAL_LOGIN")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SocialLoginEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_social_login_seq")
    @SequenceGenerator(
        name = "user_social_login_seq",
        sequenceName = "SEQ_USER_SOCIAL_LOGIN_SOCIAL_LOGIN_ID",
        allocationSize = 1
    )
    @Column(name = "SOCIAL_LOGIN_ID")
    private Long socialLoginId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UserEntity user;

    @Column(name = "PROVIDER", length = 50, nullable = false)
    private String provider;

    @Column(name = "SOCIAL_ID", length = 100, nullable = false)
    private String socialId;

    @Column(name = "SOCIAL_EMAIL", length = 100)
    private String socialEmail;

    @Column(name = "LINKED_AT", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date linkedAt;
} 