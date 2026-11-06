package com.lumiere.beauty.repo;

import com.lumiere.beauty.model.Product;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface ProductRepository extends JpaRepository<Product,String> {
    List<Product> findByDeletedFalseOrderByIdAsc();
    List<Product> findByDeletedFalseAndStatusOrderByIdAsc(String status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id and p.deleted = false")
    Optional<Product> lockActiveProduct(String id);
}
