package fr.inria.wimmics.smilk.linking;




import fr.inria.wimmics.smilk.localsimilarity.Similarity;
import fr.inria.wimmics.smilk.localsimilarity.SimilarityFactory;
import it.cnr.isti.hpc.benchmark.Stopwatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.dexter.disambiguation.Disambiguator;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.relatedness.RelatednessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 


public class CollectiveEntityLinking implements Disambiguator {
  
    private static final Logger logger = LoggerFactory.getLogger(CollectiveEntityLinking.class);
    public static Stopwatch stopwatch = new Stopwatch();
    private double epsilon;
    private int max_iteration;
    private int window;
    private double alpha;
    private double beta;
    private double  coherence;
    private double lamdba;
    private String similarity;
    private String relatedness;
    private   RelatednessFactory globalrelatedness;
    private   SimilarityFactory localSimilarity;
  
   

    @Override
    public EntityMatchList disambiguate(DexterLocalParams localParams, SpotMatchList sml) {
         EntityMatchList eml = new EntityMatchList();
       
         init(null, localParams);
         
         stopwatch.start("GraphConstruction");
        
         localSimilarity=new SimilarityFactory(similarity);

 
     
         
      
         //ContextGraph cntMatrix=new ContextGraph(sml,window,localSimilarity,globalrelatedness,alpha);
         logger.info("Graph Construction is performed in {} millis",
                stopwatch.stop("GraphConstruction"));
         
         stopwatch.start("PageRank");
        //   cntMatrix.PageRankJung(max_iteration, epsilon, lamdba);
        
        if(sml.size()<2){
            
            for(SpotMatch s:sml){
                
                for(EntityMatch e :s.getEntities()){
                    double labelsim = localSimilarity.getLableSimScore(s, e);
                   
                    double s1= this.alpha * (labelsim + e.getCommonness());
                     double s2= (1 - this.alpha) * e.getContextScore();
                    double score=(this.alpha * (labelsim * e.getCommonness())) * ((1 - this.alpha) * e.getContextScore());
                  
                    System.out.println(e.getEntity().getName() +"| "+ s1 +"| "+s2+"| "+score);
                   // double score=e.getCommonness();
                    
                    e.setScore(score);
                }   
               
                eml.addAll(s.getEntities());
            }
            
        } else{
         
         globalrelatedness=new RelatednessFactory(relatedness);
         ContextMatrix cntMatrix=new ContextMatrix(sml,window,localSimilarity,globalrelatedness,alpha,coherence);
          //ContextGraph cntMatrix=new ContextGraph(sml,window,localSimilarity,globalrelatedness,alpha);       
         eml.addAll(cntMatrix.pageRankLike(max_iteration, epsilon, lamdba));
         logger.info("PageRank  is performed in {} millis",
                stopwatch.stop("PageRank"));
        }
         eml.sort();
         
         
         return eml;
    }

    @Override
    public void init(DexterParams dexterParams,
            DexterLocalParams dexterModuleParams) {
       
          if(dexterModuleParams != null)
        {
            if(dexterModuleParams.containsKey("max-iteration"))
            {
                max_iteration = dexterModuleParams.getIntParam("max-iteration");
                logger.info("init, set max-iteration = {}", Integer.valueOf(max_iteration));
            }
             if(dexterModuleParams.containsKey("window-size"))
            {
                window = dexterModuleParams.getIntParam("window-size");
                logger.info("init, set window = {}", Integer.valueOf(window));
            }
            if(dexterModuleParams.containsKey("epsilon"))
            {
                epsilon = dexterModuleParams.getDoubleParam("epsilon");
                logger.info("init, set epsilon = {} ", Double.valueOf(epsilon));
            }
            if(dexterModuleParams.containsKey("lambda"))
            {
                lamdba = dexterModuleParams.getDoubleParam("lambda");
                logger.info("init, set lambda = {} ", Double.valueOf(lamdba));
            }
            if(!dexterModuleParams.getSimilarity().isEmpty())
            {
                similarity = dexterModuleParams.getSimilarity();
                logger.info("init, local similarity function = {} ", similarity);
            }
             if(dexterModuleParams.containsKey("alpha"))
            {
                alpha = dexterModuleParams.getDoubleParam("alpha");
                logger.info("init, set local similarity coefficient alpha= {}", Double.valueOf(alpha));
            }
               if(dexterModuleParams.containsKey("beta"))
            {
                beta = dexterModuleParams.getDoubleParam("beta");
                logger.info("init, set local similarity coefficient beta = {}", Double.valueOf(beta));
            }
             
              if(dexterModuleParams.containsKey("coherence"))
            {
                coherence = dexterModuleParams.getDoubleParam("coherence");
                logger.info("init, set coherence threshold= {}", Double.valueOf(coherence));
            }
            if(!dexterModuleParams.getRelatedness().isEmpty())
            {
                relatedness = dexterModuleParams.getRelatedness();
                logger.info("init, relatedness function = {} ", relatedness);
            }
         
        }

    }

}
