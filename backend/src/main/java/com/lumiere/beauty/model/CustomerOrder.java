package com.lumiere.beauty.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name="customer_orders")
public class CustomerOrder {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false,fetch=FetchType.EAGER) public UserAccount account;
    @Column(nullable=false) public Instant createdAt=Instant.now();
    @Column(nullable=false) public String status="Placed";
    @Column(nullable=false) public String name;
    @Column(nullable=false) public String email;
    @Column(nullable=false) public String phone;
    @Column(nullable=false) public String address;
    @Column(nullable=false) public String city;
    @Column(nullable=false) public String paymentMethod;
    @Column(nullable=false,precision=12,scale=2) public BigDecimal total=BigDecimal.ZERO;
    @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER)
    @OrderBy("id ASC") public List<OrderItem> items=new ArrayList<>();
    public CustomerOrder() {}
    public void addItem(OrderItem item) { item.order=this; items.add(item); }
}
