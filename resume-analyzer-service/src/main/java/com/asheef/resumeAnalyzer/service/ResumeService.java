package com.asheef.resumeAnalyzer.service;

import com.asheef.resumeAnalyzer.dto.ResumeUploadRequest;

public interface ResumeService {
    void processResume(ResumeUploadRequest resumeUploadRequest);
}
