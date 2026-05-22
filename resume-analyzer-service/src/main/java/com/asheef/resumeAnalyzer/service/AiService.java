package com.asheef.resumeAnalyzer.service;

import org.jspecify.annotations.Nullable;

public interface AiService {
    @Nullable String summarizeUser(Long id);
}
