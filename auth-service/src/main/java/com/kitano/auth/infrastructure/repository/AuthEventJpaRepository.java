package com.kitano.auth.infrastructure.repository;


import com.kitano.core.model.SystemEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthEventJpaRepository extends JpaRepository<SystemEvent, String> {
}
