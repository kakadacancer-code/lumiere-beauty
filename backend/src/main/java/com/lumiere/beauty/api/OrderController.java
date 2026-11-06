package com.lumiere.beauty.api;

import com.lumiere.beauty.api.Dto.*;
import com.lumiere.beauty.model.*;
import com.lumiere.beauty.repo.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;
@RestController @RequestMapping("/api")
public class OrderController {
    private final ProductRepository products;
    private final OrderRepository orders;
    public OrderController(ProductRepository products,OrderRepository orders){this.products=products;this.orders=orders;}
    @PostMapping("/orders") @ResponseStatus(HttpStatus.CREATED) @Transactional
    public OrderView place(@Valid @RequestBody Checkout input,Authentication a) {
        if(!Set.of("Cash on delivery","Bank transfer (demo)").contains(input.paymentMethod()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported payment method");
        UserAccount user=(UserAccount)a.getPrincipal();
        CustomerOrder order=new CustomerOrder();order.account=user;order.name=input.name().trim();order.email=input.email().trim();
        order.phone=input.phone().trim();order.address=input.address().trim();order.city=input.city().trim();
        order.paymentMethod=input.paymentMethod();
        Set<String> unique=new HashSet<>();
        // Lock products in sorted order to avoid concurrent-checkout races / deadlocks.
        List<PurchaseItem> lines=input.items().stream().sorted(Comparator.comparing(PurchaseItem::productId)).toList();
        for(PurchaseItem line:lines) {
            if(!unique.add(line.productId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Duplicate product in order");
            Product p=products.lockActiveProduct(line.productId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"A selected product is unavailable"));
            if(!"Active".equals(p.status)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,p.name+" is unavailable");
            if(p.stock<line.quantity()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Not enough stock for "+p.name);
            p.stock-=line.quantity();
            OrderItem item=new OrderItem();item.productId=p.id;item.productName=p.name;item.quantity=line.quantity();
            item.unitPrice=p.price;item.lineTotal=p.price.multiply(BigDecimal.valueOf(line.quantity()));
            order.total=order.total.add(item.lineTotal);order.addItem(item);
        }
        return OrderView.of(orders.save(order));
    }
    @GetMapping("/orders/my") @Transactional(readOnly=true)
    public List<OrderView> my(Authentication a) {
        UserAccount user=(UserAccount)a.getPrincipal();
        return orders.findByAccountIdOrderByCreatedAtDesc(user.id).stream().map(OrderView::of).toList();
    }
    @GetMapping("/admin/orders") @Transactional(readOnly=true)
    public List<OrderView> all() {return orders.findAllByOrderByCreatedAtDesc().stream().map(OrderView::of).toList();}
}
