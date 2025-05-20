package ru.projects.cart_service.dto;

import java.util.Set;

public record CartDto(
        Set<CartItemDto> cartItems
) {
}
