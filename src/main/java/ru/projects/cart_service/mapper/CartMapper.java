package ru.projects.cart_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.projects.cart_service.dto.CartDto;
import ru.projects.cart_service.dto.CartItemDto;
import ru.projects.cart_service.model.Cart;
import ru.projects.cart_service.model.CartItem;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface CartMapper {
    CartDto toCartDto(Cart cart);
    CartItemDto toCartItemDto(CartItem cartItem);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    CartItem toCartItem(CartItemDto cartItemDto);
    Set<CartItem> toCartItemSet(Set<CartItemDto> cartItemDtoList);
}
