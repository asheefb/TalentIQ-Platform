package com.asheef.resumeAnalyzer.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HuggingFaceClient {

    private final WebClient webClient;

    public HuggingFaceClient(WebClient webClient) {
        this.webClient = webClient;
    }
}
