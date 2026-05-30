package com.asheef.resumeAnalyzer.client;

import com.asheef.resumeAnalyzer.constants.Constant;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
public class HuggingFaceClient {

    @Value("${huggingface.api.key}")
    private String apiKey;

    private final WebClient webClient;

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceClient.class);

    public HuggingFaceClient(@Qualifier("huggingFaceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public String summarize(String prompt) {
        try {
            String response = webClient.post()
                    .uri("/microsoft/Phi-3-mini-4k-instruct")
                    .header(HttpHeaders.AUTHORIZATION,
                            Constant.BEARER_PREFIX + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("inputs", prompt))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("HuggingFace response: {}", response);

            return response;
        } catch (WebClientResponseException e) {

            log.error("Error from HuggingFace: {}", e.getResponseBodyAsString());
            return e.getResponseBodyAsString();
        }

//        return webClient.post()
//                .uri("/microsoft/Phi-3-mini-4k-instruct")
//                .header(
//                        HttpHeaders.AUTHORIZATION,
//                        "Bearer " + apiKey
//                )
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(Map.of("inputs", prompt))
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
    }
}
