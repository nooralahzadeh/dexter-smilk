/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.smilk;
 
 
import com.google.gson.Gson;
import fr.inria.wimmics.smilk.annotation.wikipedia.AnnotatedSpot;
import fr.inria.wimmics.smilk.eval.AssessmentRecord;
import fr.inria.wimmics.smilk.util.MyPartition;
import it.cnr.isti.hpc.dexter.StandardTagger;
import it.cnr.isti.hpc.dexter.Tagger;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.common.FlatDocument;
import it.cnr.isti.hpc.dexter.disambiguation.Disambiguator;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.spotter.Spotter;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.dexter.util.DexterParamsXMLParser;
import it.cnr.isti.hpc.text.MyPair;
import it.cnr.isti.hpc.text.PosTagger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fnoorala
 */
public class PipelineService {

   private static final Logger logger = LoggerFactory
            .getLogger(PipelineService.class);
//    
    DexterParams dexterParams;
    Tagger tagger;
    PosTagger ps;
    Map<String, DexterParamsXMLParser.Tagger> taggers;
    DexterParamsXMLParser XMLparams;

    public PipelineService(String xmlconfig,String lang, String taggerName, String spotter, String tool, String disambiguator) {

        taggers = new HashMap<String, DexterParamsXMLParser.Tagger>();
        
        XMLparams = DexterParamsXMLParser.load(xmlconfig);
        
        dexterParams = DexterParams.getInstance(xmlconfig, lang);
 
        for (DexterParamsXMLParser.Tagger tagger : XMLparams.getTaggers()
                .getTaggers()) {
            taggers.put(tagger.getName(), tagger);
        }

        DexterParamsXMLParser.Tagger t = taggers.get(taggerName);

        
        tagger = new StandardTagger(taggerName, dexterParams.getSpotter(spotter,tool), dexterParams.getDisambiguator(disambiguator,t.getSimilarities(), t.getRelatedness()));
      
        
        
        ps = PosTagger.getInstance();

    }

    public String annotate(String input) {

        EntityMatchList emls = new EntityMatchList();
//         List<AssessmentRecord> recordList=new ArrayList<AssessmentRecord>();
  
        int maxSentences = 10;

         //Creating the output files
          AssessmentRecord record = new AssessmentRecord();
           // String input = "La position de Paris, sur une île permettant le franchissement du grand fleuve navigable qu'est la Seine par une voie reliant le Nord et le Sud des Gaules, en fait dès l'Antiquité une cité importante, capitale des Parisii, puis lieu de séjour d'un empereur";
        if (!input.isEmpty()) {

            Document doc = new FlatDocument(input);
                
            record.setText(input);
             
                //split the document to sentences
            List<MyPair> sentences = ps.extractSentecnesOffset(doc.getContent());
            List<List<MyPair>> partition = MyPartition.partition(sentences, maxSentences);

            for (List<MyPair> pair : partition) {
                
                List<AnnotatedSpot> annotatedSpots = new ArrayList<AnnotatedSpot>();
                
                int contextStart = pair.get(0).getFirst();
                int contextEnd = pair.get(pair.size() - 1).getSecond();
                Document subdoc = new FlatDocument(doc.getContent().substring(contextStart, contextEnd));
                EntityMatchList eml = tagger.tag(subdoc);

                if (!eml.isEmpty()) {

                    System.out.println(eml);

                    emls.addAll(eml);
                }
                 for (EntityMatch entityMacth : emls) { 
                    AnnotatedSpot annotatedSpot = new AnnotatedSpot();
                    annotatedSpot.setSpot(entityMacth.getMention());
                    annotatedSpot.setStart(entityMacth.getStart());
                    annotatedSpot.setEnd(entityMacth.getEnd());
                    annotatedSpot.setWikiname(entityMacth.getEntity().getName());
                    //annotatedSpot.setType(entityMacth.getType());
                    annotatedSpots.add(annotatedSpot);
                }
                record.setAnnotatedSpot(annotatedSpots);
            }
          //  recordList.add(record);
        }

        return record.toJson();

    }

}
