package com.asheef.resumeAnalyzer.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ResumeUploadRequest {

    private Integer userId;
    private MultipartFile file;
}
