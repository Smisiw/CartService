package ru.projects.cart_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.projects.cart_service.dto.ProductVariationDto;
import ru.projects.cart_service.dto.ValidatedCartItemDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VariationMapper {

    @Mapping(target = "available", expression = "java(productVariationDto.quantity() - productVariationDto.reserved())")
    ValidatedCartItemDto toValidatedCartItemDto(ProductVariationDto productVariationDto);
    List<ValidatedCartItemDto> toValidatedCartItemDtoList(List<ProductVariationDto> productVariationDtos);
}
