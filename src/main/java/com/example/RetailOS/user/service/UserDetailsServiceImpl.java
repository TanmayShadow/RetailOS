package com.example.RetailOS.user.service;

import com.example.RetailOS.user.entity.User;
import com.example.RetailOS.user.repository.UserRepository;
import com.example.RetailOS.platform.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username){
        User user = userRepository.findByEmail(username)
                .orElseThrow(()->new UsernameNotFoundException(
                        "User not found with email: " + username));
        return UserPrincipal.from(user);
    }
}
