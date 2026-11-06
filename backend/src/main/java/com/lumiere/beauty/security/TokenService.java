package com.lumiere.beauty.security;

import com.lumiere.beauty.model.*;
import com.lumiere.beauty.repo.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class TokenService {
    private final AuthTokenRepository tokens;
    private final SecureRandom random = new SecureRandom();
    public TokenService(AuthTokenRepository tokens) { this.tokens=tokens; }
    public String issue(UserAccount user) {
        byte[] bytes=new byte[32]; random.nextBytes(bytes);
        String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.save(new AuthToken(hash(raw),user,Instant.now().plus(Duration.ofDays(7))));
        return raw;
    }
    public Optional<UserAccount> findAccount(String raw) {
        if(raw == null || raw.length() < 30 || raw.length()>200) return Optional.empty();
        return tokens.findById(hash(raw)).filter(t -> t.expiresAt.isAfter(Instant.now()))
                .map(t -> t.account).filter(a -> "Active".equals(a.status));
    }
    public void revoke(String raw) { if(raw!=null && raw.length()>29 && raw.length()<201) tokens.deleteById(hash(raw)); }
    public void revokeAll(Long userId) { tokens.deleteByAccountId(userId); }
    private String hash(String raw) {
        try {
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch(NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
