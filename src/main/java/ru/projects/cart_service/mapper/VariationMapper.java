package ru.projects.cart_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.projects.cart_service.dto.CartItemDto;
import ru.projects.cart_service.dto.ProductVariationDto;
import ru.projects.cart_service.dto.ValidatedCartItemDto;
import ru.projects.cart_service.exception.CartItemNotFoundException;
import ru.projects.cart_service.model.CartItem;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface VariationMapper {

    @Mapping(target = "available", expression = "java(productVariationDto.quantity() - productVariationDto.reserved())")
    @Mapping(target = "quantity", expression = "java(cartItem.getQuantity())")
    @Mapping(target = "id", source = "productVariationDto.id")
    ValidatedCartItemDto toValidatedCartItemDto(ProductVariationDto productVariationDto, CartItem cartItem);
    @Mapping(target = "available", expression = "java(productVariationDto.quantity() - productVariationDto.reserved())")
    @Mapping(target = "quantity", expression = "java(cartItemDto.quantity())")
    @Mapping(target = "id", source = "productVariationDto.id")
    ValidatedCartItemDto toValidatedCartItemDto(ProductVariationDto productVariationDto, CartItemDto cartItemDto);
    default Set<ValidatedCartItemDto> toValidatedCartItemDtoSetFromCartItems(Set<ProductVariationDto> productVariationDtos, Set<CartItem> cartItems) {
        Map<Long, CartItem> itemMap = cartItems.stream()
                .collect(Collectors.toMap(CartItem::getProductVariationId, Function.identity()));
        return productVariationDtos.stream()
                .map(productVariationDto -> {
                    CartItem item = itemMap.get(productVariationDto.id());
                    if (item == null) {
                        throw new CartItemNotFoundException("Cart item with id " + productVariationDto.id() + " not found");
                    }
                    return toValidatedCartItemDto(productVariationDto, item);
                })
                .collect(Collectors.toSet());
    }
    default Set<ValidatedCartItemDto> toValidatedCartItemDtoSetFromCartItemDtos(Set<ProductVariationDto> productVariationDtos, Set<CartItemDto> cartItemDtos) {
        Map<Long, CartItemDto> itemMap = cartItemDtos.stream()
                .collect(Collectors.toMap(CartItemDto::productVariationId, Function.identity()));
        return productVariationDtos.stream()
                .map(productVariationDto -> {
                    CartItemDto item = itemMap.get(productVariationDto.id());
                    if (item == null) {
                        throw new CartItemNotFoundException("Cart item with id " + productVariationDto.id() + " not found");
                    }
                    return toValidatedCartItemDto(productVariationDto, item);
                })
                .collect(Collectors.toSet());
    }
}
