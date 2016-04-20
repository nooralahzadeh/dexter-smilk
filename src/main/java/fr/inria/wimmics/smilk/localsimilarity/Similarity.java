

package fr.inria.wimmics.smilk.localsimilarity;

import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;


/**
 * Contains the similarity between spot and entity, and several functions 
 * to implement a similarity function.
 * 
 * @author  
 */
public abstract class Similarity implements Comparable<Similarity> {

	protected SpotMatch x;
	protected EntityMatch y;
	protected double score;
        
        protected String x1;
	protected String y1;


	

	public Similarity() {
		super();
	}

	protected Similarity(SpotMatch x, EntityMatch y) {
                
	       this();
               set(x, y);
                

	}
        protected Similarity(String x1, String y1) {
                
	       this();
               set(x1, y1);
                

	}

        public void set(SpotMatch x, EntityMatch y) {
                   this.x = x;
                   this.y = y;
                   score = score();
        }
        public void set(String  x, String y) {
                   this.x1 = x;
                   this.y1 = y;
                   score = score();
        }
        
	public void setScore(double score) {
		this.score = score;
	}

	protected abstract double score();

	@Override
	public int compareTo(Similarity r) {
		if (this.equals(r))
			return 0;
		if (r.getScore() > score)
			return 1;
		return -1;
	}

	public abstract String getName();

	public boolean hasNegativeScores() {
		return false;
	}

	public abstract Similarity copy();

	@Override
	public int hashCode() {
		
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Similarity other = (Similarity) obj;
		if (x == null ? other.x != null : !x.equals(other.x))
			return false;
		if (y == null ? other.y != null : !y.equals(other.y))
			return false;
		return true;
	}

	public double getScore() {
		return score;
	}

	/**
	 * @return the x
	 */
	public SpotMatch getX() {
		return x;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(SpotMatch x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public EntityMatch getY() {
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(EntityMatch y) {
		this.y = y;
	}

	
	@Override
	public String toString() {
		return "<" + x.getMention() + "," + y.getEntity().getName() + "> \t" + score;
	}

	

	
	

}
