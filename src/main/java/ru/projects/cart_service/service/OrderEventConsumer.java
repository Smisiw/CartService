package ru.projects.cart_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.projects.cart_service.dto.OrderCreatedEvent;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderEventConsumer {
    private final CartService cartService;

    @KafkaListener(topics = "order.created", groupId = "cart-service-group")
    public void handleOrderCreated(String orderCreatedEventJSON) {
        ObjectMapper objectMapper = new ObjectMapper();
        OrderCreatedEvent orderCreatedEvent;
        try {
            orderCreatedEvent = objectMapper.readValue(orderCreatedEventJSON, OrderCreatedEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        cartService.removeCartItems(
                orderCreatedEvent.items().stream()
                        .map(OrderCreatedEvent.Item::productVariationId)
                        .collect(Collectors.toSet()),
                orderCreatedEvent.userId()
        );
        System.out.println("Cart items from order with id " + orderCreatedEvent.orderId() + " removed successfully");
    }
}
