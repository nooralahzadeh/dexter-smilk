package fr.inria.wimmics.smilk.relatedness;

import fr.inria.wimmics.smilk.util.Utility;
import it.cnr.isti.hpc.LinearAlgebra;
import it.cnr.isti.hpc.Word2VecCompress;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.relatedness.Relatedness;
import it.cnr.isti.hpc.dexter.spot.Spot;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.LowerCaseCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.UnicodeCleaner;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.dexter.util.DexterParamsXMLParser;
import it.cnr.isti.hpc.structure.LRUCache;
import it.unimi.dsi.fastutil.io.BinIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the relatedness function based on wiki2vec
 *
 * @author fnoorala
 */
public class DiscoveryHubRelatedness extends Relatedness {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(DiscoveryHubRelatedness.class);
    private static DexterParams params = DexterParams.getInstance();
    DiscoveryHub discovery;

    private static LRUCache<String, Set<String>> cache;
    SpotManager sm;

    public DiscoveryHubRelatedness() {
        sm = SpotManager.getStandardSpotCleaner();

        //2,300
        discovery = new DiscoveryHub(params.getSparqlEndPointPath(),2,300);
        int cachesize = params.getCacheSize("spotter"); 
        cache = new LRUCache<String, Set<String>>(cachesize);
    }

    protected DiscoveryHubRelatedness(Entity x, Entity y) {
        super(x, y);

    }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               

    @Override
    protected double score() {

        
        try {
            
            
            String entX = URIUtil.encodeQuery(x.getName());
            String entY = URIUtil.encodeQuery(y.getName());
            
           
                
                Set<String> neighboursX = null;                                                                                                                                                                                                                                                                                                                                                                                                             
                Set<String> neighboursY=null;
                if (cache.containsKey(entX)) {
                    // hit in cache
                    neighboursX = cache.get(entX);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
                } else {
                    
                    if(x.getNeighboursDiscovered()==null)
                             neighboursX = discovery.discover(entX);
                    else
                        neighboursX=x.getNeighboursDiscovered();

                    cache.put(entX, neighboursX);
                }
                
                if (cache.containsKey(entY)) {
                    // hit in cache
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                neighboursY = cache.get(entY);                          
                     
                } else {
                                                                                                                                                                                                                                                                                                        
                    if(y.getNeighboursDiscovered()==null)
                         neighboursY = discovery.discover(entY);
                    else
                        neighboursY=y.getNeighboursDiscovered();

                    cache.put(entY, neighboursY);
                }
                
                
                if (neighboursX != null && neighboursY != null) {
                    List<String> common = Utility.intersection(new ArrayList<String>(neighboursX), new ArrayList<String>(neighboursY));
                  
                    List<String> union=Utility.union(new ArrayList<String>(neighboursX), new ArrayList<String>(neighboursY));
                   // System.out.println(entX +" -->"+entY +"-- "+ common.size() );
                    // Double s=(double) common.size() ;
                    Double s=((double) common.size()/ (double) union.size());
                    s=(s.isNaN() ||s.isInfinite())? 0: s;
                    System.out.println(entX +" -->"+entY +"-- "+ s );
                    return s;
                } else {
                    return 0;
                }
                
            } catch (Exception ex) {
                return 0;
            }
            
       

        }
    

    @Override
    public String getName() {
        return "DiscoveryHub";
    }

    @Override
    public Relatedness copy() {
        DiscoveryHubRelatedness rel = new DiscoveryHubRelatedness(x, y);
        rel.setScore(score);
        return rel;
    }

    @Override
    public boolean hasNegativeScores() {
        return false;
    }

}
