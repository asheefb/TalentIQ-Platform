package com.asheef.resumeAnalyzer.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "resume_chunk")
public class ResumeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(columnDefinition = "LONGTEXT")
    private String embedding;
}
