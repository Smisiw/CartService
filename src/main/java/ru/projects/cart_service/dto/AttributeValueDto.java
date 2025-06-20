package ru.projects.cart_service.dto;

import java.util.UUID;

public record AttributeValueDto(
        UUID attributeId,
        String attributeName,
        String value
) {
}
