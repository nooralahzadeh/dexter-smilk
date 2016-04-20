package fr.inria.wimmics.smilk.relatedness;

import static fr.inria.wimmics.smilk.relatedness.MilneRelatedness_INOUT.unionArrays;
import fr.inria.wimmics.smilk.util.Utility;
import it.cnr.isti.hpc.LinearAlgebra;
import it.cnr.isti.hpc.Word2VecCompress;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.graph.IncomingNodes;
import it.cnr.isti.hpc.dexter.graph.NodeFactory;
import it.cnr.isti.hpc.dexter.graph.OutcomingNodes;
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
public class DiscoveryHub_MilnRelatedness extends Relatedness {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(DiscoveryHub_MilnRelatedness.class);
    private static DexterParams params = DexterParams.getInstance();
    DiscoveryHub discovery;

    private static LRUCache<String, Set<String>> cache;
    SpotManager sm;

    private static IncomingNodes in = NodeFactory
            .getIncomingNodes(NodeFactory.STD_TYPE);
    
    private static final int W = in.size();
    private static final double logW = Math.log(W);

    public DiscoveryHub_MilnRelatedness() {

        sm = SpotManager.getStandardSpotCleaner();

        discovery = new DiscoveryHub(params.getSparqlEndPointPath(), 3, 200);
        int cachesize = params.getCacheSize("spotter");
        cache = new LRUCache<String, Set<String>>(cachesize);
    }

    protected DiscoveryHub_MilnRelatedness(Entity x, Entity y) {
        super(x, y);

    }

    @Override
    protected double score() {

        try {

            double milRel=0;
           int[] inX = in.getNeighbours(x.getId());
		int[] inY = in.getNeighbours(y.getId());
		int sizex = inX.length;
		int sizey = inY.length;

		int maxXY = Math.max(sizex, sizey);
		int minXY = Math.min(sizex, sizey);
		if (minXY == 0){
			milRel= 0;
                }else{
                    
                
		int intersection = intersectionSize(inX, inY);
		if (intersection == 0){
			milRel= 0;
                } else {
		 milRel = 1 - ((Math.log(maxXY) - Math.log(intersection)) / (logW - Math
				.log(minXY)));
                }
                }
             
            
            if (milRel < 0) {
                milRel = 0;
            }

            String entX = URIUtil.encodeQuery(x.getName());
            String entY = URIUtil.encodeQuery(y.getName());

            Set<String> neighboursX = null;
            Set<String> neighboursY = null;
            if (cache.containsKey(entX)) {
                // hit in cache
                neighboursX = cache.get(entX);

            } else {
                neighboursX = discovery.discover(entX);;

                cache.put(entX, neighboursX);
            }

            if (cache.containsKey(entY)) {
                // hit in cache
                neighboursY = cache.get(entY);

            } else {
                neighboursY = discovery.discover(entY);

                cache.put(entY, neighboursY);
            }

            if (neighboursX != null && neighboursY != null) {
               
                List<String> common = Utility.intersection(new ArrayList<String>(neighboursX), new ArrayList<String>(neighboursY));
                   // System.out.println(entX +" -->"+entY +"-- "+ common.size() );

                return (double) common.size()+milRel;
            } else {
                return milRel;
            }

        } catch (Exception ex) {
            return 0;
        }

    }

    @Override
    public String getName() {
        return "DiscoveryHub+Milne";
    }

    @Override
    public Relatedness copy() {
        DiscoveryHub_MilnRelatedness rel = new DiscoveryHub_MilnRelatedness(x, y);
        rel.setScore(score);
        return rel;
    }

    @Override
    public boolean hasNegativeScores() {
        return false;
    }

    public int intersectionSize(int[] a, int[] b) {
        int i = 0, j = 0;
        int size = 0;
        int aSize = a.length;
        int bSize = b.length;
        while ((i < aSize) && (j < bSize)) {
            if (a[i] < b[j]) {
                i++;
                continue;
            }
            if (a[i] > b[j]) {
                j++;
                continue;
            }
            // => (a[i] == a[j])
            size++;
            i++;
            j++;

        }
        return size;
    }

    public int[] joingArrays(int[] array1, int[] array2) {

        int[] array1and2 = new int[array1.length + array2.length];
        System.arraycopy(array1, 0, array1and2, 0, array1.length);
        System.arraycopy(array2, 0, array1and2, array1.length, array2.length);
        return array1and2;
    }

    /* Union of multiple arrays */
    public static int[] unionArrays(int[]... arrays) {
        int maxSize = 0;
        int counter = 0;

        for (int[] array : arrays) {
            maxSize += array.length;
        }
        int[] accumulator = new int[maxSize];

        for (int[] array : arrays) {
            for (int i : array) {
                if (!isDuplicated(accumulator, counter, i)) {
                    accumulator[counter++] = i;
                }
            }
        }

        int[] result = new int[counter];
        for (int i = 0; i < counter; i++) {
            result[i] = accumulator[i];
        }

        return result;
    }

    public static boolean isDuplicated(int[] array, int counter, int value) {
        for (int i = 0; i < counter; i++) {
            if (array[i] == value) {
                return true;
            }
        }
        return false;
    }
}
