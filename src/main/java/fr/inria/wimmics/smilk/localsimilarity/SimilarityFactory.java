
package fr.inria.wimmics.smilk.localsimilarity;


import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to retrieve a particular similarity function given its name.
 * 
 * @author fnoorala
 */
public class SimilarityFactory {

	private static DexterParams params = DexterParams.getInstance();
        
        private static final Logger logger = LoggerFactory.getLogger(SimilarityFactory.class);

	private static Map<String,Map<String, Similarity>> relmap = new HashMap<String, Map<String, Similarity>>();
	public Map<String, Similarity> similarities=new HashMap<String, Similarity> ();

	public SimilarityFactory() {
		String type = params.getDefaultSimilarity();
		if (!relmap.containsKey(type)) {
			// params.
                    Similarity labelsimilarity;
                    Similarity contextsimilarity;
                    if(params.getSimilarity(type).getLableSimilarity()!=null){
                         labelsimilarity = params.getLabelSimilarity(params.getSimilarity(type).getLableSimilarity());
                         similarities.put("label",labelsimilarity);
                    }
                    if(params.getSimilarity(type).getContectSimilarity()!=null){
                        
                         contextsimilarity = params.getContextSimilarity(params.getSimilarity(type).getLableSimilarity());
                         similarities.put("context",contextsimilarity);
                     }
                        
			relmap.put(type, similarities);
		} else {
			similarities = relmap.get(type);
		}
		if (similarities == null) {
			throw new UnsupportedOperationException("cannot find similarity "
					+ type);
		}
                logger.info("similarity is going to calulate by  = {}", type);
	}

	public SimilarityFactory(String type) {
		 if (!relmap.containsKey(type)) {
			// params.
                    Similarity labelsimilarity;
                    Similarity contextsimilarity;
                    if(params.getSimilarity(type).getLableSimilarity()!=null){
                         labelsimilarity = params.getLabelSimilarity(params.getSimilarity(type).getLableSimilarity());
                         similarities.put("label",labelsimilarity);
                    }
                    if(params.getSimilarity(type).getContectSimilarity()!=null){
                        
                         contextsimilarity = params.getContextSimilarity(params.getSimilarity(type).getContectSimilarity());
                         similarities.put("context",contextsimilarity);
                     }
                        
			relmap.put(type, similarities);
		} else {
			similarities = relmap.get(type);
		}
		if (similarities == null) {
			throw new UnsupportedOperationException("cannot find similarity "
					+ type);
		}
                logger.info("similarity is going to calulate by  = {}", type);
        }

//	public static void register(Map<String, Similarity> rel) {
//            for(Similarity sim : rel)
//		relmap.put(sim.getName(), rel);
//                
//	}

	public double getLableSimScore(SpotMatch x, EntityMatch y) {
                Similarity sim=similarities.get("label");
                if(sim!=null){
                     sim.set(x, y);
                        return sim.getScore();}
                else{
                    return 0;
                }
        }

        public double getContextSimScore(SpotMatch x, EntityMatch y) {
                Similarity sim=similarities.get("context");
                if(sim!=null){
                     sim.set(x, y);
                        return sim.getScore();}
                else{
                    return 0;
                }
	}
	 
	

}
