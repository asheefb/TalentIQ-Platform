package com.asheef.resumeAnalyzer.dto;

import lombok.Data;

@Data
public class AskQuestionRequest {

    private Integer userId;

    private String question;
}
