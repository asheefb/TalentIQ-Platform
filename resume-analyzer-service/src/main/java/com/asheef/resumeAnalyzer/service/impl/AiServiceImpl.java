package com.asheef.resumeAnalyzer.service.impl;

import com.asheef.resumeAnalyzer.service.AiService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class AiServiceImpl implements AiService {
    @Override
    public @Nullable String summarizeUser(Long id) {
        return "";
    }
}
