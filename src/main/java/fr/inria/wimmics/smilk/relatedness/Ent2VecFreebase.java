package fr.inria.wimmics.smilk.relatedness;

import it.cnr.isti.hpc.LinearAlgebra;
import it.cnr.isti.hpc.Word2VecCompress;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.relatedness.Relatedness;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.LowerCaseCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.UnicodeCleaner;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.dexter.util.DexterParamsXMLParser;
import it.unimi.dsi.fastutil.io.BinIO;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the relatedness function based on wiki2vec
 *
 * @author fnoorala
 */
public class Ent2VecFreebase extends Relatedness {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(Ent2VecFreebase.class);
    private static DexterParams params = DexterParams.getInstance();

    Word2VecCompress entity_model;
    int word_size;
    SpotManager sm;

    public Ent2VecFreebase() {
        sm =SpotManager.getStandardSpotCleaner();
       
        sm.add(new LowerCaseCleaner());

        try {
            entity_model = (Word2VecCompress) BinIO.loadObject(params.getEntFreebaseModelData());
            word_size = entity_model.dimensions();

            //System.out.println(wiki2vec.vocab().);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Ent2VecFreebase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Ent2VecFreebase.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected Ent2VecFreebase(Entity x, Entity y) {
        super(x, y);

    }

    @Override
    protected double score() {

        
        //how to convert to freebase name
        String entX =  sm.clean(x.getName()).replaceAll("[\\.,]","").replaceAll("\\s+", "_");
        String entY =  sm.clean(y.getName()).replaceAll("[\\.,]","").replaceAll("\\s+", "_");
        
        try {
            
            float[] entvec1 = entity_model.get("/en/" + entX);
            float[] entvec2 = entity_model.get("/en/" + entY);
             
            float dotpord = LinearAlgebra.inner(word_size, entvec1, 0, entvec2, 0);
            
            float norm1 = LinearAlgebra.norm(entvec1);
            float norm2 = LinearAlgebra.norm(entvec2);

            float sim = dotpord / (norm1) * (norm2);

            return (double) sim;
        } catch (NullPointerException ex) {
            return 0;
        }

    }

    @Override
    public String getName() {
        return "ent2vec_freebase";
    }

    @Override
    public Relatedness copy() {
        Ent2VecFreebase rel = new Ent2VecFreebase(x, y);
        rel.setScore(score);
        return rel;
    }

    @Override
    public boolean hasNegativeScores() {
        return false;
    }

}
