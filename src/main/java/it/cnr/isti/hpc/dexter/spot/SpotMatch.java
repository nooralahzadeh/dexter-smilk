/**
 *  Copyright 2012 Diego Ceccarelli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package it.cnr.isti.hpc.dexter.spot;

import fr.inria.wimmics.smilk.localsimilarity.SimilarityFactory;
import it.cnr.isti.hpc.dexter.common.Field;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.text.Token;
import java.util.ArrayList;

import java.util.List;

/**
 * SpotMatch contains all the additional informations regarding a spot matched
 * in a particular position of the text. <br>
 * In particular, such object contains:
 * <ul>
 * <li>a reference to the original spot</li>
 * <li>a list of candidates entities, with relevance scores possibly affected by
 * the position and the context of the match</li>
 * <li>the document {@link Field field} where the spot was matched</li>
 * <li>the position in the field where the match starts</li>
 * <li>the position in the field where the match stops</li>
 * 
 * 
 * 
 * @see EntityMatchList
 * @see Field
 * @see Spot
 * 
 * @author Diego Ceccarelli, diego.ceccarelli@isti.cnr.it
 */
public class SpotMatch implements Comparable<SpotMatch> {
         
	protected Spot spot;
	protected EntityMatchList entities;
	protected Field field;

    public SpotMatch(Spot spot, Field field, int start, int end,List<String> pos, String conntext) {
        this.spot = spot;
        this.field = field;
        this.start = start;
        this.end = end;
        this.context=conntext;
    }

    public SpotMatch(Spot spot, Field field, int start, int end, int startSE, int endSE) {
        this.spot = spot;
        this.field = field;
        this.start = start;
        this.end = end;
        this.startSE = startSE;
        this.endSE = endSE;
    }
        
        
	private int start;
	private int end;
        
        
        private int startSE;
        private int endSE;
        private String processFindEntity;
        
        
        private List<String> pos=new ArrayList<String>();
        private List<String> NeTypes=new ArrayList<String>();
       
        private List<Token> subTokens=new ArrayList<Token>();
        
        private String context=new String();
        
	public SpotMatch(Spot spot) {
		this.spot = spot;
	}
        
        public SpotMatch(SpotMatch spotMatch) {
	this.spot = spotMatch.getSpot();
        this.spot = spot;
         
        this.field = spotMatch.getField();
        this.start = spotMatch.getStart();
        this.end = spotMatch.getEnd();
        this.startSE =spotMatch.getStartSE() ;
        this.endSE =spotMatch.getEndSE() ;
        this.context=spotMatch.getContext();
        this.NeTypes=spotMatch.getNeTypes();
        this.pos=spotMatch.getPos();
        this.subTokens=spotMatch.getSubTokens();
        this.processFindEntity=spotMatch.getProcessFiEntity();
        
        
	}
        

	// public void incrementOccurrences() {
	// occurrences++;
	// }

	public SpotMatch(Spot spot, EntityMatchList entities) {
		this(spot);
		this.entities = entities;
	}

        public SpotMatch(SpotMatch spotMatch, EntityMatchList entities) {
		this(spotMatch);
		this.entities = entities;
//                for (EntityMatch e : entities) {
//			this.entities.add(new EntityMatch(e.getEntity().clone(), spot
//					.getEntityCommonness(e.getEntity()), this));
//		}
                
	}
	

	// public double getImportance() {
	// return spot.getIdf() * occurrences;
	// }

	public SpotMatch(Spot s, Field field) {
		this(s);
		this.field = field;
	}
        
       
        
	public void setProbability(double probability) {
		spot.setLinkProbability(probability);
	}

	@Override
	public int compareTo(SpotMatch m) {
		if (spot.getIdf() > m.spot.getIdf())
			return 1;
		if (spot.getIdf() < m.spot.getIdf())
			return -1;
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
                
		SpotMatch other = (SpotMatch) obj;
		if (spot == null) {
			if (other.spot != null)
				return false;
		} else if (this.getStart()!=other.getStart() && this.getEnd()!=other.getEnd())
			return false;
		return true;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
        public int getStartSE() {
		return startSE;
	}

	public void setStartSE(int startSE) {
		this.startSE = startSE;
	}

	public int getEndSE() {
		return endSE;
	}

	public void setEndSE(int endSE) {
		this.endSE = endSE;
	}

	public EntityMatchList getEntities() {
		return entities;
	}

	public Spot getSpot() {
		return spot;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((spot == null) ? 0 : spot.hashCode());
		return result;
	}

	public void setEntities(EntityMatchList entities) {
		this.entities = entities;
	}

	public String getMention() {
		return spot.getMention();
	}

	// @Override
	// public int compareTo(Match m) {
	// if (spot.getSpotProbability() > m.spot.getSpotProbability()) return 1;
	// if (spot.getSpotProbability() < m.spot.getSpotProbability()) return -1;
	// return 1;
	// }

	public void setSpot(Spot spot) {
		this.spot = spot;
	}

	// public String toString() {
	// return spot.toString() + " occ: " + occurrences;
	// }

	/**
	 * Returns probability to be a link to a entity for the text of this spot,
	 * it is computed dividing the number of documents in Wikipedia containing
	 * this spot as a anchor by the number of documents in wikipedia containing
	 * this spot as simple text.
	 * 
	 * @returns the link probability
	 */
	public double getProbability() {
		return spot.getLinkProbability();
	}

	/**
	 * Returns true if this spot and the given spots overlaps in the annotated
	 * text, e.g.,
	 * <code> "neruda pablo picasso" -> 'neruda pablo' 'pablo picasso'
	 </code>.
	 * 
	 * @param s
	 *            - The spot to check
	 * @return
	 */
	public boolean overlaps(SpotMatch s) {
//		if (!field.equals(s.getField())) {
//			return false;
//		}
		boolean startOverlap = ((s.getStart() >= this.getStart()) && (s
				.getStart() <= this.getEnd()));
		if (startOverlap)
			return true;
		boolean endOverlap = ((s.getEnd() >= this.getStart()) && (s.getEnd() <= this
				.getEnd()));
		return endOverlap;
	}

	public double getEntityCommonness(Entity entity) {
		return spot.getEntityCommonness(entity);
	}
        

	public int getFrequency() {
		return spot.getFrequency();
	}

	public int getLinkFrequency() {
		return spot.getLink();
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public double getLinkProbability() {
		return spot.getLinkProbability();
	}
        
        public List<String> getPos() {
		return pos;
	}
        
        public void setPos(List<String> pos) {
		this.pos=pos;
	}
        
         public List<String> getNeTypes() {
		return NeTypes;
	}
        
        public void setNeTypes(List<String> NeTypes) {
		this.NeTypes=NeTypes;
	}
          
          public String getContext() {
		return context;
	}

           public void setContext(String context) {
		this.context=context;
	}
           
             public String getProcessFiEntity() {
		return processFindEntity;
	}

           public void setProcessFiEntity(String processFindEntity) {
		this.processFindEntity=processFindEntity;
	}
         public List<Token> getSubTokens() {
		return subTokens;
	}
        
        public void setSubTokens(List<Token> subtokens) {
		this.subTokens=subtokens;
	}
}