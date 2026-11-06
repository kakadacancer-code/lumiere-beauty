package com.lumiere.beauty.model;

import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="user_accounts",uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class UserAccount {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public String name;
    @Column(nullable=false,unique=true) public String email;
    @Column(nullable=false) public String passwordHash;
    @Column(nullable=false) public String role="Customer";
    @Column(nullable=false) public String status="Active";
    @Column(nullable=false) public Instant createdAt=Instant.now();
    public UserAccount() {}
}
