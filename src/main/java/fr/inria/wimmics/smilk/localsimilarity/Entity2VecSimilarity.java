
package fr.inria.wimmics.smilk.localsimilarity;



/**
 * 
 * @author fnoorala
 * 
 *          
 */

import it.cnr.isti.hpc.EntityScorer;
import it.cnr.isti.hpc.LREntityScorer;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.lucene.LuceneHelper;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;

import it.cnr.isti.hpc.Word2VecCompress;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.unimi.dsi.fastutil.io.BinIO;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
 
public class Entity2VecSimilarity extends Similarity   {
    Word2VecCompress word_model;
    Word2VecCompress entity_model;
    EntityScorer  scorer  ;

    DexterParams params= DexterParams.getInstance();
            
    
    
    public Entity2VecSimilarity() {
           
            try {
                  
               //   word_model = (Word2VecCompress)BinIO.loadObject(params.getWordModelData());
                //  entity_model = (Word2VecCompress)BinIO.loadObject(params.getEntModelData());
                  word_model =  null;
                  entity_model = null;
                
            } catch ( Exception ex) {
                Logger.getLogger(Entity2VecSimilarity.class.getName()).log(Level.SEVERE, null, ex);
            }  

	}

	protected Entity2VecSimilarity(SpotMatch x, EntityMatch y) {
		super(x, y);

	}

	@Override
	protected double score() {
            scorer = new LREntityScorer(word_model, entity_model);
            List<String> words=new ArrayList<String>();
            for(String word:x.getContext().split("\\s*"))
                words.add(word);
            return (double)  scorer.score(y.getEntity().getName(), words ) ;
        
	}

	
	@Override
	public String getName() {
		return "en2vecSimilarity";
	}

	@Override
	public Similarity copy() {
		Entity2VecSimilarity sim = new Entity2VecSimilarity(x, y);
		sim.setScore(score);
		return sim;
	}

	@Override
	public boolean hasNegativeScores() {
		return false;
	}

   
}
