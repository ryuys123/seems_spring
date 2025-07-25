package com.test.seems.guidance.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_GUIDANCE_TYPES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuidanceTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GUIDANCE_TYPES_GUIDANCE_TYPE_ID")
    @SequenceGenerator(name = "SEQ_GUIDANCE_TYPES_GUIDANCE_TYPE_ID", sequenceName = "SEQ_GUIDANCE_TYPES_GUIDANCE_TYPE_ID", allocationSize = 1)
    @Column(name = "GUIDANCE_TYPE_ID")
    private Long guidanceTypeId;

    @Column(name = "GUIDANCE_TYPE_NAME", nullable = false, length = 100)
    private String guidanceTypeName;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;
}
