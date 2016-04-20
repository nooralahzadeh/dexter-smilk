package fr.inria.wimmics.smilk.linking;

import ciir.umass.edu.eval.Evaluator;
import ciir.umass.edu.learning.RANKER_TYPE;
import fr.inria.wimmics.smilk.learntorank.Features;
import fr.inria.wimmics.smilk.localsimilarity.Rricesimilarity;
import fr.inria.wimmics.smilk.localsimilarity.Simmetrics;
import it.cnr.isti.hpc.benchmark.Stopwatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.dexter.disambiguation.Disambiguator;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.relatedness.RelatednessFactory;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.text.MyPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class rankLibLabeling implements Disambiguator {

    private static final Logger logger = LoggerFactory.getLogger(rankLibLabeling.class);
    public static Stopwatch stopwatch = new Stopwatch();
    Map<Map<String, MyPair>, Double> visited;
    private int window;
    private SpotManager sm;
    String rankLibModel;

    Simmetrics simmetrics;
    Rricesimilarity rricesimilarity;
    RelatednessFactory globalrelatedness;
    Evaluator e ;
    DexterParams params = DexterParams.getInstance();
    String[] globals = {"milnewitten", "naive", "pmi", "jacard", "milnewitten_inout"};

    @Override
   
    public EntityMatchList disambiguate(DexterLocalParams localParams, SpotMatchList sml) {
       
 
        
         // add NIL for each spotmatch as one alternative
          
//        for(int i=0;i<sml.size();i++){
//          
//             EntityMatch nil_enMatch=new EntityMatch(0, 0,sml.get(i));
//             sml.get(i).getEntities().add(nil_enMatch);
//             
//        }
        
       e = new Evaluator(RANKER_TYPE.LAMBDAMART, "NDCG@1", "ERR@1");

        sm = SpotManager.getStandardSpotManager();
        rricesimilarity = new Rricesimilarity();
        simmetrics = new Simmetrics();

        

        List<String> datafile = new ArrayList<String>();

        EntityMatchList eml = new EntityMatchList();

        init(null, localParams);

        rankLibModel = params.getDefaultModel().getPath() + rankLibModel;

        stopwatch.start("Linking");

        //Labeling the new instance 
        String tab = " ";

        Map<Integer, List<Integer>> Spot_entityMatchIndexes = new HashMap<Integer, List<Integer>>();
        
        List<SpotMatch> new_Sml=  extractFeatures(sml);
        
        
        for (int i = 0; i < new_Sml.size(); i++) {

            SpotMatch train = new_Sml.get(i);
            String key = train.getMention();
            int sequence = i + 1;
            String qid = Integer.toString(sequence);
            List<Integer> entity_List = new ArrayList<>();

            for (int x = 0; x < train.getEntities().size(); x++) {

                EntityMatch candidate = train.getEntities().get(x);
                entity_List.add(x);
                StringBuilder sample = new StringBuilder();

                int index = 1;
                sample.append('0');
                sample.append(tab);

                sample.append("qid:");
                sample.append(qid);
                sample.append(tab);

                sample.append(index + ":");
                sample.append((float) candidate.getContextScore());
                sample.append(tab);

                index++;
                sample.append(index + ":");
                sample.append((float) candidate.getContextScoreEnt2Vec());
                sample.append(tab);

                index++;
                sample.append(index + ":");
                sample.append((float) candidate.getContextScoreTFIDF());
                sample.append(tab);

                Features f = candidate.getFeatures();

                index++;
                sample.append(index + ":");
                sample.append((float) f.getLabel_dice());
                sample.append(tab);

                index++;
                sample.append(index + ":");
                sample.append((float) f.getLabel_jaroWinkler());
                sample.append(tab);

                index++;
                sample.append(index + ":");
                sample.append((float) f.getLabel_levenshtin());
                sample.append(tab);

                index++;
                sample.append(index + ":");
                sample.append((float) f.getLabel_soundex());
                sample.append(tab);

                index++;
                sample.append(index + ":");
                sample.append((float) f.getLabelsim());
                sample.append(tab);

                index++;
                sample.append(index + ":");
                sample.append((float) f.getMilRel_inout());
                sample.append(tab);

                index++;
                sample.append(index + ":");
                sample.append((float) f.getMilneRel_in());
                sample.append(tab);

                index++;
                sample.append(index + ":");
                sample.append((float) f.getNaviveRel());
                sample.append(tab);

                index++;
                sample.append(index + ":");
                sample.append((float) f.getPmiRel());
                sample.append(tab);

                index++;
                sample.append(index + ":");
                sample.append((float) f.getJacardRel());
                sample.append(tab);

                index++;
                sample.append(index + ":");
                sample.append((float) f.getGlobalsim());
                sample.append(tab);

                sample.append("#");
                sample.append(key + " [" + train.getStart() + " : " + train.getEnd() + "]");
                datafile.add(sample.toString());

            }
            Spot_entityMatchIndexes.put(i, entity_List);

        }
     

        Map<Integer, Map<Integer,Double>> spot_entityMatch = e.score(rankLibModel, datafile);
            
        for (int i = 0; i < new_Sml.size(); i++) {

            Map<Integer,Double> pairs = spot_entityMatch.get(i+1);

            for (int j = 0; j < new_Sml.get(i).getEntities().size(); j++) {
                
                for (Integer p : pairs.keySet()) {

                    if ( p == j) {
                        double score = (double) pairs.get(p);
                        EntityMatch entityMatch = new EntityMatch(new_Sml.get(i).getEntities().get(j).getEntity(), score, new_Sml.get(i));
                        eml.add(entityMatch);
                        break;
                    }

                }

            }
        }
        eml.sort();

        return eml;
    }

    @Override
    public void init(DexterParams dexterParams,
            DexterLocalParams dexterModuleParams) {

        if (dexterModuleParams != null) {

            if (dexterModuleParams.containsKey("window-size")) {
                window = dexterModuleParams.getIntParam("window-size");
                logger.info("init, set window = {}", Integer.valueOf(window));
            }
            if (dexterModuleParams.containsKey("ml-model")) {
                rankLibModel = dexterModuleParams.getParam("ml-model");
                logger.info("init, local similarity function = {} ", rankLibModel);
            }

        }

    }

    public List<SpotMatch> extractFeatures(List<SpotMatch> spotmatches) {

        CopyOnWriteArrayList<SpotMatch> new_Spotmatches = new CopyOnWriteArrayList<SpotMatch>(spotmatches);

        visited = new HashMap<Map<String, MyPair>, Double>();

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

}
