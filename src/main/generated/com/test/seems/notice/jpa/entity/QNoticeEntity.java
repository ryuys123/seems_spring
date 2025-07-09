package com.test.seems.notice.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNoticeEntity is a Querydsl query type for NoticeEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNoticeEntity extends EntityPathBase<NoticeEntity> {

    private static final long serialVersionUID = -1740486527L;

    public static final QNoticeEntity noticeEntity = new QNoticeEntity("noticeEntity");

    public final StringPath content = createString("content");

    public final DatePath<java.sql.Date> impEndDate = createDate("impEndDate", java.sql.Date.class);

    public final StringPath importance = createString("importance");

    public final DatePath<java.sql.Date> noticeDate = createDate("noticeDate", java.sql.Date.class);

    public final NumberPath<Integer> noticeNo = createNumber("noticeNo", Integer.class);

    public final StringPath originalFilePath = createString("originalFilePath");

    public final NumberPath<Integer> readCount = createNumber("readCount", Integer.class);

    public final StringPath renameFilePath = createString("renameFilePath");

    public final StringPath title = createString("title");

    public final StringPath userId = createString("userId");

    public QNoticeEntity(String variable) {
        super(NoticeEntity.class, forVariable(variable));
    }

    public QNoticeEntity(Path<? extends NoticeEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNoticeEntity(PathMetadata metadata) {
        super(NoticeEntity.class, metadata);
    }

}

