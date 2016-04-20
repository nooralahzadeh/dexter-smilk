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
import it.cnr.isti.hpc.dexter.relatedness.RelatednessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 


public class NaiveLinking implements Disambiguator {
  
    private static final Logger logger = LoggerFactory.getLogger(NaiveLinking.class);
    public static Stopwatch stopwatch = new Stopwatch();
    
    private int window;
    private double alpha;
    private double beta;
    private double gamma;
    
    private String relatedness;
    private   RelatednessFactory globalrelatedness;
    
  
   

    @Override
    public EntityMatchList disambiguate(DexterLocalParams localParams, SpotMatchList sml) {
         EntityMatchList eml = new EntityMatchList();
         init(null, localParams);
         
         stopwatch.start("GraphConstruction");
         globalrelatedness=new RelatednessFactory(relatedness);
       

 
         NaiveContextMatrix cntMatrix=new NaiveContextMatrix(sml,window,alpha,beta,gamma, globalrelatedness );
       
         logger.info("Graph Construction is performed in {} millis",
                stopwatch.stop("GraphConstruction"));
         
         stopwatch.start("PageRank");
           eml.addAll(cntMatrix.Rank(  ));
         
         logger.info("PageRank  is performed in {} millis",
                stopwatch.stop("PageRank"));
         eml.sort();
         
         
         return eml;
    }

    @Override
    public void init(DexterParams dexterParams,
            DexterLocalParams dexterModuleParams) {
       
          if(dexterModuleParams != null)
        {
             
             if(dexterModuleParams.containsKey("window-size"))
            {
                window = dexterModuleParams.getIntParam("window-size");
                logger.info("init, set window = {}", Integer.valueOf(window));
            }
          
             if(dexterModuleParams.containsKey("alpha"))
            {
                alpha = dexterModuleParams.getDoubleParam("alpha");
                logger.info("init, set alpha = {} ", Double.valueOf(alpha));
            }
              if(dexterModuleParams.containsKey("beta"))
            {
                beta = dexterModuleParams.getDoubleParam("beta");
                logger.info("init, set beta = {} ", Double.valueOf(beta));
            }
             
              if(dexterModuleParams.containsKey("gamma"))
            {
                gamma = dexterModuleParams.getDoubleParam("gamma");
                logger.info("init, set gamma = {} ", Double.valueOf(gamma));
            }
          
            if(!dexterModuleParams.getRelatedness().isEmpty())
            {
                relatedness = dexterModuleParams.getRelatedness();
                logger.info("init, relatedness function = {} ", relatedness);
            }
         
        }

    }

}
