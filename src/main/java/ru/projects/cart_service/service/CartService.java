package ru.projects.cart_service.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.projects.cart_service.dto.CartDto;
import ru.projects.cart_service.dto.CartItemDto;
import ru.projects.cart_service.dto.ProductVariationDto;
import ru.projects.cart_service.dto.ValidatedCartItemDto;
import ru.projects.cart_service.exception.ExternalServiceException;
import ru.projects.cart_service.mapper.CartMapper;
import ru.projects.cart_service.mapper.VariationMapper;
import ru.projects.cart_service.model.Cart;
import ru.projects.cart_service.model.CartItem;
import ru.projects.cart_service.repository.CartRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;
    private final VariationMapper variationMapper;
    private final CartMapper cartMapper;

    public List<ValidatedCartItemDto> validateCartItems(CartDto cartDto) {
        List<Long> variationIds = cartDto.cartItems().stream()
                .map(CartItemDto::productVariationId)
                .toList();

        List<ProductVariationDto> variations;
        try {
            variations = productServiceClient.getVariationsByIds(variationIds);
        } catch (FeignException e) {
            throw new ExternalServiceException("Failed to retrieve product data");
        }

        return variationMapper.toValidatedCartItemDtoList(variations);
    }

    public List<ValidatedCartItemDto> getCartItems() {
        Cart cart = findCartByAuthorizedUser();
        return validateCartItems(cartMapper.toCartDto(cart));
    }

    public List<ValidatedCartItemDto> mergeCarts(CartDto cartDto) {
        Cart cart = findCartByAuthorizedUser();
        Set<CartItem> cartItems = cartMapper.toCartItemSet(cartDto.cartItems());
        cart.addCartItems(cartItems);
        cartRepository.save(cart);
        return validateCartItems(cartMapper.toCartDto(cart));
    }

    private Cart findCartByAuthorizedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long userId =Long.parseLong(userDetails.getUsername());
        return cartRepository.findByUserId(userId).orElse(new Cart(userId));
    }
}
