package com.test.seems.fortune.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDailyMessageEntity is a Querydsl query type for DailyMessageEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDailyMessageEntity extends EntityPathBase<DailyMessageEntity> {

    private static final long serialVersionUID = -1095322504L;

    public static final QDailyMessageEntity dailyMessageEntity = new QDailyMessageEntity("dailyMessageEntity");

    public final DateTimePath<java.time.LocalDateTime> createdDate = createDateTime("createdDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> guidanceTypeId = createNumber("guidanceTypeId", Long.class);

    public final StringPath messageContent = createString("messageContent");

    public final DatePath<java.time.LocalDate> messageDate = createDate("messageDate", java.time.LocalDate.class);

    public final NumberPath<Long> messageId = createNumber("messageId", Long.class);

    public final StringPath userId = createString("userId");

    public QDailyMessageEntity(String variable) {
        super(DailyMessageEntity.class, forVariable(variable));
    }

    public QDailyMessageEntity(Path<? extends DailyMessageEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDailyMessageEntity(PathMetadata metadata) {
        super(DailyMessageEntity.class, metadata);
    }

}

