/**
 *
 */
package fr.inria.wimmics.smilk.relatedness;

import it.cnr.isti.hpc.dexter.relatedness.*;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.relatedness.*;
import it.cnr.isti.hpc.dexter.graph.*;
import it.cnr.isti.hpc.dexter.graph.NodeFactory;

/**
 *  Pointwise mutual information
 * @author fnoorala
 */
public class NaiveRelatedness extends Relatedness {

    private static IncomingNodes in = NodeFactory
            .getIncomingNodes(NodeFactory.STD_TYPE);
    private static OutcomingNodes out = NodeFactory
            .getOutcomingNodes(NodeFactory.STD_TYPE);

    

    public NaiveRelatedness() {

    }

    protected NaiveRelatedness(Entity x, Entity y) {
        super(x, y);

    }

    @Override
    protected double score() {
        
        int[] inY = in.getNeighbours(y.getId());
        int[] outX = out.getNeighbours(x.getId());
        
                
        int sizey = inY.length ;

     

        int intersection = intersectionSize(outX,inY);
         
        if (intersection == 0) {
            return 0;
        }
        
        double rel =(double)(1+ intersection)/ ( double) (1+sizey);
       
         
        return  rel;

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
    public static int[] unionArrays(int[]... arrays)
    {
        int maxSize = 0;
        int counter = 0;

        for(int[] array : arrays) maxSize += array.length;
        int[] accumulator = new int[maxSize];

        for(int[] array : arrays)
            for(int i : array)
                if(!isDuplicated(accumulator, counter, i))
                    accumulator[counter++] = i;

        int[] result = new int[counter];
        for(int i = 0; i < counter; i++) result[i] = accumulator[i];

        return result;
    }

    public static boolean isDuplicated(int[] array, int counter, int value)
    {
        for(int i = 0; i < counter; i++) if(array[i] == value) return true;
        return false;
    }
    

    @Override
    public String getName() {
        return "naive";
    }

    @Override
    public Relatedness copy() {
        NaiveRelatedness rel = new NaiveRelatedness(x, y);
        rel.setScore(score);
        return rel;
    }

    @Override
    public boolean hasNegativeScores() {
        return false;
    }

}
