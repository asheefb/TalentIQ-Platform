package com.asheef.user_service.service.impl;

import com.asheef.user_service.constants.Constant;
import com.asheef.user_service.entity.User;
import com.asheef.user_service.mapper.UserMapper;
import com.asheef.user_service.model.dto.UpdateUserDto;
import com.asheef.user_service.model.dto.UserRequestDto;
import com.asheef.user_service.model.dto.UsersDto;
import com.asheef.user_service.model.response.UserResponseDto;
import com.asheef.user_service.repository.UserRepository;
import com.asheef.user_service.util.ResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRequestDto userRequestDto;
    private UpdateUserDto updateUserDto;
    private UsersDto usersDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setMobile("9876543210");
        testUser.setAddress("Test Address");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);

        userRequestDto = new UserRequestDto("Test User", "test@example.com", "9876543210", "Test Address", "password123");

        updateUserDto = new UpdateUserDto();
        updateUserDto.setName("Updated Name");
        updateUserDto.setMobile("9876543211");
        updateUserDto.setAddress("Updated Address");

        usersDto = new UsersDto();
        usersDto.setPageNo(0);
        usersDto.setPageSize(10);
        usersDto.setSortBy("id");
        usersDto.setDirection("asc");
        usersDto.setIsActive(true);
        usersDto.setSearch("test");
        usersDto.setFieldName("name");
    }

    @Test
    void createUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByMobile(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<ResponseDto> response = userService.createUser(userRequestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals(Constant.USER_ADDED_SUCCESS, response.getBody().getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        ResponseEntity<ResponseDto> response = userService.createUser(userRequestDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getSuccess());
        assertTrue(response.getBody().getMessage().contains(Constant.USER_ALREADY_EXISTS));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_MobileAlreadyExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByMobile(anyString())).thenReturn(Optional.of(testUser));

        ResponseEntity<ResponseDto> response = userService.createUser(userRequestDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getSuccess());
        assertTrue(response.getBody().getMessage().contains(Constant.USER_ALREADY_EXISTS));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void isUserExistWithEmail_True() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        boolean result = userService.isUserExistWithEmail("test@example.com");

        assertTrue(result);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void isUserExistWithEmail_False() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        boolean result = userService.isUserExistWithEmail("nonexistent@example.com");

        assertFalse(result);
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void isUserExistWithMobile_True() {
        when(userRepository.findByMobile(anyString())).thenReturn(Optional.of(testUser));

        boolean result = userService.isUserExistWithMobile("9876543210");

        assertTrue(result);
        verify(userRepository).findByMobile("9876543210");
    }

    @Test
    void isUserExistWithMobile_False() {
        when(userRepository.findByMobile(anyString())).thenReturn(Optional.empty());

        boolean result = userService.isUserExistWithMobile("9876543211");

        assertFalse(result);
        verify(userRepository).findByMobile("9876543211");
    }

    @Test
    void getUsers_Success() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(1);
        userResponseDto.setName("Test User");
        userResponseDto.setEmail("test@example.com");
        userResponseDto.setMobile("9876543210");
        userResponseDto.setAddress("Test Address");
        userResponseDto.setIsActive(true);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

        ResponseEntity<ResponseDto> response = userService.getUsers(usersDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(Constant.SUCCESS, response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getUsers_WithoutSearch() {
        usersDto.setSearch(null);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        UserResponseDto userResponseDto = new UserResponseDto();

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

        ResponseEntity<ResponseDto> response = userService.getUsers(usersDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void changeUserStatus_Success_Activate() {
        testUser.setIsActive(false);
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<ResponseDto> response = userService.changeUserStatus(1, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertTrue(response.getBody().getMessage().contains(Constant.ACTIVATED));
        assertTrue(response.getBody().getMessage().contains(Constant.SUCCESS));
        verify(userRepository).save(testUser);
    }

    @Test
    void changeUserStatus_Success_Deactivate() {
        testUser.setIsActive(true);
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<ResponseDto> response = userService.changeUserStatus(1, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertTrue(response.getBody().getMessage().contains(Constant.DEACTIVATED));
        assertTrue(response.getBody().getMessage().contains(Constant.SUCCESS));
        verify(userRepository).save(testUser);
    }

    @Test
    void changeUserStatus_NoChangeNeeded() {
        testUser.setIsActive(true);
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));

        ResponseEntity<ResponseDto> response = userService.changeUserStatus(1, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(Constant.SUCCESS, response.getBody().getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeUserStatus_NullStatus() {
        ResponseEntity<ResponseDto> response = userService.changeUserStatus(1, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
        assertEquals("Status cannot be null", response.getBody().getMessage());
        verify(userRepository, never()).findById(anyInt());
    }

    @Test
    void changeUserStatus_UserNotFound() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.changeUserStatus(1, true);
        });

        assertEquals(Constant.USER_NOT_FOUND, exception.getMessage());
        verify(userRepository).findById(1);
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<ResponseDto> response = userService.updateUser(1, updateUserDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(Constant.SUCCESS, response.getBody().getMessage());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_MobileAlreadyExists() {
        User existingUser = new User();
        existingUser.setId(2);
        existingUser.setMobile("9876543211");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(userRepository.findByMobile(anyString())).thenReturn(Optional.of(existingUser));

        ResponseEntity<ResponseDto> response = userService.updateUser(1, updateUserDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
        assertTrue(response.getBody().getMessage().contains(Constant.USER_ALREADY_EXISTS));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_UserNotFound() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.updateUser(1, updateUserDto);
        });

        assertEquals(Constant.USER_NOT_FOUND, exception.getMessage());
        verify(userRepository).findById(1);
    }

    @Test
    void updateUser_OnlyNameChanged() {
        updateUserDto.setMobile(testUser.getMobile());
        updateUserDto.setAddress(testUser.getAddress());

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<ResponseDto> response = userService.updateUser(1, updateUserDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_OnlyAddressChanged() {
        updateUserDto.setName(testUser.getName());
        updateUserDto.setMobile(testUser.getMobile());

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<ResponseDto> response = userService.updateUser(1, updateUserDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        ResponseEntity<ResponseDto> response = userService.deleteUser(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(Constant.SUCCESS, response.getBody().getMessage());
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_UserNotFound() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            userService.deleteUser(1);
        });

        assertEquals(Constant.USER_NOT_FOUND, exception.getMessage());
        verify(userRepository).findById(1);
        verify(userRepository, never()).delete(any(User.class));
    }
}
