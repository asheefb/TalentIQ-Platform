package com.asheef.user_service.controller;

import com.asheef.user_service.constants.Constant;
import com.asheef.user_service.entity.User;
import com.asheef.user_service.enums.Role;
import com.asheef.user_service.model.dto.UserRequestDto;
import com.asheef.user_service.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

/**
 * Internal, service-to-service only endpoints (called by auth-service).
 * <p>
 * These intentionally expose the BCrypt password hash; they MUST NOT be
 * routed through the public API Gateway. Network segmentation / service mesh
 * should restrict access. Alternatively, protect with a shared internal token
 * header validated here.
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private static final Logger log = LoggerFactory.getLogger(InternalUserController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InternalUserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserCredentialView> findByEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        return ResponseEntity.ok(new UserCredentialView(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name(),
                user.getIsActive()
        ));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Void> register(@Valid @RequestBody UserRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException(Constant.USER_ALREADY_EXISTS + " with email " + dto.getEmail());
        }
        if (userRepository.existsByMobile(dto.getMobile())) {
            throw new IllegalArgumentException("User already exists with mobile " + dto.getMobile());
        }
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setAddress(dto.getAddress());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        log.info("Internal register OK email={}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileView> getUser(@PathVariable Integer id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(Constant.USER_NOT_FOUND));

        log.info("Internal getUser OK id={}", id);

        log.info("full user={}", user);

        return ResponseEntity.ok(
                new UserProfileView(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getMobile(),
                        user.getAddress(),
                        user.getRole().name()
                )
        );
    }

    /**
     * Lightweight record for internal response — no entity leakage.
     */
    public record UserCredentialView(
            Integer id,
            String email,
            String password,
            String role,
            Boolean isActive
    ) {
    }

    public record UserProfileView(
            Integer id,
            String name,
            String email,
            String mobile,
            String address,
            String role
    ) {
    }
}
