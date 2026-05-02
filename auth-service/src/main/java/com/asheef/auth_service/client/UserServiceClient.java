package com.asheef.auth_service.client;

import com.asheef.auth_service.model.dto.RegisterRequest;
import com.asheef.auth_service.model.response.UserCredentialDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.NoSuchElementException;

@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final WebClient userServiceWebClient;

    public UserServiceClient(WebClient userServiceWebClient) {
        this.userServiceWebClient = userServiceWebClient;
    }

    public UserCredentialDto findByEmail(String email) {
        log.debug("Fetching credentials for email={} from user-service", email);
        try {
            return userServiceWebClient.get()
                    .uri("/internal/users/by-email/{email}", email)
                    .retrieve()
                    .bodyToMono(UserCredentialDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new NoSuchElementException("User not found");
        }
    }

    public void register(RegisterRequest request) {
        log.debug("Registering user email={} via user-service", request.getEmail());
        userServiceWebClient.post()
                .uri("/internal/users")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status == HttpStatus.BAD_REQUEST,
                        resp -> resp.bodyToMono(String.class)
                                .map(IllegalArgumentException::new))
                .toBodilessEntity()
                .block();
    }
}
