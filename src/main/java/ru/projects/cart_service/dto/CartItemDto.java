package ru.projects.cart_service.dto;

public record CartItemDto(
        Long productVariationId,
        int quantity
) {
}
