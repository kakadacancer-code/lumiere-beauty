package com.lumiere.beauty.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) public CustomerOrder order;
    @Column(nullable=false) public String productId;
    @Column(nullable=false) public String productName;
    @Column(nullable=false) public int quantity;
    @Column(nullable=false,precision=12,scale=2) public BigDecimal unitPrice;
    @Column(nullable=false,precision=12,scale=2) public BigDecimal lineTotal;
    public OrderItem() {}
}
