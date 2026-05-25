package com.asheef.resumeAnalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmbeddingRequest {

    private String input;
    private String model;
}
