package com.asheef.resumeAnalyzer.client;

import com.asheef.resumeAnalyzer.constants.Constant;
import com.asheef.resumeAnalyzer.dto.EmbeddingRequest;
import com.asheef.resumeAnalyzer.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Component
public class GeminiEmbeddingClient {


    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    public GeminiEmbeddingClient(
            @Qualifier("geminiWebClient")
            WebClient webClient
    ) {
        this.webClient = webClient;
    }

    public String generateEmbedding(String text) {

        try {
            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/gemini-embedding-001:embedContent")
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(
                            Map.of(
                                    "model", "models/gemini-embedding-001",
                                    "content", Map.of(
                                            "parts",
                                            List.of(
                                                    Map.of("text", text)
                                            )
                                    )
                            )
                    )
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                throw new AiServiceException(Constant.RATE_LIMIT_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS);
            }

            throw new AiServiceException("Error from Gemini: " + e.getResponseBodyAsString(), HttpStatus.valueOf(e.getStatusCode().value()));
        }
    }

}
