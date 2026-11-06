package com.lumiere.beauty.api;

import com.lumiere.beauty.api.Dto.ProductInput;
import com.lumiere.beauty.model.Product;
import com.lumiere.beauty.repo.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api")
public class ProductController {
    private final ProductRepository repo;
    public ProductController(ProductRepository repo){this.repo=repo;}
    private boolean admin(Authentication a) {return a!=null && a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN"));}
    @GetMapping("/products") public List<Product> all(Authentication a) {
        return admin(a) ? repo.findByDeletedFalseOrderByIdAsc() : repo.findByDeletedFalseAndStatusOrderByIdAsc("Active");
    }
    @GetMapping("/products/{id}") public Product one(@PathVariable String id,Authentication a) {
        Product p=repo.findById(id).filter(x->!x.deleted).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));
        if(!admin(a) && !"Active".equals(p.status)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found");
        return p;
    }
    @GetMapping("/brands") public List<String> brands() {
        return repo.findByDeletedFalseAndStatusOrderByIdAsc("Active").stream().map(x->x.brand).distinct().sorted().toList();
    }
    @GetMapping("/categories") public List<String> categories() {
        return repo.findByDeletedFalseAndStatusOrderByIdAsc("Active").stream().map(x->x.category).distinct().sorted().toList();
    }
    @PostMapping("/products") @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody ProductInput input) {
        Product p=new Product();p.id="P-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.ROOT);
        assign(p,input);return repo.save(p);
    }
    @PutMapping("/products/{id}") public Product update(@PathVariable String id,@Valid @RequestBody ProductInput input) {
        Product p=repo.findById(id).filter(x->!x.deleted).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));
        assign(p,input);return repo.save(p);
    }
    @DeleteMapping("/products/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String id) {
        Product p=repo.findById(id).filter(x->!x.deleted).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));
        p.deleted=true;repo.save(p); // Soft delete preserves order history
    }
    private void assign(Product p, ProductInput data) {
        if(!Set.of("Active","Draft").contains(data.status())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Status must be Active or Draft");
        p.name=data.name().trim();p.brand=data.brand().trim();p.category=data.category().trim();
        p.price=data.price();p.stock=data.stock();p.status=data.status();
        p.featured=Boolean.TRUE.equals(data.featured());p.imageUrl=data.imageUrl();p.description=data.description();
    }
}
