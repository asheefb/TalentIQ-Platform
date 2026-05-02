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
import java.util.logging.Logger;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final AsyncService asyncService;

    private static final Logger log = Logger.getLogger(UserServiceImpl.class.getName());

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, AsyncService asyncService) {
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

    @CacheEvict(value = "users", allEntries = true)
    @Override
    public ResponseEntity<ResponseDto> createUser(UserRequestDto dto) {

        if (isUserExistWithEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body(
                    new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(), Constant.USER_ALREADY_EXIST + " with email " + dto.getEmail())
            );
        }

        if (isUserExistWithMobile(dto.getMobile())) {
            return ResponseEntity.badRequest().body(
                    new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(), Constant.USER_ALREADY_EXIST + " with mobile " + dto.getMobile())
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
        log.info("User created successfully with id " + user.getId());
        asyncService.sendWelcomeEmail(dto.getEmail());
        return ResponseEntity.ok(
                new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), Constant.USER_ADDED_SUCCESS)
        );
    }

    @Cacheable(
            value = "users",
            key = "T(java.util.Objects).hash(#request.pageNo, #request.pageSize, #request.isActive, #request.search, #request.fieldName, #request.sortBy, #request.direction)"
    )
    @Override
    public ResponseEntity<ResponseDto> getUsers(UsersDto request) {

        log.info("🔥 Fetching users from DATABASE");

        Specification<User> spec = (root, query, cb) -> cb.conjunction();

        spec = spec.and(UserSpecification.isActive(request.getIsActive()));

        if (request.getSearch() != null && !request.getSearch().isEmpty())
            spec = spec.and(UserSpecification.getSearch(request.getSearch(), request.getFieldName()));

        Sort sort = request.getDirection().equalsIgnoreCase("desc")
                ? Sort.by(request.getSortBy()).descending()
                : Sort.by(request.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(request.getPageNo(), request.getPageSize(), sort);

        Page<User> users = userRepository.findAll(spec, pageable);
        Page<UserResponseDto> map = users.map(userMapper::toDto);

        return ResponseEntity.ok(
                new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), map, Constant.SUCCESS)
        );
    }

    @CacheEvict(value = "users", allEntries = true)
    @Override
    public ResponseEntity<ResponseDto> changeUserStatus(Integer userId, Boolean isActive) {

        if (isActive == null) {
            return ResponseEntity.badRequest().body(
                    new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(), "Status cannot be null")
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(Constant.USER_NOT_FOUND));
        String msg = "";
        if (user.getIsActive() != isActive) {
            msg = isActive ? Constant.ACTIVATED : Constant.DEACTIVATED;
            user.setIsActive(isActive);

            userRepository.save(user);
            return ResponseEntity.ok(
                    new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), msg + " " + Constant.SUCCESS + "fully")
            );
        }

        return ResponseEntity.ok(
                new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), Constant.SUCCESS)
        );
    }

    @CacheEvict(value = "users", allEntries = true)
    @Override
    public ResponseEntity<ResponseDto> updateUser(Integer userId, UpdateUserDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException((Constant.USER_NOT_FOUND)));

        if (!user.getName().equals(dto.getName())) {
            user.setName(dto.getName());
        }

        if (!user.getMobile().equals(dto.getMobile())) {
            if (isUserExistWithMobile(dto.getMobile())) {
                return ResponseEntity.badRequest().body(
                        new ResponseDto(Boolean.FALSE, HttpStatus.BAD_REQUEST.value(), Constant.USER_ALREADY_EXIST + " with mobile " + dto.getMobile())
                );
            }
            user.setMobile(dto.getMobile());
        }

        if (user.getAddress() == null || !user.getAddress().equals(dto.getAddress())) {
            user.setAddress(dto.getAddress());
        }

        userRepository.save(user);

        return ResponseEntity.ok().body(
                new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), Constant.SUCCESS)
        );
    }

    @CacheEvict(value = "users", allEntries = true)
    @Override
    public ResponseEntity<ResponseDto> deleteUser(Integer id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(Constant.USER_NOT_FOUND));

        userRepository.delete(user);

        return ResponseEntity.ok().body(
                new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), Constant.SUCCESS)
        );
    }
}
