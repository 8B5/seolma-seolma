package com.ecommerce.coupon.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCoupon is a Querydsl query type for Coupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoupon extends EntityPathBase<Coupon> {

    private static final long serialVersionUID = -131423773L;

    public static final QCoupon coupon = new QCoupon("coupon");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> issuedAt = createDateTime("issuedAt", java.time.LocalDateTime.class);

    public final BooleanPath isUsed = createBoolean("isUsed");

    public final NumberPath<Long> templateId = createNumber("templateId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> usedAt = createDateTime("usedAt", java.time.LocalDateTime.class);

    public final StringPath userId = createString("userId");

    public QCoupon(String variable) {
        super(Coupon.class, forVariable(variable));
    }

    public QCoupon(Path<? extends Coupon> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCoupon(PathMetadata metadata) {
        super(Coupon.class, metadata);
    }

}

