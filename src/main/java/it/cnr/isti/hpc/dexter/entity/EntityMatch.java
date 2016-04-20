/**
 * Copyright 2012 Diego Ceccarelli
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package it.cnr.isti.hpc.dexter.entity;

import fr.inria.wimmics.smilk.learntorank.Features;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;

import java.util.Comparator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EntityMatch contains the confidence score of an entity associated to a spot
 *
 * @author Diego Ceccarelli, diego.ceccarelli@isti.cnr.it created on 06/ago/2012
 */
public class EntityMatch implements Comparable<EntityMatch> {

    private static final Logger logger = LoggerFactory
            .getLogger(EntityMatch.class);

    /**
     * the entity matched
     */
    private Entity entity;

    /**
     * the spot where the entity was matched
     */
    private SpotMatch spot;

    /**
     * the confidence score of the match
     */
    private double score;

    /**
     * the context score of the match
     */
    private double contextScore;
    
    
    private double contextscore_tfidf;
    private double contextscore_ent2vec;
    private Set<String> neighbours_discoveryHub;
    
    private Features features;
    
   private double commenness;

    private EntityMatch(Entity e, double score) {
        super();
        this.entity = new Entity(e.getId(), e.getFrequency());
        this.score = score;
        if (Double.isNaN(score)) {
            logger.warn("score is NaN");
            this.score = 0;
        }
    }

    private EntityMatch(int id, double score) {
        super();
        this.entity = new Entity(id);
        this.score = score;
        if (Double.isNaN(score)) {
            logger.warn("score is NaN");
            this.score = 0;
        }
    }

    public EntityMatch(Entity e, double score, SpotMatch spot) {
        this(e, score);
        this.spot = spot;
    }

    public EntityMatch(Entity e, double score, double contextScore, SpotMatch spot) {
        this(e, score);
        this.spot = spot;
        this.setContextScore(contextScore);
    }

    public EntityMatch(int id, double score, SpotMatch spot) {
        this(id, score);
        this.spot = spot;
    }

    /**
     * @returns the commonness of the entity for the spot: the probability that
     * the target of the spot is this entity ( <code>p(entity|spot)</code>)
     */
    public double getCommonness() {
        return spot.getEntityCommonness(entity);
    }

     public void setCommonness(double commenness) {
        this.commenness=commenness;
    }
    @Override
    public int compareTo(EntityMatch em) {
        if (score > em.getScore()) {
            return -1;
        }
        if (score < em.getScore()) {
            return 1;
        }
        return 0;
    }

    public Entity getEntity() {
        return entity;
    }

    public int getId() {
        return entity.getId();
    }

    public double getScore() {
        return score;
    }
    //<fnoorala>

    public String getType() {
        if (spot.getNeTypes().size() > 0) {
            if (spot.getNeTypes().contains("ORGANIZATION")) {
                return "ORGANIZATION";
            } else if (spot.getNeTypes().contains("PERSON")) {
                return "PERSON";
            } else if (spot.getNeTypes().contains("LOCATION")) {
                return "LOCATION";
            } else {
                return spot.getNeTypes().get(0);
            }
        } else {
            return "MISC";
        }
    }

    public double getContextScore() {
        return contextScore;
    }

    public void setContextScore(double contextScore) {

        Double score=contextScore;
        if (score.isNaN()|| score.isInfinite()) {
            logger.warn("contextScore is NaN");
            this.contextScore = 0;
        }else
             this.contextScore = contextScore;
    }

    public double getContextScoreTFIDF() {
        return contextscore_tfidf;
    }

    public void setContextScoreTFIDF(double contextScore_tfidf) {

        Double score=contextScore_tfidf;
       
        if (score.isNaN() || score.isInfinite()) {
            logger.warn("contextScore TFIDF is NaN");
            this.contextscore_tfidf = 0;
        }else
             this.contextscore_tfidf = contextScore_tfidf;
    }
    
     public double getContextScoreEnt2Vec() {
        return contextscore_ent2vec;
    }

    public void setContextScoreEnt2Vec(double contextscore_ent2vec) {

         Double score=contextscore_ent2vec;
        if (score.isNaN() || score.isInfinite()) {
            logger.warn("contextScore ent2vec is NaN");
            this.contextscore_ent2vec = 0;
        }else
             this.contextscore_ent2vec = contextscore_ent2vec;
    }
    
    
    public Set<String> getNeighboursDiscovered() {
        return neighbours_discoveryHub;
    }

    public void setNeighboursDiscovered(Set<String> neighbours) {

         this.neighbours_discoveryHub=neighbours;
    }
    
    
    public Features getFeatures(){
        return features;
    }
    public void setFeatures(Features features){
        this.features= features;
    }
    
    //</fnoorala>
    public SpotMatch getSpot() {
        return spot;
    }

    public int getStart() {
        return spot.getStart();
    }

    public int getEnd() {
        return spot.getEnd();
    }

    public String getMention() {
        return spot.getMention();
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public static class SortByPosition implements Comparator<EntityMatch> {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(EntityMatch em, EntityMatch em1) {
            return em.getStart() - em1.getStart();
        }
    }

    
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entity == null) ? 0 : entity.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EntityMatch other = (EntityMatch) obj;
        if (entity == null) {
            if (other.entity != null) {
                return false;
            }
        } else if (!entity.equals(other.entity)) {
            return false;
        }
        return true;
    }

    public void setId(int id) {
        this.entity.setId(id);
    }

    public void setScore(double score) {

        this.score = score;
        if (Double.isNaN(score)) {
            logger.warn("score is NaN");
            this.score = 0;
        }
    }

    public void setSpot(SpotMatch spot) {
        this.spot = spot;
    }

    @Override
    public String toString() {
        String str = spot.getMention() + "[" + getStart() + "," + getEnd()
                + "]" + "\t score: " + score + "\t" + "prior:"
                + getCommonness() +"\t Type"+ spot.getNeTypes()+ "\n" + entity.toString() + "\n";
        return str;
    }

    public String toEntityString() {
        // String str = entity.getName() + "\t" + spot.getSpot() + "["
        // + spot.getStart() + "," + spot.getEnd() + "]" + "\t"
        // + entity.toString() + "\n";
        return "";
    }

    /**
     * @return the frequency of this entity, i.e., how many times this entity
     * was refernced by other entities.
     */
    public int getFrequency() {
        return entity.getFrequency();
    }

    /**
     * @return the spot link probability, i.e., the probability for the spot of
     * this entity to be a link to an entity.
     */
    public double getSpotLinkProbability() {
        return spot.getLinkProbability();
    }

    public static class SpotLengthComparator implements Comparator<EntityMatch> {

        @Override
        public int compare(EntityMatch em1, EntityMatch em2) {
            return em1.getSpot().getMention().length()
                    - em2.getSpot().getMention().length();
        }
    }

    /**
     * Returns true if this spot and the given spots overlaps in the annotated
     * text, e.g. <br>
     * <br>
     * <code> "neruda pablo picasso" </code>. <br>
     * <br>
     * the spots <code>neruda pablo</code> and <code>pablo picasso</code>
     * overlaps.
     *
     * @param s the entity match to check
     * @return true if this spot and the given spots overlaps, false otherwise
     */
    public boolean overlaps(EntityMatch s) {
        return getSpot().overlaps(s.getSpot());
        // boolean startOverlap = ((s.getStart() >= this.getStart()) && (s
        // .getStart() <= this.getEnd()));
        // if (startOverlap)
        // return true;
        // boolean endOverlap = ((s.getEnd() >= this.getStart()) && (s.getEnd()
        // <= this
        // .getEnd()));
        // return endOverlap;
    }
}
