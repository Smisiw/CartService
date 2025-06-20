package ru.projects.cart_service.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.projects.cart_service.dto.CartDto;
import ru.projects.cart_service.dto.CartItemDto;
import ru.projects.cart_service.dto.ProductVariationDto;
import ru.projects.cart_service.dto.ValidatedCartItemDto;
import ru.projects.cart_service.exception.CartItemNotFoundException;
import ru.projects.cart_service.exception.ExternalServiceException;
import ru.projects.cart_service.mapper.CartMapper;
import ru.projects.cart_service.mapper.VariationMapper;
import ru.projects.cart_service.model.Cart;
import ru.projects.cart_service.model.CartItem;
import ru.projects.cart_service.repository.CartRepository;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;
    private final VariationMapper variationMapper;
    private final CartMapper cartMapper;

    public Set<ValidatedCartItemDto> validateCartItems(CartDto cartDto) {
        Set<UUID> variationIds = cartDto.cartItems().stream()
                .map(CartItemDto::productVariationId)
                .collect(Collectors.toSet());

        Set<ProductVariationDto> variations;
        try {
            variations = productServiceClient.getVariationsByIds(variationIds).getBody();
        } catch (FeignException e) {
            throw new ExternalServiceException("Failed to retrieve product data");
        }
        if (variations == null || variations.isEmpty()) {
            return Set.of();
        }
        return variationMapper.toValidatedCartItemDtoSetFromCartItemDtos(variations, cartDto.cartItems());
    }

    public Set<ValidatedCartItemDto> getCartItems(UUID userId) {
        Cart cart = cartRepository.findById(userId).orElse(new Cart(userId));
        Set<UUID> variationIds = cart.getItems().stream()
                .map(CartItem::getProductVariationId)
                .collect(Collectors.toSet());
        Set<ProductVariationDto> variations;
        try {
            variations = productServiceClient.getVariationsByIds(variationIds).getBody();
        } catch (FeignException e) {
            throw new ExternalServiceException("Failed to retrieve product data");
        }
        if (variations == null || variations.isEmpty()) {
            return Set.of();
        }
        if (variations.size() != variationIds.size()) {
            Set<CartItem> itemsToRemove = cart.getItems().stream().filter(cartItem ->
                    !variations.stream()
                    .map(ProductVariationDto::id)
                    .collect(Collectors.toSet()
                    ).contains(cartItem.getId())
            ).collect(Collectors.toSet());
            cart.removeCartItems(itemsToRemove);
            cartRepository.save(cart);
        }
        return variationMapper.toValidatedCartItemDtoSetFromCartItems(variations, cart.getItems());
    }

    public void mergeCarts(CartDto cartDto, UUID userId) {
        Cart cart = cartRepository.findById(userId).orElse(new Cart(userId));
        Set<CartItem> cartItems = cartMapper.toCartItemSet(cartDto.cartItems());
        cart.addCartItems(cartItems);
        cartRepository.save(cart);
    }

    public void addCartItem(UUID productVariationId, UUID userId) {
        Cart cart = cartRepository.findById(userId).orElse(new Cart(userId));
        CartItem cartItem = cart.getItems().stream().filter(item -> item.getProductVariationId().equals(productVariationId)).findFirst().orElse(null);
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        } else {
            cartItem = new CartItem();
            cartItem.setProductVariationId(productVariationId);
            cartItem.setQuantity(1);
        }
        cart.addCartItem(cartItem);
        cartRepository.save(cart);
    }

    public void subtractCartItem(UUID productVariationId, UUID userId) {
        Cart cart = cartRepository.findById(userId).orElse(new Cart(userId));
        CartItem cartItem = cart.getItems().stream().filter(item -> item.getProductVariationId().equals(productVariationId)).findFirst().orElseThrow(
                () -> new CartItemNotFoundException("CartItem with id " + productVariationId + " not found")
        );
        if (cartItem.getQuantity() == 1) {
            cart.removeCartItem(cartItem);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            cart.addCartItem(cartItem);
        }
        cartRepository.save(cart);
    }

    public void removeCartItems(Set<UUID> cartItemIds, UUID userId) {
        Cart cart = cartRepository.findById(userId).orElse(new Cart(userId));
        cart.getItems().removeIf(item -> cartItemIds.contains(item.getProductVariationId()));
        cartRepository.save(cart);
    }

    public void clearCart(UUID userId) {
        Cart cart = cartRepository.findById(userId).orElse(new Cart(userId));
        cart.removeAllCartItems();
        cartRepository.save(cart);
    }
}
