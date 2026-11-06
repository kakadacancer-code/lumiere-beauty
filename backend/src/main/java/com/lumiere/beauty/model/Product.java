package com.lumiere.beauty.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="products")
public class Product {
    @Id @Column(length=40) public String id;
    @Column(nullable=false) public String name;
    @Column(nullable=false) public String brand;
    @Column(nullable=false) public String category;
    @Column(nullable=false,precision=12,scale=2) public BigDecimal price;
    @Column(nullable=false) public int stock;
    @Column(nullable=false) public String status;
    @Column(nullable=false) public boolean featured;
    @Column(nullable=false) public boolean deleted = false;
    @Column(length=2000) public String imageUrl;
    @Column(columnDefinition="TEXT") public String description;
    public Product() {}
}
