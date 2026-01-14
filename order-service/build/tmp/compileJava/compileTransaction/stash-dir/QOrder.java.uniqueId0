package com.ecommerce.order.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrder is a Querydsl query type for Order
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrder extends EntityPathBase<Order> {

    private static final long serialVersionUID = -145521697L;

    public static final QOrder order = new QOrder("order1");

    public final NumberPath<Long> appliedCouponId = createNumber("appliedCouponId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> cancelledAt = createDateTime("cancelledAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> orderedAt = createDateTime("orderedAt", java.time.LocalDateTime.class);

    public final StringPath ordererName = createString("ordererName");

    public final EnumPath<PaymentMethod> paymentMethod = createEnum("paymentMethod", PaymentMethod.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final StringPath productSnapshotName = createString("productSnapshotName");

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final StringPath shippingAddress = createString("shippingAddress");

    public final EnumPath<OrderStatus> status = createEnum("status", OrderStatus.class);

    public final NumberPath<java.math.BigDecimal> totalPrice = createNumber("totalPrice", java.math.BigDecimal.class);

    public final StringPath userId = createString("userId");

    public QOrder(String variable) {
        super(Order.class, forVariable(variable));
    }

    public QOrder(Path<? extends Order> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrder(PathMetadata metadata) {
        super(Order.class, metadata);
    }

}

