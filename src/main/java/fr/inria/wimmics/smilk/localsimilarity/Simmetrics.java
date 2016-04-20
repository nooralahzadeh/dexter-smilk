/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.smilk.localsimilarity;

/**
 *
 * @author fnoorala
 */
import uk.ac.shef.wit.simmetrics.similaritymetrics.*;

public class Simmetrics {

    private AbstractStringMetric metric;

    public Simmetrics() {
        metric = new ChapmanLengthDeviation();
    }

    public double ChapmanLengthDeviation(String str1, String str2) {
        double result = metric.getSimilarity(str1, str2);
        return result;
    }

    public double ChapmanMatchingSoundex(String str1, String str2) {
        metric = new ChapmanMatchingSoundex();
        double result = metric.getSimilarity(str1, str2);
        return result;
    }

    public double ChapmanMeanLength(String str1, String str2) {
        metric = new ChapmanMeanLength();
        double result = metric.getSimilarity(str1, str2);
        return result;
    }

    public double EuclideanDistance(String str1, String str2) {
        metric = new EuclideanDistance();
        double result = metric.getSimilarity(str1, str2);
        return result;
    }

    public double Levenshtein(String str1, String str2) {
        metric = new Levenshtein();
        double result = metric.getSimilarity(str1, str2);
        return result;
    }

    public double MongeElkan(String str1, String str2) {
        metric = new MongeElkan();
        double result = metric.getSimilarity(str1, str2);
        return result;
    }

    public double NeedlemanWunch(String str1, String str2) {
        metric = new NeedlemanWunch();
        double result = metric.getSimilarity(str1, str2);
        return result;
    }

    public double QGramsDistance(String str1, String str2) {
        metric = new QGramsDistance();
        double result = metric.getSimilarity(str1, str2);
        return result;
    }

    public double SmithWatermanGotoh(String str1, String str2) {
        metric = new SmithWatermanGotoh();
        double result = metric.getSimilarity(str1, str2);
        return result;
    }

    public double SmithWatermanGotohWindowedAffine(String str1, String str2) {
        metric = new SmithWatermanGotohWindowedAffine();
        double result = metric.getSimilarity(str1, str2);
        return result;
    }

    public double SmithWaterman(String str1, String str2) {
        metric = new SmithWaterman();
        double result = metric.getSimilarity(str1, str2);
        return result;
    }

    public double Soundex(String str1, String str2) {
        metric = new Soundex();
        double result = metric.getSimilarity(str1, str2);
        return result;
    }

    public double getMatchLikelyhood(final String str1, final String str2) {
        double avg = 0F, result = 0F;
        metric = new SmithWaterman();
        result = metric.getSimilarity(str1, str2);
        avg += result;
        metric = new SmithWatermanGotoh();
        result = metric.getSimilarity(str1, str2);
        avg += result;
        metric = new SmithWatermanGotohWindowedAffine();
        result = metric.getSimilarity(str1, str2);
        avg += result;
        metric = new MongeElkan();
        result = metric.getSimilarity(str1, str2);
        avg += result;
        return (avg / 4.0F) ;
    }
}
