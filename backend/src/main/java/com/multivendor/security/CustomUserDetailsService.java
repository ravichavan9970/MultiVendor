package com.multivendor.security;

import com.multivendor.model.User;
import com.multivendor.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String cleanId = identifier != null ? identifier.trim() : "";
        User user = userRepository.findByEmail(cleanId)
                .or(() -> userRepository.findByPhoneNumber(cleanId))
                .or(() -> userRepository.findByPhoneNumber("+" + cleanId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or phone number: " + identifier));
        return UserPrincipal.create(user);
    }
}
