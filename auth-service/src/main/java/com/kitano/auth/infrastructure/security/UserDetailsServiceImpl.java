package com.kitano.auth.infrastructure.security;

import com.kitano.auth.infrastructure.repository.AuthUserJpaRepository;
import com.kitano.core.model.HomeLabUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final AuthUserJpaRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(AuthUserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username (username in our case).
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HomeLabUser user = userRepository.findByUsername(username);
        if (user == null) {
            LOGGER.warn("User not found with username: {}", user);
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }

        return UserDetailsImpl.build(user);
    }
}