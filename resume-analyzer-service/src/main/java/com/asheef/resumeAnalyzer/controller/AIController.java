package com.asheef.resumeAnalyzer.controller;

import com.asheef.resumeAnalyzer.dto.AskQuestionRequest;
import com.asheef.resumeAnalyzer.dto.response.AiSummaryResponse;
import com.asheef.resumeAnalyzer.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AiService aiService;

    public AIController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/summarize-user/{id}")
    public ResponseEntity<AiSummaryResponse> summarize(@PathVariable Integer id) {
        return ResponseEntity.ok(aiService.summarizeUser(id));
    }
}
