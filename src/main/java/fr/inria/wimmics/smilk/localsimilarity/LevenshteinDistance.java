
package fr.inria.wimmics.smilk.localsimilarity;

import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.QuotesCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.UnderscoreCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.UnicodeCleaner;
 



/**
 * Implements the name similarity between spot and entity based on a normalized distance or similarity score between two strings. 
 * A score of 0.0 means that the two strings are absolutely dissimilar, and 1.0 means that absolutely similar (or equal). 
 * Anything in between indicates how similar each the two strings are.
 * [1].
 * 
 * <br>
 * <br>
 * [1]  https://github.com/rrice/java-string-similarity
 * 
 * @author fnoorala
 * 
 *          
 */
public class LevenshteinDistance extends Similarity {
       
    Rricesimilarity sm=new Rricesimilarity();
      
        
	public LevenshteinDistance() {

	}

	public LevenshteinDistance(SpotMatch x, EntityMatch y) {
                super(x,y);
		 

	}

	@Override
	protected double score() {
            
              SpotManager cleaner = new SpotManager();
		cleaner.add(new UnicodeCleaner());
                cleaner.add(new UnderscoreCleaner());
                cleaner.add(new QuotesCleaner());
              
                String entiyTilte=cleaner.clean(y.getEntity().getName());
		double score= sm.levenshteinDistance(x.getMention(),entiyTilte);
		return score;

	}

	
	@Override
	public String getName() {
		return "levenshtein";
	}

	@Override
	public Similarity copy() {
		LevenshteinDistance sim = new LevenshteinDistance(x, y);
		sim.setScore(score);
		return sim;
	}

	@Override
	public boolean hasNegativeScores() {
		return false;
	}

   
}
