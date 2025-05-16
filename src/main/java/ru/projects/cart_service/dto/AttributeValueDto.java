package ru.projects.cart_service.dto;

public record AttributeValueDto(
        Long attributeId,
        String attributeName,
        String value
) {
}
