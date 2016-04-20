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

import approxmatch.ApproximateMatcher;
import approxmatch.ApproximateMatcher.Match;
import fr.inria.wimmics.smilk.localsimilarity.LevenshteinDistance;
import fr.inria.wimmics.smilk.localsimilarity.Similarity;
import fr.inria.wimmics.smilk.localsimilarity.SimilarityMetrics;
import fr.inria.wimmics.smilk.nerd.Coreference;
import fr.inria.wimmics.smilk.nerd.HolmesNerd;
import fr.inria.wimmics.smilk.nerd.InriaNerd;
import fr.inria.wimmics.smilk.nerd.NameEntityRecognizer;
import fr.inria.wimmics.smilk.nerd.OpenNLPNerd;
import fr.inria.wimmics.smilk.nerd.RencoNerd;
import fr.inria.wimmics.smilk.nerd.StanfordCoref;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.common.Field;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.entity.EntityRanker;
import it.cnr.isti.hpc.dexter.label.IdHelper;
import it.cnr.isti.hpc.dexter.label.IdHelperFactory;
import it.cnr.isti.hpc.dexter.lucene.LuceneHelper;
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
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.structure.LRUCache;
import it.cnr.isti.hpc.text.Token;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spotter
 *
 *
 */
public class NameEntitySpotterWithoutShingler extends AbstractSpotter implements Spotter {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(NameEntitySpotterWithoutShingler.class);

    private static LRUCache<String, Spot> cache;
    int cachesize;

    DexterParams params = DexterParams.getInstance();
    SpotRepository spotRepo;

    int WINDOW_SIZE;

    public ContextExtractor context;

    private final SpotManager sm;

    List<Token> CleanTokens;
    String tools;

    NameEntityRecognizer ne;
    ApproximateMatcher apm;
    EntityRanker er;
    private static final LuceneHelper lucene = LuceneHelper.getDexterLuceneHelper();
    private static IdHelper helper = IdHelperFactory.getStdIdHelper();

    SimilarityMetrics match;

    public NameEntitySpotterWithoutShingler() {

        int cachesize = params.getCacheSize("spotter");
        cache = new LRUCache<String, Spot>(cachesize);
        SpotRepositoryFactory factory = new SpotRepositoryFactory();
        spotRepo = factory.getStdInstance();
        sm = SpotManager.getStandardSpotManager();

        apm = new ApproximateMatcher();
        apm.setIndexFile(params.getDefaultModel().getPath() + "/fmindex_fmi");
        apm.setMinScore(0.95f);
        er = new EntityRanker();
        match = new SimilarityMetrics();

    }

    @Override
    public SpotMatchList match(DexterLocalParams localParams, Document document) {

        init(null, localParams);
        switch (tools) {
            case "inria":
                ne = new InriaNerd();
                break;
            case "openNLP":
                ne = new OpenNLPNerd();
                break;

            case "renco":
                ne = new RencoNerd();
                break;

            case "holmes":
                ne = new HolmesNerd();
                break;
                
            case "stanford":
                ne = new StanfordCoref();
                break;
        }

        SpotMatchList matches = new SpotMatchList();
        CleanTokens = new LinkedList<Token>();

        try {
       
            Document newDocument = document;
            if(!tools.equalsIgnoreCase("stanford")){
                        
            Coreference coref = new Coreference();
             newDocument = coref.replacement(document);
            
            }
            if (newDocument == null) {
                newDocument = document;
            }
           
            context = new ContextExtractor(newDocument.getContent(), true);

            for (Token t : ne.nerd(newDocument)) {
                String cleanToken = sm.clean(t.getText());
                if (cleanToken.isEmpty()) {
                    continue;
                }

                t.setText(cleanToken);

                CleanTokens.add(t);
               
            }

             System.out.println(CleanTokens);
            for (Token t : CleanTokens) {

                
                Field field = new Field(t.getText(), t.getText());

                Spot s = null;
                String text = t.getText().toLowerCase();

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

                    logger.debug("no resource found in repository for [{}], Looking for partial match ", t.getText());

                    List<Integer> filters = new ArrayList<Integer>();

                    List<Integer> wikiids_title = lucene.query(text, "infobox", 20);

                    if (wikiids_title.size() == 0) {
                        wikiids_title = lucene.query(text, "emph", 20);
                    }

                    if (wikiids_title.size() > 0) {

                        //Fix me check if the extracted entity has any mactch with the spot as named entity ; if there is no ant match words, we can see how the
                        //entities are represented i dbpedia
                        // and extract the resource which has match phrace with the spot 
                        // and make it as entity insteas the others (check jarowinkler distance)
                        for (int id : wikiids_title) {
                            String entity_name = helper.getLabel(id);
                            System.out.println(text+"|"+ entity_name+"|"+ match.score(text, entity_name));
                            if (match.score(text, entity_name) > 0.5) {
                                filters.add(id);
                            }

                        }

                        if (filters.size() > 0) {

                            List<Entity> entities = new ArrayList<Entity>();

                            for (int idx : filters) {
                                entities.add(new Entity(idx));
                            }

                            s = new Spot(text, entities, 1, 1);

                        } else {

                            logger.debug("no resource found in repository for [{}], Looking for approximate match ", t.getText());

                            Match[] mch = apm.getMatches(text);
                            
                            //extract just 5  most approximate match
                            int max = (mch.length > 5) ? 5: mch.length;
                            for (int idx = 0; idx < max; idx++) {
                             s = spotRepo.getSpot(mch[idx].str);
                            }
                        }
                    }
                }

                if (s == null) {
                    s = new Spot(text);
                }

                SpotMatch match = new SpotMatch(s, field);
                logger.debug("adding {} to matchset ", s);

                match.setStart(t.getStart());
                match.setEnd(t.getEnd());

                //context
                context.setTokSen_StartEnd(t);

                match.setStartSE(t.getStartSE());
                match.setEndSE(t.getEndSE());

                //extract context around the spot 
                context.setWindowSize(WINDOW_SIZE);
                String spotContext = context.getCntxtSentence(match);

                match.setContext(spotContext);
                EntityMatchList entities = new EntityMatchList();

                if (match.getSpot().getEntities() != null) {
                    entities = er.rank(match);
                }

                match.setEntities(entities);

                matches.add(match);
            }

            matches = filter(localParams, matches);

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(NameEntitySpotterWithoutShingler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return matches;
    }

    @Override
    public void init(DexterParams dexterParams,
            DexterLocalParams dexterModuleParams) {

        if (dexterModuleParams != null) {
            if (dexterModuleParams.containsKey("context-window-size")) {
                WINDOW_SIZE = dexterModuleParams.getIntParam("context-window-size");
                logger.info("init, set context-window-size = {}", Integer.valueOf(WINDOW_SIZE));
            }

            if (dexterModuleParams.containsKey("tools")) {
                tools = dexterModuleParams.getParam("tools");
                logger.info("init, set named entity recognition to  = {}", tools);
            }

        }

    }

}
