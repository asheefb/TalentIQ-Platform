package com.asheef.resumeAnalyzer.dto;

import com.asheef.resumeAnalyzer.entity.ResumeChunk;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChunkSimilarity {

    private ResumeChunk chunk;
    private Double score;
}
