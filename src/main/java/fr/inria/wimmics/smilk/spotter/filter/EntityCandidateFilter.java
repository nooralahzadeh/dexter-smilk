/**
 *  Copyright 2014 Diego Ceccarelli
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

package fr.inria.wimmics.smilk.spotter.filter;

import it.cnr.isti.hpc.dexter.spotter.filter.*;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Extract limited number of entities for each spot n=15 in literature
 * 
 * @author fnoorala
 */
public class EntityCandidateFilter implements SpotMatchFilter {

	private static final Logger logger = LoggerFactory
			.getLogger(EntityCandidateFilter.class);

	 
	private int max ;

	@Override
	public SpotMatchList filter(DexterLocalParams params, SpotMatchList sml) {

             //  init(null,params);
                
		SpotMatchList filtered = new SpotMatchList();
                
		for (SpotMatch spot : sml) {
                     
			EntityMatchList list = spot.getEntities();
                        
                        
                        for(EntityMatch e:list){
                            
                            e.setContextScore(e.getContextScoreEnt2Vec()+e.getContextScoreTFIDF());
                            
                        }
                     
                        //sort based on  context similarity
                      list.sortByConetexScore();
                       
                      EntityMatchList enitiies=new EntityMatchList();
                      
                      int threshold=list.size();
                      //  int threshold =max;
			if (list.size() > threshold) {
                            
                            for(int i=0;i < threshold;i++){
                                    enitiies.add(list.get(i));
                                }
                               
				SpotMatch s = new SpotMatch(spot,enitiies) ;
                                filtered.add(s);
                                
			} else {
				filtered.add(spot);
			}
                        
           

                }
                 
		return filtered;
	}

	@Override
	public void init(DexterParams dexterParams, DexterLocalParams initParams) {
		 
		if (initParams.containsKey("max-number-of-entities")) {
			max = initParams.getIntParam("max-number-of-entities");
			logger.info("max param set to {}", max);
		}
               
                

	}

    @Override
    public SpotMatchList filter(DexterLocalParams params, SpotMatchList eml, Document doc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
