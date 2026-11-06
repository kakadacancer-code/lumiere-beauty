package com.lumiere.beauty.security;

import com.lumiere.beauty.model.UserAccount;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
@Component
public class TokenFilter extends OncePerRequestFilter {
    private final TokenService tokens;
    public TokenFilter(TokenService tokens) { this.tokens=tokens; }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String authorization=req.getHeader("Authorization");
        if(authorization!=null && authorization.startsWith("Bearer ")) {
            String token=authorization.substring(7);
            tokens.findAccount(token).ifPresent(user -> {
                UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(
                    user,null,List.of(new SimpleGrantedAuthority("ROLE_"+user.role.toUpperCase())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        chain.doFilter(req,res);
    }
}
