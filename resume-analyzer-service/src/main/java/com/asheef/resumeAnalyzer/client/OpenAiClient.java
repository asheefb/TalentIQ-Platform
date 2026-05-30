package com.asheef.resumeAnalyzer.client;

import com.asheef.resumeAnalyzer.constants.Constant;
import com.asheef.resumeAnalyzer.dto.OpenAiRequest;
import com.asheef.resumeAnalyzer.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@Component
public class OpenAiClient {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String apiKey;

    public OpenAiClient(@Qualifier("openAiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public String summarize(OpenAiRequest request) {

        try {

            String response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();


            return response;

        } catch (WebClientResponseException e) {

            if (e.getStatusCode().value() == 429) {
                throw new AiServiceException(Constant.RATE_LIMIT_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS);
            }

            throw new AiServiceException("Error from OpenAI: " + e.getResponseBodyAsString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
