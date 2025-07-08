package com.test.seems.security.jwt.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name="TB_REFRESH_TOKENS")
public class RefreshToken {
    @Id
    @Column(name="id", length=50, nullable=false)
    private String id;

    @Column(name="USER_ID", nullable = false)
    private String userId;

    @Column(name="token_value", nullable = false, length=512, unique=true)
    private String tokenValue;
}
