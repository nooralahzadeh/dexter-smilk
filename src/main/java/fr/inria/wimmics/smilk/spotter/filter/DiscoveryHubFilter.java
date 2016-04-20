package fr.inria.wimmics.smilk.spotter.filter;

import fr.inria.wimmics.smilk.relatedness.DiscoveryHub;
import fr.inria.wimmics.smilk.util.Utility;
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
import it.cnr.isti.hpc.text.MyPair;
import it.unimi.dsi.fastutil.io.BinIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 *
 * @author fnoorala
 */
public class DiscoveryHubFilter implements SpotMatchFilter {

    private static final Logger logger = LoggerFactory.getLogger(DiscoveryHubFilter.class);

    
    DiscoveryHub discovery;
   

    DexterParams dexterparams = DexterParams.getInstance();

    @Override

    public SpotMatchList filter(DexterLocalParams params, SpotMatchList sml) {

        //2,200
        discovery = new DiscoveryHub(dexterparams.getSparqlEndPointPath(), 3, 200);

        SpotMatchList filtered = new SpotMatchList();
        

        for (int i = 0; i < sml.size(); i++) {

            SpotMatch s = sml.get(i);
            for (int j = 0; j < s.getEntities().size(); j++) {
                try {
                    EntityMatch match = s.getEntities().get(j);
                    Set<String> neighbours = discovery.discover(URIUtil.encodeQuery(match.getEntity().getName()));
                    match.setNeighboursDiscovered(neighbours);
                    match.getEntity().setNeighboursDiscovered(neighbours);

                    // create the context words
                } catch (URIException ex) {
                    java.util.logging.Logger.getLogger(DiscoveryHubFilter.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }
        
        filtered.addAll(sml);
        return filtered;
    }

    @Override
    public void init(DexterParams dexterParams, DexterLocalParams initParams) {

       
    }

    @Override
    public SpotMatchList filter(DexterLocalParams params, SpotMatchList eml, Document doc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
