package fr.inria.wimmics.smilk.cli;

import fr.inria.wimmics.smilk.annotation.datasets.SmilkNIF;
import fr.inria.wimmics.smilk.annotation.wikipedia.AnnotatedSpot;
import fr.inria.wimmics.smilk.annotation.wikipedia.DatasetCollection;

import fr.inria.wimmics.smilk.eval.AnnotatedSpotReader;
import fr.inria.wimmics.smilk.eval.AssessmentRecord;
import fr.inria.wimmics.smilk.eval.JsonAnnotatedSpotReader;
import fr.inria.wimmics.smilk.util.ListFilesUtil;
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
public class SmilkOkeAannotationCLI extends AbstractCommandLineInterface {

    public static Stopwatch stopwatch = new Stopwatch();
    private static final Logger logger = LoggerFactory.getLogger(SmilkOkeAannotationCLI.class);
    private static final int maxSentences = 10;

    private static String[] params = new String[]{INPUT, "tmp", OUTPUT, "type"};

    private static final String USAGE = "java -cp $jar " + SmilkCLI.class
            + " -input PathToDataset -output PathToSaveResult";

    public static void main(String[] args) throws IOException {

        SmilkOkeAannotationCLI cli = new SmilkOkeAannotationCLI(args);

        String inputFile = cli.getInput();

        //Creating the output files
        logger.info("Running conversion on {} file name", inputFile);

        // w : with ROLE
        if (cli.getParam("type").equalsIgnoreCase("w")) {
            SmilkNIF.writeToFile(SmilkNIF.readOKEAnnotation(inputFile, Lang.TTL, true), cli.getParam("tmp"));
        }else{
            SmilkNIF.writeToFile(SmilkNIF.readOKEAnnotation(inputFile, Lang.TTL, false), cli.getParam("tmp"));
        }

        stopwatch.stat("Annotating");
        logger.info("Annotation apply to {} file name", cli.getParam("tmp"));
        DexterParams params = DexterParams.getInstance();
        Tagger tagger = params.getTagger("smilk");

        AnnotatedSpotReader reader = new JsonAnnotatedSpotReader(cli.getParam("tmp"));
        String source = "";

        List<AssessmentRecord> records = new ArrayList<AssessmentRecord>();

        while (reader.hasNext()) {
            reader.next();
            source = reader.getRawText();

            AssessmentRecord record = new AssessmentRecord();

            if (!source.isEmpty()) {
                record.setText(source);
                record.setDocId(reader.getCurrentDocId());
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

                for (EntityMatch entityMacth : emls) {
                    AnnotatedSpot annotatedSpot = new AnnotatedSpot();
                    annotatedSpot.setDocId(reader.getCurrentDocId());
                    annotatedSpot.setSpot(entityMacth.getMention());
                    annotatedSpot.setStart(entityMacth.getStart());
                    annotatedSpot.setEnd(entityMacth.getEnd());
                    annotatedSpot.setEntity(entityMacth.getEntity().getName());
                    // annotatedSpot.setEntity(Integer.toString(entityMacth.getEntity().getId()));
                    annotatedSpots.add(annotatedSpot);
                }
                record.setAnnotatedSpot(annotatedSpots);
            }

            records.add(record);
        }
        
        SmilkNIF.writeToFile(records, cli.getOutput());
        logger.info("Annotating performed in {} millis",
                stopwatch.stop("Annotating"));
    }

    public SmilkOkeAannotationCLI(String[] args) {
        super(args, params, USAGE);
    }

}
