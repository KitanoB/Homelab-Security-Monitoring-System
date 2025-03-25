package com.kitano.auth.application;

import com.kitano.auth.infrastructure.model.HomelabUserCreateDTO;
import com.kitano.auth.infrastructure.model.HomelabUserDTO;
import com.kitano.auth.infrastructure.repository.AuthJpaRepository;
import com.kitano.core.model.HomeLabUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final AuthJpaRepository repository;

    public AuthService(AuthJpaRepository repository) {
        this.repository = repository;
    }

    public HomelabUserDTO login(HomelabUserDTO user) {
        LOGGER.info("Authenticating user: {}", user);

        HomeLabUser foundUser = repository.findByUsernameAndEmail(user.getUsername(), user.getEmail());

        if (foundUser == null) {
            LOGGER.error("User not found: {}", user);
            return null;
        }

        checkPassword(user, foundUser);

        return new HomelabUserDTO(foundUser);

    }

    public HomelabUserDTO signin(HomelabUserCreateDTO user) {
        LOGGER.info("Signing in user: {}", user);

        HomeLabUser foundUser = repository.findByUsernameAndEmail(user.getUsername(), user.getEmail());

        if (foundUser == null) {
            LOGGER.error("User not found: {}", user);
            return null;
        }

        checkPassword(user, foundUser);

        return new HomelabUserDTO(foundUser);
    }

    public boolean signout(HomelabUserDTO user) {
        LOGGER.info("Signing out user: {}", user);

        HomeLabUser foundUser = repository.findByUsernameAndEmail(user.getUsername(), user.getEmail());

        if (foundUser == null) {
            LOGGER.error("User not found: {}", user);
            return false;
        }

        return true;
    }

    public boolean ban(HomelabUserDTO user) {
        LOGGER.info("Banning user: {}", user);

        HomeLabUser foundUser = repository.findByUsernameAndEmail(user.getUsername(), user.getEmail());

        if (foundUser == null) {
            LOGGER.error("User not found: {}", user);
            return false;
        }

        return true;
    }

    private void checkPassword(HomelabUserDTO user, HomeLabUser foundUser) {
    }

}
