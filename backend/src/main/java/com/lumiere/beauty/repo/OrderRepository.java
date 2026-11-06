package com.lumiere.beauty.repo;

import com.lumiere.beauty.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OrderRepository extends JpaRepository<CustomerOrder,Long> {
    List<CustomerOrder> findByAccountIdOrderByCreatedAtDesc(Long id);
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
    boolean existsByAccountId(Long id);
}
