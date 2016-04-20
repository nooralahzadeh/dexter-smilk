package fr.inria.wimmics.smilk.spotter.filter;

 
import it.cnr.isti.hpc.dexter.common.Document;

import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.spotter.filter.SpotMatchFilter;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.text.Token;

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
public class MINcharacterFilter implements SpotMatchFilter {

    private static final Logger logger = LoggerFactory
            .getLogger(MINcharacterFilter.class);

    private int threshold = 0;

    @Override
    public SpotMatchList filter(DexterLocalParams params, SpotMatchList sml) {

        SpotMatchList filtered = new SpotMatchList();

        for (SpotMatch spot : sml) {
            if (isInteresting(spot)) {

                filtered.add(spot);
            }

        }
        return filtered;
    }

    public boolean isInteresting(SpotMatch s) {

        if (s.getMention().trim().length() >= threshold) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void init(DexterParams dexterParams, DexterLocalParams initParams) {

        if (initParams.containsKey("length-threshold")) {
            threshold = initParams.getIntParam("length-threshold");
            logger.info("removes spots less than {}", threshold);

        }
    }

    @Override
    public SpotMatchList filter(DexterLocalParams params, SpotMatchList eml, Document doc) {
        return null;
    }
}
