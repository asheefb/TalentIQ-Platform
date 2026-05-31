package com.asheef.resumeAnalyzer.helper;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

public class Parser {

    public String extractText(MultipartFile file) {

        try {
            String fileName = file.getOriginalFilename();

            if (fileName == null) {
                throw new RuntimeException("File name is missing");
            }

            if (fileName.toLowerCase().endsWith(".pdf")) {

                try (PDDocument document =
                             Loader.loadPDF(file.getBytes())) {

                    return new PDFTextStripper().getText(document);
                }

            } else if (fileName.toLowerCase().endsWith(".docx")) {

                try (XWPFDocument document =
                             new XWPFDocument(file.getInputStream())) {

                    return new XWPFWordExtractor(document).getText();
                }

            } else {
                throw new RuntimeException(
                        "Only PDF and DOCX files are supported");
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to extract resume text", e);
        }
    }
}
