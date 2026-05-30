package com.asheef.resumeAnalyzer.client;

import com.asheef.resumeAnalyzer.constants.Constant;
import com.asheef.resumeAnalyzer.dto.EmbeddingRequest;
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

/*
This one only is for chat completion
Groq supports certain chat/completion models like:
 */

@Component
public class GroqClient {

    @Value("${groq.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public GroqClient(@Qualifier("groqWebClient") WebClient webClient) {
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


            System.out.println("OPENAI RESPONSE : " + response);

            return response;

        } catch (WebClientResponseException e) {

            if (e.getStatusCode().value() == 429) {
                throw new AiServiceException(Constant.RATE_LIMIT_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS);
            }

            throw new AiServiceException("Error from OpenAI: " + e.getResponseBodyAsString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String chat(){
        return "";
    }

    public String askQuestion(){
        return "";
    }



    public String generateEmbedding(String chunk) {
        try {
            EmbeddingRequest embeddingRequest = new EmbeddingRequest(chunk,
                    "text-embedding-004"); //text-embedding-3-small

            String response = webClient.post()
                    .uri("/embeddings")
                    .header(HttpHeaders.AUTHORIZATION, Constant.BEARER_PREFIX + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(embeddingRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return response;
        } catch (WebClientResponseException e) {

            if (e.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                throw new AiServiceException(Constant.RATE_LIMIT_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS);
            }

            throw new AiServiceException("Error from Groq: " + e.getResponseBodyAsString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
