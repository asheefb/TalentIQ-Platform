package com.asheef.resumeAnalyzer.repository;

import com.asheef.resumeAnalyzer.entity.ResumeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeChunkRepository extends JpaRepository<ResumeChunk, Integer> {

    List<ResumeChunk> findByUserId(Integer userId);
}
