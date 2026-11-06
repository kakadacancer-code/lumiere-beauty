package com.lumiere.beauty.repo;

import com.lumiere.beauty.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface UserAccountRepository extends JpaRepository<UserAccount,Long> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    List<UserAccount> findAllByOrderByIdAsc();
}
