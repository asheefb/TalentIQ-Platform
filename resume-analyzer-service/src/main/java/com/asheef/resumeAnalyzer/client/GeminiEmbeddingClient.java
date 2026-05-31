package com.asheef.resumeAnalyzer.client;

import com.asheef.resumeAnalyzer.constants.Constant;
import com.asheef.resumeAnalyzer.dto.EmbeddingRequest;
import com.asheef.resumeAnalyzer.dto.response.EmbeddingResponse;
import com.asheef.resumeAnalyzer.exception.AiServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

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

    public EmbeddingResponse generateEmbedding(String text) {

        try {
            EmbeddingRequest request = new EmbeddingRequest("gemini-embedding-001",
                    new EmbeddingRequest.Content(
                            List.of(new EmbeddingRequest.Part(text))
                    ));
            String  response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/gemini-embedding-001:embedContent")
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(response, EmbeddingResponse.class);

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                throw new AiServiceException(Constant.RATE_LIMIT_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS);
            }

            throw new AiServiceException("Error from Gemini: " + e.getResponseBodyAsString(), HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
