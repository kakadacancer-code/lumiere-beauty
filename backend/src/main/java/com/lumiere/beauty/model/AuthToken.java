package com.lumiere.beauty.model;

import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="auth_tokens")
public class AuthToken {
    @Id @Column(length=64) public String tokenHash;
    @ManyToOne(fetch=FetchType.EAGER,optional=false) public UserAccount account;
    @Column(nullable=false) public Instant expiresAt;
    public AuthToken() {}
    public AuthToken(String hash, UserAccount user, Instant expiry) {tokenHash=hash; account=user; expiresAt=expiry;}
}
