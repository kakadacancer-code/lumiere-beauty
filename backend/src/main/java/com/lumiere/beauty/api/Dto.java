package com.lumiere.beauty.api;

import com.lumiere.beauty.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Dto {
    private Dto() {}
    public record ProductInput(@NotBlank String name, @NotBlank String brand, @NotBlank String category,
        @NotNull @DecimalMin("0.00") BigDecimal price, @NotNull @Min(0) Integer stock,
        @NotBlank String status, Boolean featured, String imageUrl, String description) {}
    public record Register(@NotBlank String name, @Email @NotBlank String email,
        @NotBlank @Size(min=8,max=72) String password) {}
    public record Login(@Email @NotBlank String email, @NotBlank String password) {}
    public record AccountView(Long id, String name, String email, String role, String status) {
        public static AccountView of(UserAccount a) { return new AccountView(a.id,a.name,a.email,a.role,a.status); }
    }
    public record Session(String token, AccountView user) {}
    public record PurchaseItem(@NotBlank String productId, @Min(1) int quantity) {}
    public record Checkout(@NotBlank String name, @Email @NotBlank String email, @NotBlank String phone,
        @NotBlank String address, @NotBlank String city, @NotBlank String paymentMethod,
        @NotEmpty List<@Valid PurchaseItem> items) {}
    public record ItemView(String productId, String productName, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        public static ItemView of(OrderItem item) {
            return new ItemView(item.productId,item.productName,item.quantity,item.unitPrice,item.lineTotal);
        }
    }
    public record OrderView(Long id, Instant createdAt, String status, String name, String email,
        String phone, String address, String city, String paymentMethod, BigDecimal total,
        List<ItemView> items) {
        public static OrderView of(CustomerOrder order) {
            return new OrderView(order.id,order.createdAt,order.status,order.name,order.email,order.phone,
                order.address,order.city,order.paymentMethod,order.total,
                order.items.stream().map(ItemView::of).toList());
        }
    }
    public record AdminCreateUser(@NotBlank String name, @Email @NotBlank String email,
        @NotBlank @Size(min=8,max=72) String password, @NotBlank String role) {}
    public record AdminUpdateUser(String role, String status) {}
}
