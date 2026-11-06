package com.lumiere.beauty.config;

import com.lumiere.beauty.model.*;
import com.lumiere.beauty.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.Locale;
@Configuration
public class SeedData {
    @Bean CommandLineRunner initialize(ProductRepository products,UserAccountRepository users,
        PasswordEncoder passwordEncoder,@Value("${app.admin.email:}") String adminEmail,
        @Value("${app.admin.password:}") String adminPassword) {
        return args -> {
            if(products.count()==0) {
                seedProduct(products, "P-1001", "Capture Youth Glow Booster Serum", "Dior", "Skincare", 92, 18, "Active", true, "/images/serum.svg", "Sample luxury skincare serum listing for the cosmetics shop catalogue.");
                seedProduct(products, "P-1002", "Soft Pinch Liquid Blush", "Rare Beauty", "Makeup", 25, 34, "Active", true, "/images/blush.svg", "Sample cream blush listing with a lightweight, blendable finish.");
                seedProduct(products, "P-1003", "Pro Filt’r Soft Matte Foundation", "Fenty Beauty", "Makeup", 40, 22, "Active", true, "/images/foundation.svg", "Sample foundation listing for exploring shade and stock management.");
                seedProduct(products, "P-1004", "Rouge Dior Lipstick", "Dior", "Makeup", 46, 15, "Active", false, "/images/lipstick.svg", "Sample lip colour listing in the makeup collection.");
                seedProduct(products, "P-1005", "Niacinamide 10% + Zinc 1%", "The Ordinary", "Skincare", 14, 50, "Active", false, "/images/serum.svg", "Sample active-skincare product listing.");
                seedProduct(products, "P-1006", "Hydrating Facial Cleanser", "CeraVe", "Skincare", 18, 29, "Active", false, "/images/cleanser.svg", "Sample gentle facial cleanser listing.");
                seedProduct(products, "P-1007", "Gloss Bomb Lip Gloss", "Fenty Beauty", "Makeup", 23, 0, "Draft", false, "/images/lipstick.svg", "Sample lip gloss listing, currently saved as a draft.");
                seedProduct(products, "P-1008", "Miss Dior Eau de Parfum", "Dior", "Fragrance", 115, 8, "Active", false, "/images/fragrance.svg", "Sample fragrance listing for the beauty catalogue.");
                seedProduct(products, "P-1009", "Relief Sun: Rice + Probiotics SPF50+", "Beauty of Joseon", "Skincare", 19, 41, "Active", true, "/images/sunscreen.svg", "Sample daily sunscreen listing.");
                seedProduct(products, "P-1010", "Lip Sleeping Mask", "Laneige", "Skincare", 24, 26, "Active", false, "/images/cream.svg", "Sample overnight lip-care product listing.");
            }
            // ADMIN credentials come from environment variables, never from the browser.
            if(!adminEmail.isBlank() && !adminPassword.isBlank() && adminPassword.length()>=8
                && users.findByEmailIgnoreCase(adminEmail).isEmpty()) {
                UserAccount admin=new UserAccount();admin.name="Store Administrator";
                admin.email=adminEmail.toLowerCase(Locale.ROOT).trim();admin.passwordHash=passwordEncoder.encode(adminPassword);
                admin.role="Admin";admin.status="Active";users.save(admin);
            }
        };
    }
    private void seedProduct(ProductRepository products,String id,String name,String brand,String category,
        int price,int stock,String status,boolean featured,String imageUrl,String description) {
        Product p=new Product();p.id=id;p.name=name;p.brand=brand;p.category=category;
        p.price=BigDecimal.valueOf(price);p.stock=stock;p.status=status;p.featured=featured;
        p.imageUrl=imageUrl;p.description=description;products.save(p);
    }
}
