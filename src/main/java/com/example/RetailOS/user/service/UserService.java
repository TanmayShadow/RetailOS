package com.example.RetailOS.user.service;

import com.example.RetailOS.platform.exception.BadRequestException;
import com.example.RetailOS.platform.exception.ResourceAlreadyExistsException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.platform.security.permission.Role;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import com.example.RetailOS.user.dto.request.InviteUserRequest;
import com.example.RetailOS.user.dto.request.UpdateUserRoleRequest;
import com.example.RetailOS.user.dto.response.UserResponse;
import com.example.RetailOS.user.entity.User;
import com.example.RetailOS.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public List<UserResponse> getUsers() {
        return userRepository
                .findAllByShopIdOrderByFirstNameAsc(currentUserService.getCurrentShopId())
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse inviteUser(InviteUserRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        requireShopRole(request.getRole());

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("A user with this email already exists.");
        }

        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .shopId(shopId)
                .active(true)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUserRole(UUID id, UpdateUserRoleRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        User user = requireUser(id, shopId);
        requireShopRole(request.getRole());

        if (user.getRole() == Role.OWNER && request.getRole() != Role.OWNER) {
            requireNotSoleOwner(shopId);
        }

        user.setRole(request.getRole());
        return UserResponse.from(userRepository.save(user));
    }

    /**
     * A shop can never be left with zero owners — enforced here rather
     * than trusting the frontend, since this is the only thing standing
     * between an owner and permanently locking themselves out of user
     * management.
     */
    @Transactional
    public void deleteUser(UUID id) {
        UUID shopId = currentUserService.getCurrentShopId();
        User user = requireUser(id, shopId);

        if (user.getRole() == Role.OWNER) {
            requireNotSoleOwner(shopId);
        }

        userRepository.delete(user);
    }

    private void requireNotSoleOwner(UUID shopId) {
        if (userRepository.countByShopIdAndRole(shopId, Role.OWNER) <= 1) {
            throw new BadRequestException(
                    "A shop must have at least one owner — promote another user first.");
        }
    }

    /**
     * SUPER_ADMIN is a platform-level role, not a shop role — it must
     * never be assignable through the shop's own user management.
     */
    private void requireShopRole(Role role) {
        if (role == Role.SUPER_ADMIN) {
            throw new BadRequestException("That role can't be assigned from here.");
        }
    }

    private User requireUser(UUID id, UUID shopId) {
        return userRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
