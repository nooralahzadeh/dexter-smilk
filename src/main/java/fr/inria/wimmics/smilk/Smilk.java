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
package fr.inria.wimmics.smilk;

import fr.inria.wimmics.smilk.annotation.wikipedia.AnnotatedSpot;
import fr.inria.wimmics.smilk.annotation.wikipedia.DatasetCollection;
import fr.inria.wimmics.smilk.eval.AnnotatedSpotReader;
import fr.inria.wimmics.smilk.eval.AssessmentRecord;
import fr.inria.wimmics.smilk.eval.JsonAnnotatedSpotReader;
import fr.inria.wimmics.smilk.util.ListFilesUtil;
import fr.inria.wimmics.smilk.util.MyPartition;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a file containing plain text  
 *
 */
public class Smilk {
    public static Stopwatch stopwatch = new Stopwatch();
    private static final Logger logger = LoggerFactory.getLogger(Smilk.class);

    int maxSentences=10;
    Tagger tagger;
    DexterParams params;

    public Smilk() {
         stopwatch.stat("Annotating");
         params = DexterParams.getInstance();
         tagger = params.getTagger("smilk");
        
    }

    
 
    
  
    public List<AnnotatedSpot> Smilk (String input) {
        
             
                List<AnnotatedSpot> annotatedSpots = new ArrayList<AnnotatedSpot>();

                Document doc = new FlatDocument(input);
               

                //split the document to sentences
                PosTagger ps = PosTagger.getInstance();
                List<MyPair> sentences = ps.extractSentecnesOffset(doc.getContent());
                List<List<MyPair>> partition = MyPartition.partition(sentences, maxSentences);
                EntityMatchList emls = new EntityMatchList();
                
                for (List<MyPair> pair : partition) {
                    
                    int contextStart = pair.get(0).getFirst();
                    int contextEnd = pair.get(pair.size() - 1).getSecond();
                    Document subdoc = new FlatDocument(doc.getContent().substring(contextStart, contextEnd));
                    EntityMatchList eml = tagger.tag(subdoc);
                    if(!eml.isEmpty()){  
                    emls.addAll(eml);
                   }                                  
                   
                }
      
                for (EntityMatch entityMacth : emls) {
                    AnnotatedSpot annotatedSpot = new AnnotatedSpot();
                    annotatedSpot.setSpot(entityMacth.getMention());
                    annotatedSpot.setStart(entityMacth.getStart());
                    annotatedSpot.setEnd(entityMacth.getEnd());
                    annotatedSpot.setWikiname(entityMacth.getEntity().getName());
                    annotatedSpot.setEntity(Integer.toString(entityMacth.getEntity().getId()));
                    annotatedSpots.add(annotatedSpot);
                }
                 
                   
        logger.info("Annotating performed in {} millis",
                stopwatch.stop("Annotating"));
        
        return annotatedSpots;
    }
    
   

}
