package com.sparta.user.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryDslUtil {

    public static OrderSpecifier<?>[] getOrderSpecifiers(Sort sort, Map<String, ComparableExpressionBase<?>> fieldMap) {
        List<OrderSpecifier<?>> orders = sort.stream().map(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            ComparableExpressionBase<?> expression = fieldMap.get(order.getProperty());
            if (expression == null) {
                throw new IllegalArgumentException("정렬할 수 없는 필드: " + order.getProperty());
            }
            return new OrderSpecifier<>(direction, expression);
        }).collect(Collectors.toList());

        return orders.toArray(new OrderSpecifier<?>[0]);
    }
}