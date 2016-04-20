/*
 * Copyright 2015 fnoorala.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.inria.wimmics.smilk.learntorank;

import approxmatch.ApproximateMatcher;
import ciir.umass.edu.eval.Evaluator;
import ciir.umass.edu.learning.RANKER_TYPE;
import fr.inria.wimmics.smilk.annotation.datasets.SmilkNIF;
import static fr.inria.wimmics.smilk.annotation.datasets.SmilkNIF.*;
import fr.inria.wimmics.smilk.annotation.wikipedia.AnnotatedSpot;
import fr.inria.wimmics.smilk.eval.AssessmentRecord;
import fr.inria.wimmics.smilk.localsimilarity.LevenshteinDistance;
import fr.inria.wimmics.smilk.localsimilarity.MatchLikelyhoodSimilarity;
import fr.inria.wimmics.smilk.localsimilarity.Rricesimilarity;
import fr.inria.wimmics.smilk.localsimilarity.SimilarityFactory;
import fr.inria.wimmics.smilk.localsimilarity.Simmetrics;
import fr.inria.wimmics.smilk.spotter.CandidateGen;
import it.cnr.isti.hpc.EntityScorer;
import it.cnr.isti.hpc.LREntityScorer;
import it.cnr.isti.hpc.Word2VecCompress;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.label.IdHelper;
import it.cnr.isti.hpc.dexter.label.IdHelperFactory;
import it.cnr.isti.hpc.dexter.lucene.LuceneHelper;
import it.cnr.isti.hpc.dexter.relatedness.RelatednessFactory;
import it.cnr.isti.hpc.dexter.spot.ContextExtractor;
import it.cnr.isti.hpc.dexter.spot.Spot;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.dexter.spot.repo.SpotRepository;
import it.cnr.isti.hpc.dexter.spot.repo.SpotRepositoryFactory;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.text.MyPair;
import it.cnr.isti.hpc.text.Token;
import it.unimi.dsi.fastutil.io.BinIO;
import java.awt.PageAttributes;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.Lang;

/**
 *
 * @author fnoorala
 */
public class RankLibLearningLabelingCLI {

    /**
     * @param args the command line arguments
     *
     *
     */
    //static DexterParams params = DexterParams.getInstance();
    //private static final LuceneHelper lucene = LuceneHelper.getDexterLuceneHelper();
   // private static IdHelper helper = IdHelperFactory.getStdIdHelper();

    private static SpotRepository spotRepo;

    private static SpotManager sm;
    private static ApproximateMatcher apm;

    static Word2VecCompress word_model;
    static Word2VecCompress entity_model;
    static EntityScorer scorer;
    static String uri;
    static CandidateGen candidateGen;
    static int window;
    static Map<Map<String, MyPair>, Double> visited;
    static RelatednessFactory globalrelatedness;
    static Simmetrics simmetrics;
    static Rricesimilarity rricesimilarity;
    static ContextExtractor context;

    public static void main(String[] args) {
       
            
//        PrintWriter writer = null;
//        try {
            String inputFile = "/user/fnoorala/home/NetBeansProjects/dexter/en.data/KORE50_AIDA/kore50-nif-2014-11-03.ttl";
            String modeldir="/user/fnoorala/home/NetBeansProjects/dexter/en.data/OKE/ML/";
            String modelfile="train";
            int kcv=10;
            float tvs= 0.2f;
         //   FeatureExtractor featureExtractor=new FeatureExtractor();
          //  List<String> datafile= featureExtractor.enrichDatasetML(inputFile, "KORE");
            
            
            // save the features
            String featureFile="/user/fnoorala/home/NetBeansProjects/dexter/en.data/OKE/ML/OKE.train.dataset.txt";
            
//            writer = new PrintWriter(featureFile, "UTF-8");
//            
//            for(String line:datafile){
//                writer.println(line);
//            }
//            writer.close();
            
            //train model
            Evaluator e = new Evaluator(RANKER_TYPE.LAMBDAMART, "NDCG@1", "ERR@1");
            e.evaluate(featureFile, "", kcv, tvs, modeldir, modelfile);
            
            
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(RankLibLearningLabelingCLI.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (UnsupportedEncodingException ex) {
//            Logger.getLogger(RankLibLearningLabelingCLI.class.getName()).log(Level.SEVERE, null, ex);
//        } 
            
            
              

    }
  
    
  
}
