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
package it.cnr.isti.hpc.dexter.entity;

import it.cnr.isti.hpc.dexter.common.Field;
import it.cnr.isti.hpc.dexter.lucene.LuceneHelper;
import it.cnr.isti.hpc.dexter.spot.ContextExtractor;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.util.DexterParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EntityRanker assigns to the entities retrieved for a spot a score. The score
 * can be based on two different measure:
 * <ul>
 * <li>the similarity (tf-idf) between a text window around the spot and the
 * wikipedia article related to the entity, if in properties
 * <code>rank.by.similarity</code> is true;</li>
 * <li>the commonness score, i.e. the probability that the target of the spot is
 * this entity ( <code>p(entity|spot)</code>), if in properties
 * <code>rank.by.commonness</code> is true.
 * </ul>
 * 
 * The entity ranker also prune candidate entities with a score lower than the
 * value 'entity.commonness.threshold' defined in the project.properties.
 * 
 * <strong> WARNING: </strong> this class could be removed or radically modified
 * in the future
 * 
 * 
 * @author Diego Ceccarelli, diego.ceccarelli@isti.cnr.it created on 06/ago/2012
 */
public class EntityRanker {

     
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(EntityRanker.class);

	DexterParams params = DexterParams.getInstance();

	ContextExtractor context;
	 
	// FIXME move candidate entity ranker outside
	private final boolean RANK_BY_PRIOR = true;
       

	double commonnessThreshold = params.getThreshold("commonness");
       
	// private final int WINDOW_SIZE = properties.getInt("context.window.size");

	public EntityRanker() {

//            if (RANK_BY_SIMILARITY) {
//		  logger.info("(e|s) using cosine similarity");
//		  helper = LuceneHelper.getDexterLuceneHelper();
//			  }  else
//			logger.info("(e|s) NO cosine similarity");
                
		if (RANK_BY_PRIOR) {
			logger.info("(e|s) using prior probability / commonness");
		} else
			logger.info("(e|s) NO PROBABILITY");
		 
	}

	public EntityMatchList rank(SpotMatch spot) {
               // System.out.println(spot.getMention()+"---------");
		// EntityMatchList eml = filterEntitiesByPrior(spot);
		EntityMatchList eml = new EntityMatchList();
		EntityMatch match = null;
		// can't happen that prior and similarity are both true
		// assert (!RANK_BY_PRIOR || !RANK_BY_SIMILARITY);
                
		if (RANK_BY_PRIOR) {

			for (Entity e : spot.getSpot().getEntities()) {
                         //   System.out.println(e.getFrequency()+" "+e.getName()+" "+spot.getEntityCommonness(e));
				if (spot.getEntityCommonness(e) < commonnessThreshold) {
					logger.debug("filtering entity {}, low commonness ",
							e.getId());
					continue;
				}
				match = new EntityMatch(e, 0, spot);
                                match.setCommonness(spot.getEntityCommonness(e));
				eml.add(match);
			}

		}
                
	
		return eml;
	}

}
