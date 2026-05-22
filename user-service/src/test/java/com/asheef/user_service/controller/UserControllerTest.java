package com.asheef.user_service.controller;

import com.asheef.user_service.exception.GlobalExceptionHandler;
import com.asheef.user_service.model.dto.UpdateUserDto;
import com.asheef.user_service.model.dto.UserRequestDto;
import com.asheef.user_service.model.dto.UsersDto;
import com.asheef.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
})
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserController userController;

    @Test
    void createUser() throws Exception {
        UserRequestDto dto = new UserRequestDto("asheef", "a@gmail.com", "9876543210", "abcd", "password123");

        ResponseDto responseDto = new ResponseDto(true, 201, "User created");

        when(userService.createUser(any(UserRequestDto.class)))
                .thenReturn(new ResponseEntity<>(responseDto, HttpStatus.CREATED));

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getUsers() throws Exception {
        UsersDto dto = new UsersDto();
        dto.setPageNo(0);
        dto.setPageSize(10);
        dto.setSortBy("id");
        dto.setDirection("asc");
        dto.setIsActive(true);
        dto.setSearch("test");
        dto.setFieldName("name");

        Map<String, Object> data = new HashMap<>();
        data.put("users", new ArrayList<>());
        data.put("totalPages", 1);
        data.put("totalItems", 1);
        data.put("currentPage", 1);
        data.put("sortBy", "id");
        data.put("direction", "asc");
        data.put("search", "test");
        data.put("fieldName", "name");
        data.put("isActive", true);

        ResponseDto responseDto = new ResponseDto(
                true,
                HttpStatus.OK.value(),
                data,
                "Users retrieved successfully"
        );

        when(userService.getUsers(any(UsersDto.class)))
                .thenReturn(new ResponseEntity<>(responseDto, HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.get("/users")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc")
                        .param("isActive", "true")
                        .param("search", "test")
                        .param("fieldName", "name"))
                .andExpect(status().isOk());
    }

    @Test
    void changeUserStatus() throws Exception {
        Integer userId = 1;
        Boolean isActive = true;

        ResponseDto responseDto = new ResponseDto(
                true,
                200,
                "User status updated successfully"
        );

        when(userService.changeUserStatus(userId, isActive))
                .thenReturn(new ResponseEntity<>(responseDto, HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{userId}/status", userId)
                        .param("isActive", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser() throws Exception {
        Integer userId = 1;
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("Updated Name");
        dto.setMobile("9876543210");
        dto.setAddress("Updated Address");

        ResponseDto responseDto = new ResponseDto(
                true,
                200,
                "User updated successfully"
        );

        when(userService.updateUser(userId, dto))
                .thenReturn(new ResponseEntity<>(responseDto, HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser() throws Exception {
        Integer userId = 1;

        ResponseDto responseDto = new ResponseDto(
                true,
                200,
                "User deleted successfully"
        );

        when(userService.deleteUser(userId))
                .thenReturn(new ResponseEntity<>(responseDto, HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/{id}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_ValidationError_BlankName() throws Exception {
        UserRequestDto dto = new UserRequestDto();
        dto.setName("");
        dto.setEmail("test@example.com");
        dto.setMobile("9876543210");
        dto.setPassword("password123");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_ValidationError_InvalidEmail() throws Exception {
        UserRequestDto dto = new UserRequestDto();
        dto.setName("Test User");
        dto.setEmail("invalid-email");
        dto.setMobile("9876543210");
        dto.setPassword("password123");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_ValidationError_InvalidMobile() throws Exception {
        UserRequestDto dto = new UserRequestDto();
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setMobile("1234567890");
        dto.setPassword("password123");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_ValidationError_ShortPassword() throws Exception {
        UserRequestDto dto = new UserRequestDto();
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setMobile("9876543210");
        dto.setPassword("123");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_ValidationError_BlankName() throws Exception {
        Integer userId = 1;
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("");
        dto.setMobile("9876543210");
        dto.setAddress("Test Address");

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_ValidationError_InvalidMobile() throws Exception {
        Integer userId = 1;
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("Test User");
        dto.setMobile("1234567890");
        dto.setAddress("Test Address");

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
