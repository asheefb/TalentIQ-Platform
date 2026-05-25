package com.asheef.resumeAnalyzer.dto;

import lombok.Data;

@Data
public class ResumeUploadRequest {

    private Integer userId;
    private String resumeContent;
}
