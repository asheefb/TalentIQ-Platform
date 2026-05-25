package com.asheef.resumeAnalyzer.controller;

import com.asheef.common.utils.ResponseDto;
import com.asheef.resumeAnalyzer.dto.ResumeUploadRequest;
import com.asheef.resumeAnalyzer.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ResponseDto> uploadResume(@RequestBody ResumeUploadRequest resumeUploadRequest) {
        resumeService.processResume(resumeUploadRequest);

        return ResponseEntity.ok(new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), "Resume uploaded successfully"));
    }
}
