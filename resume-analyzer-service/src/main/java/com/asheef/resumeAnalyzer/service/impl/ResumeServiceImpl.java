package com.asheef.resumeAnalyzer.service.impl;

import com.asheef.resumeAnalyzer.client.GeminiEmbeddingClient;
import com.asheef.resumeAnalyzer.dto.ResumeUploadRequest;
import com.asheef.resumeAnalyzer.dto.response.EmbeddingResponse;
import com.asheef.resumeAnalyzer.entity.ResumeChunk;
import com.asheef.resumeAnalyzer.helper.Parser;
import com.asheef.resumeAnalyzer.repository.ResumeChunkRepository;
import com.asheef.resumeAnalyzer.service.ResumeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeServiceImpl implements ResumeService {

    private final ResumeChunkRepository resumeChunkRepository;

    private final GeminiEmbeddingClient geminiEmbeddingClient;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ResumeServiceImpl(ResumeChunkRepository resumeChunkRepository, GeminiEmbeddingClient geminiEmbeddingClient) {
        this.resumeChunkRepository = resumeChunkRepository;

        this.geminiEmbeddingClient = geminiEmbeddingClient;
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
