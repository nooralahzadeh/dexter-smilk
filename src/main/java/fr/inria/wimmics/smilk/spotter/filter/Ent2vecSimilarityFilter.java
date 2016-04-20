package fr.inria.wimmics.smilk.spotter.filter;

import it.cnr.isti.hpc.EntityScorer;
import it.cnr.isti.hpc.LREntityScorer;
import it.cnr.isti.hpc.Word2VecCompress;
import it.cnr.isti.hpc.dexter.spotter.filter.*;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.lucene.LuceneHelper;
import it.cnr.isti.hpc.dexter.spot.Spot;

import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.unimi.dsi.fastutil.io.BinIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Select top #n entity candidate for spot according to context similarity of
 * the spot and the entity based on wordembedding approach (Ent-2-Vec)
 *
 * @author fnoorala
 */
public class Ent2vecSimilarityFilter implements SpotMatchFilter {

    private static final Logger logger = LoggerFactory.getLogger(Ent2vecSimilarityFilter.class);
    private LuceneHelper helper;
    private double threshold;

    Word2VecCompress word_model;
    Word2VecCompress entity_model;
    EntityScorer scorer;

    DexterParams dexterparams = DexterParams.getInstance();

    @Override
    public SpotMatchList filter(DexterLocalParams params, SpotMatchList sml) {

        SpotMatchList filtered = new SpotMatchList();
        try {

            word_model = (Word2VecCompress) BinIO.loadObject(dexterparams.getWordModelData());
            entity_model = (Word2VecCompress) BinIO.loadObject(dexterparams.getEntModelData());

            scorer = new LREntityScorer(word_model, entity_model);

            for (SpotMatch spot : sml) {

                // create the context words
                
                
                
                
                List<String> words = new ArrayList<String>();
               
                for (String word : spot.getContext().replaceAll("\\s+", " ").replaceAll("\\b" + spot.getMention() + "\\b", "").split(" ")) {
                    if(word.length()>1)
                             words.add(word.toLowerCase());
                }
                
                
                
                // score each all entity based on the context
                List<String> entities = new LinkedList<String>();
               
                for (EntityMatch e : spot.getEntities()) {
                    entities.add(e.getEntity().getName());
                     
                }
                
                List<Long> entity_ids = new ArrayList<>();

                for (String entity : entities) {
                    entity_ids.add(entity_model.word_id(entity));
                }

                float[] scores = scorer.score_ids(entity_ids, words);
                 Map <String,Double> entityScore=new HashMap<String,Double>();
               
                for (int e = 0; e < entities.size(); ++e) {               
                    //entityScore.put(entities.get(e), (double) Math.exp(scores[e]));
                     entityScore.put(entities.get(e),(double) scores[e]);
                }

                
                
                for (EntityMatch e : spot.getEntities()) {
                    e.setContextScoreEnt2Vec(Math.exp(entityScore.get(e.getEntity().getName())));
                }

                    //SpotMatch s=new SpotMatch(spot.getSpot(),spot.getField(),spot.getStart(),spot.getEnd(),spot.getPos(),spot.getContext());
               // EntityMatchList list = new EntityMatchList();
                EntityMatchList final_list = new EntityMatchList();

               

               spot.getEntities().sortByEnt2vecScore();
               
//               for (EntityMatch e : spot.getEntities()) {
//                 
//                   System.out.println(e.getEntity().getName() + "--" + e.getContextScoreEnt2Vec());
//                   
//                    
//                }

               int max=(spot.getEntities().size()> 50)? 50: spot.getEntities().size();
               // int max=  spot.getEntities().size();
                        System.out.println("------------------");
                     for(int i=0;i<max;i++){
                           if(spot.getEntities().get(i).getContextScoreEnt2Vec()==-3.4028234663852886E38||spot.getEntities().get(i).getContextScoreEnt2Vec()==0 || spot.getEntities().get(i).getEntity().getName().matches(".*\\d.*"))
                               continue;
                            System.out.println(spot.getEntities().get(i).getEntity().getName()+"| " + spot.getEntities().get(i).getContextScoreEnt2Vec());
                           final_list.add(spot.getEntities().get(i));
                           
                       }                    
                spot.setEntities(final_list);
               
               
                
                filtered.add(spot);
            }

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Ent2vecSimilarityFilter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Ent2vecSimilarityFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("-----ent2vec---");
        //System.out.println(filtered);
        return filtered;
    }

    @Override
    public void init(DexterParams dexterParams, DexterLocalParams initParams) {
        if (initParams.containsKey("context-sim-threshold")) {
            threshold = initParams.getDoubleParam("context-sim-threshold");
            logger.info("min context similarity of Entity  set to {}", threshold);
        }

    }

    @Override
    public SpotMatchList filter(DexterLocalParams params, SpotMatchList eml, Document doc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
