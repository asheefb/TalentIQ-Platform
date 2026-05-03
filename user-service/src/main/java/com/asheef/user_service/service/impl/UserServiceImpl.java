package com.asheef.user_service.service.impl;

import com.asheef.user_service.constants.Constant;
import com.asheef.user_service.enums.Role;
import com.asheef.user_service.mapper.UserMapper;
import com.asheef.user_service.model.dto.UpdateUserDto;
import com.asheef.user_service.model.dto.UserRequestDto;
import com.asheef.user_service.model.dto.UsersDto;
import com.asheef.user_service.entity.User;
import com.asheef.user_service.model.response.UserResponseDto;
import com.asheef.user_service.repository.UserRepository;
import com.asheef.user_service.repository.specifications.UserSpecification;
import com.asheef.user_service.service.AsyncService;
import com.asheef.user_service.service.UserService;
import com.asheef.user_service.util.ResponseDto;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;


@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final AsyncService asyncService;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           AsyncService asyncService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.asyncService = asyncService;
    }

    public boolean isUserExistWithEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean isUserExistWithMobile(String mobile) {
        return userRepository.findByMobile(mobile).isPresent();
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public ResponseEntity<ResponseDto> createUser(UserRequestDto dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.info("Reject createUser — email already exists: {}", dto.getEmail());
            return ResponseEntity.badRequest().body(
                    new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(),
                            Constant.USER_ALREADY_EXISTS + " with email " + dto.getEmail())
            );
        }

        if (userRepository.existsByMobile(dto.getMobile())) {
            log.info("Reject createUser — mobile already exists: {}", dto.getMobile());
            return ResponseEntity.badRequest().body(
                    new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(),
                            Constant.USER_ALREADY_EXISTS + " with mobile " + dto.getMobile())
            );
        }

        User user = new User();

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setAddress(dto.getAddress());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);
        log.info("User created successfully with id {}", user.getId());
        asyncService.sendWelcomeEmail(dto.getEmail());
        return ResponseEntity.ok(
                new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), Constant.USER_ADDED_SUCCESS)
        );
    }

    @Override
    @Cacheable(
            value = "users",
            key = "T(java.util.Objects).hash(#request.pageNo, #request.pageSize, #request.isActive, #request.search, #request.fieldName, #request.sortBy, #request.direction)"
    )
    public ResponseEntity<ResponseDto> getUsers(UsersDto request) {
        log.info("DB hit -> getUsers(pageNo={}, pageSize={}, isActive={}, search={}, field={}, sortBy={}, dir={})",
                request.getPageNo(), request.getPageSize(), request.getIsActive(),
                request.getSearch(), request.getFieldName(),
                request.safeSortBy(), request.getDirection());

        log.info("🔥 Fetching users from DATABASE");

        Specification<User> spec = Specification
                .where(UserSpecification.isActive(request.getIsActive()))
                .and(UserSpecification.search(request.getSearch(), request.getFieldName()));

        Sort sort = "desc" .equalsIgnoreCase(request.getDirection())
                ? Sort.by(request.safeSortBy()).descending()
                : Sort.by(request.safeSortBy()).ascending();

        Pageable pageable = PageRequest.of(request.getPageNo(), request.getPageSize(), sort);
        Page<User> users = userRepository.findAll(spec, pageable);
        Page<UserResponseDto> mapped = users.map(userMapper::toDto);

        return ResponseEntity.ok(
                new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), mapped, Constant.SUCCESS)
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "user", key = "#userId")
    })
    public ResponseEntity<ResponseDto> changeUserStatus(Integer userId, Boolean isActive) {

        if (isActive == null) {
            return ResponseEntity.badRequest().body(
                    new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(), "Status cannot be null")
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(Constant.USER_NOT_FOUND));

        if (!Objects.equals(user.getIsActive(), isActive)) {
            user.setIsActive(isActive);
            userRepository.save(user);
            String msg = isActive ? Constant.ACTIVATED : Constant.DEACTIVATED;
            log.info("User id={} status -> {}", userId, msg);
            return ResponseEntity.ok(
                    new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), msg + " " + Constant.SUCCESS + "fully")
            );
        }

        return ResponseEntity.ok(
                new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), Constant.SUCCESS)
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "user", key = "#userId")
    })
    public ResponseEntity<ResponseDto> updateUser(Integer userId, UpdateUserDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException((Constant.USER_NOT_FOUND)));

        if (dto.getName() != null && !Objects.equals(user.getName(), dto.getName())) {
            user.setName(dto.getName());
        }

        if (dto.getMobile() != null && !Objects.equals(user.getMobile(), dto.getMobile())) {
            if (userRepository.existsByMobile(dto.getMobile())) {
                return ResponseEntity.badRequest().body(
                        new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(),
                                Constant.USER_ALREADY_EXISTS + " with mobile " + dto.getMobile())
                );
            }
            user.setMobile(dto.getMobile());
        }

        if (!Objects.equals(user.getAddress(), dto.getAddress())) {
            user.setAddress(dto.getAddress());
        }

        userRepository.save(user);

        return ResponseEntity.ok().body(
                new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), Constant.SUCCESS)
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "user", key = "#id")
    })
    public ResponseEntity<ResponseDto> deleteUser(Integer id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(Constant.USER_NOT_FOUND));

        userRepository.delete(user);

        return ResponseEntity.ok().body(
                new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), Constant.SUCCESS)
        );
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
