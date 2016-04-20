/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.smilk.localsimilarity;

import net.ricecode.similarity.*;


/**
 *
 * @author fnoorala
 */
public class Rricesimilarity {

    public double jaroWinkler(String str1, String str2) {
        SimilarityStrategy strategy = new JaroWinklerStrategy();

        StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
        double score = service.score(str1, str2); // Score is 0.90

        return score;
    }

    public double jaro(String str1, String str2) {
        SimilarityStrategy strategy = new JaroStrategy();

        StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
        double score = service.score(str1, str2); // Score is 0.90

        return score;
    }

    public double diceCoefficient(String str1, String str2) {
        SimilarityStrategy strategy = new DiceCoefficientStrategy();

        StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
        double score = service.score(str1, str2); // Score is 0.90

        return score;
    }

    public double levenshteinDistance(String str1, String str2) {
        SimilarityStrategy strategy = new LevenshteinDistanceStrategy();

        StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
        double score = service.score(str1, str2); // Score is 0.90

        return score;
    }
}
