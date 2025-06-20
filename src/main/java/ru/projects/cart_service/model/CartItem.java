package ru.projects.cart_service.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString(exclude = "cart")
@EqualsAndHashCode(exclude = "cart")
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_variation_id"})
)
public class CartItem {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false, name = "product_variation_id")
    private UUID productVariationId;
    @Column(nullable = false)
    private int quantity;
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;
}
