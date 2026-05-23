package com.asheef.resumeAnalyzer.client;

import com.asheef.resumeAnalyzer.dto.UserDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(@Qualifier("userServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public UserDto getUserById(Integer userId) {

        return webClient.get()
                .uri("/internal/users/{id}", userId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
    }

}
