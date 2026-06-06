package com.asheef.resumeAnalyzer.service;

import com.asheef.resumeAnalyzer.dto.AskQuestionRequest;
import com.asheef.resumeAnalyzer.dto.ResumeUploadRequest;
import org.json.JSONException;

public interface ResumeService {
    void processResume(ResumeUploadRequest resumeUploadRequest);

    String askQuestion(AskQuestionRequest request) throws JSONException;
}
