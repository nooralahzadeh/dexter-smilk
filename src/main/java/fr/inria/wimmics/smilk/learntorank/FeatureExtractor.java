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
import fr.inria.wimmics.smilk.annotation.datasets.SmilkNIF;
import fr.inria.wimmics.smilk.annotation.wikipedia.AnnotatedSpot;
import fr.inria.wimmics.smilk.eval.AssessmentRecord;
 
import fr.inria.wimmics.smilk.localsimilarity.Rricesimilarity;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.Lang;

/**
 *
 * @author fnoorala
 */
public class FeatureExtractor {

    DexterParams params = DexterParams.getInstance();
    private final LuceneHelper lucene;
    private IdHelper helper;

    private SpotRepository spotRepo;

    private SpotManager sm;
    private ApproximateMatcher apm;

    Word2VecCompress word_model;
    Word2VecCompress entity_model;
    EntityScorer scorer;
    String uri;
    CandidateGen candidateGen;
    int window;
    Map<Map<String, MyPair>, Double> visited;
    RelatednessFactory globalrelatedness;
    Simmetrics simmetrics;
    Rricesimilarity rricesimilarity;
    ContextExtractor context;

    public FeatureExtractor() {
        this.window = 6;
        lucene = LuceneHelper.getDexterLuceneHelper();
        helper = IdHelperFactory.getStdIdHelper();
        sm = SpotManager.getStandardSpotManager();
        rricesimilarity = new Rricesimilarity();
        simmetrics = new Simmetrics();
        try {
            word_model = (Word2VecCompress) BinIO.loadObject(params.getWordModelData());

            entity_model = (Word2VecCompress) BinIO.loadObject(params.getEntModelData());
        } catch (IOException ex) {
            Logger.getLogger(FeatureExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FeatureExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        scorer = new LREntityScorer(word_model, entity_model);

//            candidateGen = new CandidateGen(params.getSparqlEndPointPath(), params.getDefaulyModelLang());
//            uri = params.getSparqlEndPointPath().replaceAll("sparql", "") + "resource";
    }

    public List<SpotMatch> extractFeatures(List<SpotMatch> spotmatches) {
        
     //   List<SpotMatch> new_Spotmatches=Collections.synchronizedList(spotmatches);
       
      CopyOnWriteArrayList<SpotMatch> new_Spotmatches = new CopyOnWriteArrayList<SpotMatch>(spotmatches);

        String[] globals = {"milnewitten", "naive", "pmi", "jacard", "milnewitten_inout"};

        visited = new HashMap<Map<String, MyPair>, Double>();

        for (SpotMatch smatch : new_Spotmatches) {

                    // score each all entity based on the context
            lucene.getConsinSimilarity(smatch, "summary");
            List<String> entities = new LinkedList<String>();

            for (EntityMatch e : smatch.getEntities()) {
                entities.add(e.getEntity().getName());

            }

            List<Long> entity_ids = new ArrayList<>();

            for (String entity : entities) {
                entity_ids.add(entity_model.word_id(entity));
            }

            List<String> words = new ArrayList<String>();

            for (String word : smatch.getContext().replaceAll("\\s+", " ").split(" ")) {
                words.add(word.toLowerCase());
            }

            float[] scores = scorer.score_ids(entity_ids, words);

            Map<String, Double> entityScore = new HashMap<String, Double>();

            for (int e = 0; e < entities.size(); ++e) {
                entityScore.put(entities.get(e), (double) Math.exp(scores[e]));
            }

            for (EntityMatch e : smatch.getEntities()) {
                e.setContextScoreEnt2Vec(entityScore.get(e.getEntity().getName()));
                e.setContextScore(e.getContextScoreEnt2Vec() + e.getContextScoreTFIDF());
            }

            new_Spotmatches.add(smatch);
        }

        for (int i = 0; i < new_Spotmatches.size(); i++) {

            SpotMatch s = new_Spotmatches.get(i);

            for (int j = 0; j < s.getEntities().size(); j++) {
                EntityMatch y = s.getEntities().get(j);
                Features f = new Features();

                if (y.getEntity().getName().equalsIgnoreCase("NIL")) {
                    new_Spotmatches.get(i).getEntities().get(j).setFeatures(f);
                    continue;
                }
                //label levenshtein

                String entityName = sm.clean(y.getEntity().getName());
                double levensim = rricesimilarity.levenshteinDistance(s.getMention(), entityName);
                f.setLabel_levenshtin(levensim);

                //label 
                double dicesim = rricesimilarity.diceCoefficient(s.getMention(), entityName);
                f.setLabel_dice(dicesim);

                double jarosim = rricesimilarity.jaroWinkler(s.getMention(), entityName);
                f.setLabel_jaroWinkler(jarosim);

                double soundexsim = simmetrics.Soundex(s.getMention(), entityName);
                f.setLabel_soundex(soundexsim);
                //label sim ; sum of otheres
                f.setLabelsim(levensim + dicesim + jarosim + soundexsim);

                int size = new_Spotmatches.size();

                int windowCenter = i;
                int ldelta = window / 2;
                int rdelta = window / 2;

                if (windowCenter < ldelta) {
                    rdelta += ldelta - windowCenter;
                    ldelta = windowCenter;
                }

                if (rdelta + windowCenter > size) {
                    ldelta += (rdelta + windowCenter) - size;
                    rdelta = size - windowCenter;
                }

                int start = Math.max(windowCenter - ldelta, 0);
                int end = Math.min(windowCenter + rdelta, size);

                for (String globalMethod : globals) {
                    double globalsim = 0;
                    globalrelatedness = new RelatednessFactory(globalMethod);

                    for (int x = start; x < end; x++) {

                        SpotMatch b = (SpotMatch) new_Spotmatches.get(x);

                        if (!b.equals(s)) {
                            for (EntityMatch neighbor : b.getEntities()) {

                                if (neighbor.getEntity().getName().equalsIgnoreCase("NIL")) {
                                    continue;
                                }
                                MyPair p = new MyPair(y.getEntity().getId(), neighbor.getEntity().getId());
                                Map<String, MyPair> pmap = new HashMap<String, MyPair>();
                                pmap.put(globalMethod, p);
                                double rel;

                                if (!visited.containsKey(pmap)) {
                                    // System.out.println("\t"+ neighbor.getEntity().getName());

                                    rel = globalrelatedness.getScore(y.getEntity(), neighbor.getEntity());
                                    rel = (rel > 0) ? rel : 0;

                                    // System.out.println("\t\t"+rel );
                                    visited.put(pmap, rel);
                                    p = new MyPair(neighbor.getEntity().getId(), y.getEntity().getId());
                                    pmap = new HashMap<String, MyPair>();
                                    pmap.put(globalMethod, p);
                                    visited.put(pmap, rel);

                                } else {
                                    rel = (Double) visited.get(pmap);
                                }
                                globalsim += rel;
                            }
                        }
                    }

                    switch (globalMethod) {
                        case "discoveryHub":
                            f.setDiscoveryhubRel(globalsim);
                            break;
                        case "milnewitten":
                            f.setMilneRel_in(globalsim);
                            break;

                        case "naive":
                            f.setNaviveRel(globalsim);
                            break;

                        case "pmi":
                            f.setPmiRel(globalsim);
                            break;

                        case "jacard":
                            f.setJacardRel(globalsim);
                            break;

                        case "milnewitten_inout":
                            f.setMilRel_inout(globalsim);
                            break;

                    }
                }

                f.setGlobalsim(f.getDiscoveryhubRel() + f.getJacardRel() + f.getMilRel_inout() + f.getMilneRel_in() + f.getNaviveRel() + f.getPmiRel());
                new_Spotmatches.get(i).getEntities().get(j).setFeatures(f);
            }

        }
        return new_Spotmatches;
    }
    

    public Map<Integer, List<SpotMatch>> extractNegativeCandidates(List<AssessmentRecord> records) {

        Map<Integer, List<SpotMatch>> document_spotMatches = new HashMap<Integer, List<SpotMatch>>();
        SpotRepositoryFactory factory = new SpotRepositoryFactory();
        spotRepo = factory.getStdInstance();
        apm = new ApproximateMatcher();
        apm.setIndexFile(params.getDefaultModel().getPath() + "/fmindex_fmi");
        apm.setMinScore(0.50f);

        for (AssessmentRecord record : records) {

            List<SpotMatch> spotmatches =new ArrayList<SpotMatch>();
            for (AnnotatedSpot sp : record.annotatedSpot) {

                System.out.println(sp.getSpot() + " " + sp.getEntity() + " " + sp.getType());
                System.out.println();
                Token t = new Token(sp.getSpot(), sp.getStart(), sp.getEnd());

                Set<Entity> negative_candidates = new HashSet<Entity>();
                int target = 999999999;

                if (sp.getEntity().equalsIgnoreCase("NIL")) {
                    negative_candidates.add(new Entity(0));
                } else {
                    target = helper.getId(sp.getEntity());
                }

                // Set<String> entities_sparql = candidateGen.canndidateGeneration(sp.getSpot());
                Spot s = spotRepo.getSpot(sm.clean(sp.getSpot()));

                if (s != null) {
                    s.setMention(sm.clean(sp.getSpot()));
                    int index = s.getEntities().size() + 1;

                    for (Entity e : s.getEntities()) {
                        if (e.getId() == target) {
                            negative_candidates.add(e);
                            index = s.getEntities().indexOf(e);
                            break;
                        }
                    }

                    if (index == s.getEntities().size() + 1) {
                        negative_candidates.add(new Entity(target));
                    }

                    for (int indx = 0; indx < Math.min(s.getEntities().size(), 3); indx++) {
                        if (indx == index) {
                            continue;
                        }
                        negative_candidates.add(s.getEntities().get(indx));
                    }

                }

                if (negative_candidates.size() < 3) {

                    List<Integer> wikiids = lucene.query(sp.getSpot(), "infobox", 20);
                    for (int index : wikiids) {
                        String name = helper.getLabel(index);
                        if (StringUtils.getJaroWinklerDistance(sp.getSpot(), name) > 0.8) {
                            Spot s_temp = spotRepo.getSpot(sm.clean(helper.getLabel(index)));
                            if (s_temp != null) {

                                for (int indx = 0; indx < Math.min(s_temp.getEntities().size(), 3); indx++) {
                                    negative_candidates.add(s_temp.getEntities().get(indx));
                                }
                            }
                            s = s_temp;
                        }
                    }
                }

                if (negative_candidates.size() < 3) {
                    ApproximateMatcher.Match[] matches = apm.getMatches(sp.getSpot());
                    search:

                    for (int indx = 0; indx < matches.length; indx++) {
                        if (matches[indx].score < 0.95) {

                            Spot s_temp = spotRepo.getSpot(matches[indx].str);

                            if (s_temp != null) {
                                s = s_temp;
                                for (int i = 0; i < s_temp.getEntities().size(); i++) {
                                    negative_candidates.add(s_temp.getEntities().get(i));
                                    if (negative_candidates.size() > 3) {
                                        break search;
                                    }
                                }

                            }
                        }
                    }

                }

                SpotMatch smatch = new SpotMatch(s);

                context = new ContextExtractor(record.getText(), true);
                context.setTokSen_StartEnd(t);
                smatch.setStartSE(t.getStartSE());
                smatch.setEndSE(t.getEndSE());
                //context 
                context.setWindowSize(window);
                String spotContext = context.getCntxtSentence(smatch);
                spotContext = spotContext.replaceAll("\\b" + t.getText() + "\\b", "");

                smatch.setContext(spotContext);
                smatch.setStart(sp.getStart());
                if(sp.getType()!=null){
                smatch.setNeTypes(Arrays.asList(sp.getType()));
                }
                smatch.setEnd(sp.getEnd());
                EntityMatchList eml = new EntityMatchList();
                EntityMatch match = null;

                for (Entity x : negative_candidates) {
                    match = new EntityMatch(x, 0, smatch);
                    if (x.getName().equalsIgnoreCase(sp.getEntity())) {
                        match.setScore(1);
                    }
                    eml.add(match);
                }

                smatch.setEntities(eml);
                spotmatches.add(smatch);
            }
            document_spotMatches.put(Integer.parseInt(record.docId), spotmatches);
        }

        return document_spotMatches;
    }

    public  List<String> enrichDatasetML(String inputFile,String type) {
        
        List<String> datafile=new ArrayList<String>();
        
        List<AssessmentRecord> records=new ArrayList<>();
        
        if(type.equalsIgnoreCase("OKE")){
            records = SmilkNIF.readOKEAnnotation(inputFile, Lang.TTL, true);
        }else if(type.equalsIgnoreCase("KORE"))
          records = SmilkNIF.readKOREAnnotation(inputFile, Lang.TTL, true);
        else{
            System.out.println("Not define file type for annotation");
            }
        
        Map<Integer, List<SpotMatch>> doc_spotmatches=extractNegativeCandidates(records);
        
        int sequence=1;
        for(int key : doc_spotmatches.keySet()){  
            
            List<SpotMatch> spotMatches=doc_spotmatches.get(key);
            spotMatches= extractFeatures(spotMatches);
            
            String tab=" ";
            for (SpotMatch train : spotMatches) {
               
                
                String qid=Integer.toString(sequence);
                    for (EntityMatch candidate : train.getEntities()) {
                        StringBuilder sample= new StringBuilder();
                        int index=1;
                        sample.append(candidate.getScore());
                        sample.append(tab);
                        
                        sample.append("qid:");
                        sample.append(qid);
                        sample.append(tab);
                        
                        sample.append(index+":");
                        sample.append((float) candidate.getContextScore());
                        sample.append(tab);
                        
                        index++;
                        sample.append(index+":");
                        sample.append((float)candidate.getContextScoreEnt2Vec());
                        sample.append(tab);
                        
                        index++;
                        sample.append(index+":");
                        sample.append((float)candidate.getContextScoreTFIDF());
                        sample.append(tab);
                         
                        Features f = candidate.getFeatures();
                       
                        index++;
                        sample.append(index+":");
                        sample.append((float)f.getLabel_dice());
                        sample.append(tab);
                        
                        index++;
                        sample.append(index+":");
                        sample.append((float)f.getLabel_jaroWinkler());
                        sample.append(tab);
                        
                        index++;
                        sample.append(index+":");
                        sample.append((float)f.getLabel_levenshtin());
                        sample.append(tab);
                        
                        
                        index++;
                        sample.append(index+":");
                        sample.append((float)f.getLabel_soundex());
                        sample.append(tab);
                        
                        index++;
                        sample.append(index+":");
                        sample.append((float)f.getLabelsim());
                        sample.append(tab);
                        
                        
                        index++;
                        sample.append(index+":");
                        sample.append((float)f.getMilRel_inout());
                        sample.append(tab);
                        
                        
                        index++;
                        sample.append(index+":");
                        sample.append((float)f.getMilneRel_in());
                        sample.append(tab);
                        
                        
                        index++;
                        sample.append(index+":");
                        sample.append((float)f.getNaviveRel());
                        sample.append(tab);
                        
                        index++;
                        sample.append(index+":");
                        sample.append((float)f.getPmiRel());
                        sample.append(tab);
                        
                        index++;
                        sample.append(index+":");
                        sample.append((float)f.getJacardRel());
                        sample.append(tab);
                        
                        index++;
                        sample.append(index+":");
                        sample.append((float)f.getGlobalsim());
                        sample.append(tab);
  
                        sample.append("#");
                        sample.append(key+" ["+train.getStart()+" : "+train.getEnd()+"]");
                        datafile.add(sample.toString());
                     
                    }   
                    sequence++;
                }
        }
        
        
        return datafile;
        
        
        
        }

    

}
