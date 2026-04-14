package ru.projects.cart_service.service;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.projects.cart_service.dto.*;
import ru.projects.cart_service.exception.CartItemNotFoundException;
import ru.projects.cart_service.exception.ExternalServiceException;
import ru.projects.cart_service.mapper.CartMapper;
import ru.projects.cart_service.mapper.VariationMapper;
import ru.projects.cart_service.model.Cart;
import ru.projects.cart_service.model.CartItem;
import ru.projects.cart_service.repository.CartRepository;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductServiceClient productServiceClient;
    @Mock
    private VariationMapper variationMapper;
    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private final UUID userId = UUID.randomUUID();
    private final UUID variationId = UUID.randomUUID();

    @Test
    void addCartItem_createsNewCartItem_whenItemNotInCart() {
        Cart cart = new Cart(userId);
        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));

        cartService.addCartItem(variationId, userId);

        verify(cartRepository).save(argThat(c -> {
            Optional<CartItem> item = c.getItems().stream()
                    .filter(i -> i.getProductVariationId().equals(variationId))
                    .findFirst();
            return item.isPresent() && item.get().getQuantity() == 1;
        }));
    }

    @Test
    void addCartItem_incrementsQuantity_whenItemAlreadyInCart() {
        Cart cart = new Cart(userId);
        CartItem existing = new CartItem();
        existing.setProductVariationId(variationId);
        existing.setQuantity(2);
        cart.addCartItem(existing);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));

        cartService.addCartItem(variationId, userId);

        verify(cartRepository).save(argThat(c -> {
            Optional<CartItem> item = c.getItems().stream()
                    .filter(i -> i.getProductVariationId().equals(variationId))
                    .findFirst();
            return item.isPresent() && item.get().getQuantity() == 3;
        }));
    }

    @Test
    void addCartItem_createsNewCart_whenCartDoesNotExist() {
        when(cartRepository.findById(userId)).thenReturn(Optional.empty());

        cartService.addCartItem(variationId, userId);

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void subtractCartItem_removesItem_whenQuantityIsOne() {
        Cart cart = new Cart(userId);
        CartItem item = new CartItem();
        item.setProductVariationId(variationId);
        item.setQuantity(1);
        cart.addCartItem(item);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));

        cartService.subtractCartItem(variationId, userId);

        verify(cartRepository).save(argThat(c -> c.getItems().isEmpty()));
    }

    @Test
    void subtractCartItem_decrementsQuantity_whenQuantityMoreThanOne() {
        Cart cart = new Cart(userId);
        CartItem item = new CartItem();
        item.setProductVariationId(variationId);
        item.setQuantity(3);
        cart.addCartItem(item);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));

        cartService.subtractCartItem(variationId, userId);

        verify(cartRepository).save(argThat(c -> {
            Optional<CartItem> found = c.getItems().stream()
                    .filter(i -> i.getProductVariationId().equals(variationId))
                    .findFirst();
            return found.isPresent() && found.get().getQuantity() == 2;
        }));
    }

    @Test
    void subtractCartItem_throws_whenItemNotFound() {
        Cart cart = new Cart(userId);
        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));

        assertThrows(CartItemNotFoundException.class, () -> cartService.subtractCartItem(variationId, userId));
    }

    @Test
    void removeCartItems_removesSpecifiedItems() {
        Cart cart = new Cart(userId);
        CartItem item = new CartItem();
        item.setProductVariationId(variationId);
        item.setQuantity(1);
        cart.addCartItem(item);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));

        cartService.removeCartItems(Set.of(variationId), userId);

        verify(cartRepository).save(argThat(c -> c.getItems().isEmpty()));
    }

    @Test
    void clearCart_removesAllItems() {
        Cart cart = new Cart(userId);
        CartItem item = new CartItem();
        item.setProductVariationId(variationId);
        item.setQuantity(2);
        cart.addCartItem(item);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));

        cartService.clearCart(userId);

        verify(cartRepository).save(argThat(c -> c.getItems().isEmpty()));
    }

    @Test
    void mergeCarts_addItemsToCart() {
        Cart cart = new Cart(userId);
        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));

        CartItemDto cartItemDto = new CartItemDto(variationId, 2);
        CartDto cartDto = new CartDto(Set.of(cartItemDto));

        CartItem cartItem = new CartItem();
        cartItem.setProductVariationId(variationId);
        cartItem.setQuantity(2);
        when(cartMapper.toCartItemSet(Set.of(cartItemDto))).thenReturn(Set.of(cartItem));

        cartService.mergeCarts(cartDto, userId);

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void validateCartItems_returnsEmpty_whenVariationsEmpty() {
        CartItemDto cartItemDto = new CartItemDto(variationId, 1);
        CartDto cartDto = new CartDto(Set.of(cartItemDto));

        when(productServiceClient.getVariationsByIds(any())).thenReturn(ResponseEntity.ok(Set.of()));

        Set<ValidatedCartItemDto> result = cartService.validateCartItems(cartDto);

        assertTrue(result.isEmpty());
    }

    @Test
    void validateCartItems_throwsExternalServiceException_onFeignError() {
        CartItemDto cartItemDto = new CartItemDto(variationId, 1);
        CartDto cartDto = new CartDto(Set.of(cartItemDto));

        when(productServiceClient.getVariationsByIds(any())).thenThrow(mock(FeignException.class));

        assertThrows(ExternalServiceException.class, () -> cartService.validateCartItems(cartDto));
    }

    @Test
    void getCartItems_returnsEmpty_whenNoVariationsReturned() {
        Cart cart = new Cart(userId);
        CartItem item = new CartItem();
        item.setProductVariationId(variationId);
        item.setQuantity(1);
        cart.addCartItem(item);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
        when(productServiceClient.getVariationsByIds(any())).thenReturn(ResponseEntity.ok(Set.of()));

        Set<ValidatedCartItemDto> result = cartService.getCartItems(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCartItems_throwsExternalServiceException_onFeignError() {
        when(cartRepository.findById(userId)).thenReturn(Optional.of(new Cart(userId)));
        when(productServiceClient.getVariationsByIds(any())).thenThrow(mock(FeignException.class));

        assertThrows(ExternalServiceException.class, () -> cartService.getCartItems(userId));
    }
}
