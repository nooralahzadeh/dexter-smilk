package fr.inria.wimmics.smilk.spotter.filter;

import it.cnr.isti.hpc.dexter.common.Document;

import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.spotter.filter.SpotMatchFilter;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;

import java.util.Collections;
import java.util.Comparator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes spots with is not recognized as NE in RENCO ).
 *
 * @author fnoorala
 */
public class POSTagFilter implements SpotMatchFilter {

    private static final Logger logger = LoggerFactory
            .getLogger(POSTagFilter.class);
    private static DexterParams dexterparams = DexterParams.getInstance();
    String model;

    @Override
    public SpotMatchList filter(DexterLocalParams params, SpotMatchList sml) {
        model = dexterparams.getDefaultNlpModel().getName();

        Comparator<SpotMatch> comparator = new SpotMatchStartComparator();
        SpotMatchList filtered = new SpotMatchList();
        Collections.sort(sml, comparator);
        for (SpotMatch spot : sml) {
            //System.out.println(spot.getMention()+" "+spot.getPos());      
//                      boolean clash = false;
            if (isInteresting(spot.getPos())) {
//                                            for (SpotMatch s : filtered) {
//				if (s.overlaps(spot)) {
//
//					clash = false;
//					break;
//				}
//			}
//			if (!clash) {
                filtered.add(spot);
            } else {
				// logger.info("spot [{}] in {} overlaps, ignoring", spot
                // .getSpot().getMention(), spot.getStart());
            }
//                                         }

        }
        return filtered;
    }

    private static class SpotMatchStartComparator implements
            Comparator<SpotMatch> {

        @Override
        public int compare(SpotMatch o1, SpotMatch o2) {
            int st1 = o1.getStart();
            int st2 = o2.getStart();

            return st1 - st2;
        }

    }

    public boolean isInteresting(List<String> pos) {

//        String posTags=pos.toString();
//        if(posTags.contains("(?s).*\\bNPP\\ET.*")){
//            return true;
//        }
        if (model.equalsIgnoreCase("fr")) {
            if (pos.contains("ET") || pos.contains("NPP")) {
                return true;
            } else {
                return false;
            }
        } else {
            if (pos.contains("NNPS") || pos.contains("NNP")) {
                return true;
            } else {
                return false;
            }
        }
    }


@Override
        public void init(DexterParams dexterParams, DexterLocalParams initParams) {
              
           
	}

    
   @Override
        public SpotMatchList filter(DexterLocalParams params, SpotMatchList eml,  Document doc) {
        return null;
    }
}
