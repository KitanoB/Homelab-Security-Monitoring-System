package com.kitano.auth.infrastructure.repository;

import com.kitano.core.model.HomeLabUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserJpaRepository extends JpaRepository<HomeLabUser, String> {

    HomeLabUser findByUsername(String username);

    boolean existsByUsername(String username);

}