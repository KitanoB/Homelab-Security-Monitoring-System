package com.kitano.auth.security;

import com.kitano.auth.infrastructure.repository.AuthJpaRepository;
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

    private final AuthJpaRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(AuthJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username (email in our case).
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        HomeLabUser user = userRepository.findByEmail(email);
        if (user == null) {
            LOGGER.warn("User not found with email: {}", email);
            throw new UsernameNotFoundException("User Not Found with email: " + email);
        }

        return UserDetailsImpl.build(user);
    }
}