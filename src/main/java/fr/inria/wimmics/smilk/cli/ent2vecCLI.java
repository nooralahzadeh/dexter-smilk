/**
 * Copyright 2011 Diego Ceccarelli
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.inria.wimmics.smilk.cli;

import fr.inria.wimmics.smilk.annotation.wikipedia.AnnotatedSpot;
import fr.inria.wimmics.smilk.annotation.wikipedia.DatasetCollection;
import fr.inria.wimmics.smilk.eval.AnnotatedSpotReader;
import fr.inria.wimmics.smilk.eval.AssessmentRecord;
import fr.inria.wimmics.smilk.eval.JsonAnnotatedSpotReader;
import fr.inria.wimmics.smilk.util.ListFilesUtil;
import fr.inria.wimmics.smilk.util.MyPartition;
import it.cnr.isti.hpc.EntityScorer;
import it.cnr.isti.hpc.LREntityScorer;
import it.cnr.isti.hpc.Word2VecCompress;
import it.cnr.isti.hpc.benchmark.Stopwatch;
import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;
import it.cnr.isti.hpc.dexter.StandardTagger;
import it.cnr.isti.hpc.dexter.Tagger;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.common.FlatDocument;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.text.MyPair;
import it.cnr.isti.hpc.text.PosTagger;
import it.unimi.dsi.fastutil.io.BinIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a file containing plain text prints on the stdout the entities detected
 * by the {@link StandardTagger} tagger.
 *
 */
public class ent2vecCLI extends AbstractCommandLineInterface{

    public static Stopwatch stopwatch = new Stopwatch();
    private static final Logger logger = LoggerFactory.getLogger(ent2vecCLI.class);

      static Word2VecCompress word_model;
      static  Word2VecCompress entity_model;
     static   EntityScorer  scorer  ;
	
      static DexterParams dexterparams= DexterParams.getInstance();
       

	private static String[] params = new String[] {INPUT ,"sent"};

	private static final String USAGE = "java -cp $jar " + ent2vecCLI.class
			+ " -input entity -sent sent ";
    
    
   // static final String DATASET = "/user/fnoorala/home/NetBeansProjects/dexter/dexter-core/annotation";

    public static void main(String[] args) throws IOException {
        try {
            
            String entity="Victoria_dasdasd"  ;
            String sent="David  Victoria    children Brooklyn Romeo Cruz Harper Seven" ;
          
            Double score=0.0;
            
            
            word_model   = (Word2VecCompress)BinIO.loadObject(dexterparams.getWordModelData());
            entity_model = (Word2VecCompress)BinIO.loadObject(dexterparams.getEntModelData());
            scorer = new LREntityScorer(word_model, entity_model);
            List<String> words=new ArrayList<String>();
            for(String word:sent.replaceAll("\\s+"," ").split(" ")){
                        words.add(word);
                  
                  } 
                
          System.out.println(entity +" "+ scorer.score(entity, words));
           
          
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ent2vecCLI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ent2vecCLI(String[] args) {
		super(args, params, USAGE);
	}

}
