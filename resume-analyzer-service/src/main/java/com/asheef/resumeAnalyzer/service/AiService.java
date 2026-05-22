package com.asheef.resumeAnalyzer.service;

import com.asheef.resumeAnalyzer.dto.AiSummaryResponse;
import org.jspecify.annotations.Nullable;

public interface AiService {

    AiSummaryResponse summarizeUser(Integer id);
}
