package com.test.seems.faq.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QReplyEntity is a Querydsl query type for ReplyEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReplyEntity extends EntityPathBase<ReplyEntity> {

    private static final long serialVersionUID = -1769746031L;

    public static final QReplyEntity replyEntity = new QReplyEntity("replyEntity");

    public final StringPath content = createString("content");

    public final NumberPath<Integer> faqNo = createNumber("faqNo", Integer.class);

    public final DatePath<java.sql.Date> replyDate = createDate("replyDate", java.sql.Date.class);

    public final NumberPath<Integer> replyNo = createNumber("replyNo", Integer.class);

    public final StringPath userid = createString("userid");

    public QReplyEntity(String variable) {
        super(ReplyEntity.class, forVariable(variable));
    }

    public QReplyEntity(Path<? extends ReplyEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReplyEntity(PathMetadata metadata) {
        super(ReplyEntity.class, metadata);
    }

}

