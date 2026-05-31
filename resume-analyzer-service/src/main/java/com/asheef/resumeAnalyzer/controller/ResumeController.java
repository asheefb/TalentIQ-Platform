package com.asheef.resumeAnalyzer.controller;

import com.asheef.common.utils.ResponseDto;
import com.asheef.resumeAnalyzer.dto.ResumeUploadRequest;
import com.asheef.resumeAnalyzer.service.ResumeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeService resumeService;

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    public ResumeController(ResumeService resumeService, @Qualifier("geminiWebClient") WebClient webClient) {
        this.resumeService = resumeService;
        this.webClient = webClient;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> uploadResume(@ModelAttribute ResumeUploadRequest resumeUploadRequest) {
        resumeService.processResume(resumeUploadRequest);

        return ResponseEntity.ok(new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), "Resume uploaded successfully"));
    }

    @GetMapping("/test-models")
    public String testModels() {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
