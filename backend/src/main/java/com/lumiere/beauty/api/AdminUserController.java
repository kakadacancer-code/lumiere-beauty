package com.lumiere.beauty.api;

import com.lumiere.beauty.api.Dto.*;
import com.lumiere.beauty.model.UserAccount;
import com.lumiere.beauty.repo.*;
import com.lumiere.beauty.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserAccountRepository users;
    private final OrderRepository orders;
    private final TokenService tokens;
    private final PasswordEncoder encoder;
    public AdminUserController(UserAccountRepository users,OrderRepository orders,TokenService tokens,PasswordEncoder encoder){
        this.users=users;this.orders=orders;this.tokens=tokens;this.encoder=encoder;
    }
    @GetMapping public List<AccountView> all(){return users.findAllByOrderByIdAsc().stream().map(AccountView::of).toList();}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AccountView create(@Valid @RequestBody AdminCreateUser input){
        if(!Set.of("Admin","Customer").contains(input.role())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid role");
        String email=input.email().trim().toLowerCase(Locale.ROOT);
        if(users.findByEmailIgnoreCase(email).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");
        UserAccount u=new UserAccount();u.name=input.name().trim();u.email=email;u.role=input.role();
        u.passwordHash=encoder.encode(input.password());return AccountView.of(users.save(u));
    }
    @PatchMapping("/{id}") public AccountView update(@PathVariable Long id,@RequestBody AdminUpdateUser input,Authentication a){
        UserAccount user=users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found"));
        Long self=((UserAccount)a.getPrincipal()).id;
        if(Objects.equals(id,self)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"You cannot change your own role or status");
        if(input.role()!=null) {
            if(!Set.of("Admin","Customer").contains(input.role())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid role");
            user.role=input.role();
        }
        if(input.status()!=null) {
            if(!Set.of("Active","Suspended").contains(input.status())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid status");
            user.status=input.status();
        }
        users.save(user);tokens.revokeAll(user.id);
        return AccountView.of(user);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,Authentication a){
        UserAccount user=users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found"));
        if(Objects.equals(user.id,((UserAccount)a.getPrincipal()).id)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cannot delete your own account");
        if(orders.existsByAccountId(id)) throw new ResponseStatusException(HttpStatus.CONFLICT,"This user has orders. Suspend the account instead.");
        tokens.revokeAll(id);users.delete(user);
    }
}
