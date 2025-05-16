package ru.projects.cart_service.dto;

import java.util.List;

public record CartDto(
        List<CartItemDto> cartItems
) {
}
