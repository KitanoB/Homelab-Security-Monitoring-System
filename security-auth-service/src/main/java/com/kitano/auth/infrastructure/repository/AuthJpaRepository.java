package com.kitano.auth.infrastructure.repository;

import com.kitano.auth.infrastructure.model.HomelabUserCreateDTO;
import com.kitano.auth.infrastructure.model.HomelabUserDTO;
import com.kitano.core.model.HomeLabUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthJpaRepository extends JpaRepository<HomeLabUser, String> {
    HomeLabUser findByUsernameAndEmail(String username, String email);

    HomeLabUser findByEmail(String email);

    HomelabUserCreateDTO save(HomelabUserCreateDTO user);
}