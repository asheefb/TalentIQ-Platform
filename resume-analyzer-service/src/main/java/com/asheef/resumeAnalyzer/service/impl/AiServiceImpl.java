package com.asheef.resumeAnalyzer.service.impl;

import com.asheef.resumeAnalyzer.client.OpenAiClient;
import com.asheef.resumeAnalyzer.client.UserServiceClient;
import com.asheef.resumeAnalyzer.dto.AiSummaryResponse;
import com.asheef.resumeAnalyzer.dto.OpenAiRequest;
import com.asheef.resumeAnalyzer.dto.UserDto;
import com.asheef.resumeAnalyzer.service.AiService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiServiceImpl implements AiService {

    private final UserServiceClient userServiceClient;

    private final OpenAiClient openAiClient;


    public AiServiceImpl(UserServiceClient userServiceClient, OpenAiClient openAiClient) {
        this.userServiceClient = userServiceClient;
        this.openAiClient = openAiClient;
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

        OpenAiRequest request = new OpenAiRequest(
                "gpt-3.5-turbo",
                List.of(new OpenAiRequest.Message("user", prompt))
        );

        String response = openAiClient.summarize(request);

        return new AiSummaryResponse(response);
    }
}
