
package fr.inria.wimmics.smilk.localsimilarity;

import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;



/**
 * Implements the name similarity between spot and entity based on shef.wit.simmetrics
 * [1].
 * 
 * <br>
 * <br>
 * [1] SimMetrics is a Similarity Metric Library, e.g. from edit distance's 
 * (Levenshtein, Gotoh, Jaro etc) to other metrics, (e.g Soundex, Chapman).
 * Work provided by UK Sheffield University funded by (AKT) an IRC sponsored by EPSRC, grant number GR/N15764/01.
 * 
 * @author fnoorala
 * 
 *          
 */
public class MatchLikelyhoodSimilarity extends Similarity {
      
        Simmetrics sm=new Simmetrics();
        
	public MatchLikelyhoodSimilarity() {

	}
 
	public MatchLikelyhoodSimilarity(SpotMatch x, EntityMatch y) {
		super(x, y);
               

	}

	@Override
	protected double score() {
		
		return (double) sm.getMatchLikelyhood(x.getMention(), y.getEntity().getName());

	}

	
	@Override
	public String getName() {
		return "MatchLikelyhoodSimilarity";
	}

	@Override
	public Similarity copy() {
		MatchLikelyhoodSimilarity sim = new MatchLikelyhoodSimilarity(x, y);
		sim.setScore(score);
		return sim;
	}

	@Override
	public boolean hasNegativeScores() {
		return false;
	}

   
}
