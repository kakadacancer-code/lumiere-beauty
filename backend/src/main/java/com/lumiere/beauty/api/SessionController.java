package com.lumiere.beauty.api;

import com.lumiere.beauty.api.Dto.*;
import com.lumiere.beauty.model.UserAccount;
import com.lumiere.beauty.repo.UserAccountRepository;
import com.lumiere.beauty.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;
@RestController @RequestMapping("/api/auth")
public class SessionController {
    private final UserAccountRepository users;
    private final TokenService tokens;
    private final PasswordEncoder encoder;
    public SessionController(UserAccountRepository users, TokenService tokens,PasswordEncoder encoder) {
        this.users=users;this.tokens=tokens;this.encoder=encoder;
    }
    @PostMapping("/register") public Session register(@Valid @RequestBody Register input) {
        String email=input.email().trim().toLowerCase(Locale.ROOT);
        if(users.findByEmailIgnoreCase(email).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");
        UserAccount user=new UserAccount();user.name=input.name().trim();user.email=email;
        user.passwordHash=encoder.encode(input.password());user.role="Customer";user.status="Active";
        user=users.save(user);
        return new Session(tokens.issue(user),AccountView.of(user));
    }
    @PostMapping("/login") public Session login(@Valid @RequestBody Login input) {
        UserAccount user=users.findByEmailIgnoreCase(input.email().trim())
            .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Incorrect email or password"));
        if(!encoder.matches(input.password(),user.passwordHash)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Incorrect email or password");
        if(!"Active".equals(user.status)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"This account is suspended");
        return new Session(tokens.issue(user),AccountView.of(user));
    }
    @GetMapping("/me") public AccountView me(org.springframework.security.core.Authentication authentication) {
        return AccountView.of((UserAccount) authentication.getPrincipal());
    }
    @PostMapping("/logout") public void logout(@RequestHeader(value="Authorization",required=false) String header) {
        if(header!=null && header.startsWith("Bearer ")) tokens.revoke(header.substring(7));
    }
}
