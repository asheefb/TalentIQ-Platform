package com.asheef.resumeAnalyzer.service;

import com.asheef.resumeAnalyzer.dto.AskQuestionRequest;
import com.asheef.resumeAnalyzer.dto.response.AiSummaryResponse;

public interface AiService {

    AiSummaryResponse summarizeUser(Integer id);

}
