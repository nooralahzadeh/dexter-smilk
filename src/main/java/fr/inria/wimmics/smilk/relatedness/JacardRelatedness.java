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
public class JacardRelatedness extends Relatedness {

    private static IncomingNodes in = NodeFactory
            .getIncomingNodes(NodeFactory.STD_TYPE);
    private static OutcomingNodes out = NodeFactory
            .getOutcomingNodes(NodeFactory.STD_TYPE);

    private static final int W = in.size();
    

    public JacardRelatedness() {

    }

    protected JacardRelatedness(Entity x, Entity y) {
        super(x, y);

    }

    @Override
    protected double score() {
        int[] inX = in.getNeighbours(x.getId());
        int[] inY = in.getNeighbours(y.getId());
        
//        int[] outX = out.getNeighbours(x.getId());
//        int[] outY = out.getNeighbours(y.getId());
        
        int[] union=unionArrays(inX,inY);
//        int[] unionY=unionArrays(inY,outY);
                

        int sizeUnion = union.length ; 
//        int sizey = unionY.length ;

       // int maxXY = Math.max(sizex, sizey);
       // int minXY = Math.min(sizex, sizey);
     //   if (minXY == 0) {
     //       return 0;
      //  }

        int intersection = intersectionSize(inX,inY);
        if (intersection == 0 ) {
            return 0;
        }
        
        double rel = (double) intersection /(double ) sizeUnion;
        if (rel < 0) {
            rel = 0;
        }
        return rel;

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
        return "jacard";
    }

    @Override
    public Relatedness copy() {
        JacardRelatedness rel = new JacardRelatedness(x, y);
        rel.setScore(score);
        return rel;
    }

    @Override
    public boolean hasNegativeScores() {
        return false;
    }

}
