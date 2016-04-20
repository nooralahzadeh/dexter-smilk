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
 * Given a file containing plain text prints on the stdout the entities detected
 * by the {@link StandardTagger} tagger.
 *
 */
public class SmilkCLI extends AbstractCommandLineInterface{

    public static Stopwatch stopwatch = new Stopwatch();
    private static final Logger logger = LoggerFactory.getLogger(SmilkCLI.class);

    
	private static String[] params = new String[] {INPUT,OUTPUT};

	private static final String USAGE = "java -cp $jar " + SmilkCLI.class
			+ " -input PathToDataset -output PathToSaveResult";
    
    
   // static final String DATASET = "/user/fnoorala/home/NetBeansProjects/dexter/dexter-core/annotation";

    public static void main(String[] args) throws IOException {
        SmilkCLI cli=new SmilkCLI(args);
               
        int maxSentences = 10;
        ListFilesUtil listfiles = new ListFilesUtil();
        String Dataset=cli.getInput();
        listfiles.listFiles(Dataset);
        
        for (String file : listfiles.files) {
            String inputFile = Dataset + "/" + file;

            //Creating the output files
            AssessmentRecord record = new AssessmentRecord();
            record.setDocId(file.replaceAll(".json", ""));

         //   String inputFile = DATASET + "/79240.json";

            logger.info("Running annotation on {} file name", inputFile);

            AnnotatedSpotReader reader = new JsonAnnotatedSpotReader(inputFile);
            String source = "";
            while (reader.hasNext()) {
                reader.next();
                source = reader.getRawText();
//            for (AnnotatedSpot annotation : annotations) {
//                System.out.println(annotation.getSpot() + "/ " + annotation.getPos());
//            }
            }


            
            stopwatch.stat("Annotating");

            if (!source.isEmpty()) {
                record.setText(source);
                List<AnnotatedSpot> annotatedSpots = new ArrayList<AnnotatedSpot>();

                Document doc = new FlatDocument(source);
                DexterParams params = DexterParams.getInstance();
                Tagger tagger = params.getTagger("smilk");

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
                record.setAnnotatedSpot(annotatedSpots);
            }
            
           DatasetCollection.writeToFile(record, cli.getOutput());
        }
        logger.info("Annotating performed in {} millis",
                stopwatch.stop("Annotating"));
    }
    
    public SmilkCLI(String[] args) {
		super(args, params, USAGE);
	}

}
