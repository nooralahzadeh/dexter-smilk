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
import fr.inria.wimmics.smilk.nerd.ExtractedMentions;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spotter
 *
 *
 */
public class ExtractedMentionCanGenSpaqrl extends AbstractSpotter implements Spotter {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(ExtractedMentionCanGenSpaqrl.class);

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
  //  private static final LuceneHelper lucene = LuceneHelper.getDexterLuceneHelper();
    private static IdHelper helper = IdHelperFactory.getStdIdHelper();

    SimilarityMetrics match;
    CandidateGen candidateGen;
    String uri;
LuceneHelper lucene ;
     ExtractedMentions okemention;
    public ExtractedMentionCanGenSpaqrl() {

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

        candidateGen = new CandidateGen(params.getSparqlEndPointPath(), params.getDefaulyModelLang());
        uri = params.getSparqlEndPointPath().replaceAll("sparql", "") + "resource";
        okemention=new ExtractedMentions(params.getDefaultModel().getPath(),"KORE");
        
         ne= new StanfordWoutCoref();

    }

    @Override
    public SpotMatchList match(DexterLocalParams localParams, Document document) {

        

        SpotMatchList matches = new SpotMatchList();
        CleanTokens = new LinkedList<Token>();

        try {

          String cntx = "";
             ne.nerd(document);
            for (Token t : ne.getContext()) {

                if (params.INTERESTING_TAGS_Context.contains(t.getPos().toUpperCase())) {
                    cntx += t.getText() + " ";
                }
            }

            cntx.trim();

               
        context = new ContextExtractor(cntx, true);

           List<Token> nerdTokens=okemention.extract(document.getContent());
           
            System.out.println(nerdTokens);

            for (Token t : nerdTokens) {

               
                String text = t.getText();
                
                Spot s=null;
            
                s=spotRepo.getSpot(sm.clean(text));
                
                if(s==null){
                s = new Spot(text);
                }
                
                
                Set<String> entities_sparql = candidateGen.canndidateGeneration(text);
                Set<Entity> entities = new HashSet<Entity>();
                EntityMatchList entities_matchList = new EntityMatchList();
               
                Field field = new Field(t.getText(), t.getText());
                
               

                SpotMatch match = new SpotMatch(s, field);
                logger.debug("adding {} to matchset ", s);

                match.setStart(t.getStart());
                match.setEnd(t.getEnd());

                //context
               
                context.setTokSen_StartEnd(t);
                
                match.setStartSE(t.getStartSE());
                match.setEndSE(t.getEndSE());
                
                match.setProcessFiEntity("");
                List<String> subNeTypes = new ArrayList<String>();
                List<Token> subtokens = new ArrayList<Token>();
  
               if (t.getSubTokens() != null) {
                        for (Token subtoken : t.getNESubTokens()) {
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
                spotContext= spotContext.toLowerCase().replaceAll("\\b"+t.getText().toLowerCase()+"\\b", "");
                
                match.setContext(spotContext);
                
               
                List<Integer> douplicates=new ArrayList<Integer>();
                for (String candidate : entities_sparql) {
                    int id = helper.getId(candidate.replaceAll(uri + "/", ""));
                    
                    if (id > 0) {
                        
                        if(!douplicates.contains(id)){
                         
                                              
                        EntityMatch en=new EntityMatch(id, 0,match );
                           // System.out.println(candidate +"  "+ en.getEntity().getName());
                         en.setCommonness(0);
                                
                            
                         // becasue of filter in candidate frequency filter
                      
                    
                     
                        entities_matchList.add(en);
                        douplicates.add(id);
                        }else{
                            continue;
                        }
                    }
                }
                
                match.setEntities(entities_matchList);
                matches.add(match);
            }

           // System.out.println(matches);
            matches = filter(localParams, matches);

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ExtractedMentionCanGenSpaqrl.class.getName()).log(Level.SEVERE, null, ex);
        }
         System.out.println(matches);
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
