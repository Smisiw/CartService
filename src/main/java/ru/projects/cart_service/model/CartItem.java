package ru.projects.cart_service.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = "cart")
@EqualsAndHashCode(exclude = "cart")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long productVariationId;
    @Column(nullable = false)
    private int quantity;
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;
}
