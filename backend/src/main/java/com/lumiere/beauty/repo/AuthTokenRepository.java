package com.lumiere.beauty.repo;

import com.lumiere.beauty.model.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
public interface AuthTokenRepository extends JpaRepository<AuthToken,String> {
    @Modifying @Transactional @Query("delete from AuthToken t where t.account.id = :accountId")
    int deleteByAccountId(Long accountId);
}
