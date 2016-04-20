/**
 *
 *
 *
 */
package fr.inria.wimmics.smilk.spotter;

import approxmatch.ApproximateMatcher;
import approxmatch.ApproximateMatcher.Match;
import fr.inria.wimmics.smilk.localsimilarity.SimilarityMetrics;
import fr.inria.wimmics.smilk.nerd.Coreference;
import fr.inria.wimmics.smilk.nerd.HolmesNerd;
import fr.inria.wimmics.smilk.nerd.InriaNerd;
import fr.inria.wimmics.smilk.nerd.NameEntityRecognizer;
import fr.inria.wimmics.smilk.nerd.OpenNLPNerd;
import fr.inria.wimmics.smilk.nerd.RencoNerd;
import fr.inria.wimmics.smilk.nerd.StanfordWthCoref;
import fr.inria.wimmics.smilk.nerd.StanfordCoref;
import fr.inria.wimmics.smilk.nerd.StanfordWoutCoref;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.common.Field;
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.entity.EntityRanker;
import it.cnr.isti.hpc.dexter.label.IdHelper;
import it.cnr.isti.hpc.dexter.label.IdHelperFactory;
import it.cnr.isti.hpc.dexter.lucene.LuceneHelper;
import it.cnr.isti.hpc.dexter.shingle.MyShingleExtractor;
import it.cnr.isti.hpc.dexter.shingle.Shingle;
import it.cnr.isti.hpc.dexter.spot.ContextExtractor;
import it.cnr.isti.hpc.dexter.spot.Spot;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.dexter.spot.repo.SpotRepository;
import it.cnr.isti.hpc.dexter.spot.repo.SpotRepositoryFactory;
import it.cnr.isti.hpc.dexter.spotter.AbstractSpotter;
import it.cnr.isti.hpc.dexter.spotter.Spotter;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.structure.LRUCache;
import it.cnr.isti.hpc.text.Token;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spotter
 *
 *
 */
public class NameEntity_MatchingSpotter extends AbstractSpotter implements Spotter {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(NameEntity_MatchingSpotter.class);

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

    public NameEntity_MatchingSpotter() {

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

            case "stanfordcoref":
                ne = new StanfordCoref();
                break;

            case "stanfordwithcoref":
                ne = new StanfordWthCoref();
                break;

            case "stanfordwithoutcoref":
                ne = new StanfordWoutCoref();
                break;
        }

        SpotMatchList matches = new SpotMatchList();
        CleanTokens = new LinkedList<Token>();

        try {

            List<Token> nerdTokens = ne.nerd(document);

            String cntx = "";

            for (Token t : ne.getContext()) {

                if (params.INTERESTING_TAGS_Context.contains(t.getPos().toUpperCase())) {
                    cntx += t.getText() + " ";
                }
            }

            cntx.trim();

            // ask ne to interested tokens for context!!!
            context = new ContextExtractor(cntx, true);

            for (Token t : nerdTokens) {
                String cleanToken = sm.clean(t.getText());
                if (cleanToken.isEmpty()) {
                    continue;
                }

                t.setText(cleanToken);

                CleanTokens.add(t);

            }

            System.out.println(CleanTokens);

            for (Token t : CleanTokens) {

                Spot s = null;
                String text;

                Field field = new Field(t.getText(), t.getText());

                     
                String founder = "";

 
                text = sm.clean(t.getText());

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

                    if (wikiids_title.isEmpty()) {
                        wikiids_title = lucene.query(text, "emph", 20);
                    }

                    if (wikiids_title.size() > 0) {

                            //Fix me check if the extracted entity has any mactch with the spot as named entity ; if there is no ant match words, we can see how the
                        //entities are represented i dbpedia
                        // and extract the resource which has match phrace with the spot 
                        // and make it as entity insteas the others (check jarowinkler distance)
                        for (int id : wikiids_title) {
                            String entity_name = helper.getLabel(id);
                            //System.out.println(text + "|" + entity_name + "|" + match.score(text, entity_name));
                            if (match.score(text, entity_name) > 0.5) {
                                filters.add(id);
                            }

                        }
                    }
                    if (filters.size() > 0) {
                        founder = "sim";
                        List<Entity> entities = new ArrayList<Entity>();

                        for (int idx : filters) {
                            entities.add(new Entity(idx));
                        }

                        s = new Spot(text, entities, 1, 1);

                    } else {

                        logger.debug("no resource found in repository for [{}], Looking for approximate match ", t.getText());

                        Match[] mch = apm.getMatches(text);

                            //extract just 5  most approximate match
                        //Fix me
                        int max = (mch.length > 5) ? 5 : mch.length;
                        List<Entity> entities = new ArrayList<Entity>();

                        for (int idx = 0; idx < max; idx++) {
                            Spot temps = spotRepo.getSpot(mch[idx].str);
                            entities.addAll(temps.getEntities());
                        }

                        if (!entities.isEmpty()) {
                            s = new Spot(text, entities, 1, 1);
                            founder = "approx";
                        }

                    }

                } else {
                    founder = "repo";
                }

                if (s != null) {
                    SpotMatch match=constructSpotMatchList(s, t, founder);
                  
                    EntityMatchList entities = new EntityMatchList();

                    if (match.getSpot().getEntities().size() > 0) {
                        entities = er.rank(match);
                    }
 
                    match.setEntities(entities);
                    matches.add(match);

                } else {
                    
                    if (t.getSubTokens().size() > 1) {
                        matches.addAll(extractSpotMatch(t));
                    }
                    else{
                       s = new Spot(text);
                       founder = "";
                       SpotMatch match=constructSpotMatchList(s, t, founder);
                        EntityMatchList entities = new EntityMatchList();
                        match.setEntities(entities);
                       matches.add(match);
                    }
                 }
            }
           
            matches = filter(localParams, matches);

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(NameEntity_MatchingSpotter.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(matches);
        return matches;
    }

    public SpotMatchList extractSpotMatch(Token t) {

        SpotMatchList matches = new SpotMatchList();

        Spot s = null;
        String text;

        MyShingleExtractor shingler = new MyShingleExtractor(t.getSubTokens());

        for (Shingle shingle : shingler) {
            String founder = "";

            int len=shingle.getTypes().size();
        if (!(params.INTERESTING_TAGS_NE.contains(shingle.getPosTags().get(0)) && params.INTERESTING_TAGS_NE.contains(shingle.getPosTags().get(len-1)))) {
                         continue;
                    }
            //System.out.println(shingle.subTokens +" "+ shingle.getPosTags());
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

                logger.debug("no resource found in repository for [{}], Looking for partial match ", t.getText());

                List<Integer> filters = new ArrayList<Integer>();

                List<Integer> wikiids_title = lucene.query(text, "infobox", 20);

                if (wikiids_title.isEmpty()) {
                    wikiids_title = lucene.query(text, "emph", 20);
                }

                if (wikiids_title.size() > 0) {

                            //Fix me check if the extracted entity has any mactch with the spot as named entity ; if there is no ant match words, we can see how the
                    //entities are represented i dbpedia
                    // and extract the resource which has match phrace with the spot 
                    // and make it as entity insteas the others (check jarowinkler distance)
                    for (int id : wikiids_title) {
                        String entity_name = helper.getLabel(id);
                        //System.out.println(text + "|" + entity_name + "|" + match.score(text, entity_name));
                        if (match.score(text, entity_name) > 0.5) {
                            filters.add(id);
                        }

                    }
                }
                if (filters.size() > 0) {
                    founder = "sim";
                    List<Entity> entities = new ArrayList<Entity>();

                    for (int idx : filters) {
                        entities.add(new Entity(idx));
                    }

                    s = new Spot(text, entities, 1, 1);

                } else {

                    logger.debug("no resource found in repository for [{}], Looking for approximate match ", t.getText());

                    Match[] mch = apm.getMatches(text);

                            //extract just 5  most approximate match
                    //Fix me
                    int max = (mch.length > 5) ? 5 : mch.length;
                    List<Entity> entities = new ArrayList<Entity>();

                    for (int idx = 0; idx < max; idx++) {
                        Spot temps = spotRepo.getSpot(mch[idx].str);
                        entities.addAll(temps.getEntities());
                    }

                    if (!entities.isEmpty()) {
                        s = new Spot(text, entities, 1, 1);
                        founder = "approx";
                    }

                }

            } else {
                founder = "repo";
            }

            if (s !=null){
                
            SpotMatch match=constructSpotMatchList(s, shingle, founder);
            EntityMatchList entities = new EntityMatchList();
            if (match.getSpot().getEntities().size() > 0) {
                        entities = er.rank(match);
                    }
            match.setEntities(entities);
            matches.add(match);
             }
        }
       

        return matches;
    }
    
    
    public SpotMatch  constructSpotMatchList(Spot s, Token t , String founder){
                   
        
                    SpotMatch match = new SpotMatch(s);
                    logger.debug("adding {} to matchset ", s);

                    match.setStart(t.getStart());
                    match.setEnd(t.getEnd());

                    //context
                    if (!tools.equalsIgnoreCase("stanfordwithcoref") && !tools.equalsIgnoreCase("stanfordwithoutcoref")) {
                        context.setTokSen_StartEnd(t);
                    }
                    match.setStartSE(t.getStartSE());
                    match.setEndSE(t.getEndSE());
                    match.setProcessFiEntity(founder);
                    List<String> subNeTypes = new ArrayList<String>();
                    List<Token> subtokens = new ArrayList<Token>();

                    if (t.getSubTokens() != null) {
                        for (Token subtoken : t.getSubTokens()) {
                            subNeTypes.add(subtoken.getType());
                            subtokens.add(subtoken);
                        }
                        match.setSubTokens(subtokens);
                    }

                    if (subNeTypes.size() > 0) {
                        match.setNeTypes(subNeTypes);
                    }

                    List<String> subPosTags = new ArrayList<String>();

                    if (t.getSubTokens() != null) {
                        for (Token subtoken : t.getSubTokens()) {
                            subPosTags.add(subtoken.getPos());
                        }
                    }

                    if (subPosTags.size() > 0) {
                        match.setPos(subPosTags);
                    }

                    //extract context around the spot 
                    context.setWindowSize(WINDOW_SIZE);
                    String spotContext = context.getCntxtSentence(match);

                    spotContext = spotContext.replaceAll("\\b" + t.getText() + "\\b", "");
                    match.setContext(spotContext);
                    return match;
                    
    }
    public SpotMatch  constructSpotMatchList(Spot s, Shingle t , String founder){
                   
        
                    SpotMatch match = new SpotMatch(s);
                    logger.debug("adding {} to matchset ", s);

                    match.setStart(t.getStart());
                    match.setEnd(t.getEnd());

                    //context
                    if (!tools.equalsIgnoreCase("stanfordwithcoref") && !tools.equalsIgnoreCase("stanfordwithoutcoref")) {
                        context.setTokSen_StartEnd(t);
                    }
                    match.setStartSE(t.getStartSE());
                    match.setEndSE(t.getEndSE());
                    match.setProcessFiEntity(founder);
                    List<String> subNeTypes = new ArrayList<String>();
                    List<Token> subtokens = new ArrayList<Token>();

                    if (t.getSubTokens() != null) {
                        for (Token subtoken : t.getSubTokens()) {
                            subNeTypes.add(subtoken.getType());
                            subtokens.add(subtoken);
                        }
                        match.setSubTokens(subtokens);
                    }

                    if (subNeTypes.size() > 0) {
                        match.setNeTypes(subNeTypes);
                    }

                    List<String> subPosTags = new ArrayList<String>();

                    if (t.getSubTokens() != null) {
                        for (Token subtoken : t.getSubTokens()) {
                            subPosTags.add(subtoken.getPos());
                        }
                    }

                    if (subPosTags.size() > 0) {
                        match.setPos(subPosTags);
                    }

                    //extract context around the spot 
                    context.setWindowSize(WINDOW_SIZE);
                    String spotContext = context.getCntxtSentence(match);

                    spotContext = spotContext.replaceAll("\\b" + t.getText() + "\\b", "");
                    match.setContext(spotContext);
                    return match;
                    
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