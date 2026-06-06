package com.asheef.resumeAnalyzer.service.impl;

import com.asheef.resumeAnalyzer.client.GeminiEmbeddingClient;
import com.asheef.resumeAnalyzer.client.GroqClient;
import com.asheef.resumeAnalyzer.dto.AskQuestionRequest;
import com.asheef.resumeAnalyzer.dto.ChunkSimilarity;
import com.asheef.resumeAnalyzer.dto.OpenAiRequest;
import com.asheef.resumeAnalyzer.dto.ResumeUploadRequest;
import com.asheef.resumeAnalyzer.dto.response.EmbeddingResponse;
import com.asheef.resumeAnalyzer.entity.ResumeChunk;
import com.asheef.resumeAnalyzer.helper.Parser;
import com.asheef.resumeAnalyzer.repository.ResumeChunkRepository;
import com.asheef.resumeAnalyzer.service.ResumeService;
import com.asheef.resumeAnalyzer.util.SimilarityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeServiceImpl implements ResumeService {

    private final ResumeChunkRepository resumeChunkRepository;

    private final GeminiEmbeddingClient geminiEmbeddingClient;

    private final GroqClient groqClient;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper;

    public ResumeServiceImpl(ResumeChunkRepository resumeChunkRepository, GeminiEmbeddingClient geminiEmbeddingClient, GroqClient groqClient, ObjectMapper objectMapper) {
        this.resumeChunkRepository = resumeChunkRepository;

        this.geminiEmbeddingClient = geminiEmbeddingClient;
        this.groqClient = groqClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void processResume(ResumeUploadRequest request) {
        String content = extractContent(request.getFile());
        List<String> chunks = chunkText(content);

        chunks.forEach(chunk -> {

            EmbeddingResponse embedding = geminiEmbeddingClient.generateEmbedding(chunk);

            ResumeChunk resumeChunk = new ResumeChunk();
            resumeChunk.setUserId(request.getUserId());
            resumeChunk.setContent(chunk);
            ObjectMapper mapper = new ObjectMapper();

            try {
                resumeChunk.setEmbedding(mapper.writeValueAsString(embedding.getEmbedding().getValues()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            ResumeChunk saved = resumeChunkRepository.save(resumeChunk);
            log.info("Saved resume chunk: {}", saved);

            log.info("Saved id: {}", saved.getId());

        });
    }

    @Override
    public String askQuestion(AskQuestionRequest request) throws JSONException {

        EmbeddingResponse questionResponse = geminiEmbeddingClient.generateEmbedding(request.getQuestion());

        List<Double> questionValues = questionResponse.getEmbedding().getValues();

        List<ResumeChunk> chunks = resumeChunkRepository.findByUserId(request.getUserId());

        List<ChunkSimilarity> chunkSimilarities = new ArrayList<>();

        chunks.forEach(chunk -> {
            try {
                List<Double> chunkVector = objectMapper.readValue(chunk.getEmbedding(),
                        new TypeReference<List<Double>>() {
                        });

                double score = SimilarityUtil.cosineSimilarity(questionValues, chunkVector);
                chunkSimilarities.add(
                        new ChunkSimilarity(chunk, score));

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        chunkSimilarities.sort((a, b) ->
                Double.compare(b.getScore(), a.getScore())
        );

        List<ChunkSimilarity> topChunks = chunkSimilarities.stream().limit(3).toList();

        StringBuilder context = new StringBuilder();

        topChunks.forEach(chunk -> {
            context.append(chunk.getChunk().getContent()).append("\n\n");
        });

        String prompt = """
                Answer Using Only the given context:
                Context: %s
                Question: %s
                """
                .formatted(context, request.getQuestion());

        OpenAiRequest openAiRequest = new OpenAiRequest(
                "llama-3.3-70b-versatile",
                List.of(new OpenAiRequest.Message("user", prompt))
        );

        String response = groqClient.summarize(openAiRequest);


        JSONObject responseJson = new JSONObject(response);

        return responseJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

    }

    private String extractContent(MultipartFile file) {
        return new Parser().extractText(file);
    }

    private List<String> chunkText(String text) {
        int chunkSize = 500;

        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(
                            i,
                            Math.min(i + chunkSize, text.length())
                    )
            );
        }
        return chunks;
    }
}
