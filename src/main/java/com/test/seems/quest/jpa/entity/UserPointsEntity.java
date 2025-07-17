package com.test.seems.quest.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TB_USER_POINTS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointsEntity {
    
    @Id
    @Column(name = "USER_ID", length = 255)
    private String userId;
    
    @Column(name = "POINTS", nullable = false)
    private Integer points;
} 