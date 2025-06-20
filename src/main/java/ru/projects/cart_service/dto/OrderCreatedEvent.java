package ru.projects.cart_service.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID userId,
        Set<Item> items,
        BigDecimal totalPrice,
        String paymentMethod,
        String deliveryAddress
) {
    public record Item(
            UUID id,
            UUID productVariationId,
            Integer quantity,
            BigDecimal unitPrice
    ) {}
}