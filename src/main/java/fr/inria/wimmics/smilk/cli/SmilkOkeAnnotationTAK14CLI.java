package fr.inria.wimmics.smilk.cli;

import fr.inria.wimmics.smilk.annotation.datasets.SmilkNIF;
import fr.inria.wimmics.smilk.annotation.wikipedia.AnnotatedSpot;
import fr.inria.wimmics.smilk.eval.AssessmentRecord;
import fr.inria.wimmics.smilk.util.MyPartition;
import it.cnr.isti.hpc.benchmark.Stopwatch;
import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;
import static it.cnr.isti.hpc.cli.AbstractCommandLineInterface.INPUT;
import static it.cnr.isti.hpc.cli.AbstractCommandLineInterface.OUTPUT;
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
import org.apache.jena.riot.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
/**
 *
 * @author fnoorala
 */
public class SmilkOkeAnnotationTAK14CLI {

    public static Stopwatch stopwatch = new Stopwatch();
    private static final Logger logger = LoggerFactory.getLogger(SmilkOkeAnnotationTAK14CLI.class);
    private static final int maxSentences = 10;

    public static void main(String[] args) throws IOException {
       //"/user/fnoorala/home/NetBeansProjects/dexter/en.data/OKE/example.ttl";
        String inputFile = args[0];
        //"/user/fnoorala/home/NetBeansProjects/neleval/TAK14";
        String outputDIR =args[1];

        boolean withType = true;
        // w : with ROLE
        if (args[2].equalsIgnoreCase("NO")) {
            withType = false;
        }
//
//        //Creating the output files
        logger.info("Running conversion on {} file name", inputFile);

        List<AssessmentRecord> records = SmilkNIF.readOKEAnnotation(inputFile, Lang.TTL, withType);
      //  SmilkNIF.writeToTAKformat(records, outputDIR, "gold",true);

        stopwatch.stat("Annotating");
        logger.info("Annotation apply to {} file name", inputFile);
        DexterParams params = DexterParams.getInstance();
        Tagger tagger = params.getTagger("smilk");

        //writing the gold
        List<AssessmentRecord> AnnotatedRecords = new ArrayList<AssessmentRecord>();
        
        System.out.println(records.size());
        System.out.println( );
        for (int i=0;i<records.size();i++) {
            
            logger.info("Proccessing documnet {} of {}", i,records.size());
            AssessmentRecord record=records.get(i);
            String source = record.getText();
            
            System.out.println(source );
            System.out.println( );
//             
            AssessmentRecord AnnotatedRecord = new AssessmentRecord();
            if (!source.isEmpty()) {
                AnnotatedRecord.setText(source);
                AnnotatedRecord.setDocId(record.getDocId());
                List<AnnotatedSpot> annotatedSpots = new ArrayList<AnnotatedSpot>();

                Document doc = new FlatDocument(source);

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
                    if (!eml.isEmpty()) {
                        emls.addAll(eml);
                    }

                }

                System.out.println(emls);
                
                for (EntityMatch entityMacth : emls) {
                    AnnotatedSpot annotatedSpot = new AnnotatedSpot();
                    annotatedSpot.setDocId(record.getDocId());
                    annotatedSpot.setSpot(entityMacth.getMention());
                    annotatedSpot.setStart(entityMacth.getStart());
                    annotatedSpot.setEnd(entityMacth.getEnd());
                    annotatedSpot.setEntity(entityMacth.getEntity().getName());
                   
                    if(withType){
                             annotatedSpot.setType(entityMacth.getType());
                        }else{
                             annotatedSpot.setType("MISC");
                        }
                    // annotatedSpot.setEntity(Integer.toString(entityMacth.getEntity().getId()));
                    annotatedSpots.add(annotatedSpot);
                }
                AnnotatedRecord.setAnnotatedSpot(annotatedSpots);
            }

            AnnotatedRecords.add(AnnotatedRecord);
        }

        SmilkNIF.writeToTAKformat(AnnotatedRecords, outputDIR + "/system", "system",true);
        logger.info("Annotating performed in {} millis",
                stopwatch.stop("Annotating"));
    }
}
