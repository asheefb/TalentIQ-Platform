package com.asheef.resumeAnalyzer.service.impl;

import com.asheef.resumeAnalyzer.client.GroqClient;
import com.asheef.resumeAnalyzer.client.HuggingFaceClient;
import com.asheef.resumeAnalyzer.client.OpenAiClient;
import com.asheef.resumeAnalyzer.client.UserServiceClient;
import com.asheef.resumeAnalyzer.dto.AskQuestionRequest;
import com.asheef.resumeAnalyzer.dto.response.AiSummaryResponse;
import com.asheef.resumeAnalyzer.dto.OpenAiRequest;
import com.asheef.resumeAnalyzer.dto.UserDto;
import com.asheef.resumeAnalyzer.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);
    private final UserServiceClient userServiceClient;

    private final OpenAiClient openAiClient;

    private final HuggingFaceClient huggingFaceClient;

    private final GroqClient groqClient;




    public AiServiceImpl(UserServiceClient userServiceClient, OpenAiClient openAiClient, HuggingFaceClient huggingFaceClient, GroqClient groqClient) {
        this.userServiceClient = userServiceClient;
        this.openAiClient = openAiClient;
        this.huggingFaceClient = huggingFaceClient;
        this.groqClient = groqClient;
    }

    @Override
    public AiSummaryResponse summarizeUser(Integer id) {

        // 🔥 STEP 1 → GET USER FROM USER-SERVICE
        UserDto user = userServiceClient.getUserById(id);

        String prompt = """
                Summarize this user professionally.
                
                Name: %s
                Email: %s
                Role: %s
                Address: %s
                
                Give:
                1. Professional Summary
                2. Strengths
                3. Weaknesses
                4. Suggested Improvements
                """
                .formatted(
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getAddress()
                );

//        OpenAiRequest request = new OpenAiRequest(
//                "gpt-3.5-turbo",
//                List.of(new OpenAiRequest.Message("user", prompt))
//        );
//
//        String response = openAiClient.summarize(request);

//        String response = huggingFaceClient.summarize(prompt);

        OpenAiRequest request = new OpenAiRequest(
                "llama-3.1-8b-instant",
                List.of(new OpenAiRequest.Message("user", prompt))
        );

        String response = groqClient.summarize(request);

        log.info("AI summary: {}", response);

        return new AiSummaryResponse(response);
    }
}
