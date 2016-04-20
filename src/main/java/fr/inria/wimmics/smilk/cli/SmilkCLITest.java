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
import fr.inria.wimmics.smilk.eval.AssessmentRecord;
import fr.inria.wimmics.smilk.relatedness.DiscoveryHub;

import fr.inria.wimmics.smilk.util.ListFilesUtil;
import fr.inria.wimmics.smilk.util.MyPartition;
import fr.inria.wimmics.smilk.util.Utility;
import it.cnr.isti.hpc.benchmark.Stopwatch;
import it.cnr.isti.hpc.dexter.StandardTagger;
import it.cnr.isti.hpc.dexter.Tagger;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.common.FlatDocument;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.label.IdHelper;
import it.cnr.isti.hpc.dexter.label.IdHelperFactory;
import it.cnr.isti.hpc.dexter.lucene.LuceneHelper;
import it.cnr.isti.hpc.dexter.relatedness.MilneRelatedness;
import it.cnr.isti.hpc.dexter.relatedness.RelatednessFactory;
import it.cnr.isti.hpc.dexter.spot.Spot;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.text.MyPair;
import it.cnr.isti.hpc.text.PosTagger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import it.cnr.isti.hpc.dexter.spot.repo.SpotRepository;
import it.cnr.isti.hpc.dexter.spot.repo.SpotRepositoryFactory;
import java.net.URLDecoder;
import java.util.Set;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a file containing plain text prints on the stdout the entities detected
 * by the {@link StandardTagger} tagger.
 *
 */
public class SmilkCLITest {

    public static Stopwatch stopwatch = new Stopwatch();
    private static final Logger logger = LoggerFactory.getLogger(SmilkCLITest.class);

    private static final LuceneHelper lucene = LuceneHelper.getDexterLuceneHelper();
    private static IdHelper helper = IdHelperFactory.getStdIdHelper();

    private static SpotRepository spotRepo;

    private static SpotManager sm;

    // static final String DATASET = "/user/fnoorala/home/NetBeansProjects/dexter/dexter-core/annotation";
    public static void main(String[] args) throws IOException {

        try {
//             
          
                       
            
            
           // System.out.println(URLDecoder.decode("D%C3%A9tente", "UTF-8"));
//           System.out.println( helper.getLabel(-4621074));
//            SpotRepositoryFactory factory = new SpotRepositoryFactory();
//            spotRepo = factory.getStdInstance();
        
//            Spot s = spotRepo.getSpot("paris");

            //  System.out.println(URIUtil.encodeQuery("Jose_\"Joey\"_Torres"));
//////       
//        List<Integer> wikiids = lucene.query("newcastle natal","infobox",20);
//        for(int index: wikiids){
//            String name=helper.getLabel(index);
//             Spot s=spotRepo.getSpot(sm.clean(helper.getLabel(index)));
//             System.out.println(name+":"+ StringUtils.getJaroWinklerDistance("pitt",helper.getLabel(index)));
//             
//        }
////        
//      ApproximateMatcher apm = new ApproximateMatcher();
//////
//        apm.setIndexFile("/user/fnoorala/home/NetBeansProjects/dexter/en.data/fmindex_fmi");
//         apm.setMinScore(0.50f);
////        
//         ApproximateMatcher.Match[] matches = apm.getMatches("Chung-Ang University Graduate School of Arts");
////
//       for (ApproximateMatcher.Match m : matches) {
//            System.out.println(m.str + "\t" + m.score);
//         //    System.out.println(spotRepo.getSpot(m.str));
////            
//         }
            Entity e1=new Entity(helper.getId("Oulu"));
            Entity e2=new Entity(helper.getId("YMCA"));
            RelatednessFactory globalrelatedness= new RelatednessFactory("milnewitten_inout");
            System.out.println(globalrelatedness.getScore(e1, e2));
         DiscoveryHub  discovery = new DiscoveryHub( "http://dbpedia.org/sparql", 3, 200);
//            
             Set<String> set1=discovery.discover("Oulu");
             Set<String> set2=discovery.discover("Martti_Ahtisaari");
             System.out.println(Utility.intersection(new ArrayList<String>(set1),new ArrayList<String>(set2)));
////            
//            
            int maxSentences = 10;
            ListFilesUtil listfiles = new ListFilesUtil();
//        String Dataset = "/user/fnoorala/home/NetBeansProjects/dexter/fr.data/goldenTruth";
//        listfiles.listFiles(Dataset);

//        for (String file : listfiles.files) {
//            String inputFile = Dataset + "/" + file;
//            record.setDocId(file.replaceAll(".json", ""));
            //Creating the output files
            AssessmentRecord record = new AssessmentRecord();

//3005798 8165286
//        String inputFile = Dataset + "/494111.json";
//        logger.info("Running annotation on {} file name", inputFile);
//            AnnotatedSpotReader reader = new JsonAnnotatedSpotReader(inputFile);
//            String source = "";
//           List<AnnotatedSpot> annotations=new ArrayList<AnnotatedSpot>();
//           while (reader.hasNext()) {
//                annotations= reader.next();
//                source = reader.getRawText();
////            for (AnnotatedSpot annotation : annotations) {
////                System.out.println(annotation.getSpot() + "/ " + annotation.getPos());
////
//           }   //
            //W. J. E. Bennett was educated at Westminster School and Christ Church, Oxford .
            String source = "In 1952, Martti Ahtisaari moved to Oulu with his family to seek employment. Then he continued his education in high school, graduating in 1952. He also joined the local YMCA.";
       //While Apple is an electronics company, Mango is a clothing one and Orange is a communication one.
            //Tiger lost the US Open.
            //Onassis married Kennedy on October 20, 1968.
            //Mars, Galaxy, and Bounty are all chocolate.
            //Paris and Kim are both wealthy It Girls who had sex tapes on the Internet.
            //

            //"Justin, Stefani, and Kate are among the most popular people on both MTV and Twitter."
            stopwatch.start("Annotating");

            // if (!source.isEmpty() && !annotations.isEmpty())
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

                    if (!eml.isEmpty()) {

                        System.out.println(eml);

                        emls.addAll(eml);
                    }
                }

                for (EntityMatch entityMacth : emls) {
                    AnnotatedSpot annotatedSpot = new AnnotatedSpot();
                    annotatedSpot.setSpot(entityMacth.getMention());
                    annotatedSpot.setStart(entityMacth.getStart());
                    annotatedSpot.setEnd(entityMacth.getEnd());
                    annotatedSpot.setWikiname(entityMacth.getEntity().getName());
                    annotatedSpot.setType(entityMacth.getType());
                    annotatedSpots.add(annotatedSpot);
                }
                record.setAnnotatedSpot(annotatedSpots);

                //DatasetCollection.writeToFile(record,"/user/fnoorala/home/NetBeansProjects/dexter/dexter-core/data/annodation-result" );
            }
            logger.info("Annotating performed in {} millis",
                    stopwatch.stop("Annotating"));
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(SmilkCLITest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
