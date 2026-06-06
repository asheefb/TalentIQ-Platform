package com.asheef.resumeAnalyzer.util;

import java.util.List;

public class SimilarityUtil {

    public static double cosineSimilarity(List<Double> questionValues, List<Double> chunkVector) {

        double dotProduct = 0.0;
        double questionLength = 0.0;
        double chunkLength = 0.0;

        for (int i = 0; i < questionValues.size(); i++) {
            dotProduct += questionValues.get(i) * chunkVector.get(i);
            questionLength += Math.pow(questionValues.get(i), 2);
            chunkLength += Math.pow(chunkVector.get(i), 2);
        }

        return dotProduct / (Math.sqrt(questionLength) * Math.sqrt(chunkLength));
    }
}
