
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
package fr.inria.wimmics.smilk.spotter;

import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.common.Field;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.entity.EntityRanker;
import it.cnr.isti.hpc.dexter.shingle.Shingle;
import it.cnr.isti.hpc.dexter.shingle.ShingleExtractor;
import it.cnr.isti.hpc.dexter.spot.ContextExtractor;
import it.cnr.isti.hpc.dexter.spot.Spot;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.HtmlCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.LowerCaseCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.ParenthesesCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.QuotesCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.StripCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.SymbolCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.UnicodeCleaner;
import it.cnr.isti.hpc.dexter.spot.repo.SpotRepository;
import it.cnr.isti.hpc.dexter.spot.repo.SpotRepositoryFactory;
import it.cnr.isti.hpc.dexter.spotter.AbstractSpotter;
import it.cnr.isti.hpc.dexter.spotter.Spotter;
import it.cnr.isti.hpc.dexter.spotter.filter.SpotOverlapFilter;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.structure.LRUCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.Iterator;
import java.util.List;
import org.apache.taglibs.standard.tag.common.core.ParamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spotter
 *
 * 
 */
public class DictionarySpotter extends AbstractSpotter implements Spotter {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(DictionarySpotter.class);

    private static LRUCache<String, Spot> cache;

    DexterParams params = DexterParams.getInstance();
    

    SpotRepository spotRepo;
      private final SpotManager sm;
   
    int WINDOW_SIZE;
    
    public ContextExtractor context;
    private static EntityRanker er;

    public DictionarySpotter() {
       
        int cachesize = params.getCacheSize("spotter");
        cache = new LRUCache<String, Spot>(cachesize);
        SpotRepositoryFactory factory = new SpotRepositoryFactory();
        spotRepo = factory.getStdInstance();
        sm = SpotManager.getStandardSpotManager();
       
        er= new EntityRanker();
		 
    }

    @Override
    public SpotMatchList match(DexterLocalParams localParams, Document document) {

        SpotMatchList matches = new SpotMatchList();

        context = new ContextExtractor(document.getContent(),true);
	
        
        Iterator<Field> fields = document.getFields();
        while (fields.hasNext()) {
            Field field = fields.next();
           
      
            //<fnoorala>
            ShingleExtractor shingler = new ShingleExtractor(field.getValue(), true);
            //</fnoorala>
            Spot s = null;
            String text;
          
            for (Shingle shingle : shingler) {
                logger.debug("SHINGLE: [{}] ", shingle);
                text = sm.clean(shingle.getText());
                 
                if (cache.containsKey(text)) {
                    // hit in cache
                    s = cache.get(text);
                    if (s != null) {
                        s = s.clone();
                    }
                } else {
                    s = spotRepo.getSpot(text);

                    cache.put(text, s);
                }

                if (s == null) {
                    logger.debug("no shingle for [{}] ", shingle);
                    continue;
                }
                
               
                
                SpotMatch match = new SpotMatch(s, field);
                logger.debug("adding {} to matchset ", s);
                
                
                
                match.setStart(shingle.getStart());
                match.setEnd(shingle.getEnd()); 
                
                match.setStartSE(shingle.getStartSE());
                match.setEndSE(shingle.getEndSE());
               
                //extract context around the spot 
                context.setWindowSize(WINDOW_SIZE);
                String spotContext=context.getCntxtSentence(match);
                match.setContext(spotContext);
                match.setPos(shingle.getPos());
               
                EntityMatchList entities=new EntityMatchList();
               
                 EntityMatch entitymatch = null;
                    if (match.getSpot().getEntities()!=null) {
                        for (Entity e : match.getSpot().getEntities()) {
                            entitymatch = new EntityMatch(e, 0, match);
                            Double commenness=s.getEntityCommonness(e);
                            commenness=(commenness.isInfinite() || commenness.isNaN())? 0 : commenness;
                            entitymatch.setCommonness(commenness);
                            entities.add(entitymatch);                        
                        }
                    }
                
                match.setEntities(entities);
                
                matches.add(match);

            }
        }
 
 
      matches = filter(localParams, matches);  
//      
//       for(SpotMatch sp: matches){
//            System.out.println("-----------------After-------------------------------");
//            System.out.println(sp.getEntities().size());
//            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
//////                for(EntityMatch ll: sp.getEntities()){
////             System.out.println(ll.getEntity().getName()+"/ "+ ll.getFrequency()+ "/"+ll.getCommonness()+"/"+ll.getSpotLinkProbability()+"/"+ll.getContextScore());
////                
//            }
////        }
      
      return matches;
    }

   
    
    @Override
    public void init(DexterParams dexterParams,
            DexterLocalParams dexterModuleParams) {
        
       
    }

    

}
