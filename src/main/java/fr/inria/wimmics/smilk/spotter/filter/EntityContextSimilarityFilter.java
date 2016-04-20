package fr.inria.wimmics.smilk.spotter.filter;

import fr.inria.wimmics.smilk.localsimilarity.SimilarityFactory;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Removes a entity candidate from spot if the context similarity of spot
 * according to the entity is less than threshold
 *
 * @author fnoorala
 */
public class EntityContextSimilarityFilter implements SpotMatchFilter {

    private static final Logger logger = LoggerFactory
            .getLogger(EntityContextSimilarityFilter.class);
    private LuceneHelper helper;
    private double threshold;
    private String field;

  
    @Override
    public SpotMatchList filter(DexterLocalParams params, SpotMatchList sml) {

        SpotMatchList filtered = new SpotMatchList();
           
        
            helper = LuceneHelper.getDexterLuceneHelper();
            for (SpotMatch spot : sml) {
                //SpotMatch s = new SpotMatch(spot.getSpot(), spot.getField(), spot.getStart(), spot.getEnd(), spot.getPos(), spot.getContext());
                //s.setEntities(spot.getEntities());

                SpotMatch sScored = helper.getConsinSimilarity(spot,field);
            
                EntityMatchList final_list = new EntityMatchList();
                
                if (sScored != null) {

                    EntityMatchList list = new EntityMatchList();
                   // if (sScored.getEntities()>1){
                        for (EntityMatch e : sScored.getEntities()) {
                        //&& sScored.getEntities().size()>1
                     
                            

                        if (e.getContextScoreTFIDF()< threshold ) {                  
                            continue;
                        }
                           System.out.println( e.getEntity().getName()+ "| "+e.getContextScoreTFIDF());
                        //EntityMatch se = new EntityMatch(e.getEntity(), e.getScore(), e.getContextScore(), spot);
                        list.add(e);
                    }
                        
                    spot.setEntities(list);
                   // spot.getEntities().sortByConetexScore();
                    
                  
               // int max=(spot.getEntities().size()>15)? 15: spot.getEntities().size();
                  int max= spot.getEntities().size();      
                       for(int i=0;i<max;i++){
                           
                           final_list.add(spot.getEntities().get(i));
                           
                       }                    
              
                       spot.setEntities(final_list);

                    

                }
                filtered.add(spot);

            }
       
//        System.err.println("-----tfidf----");
//        System.out.println(filtered);
        return filtered;
    }

    @Override
    public void init(DexterParams dexterParams, DexterLocalParams initParams) {
        if (initParams.containsKey("context-sim-threshold")) {
            threshold = initParams.getDoubleParam("context-sim-threshold");
            logger.info("min context similarity of Entity  set to {}", threshold);
        }
        
        if (initParams.containsKey("field")) {
            field = initParams.getParam("field");
            logger.info("Field for the context similarity of Entity  set to {}", field);
        }
        

    }

    @Override
    public SpotMatchList filter(DexterLocalParams params, SpotMatchList eml, Document doc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
