package com.asheef.resumeAnalyzer.service;

import com.asheef.resumeAnalyzer.dto.AiSummaryResponse;

public interface AiService {

    AiSummaryResponse summarizeUser(Integer id);
}
