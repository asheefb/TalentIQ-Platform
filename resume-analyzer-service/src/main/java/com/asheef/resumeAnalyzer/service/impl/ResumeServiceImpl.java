package com.asheef.resumeAnalyzer.service.impl;

import com.asheef.resumeAnalyzer.client.GeminiEmbeddingClient;
import com.asheef.resumeAnalyzer.dto.ResumeUploadRequest;
import com.asheef.resumeAnalyzer.entity.ResumeChunk;
import com.asheef.resumeAnalyzer.repository.ResumeChunkRepository;
import com.asheef.resumeAnalyzer.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
        List<String> chunks = chunkText(request.getResumeContent());

        chunks.forEach(chunk -> {

            String embedding = geminiEmbeddingClient.generateEmbedding(chunk);

            ResumeChunk resumeChunk = new ResumeChunk();
            resumeChunk.setUserId(request.getUserId());
            resumeChunk.setContent(chunk);
            resumeChunk.setEmbedding(embedding);
            ResumeChunk saved = resumeChunkRepository.save(resumeChunk);
            log.info("Saved resume chunk: {}", saved);

            log.info("Saved id: {}", saved.getId());

        });
    }

    private List<String> chunkText(String text) {
        int chunkSize = 500;

        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(
                    text.substring(
                            i,
                            Math.min(i + chunkSize, text.length())
                    )
            );
        }
        return chunks;
    }


}
