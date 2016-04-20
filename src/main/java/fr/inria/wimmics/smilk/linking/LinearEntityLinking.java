package fr.inria.wimmics.smilk.linking;

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
import it.cnr.isti.hpc.text.MyPair;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinearEntityLinking implements Disambiguator {

    private static final Logger logger = LoggerFactory.getLogger(LinearEntityLinking.class);
    public static Stopwatch stopwatch = new Stopwatch();

    private int window;
    private double alpha;
    private double beta;
  

    private String similarity;
    private String relatedness;
    private RelatednessFactory globalrelatedness;
    private SimilarityFactory localSimilarity;
    Map<MyPair, Double> visited;

    @Override
    public EntityMatchList disambiguate(DexterLocalParams localParams, SpotMatchList sml) {

        EntityMatchList eml = new EntityMatchList();

        init(null, localParams);

        stopwatch.start("Linking");

         
        localSimilarity = new SimilarityFactory(similarity);
        this.visited = new HashMap<MyPair, Double>();
        
         if(sml.size()<2){
            
            for(SpotMatch s:sml){
                
                for(EntityMatch e :s.getEntities()){
                    double labelsim = localSimilarity.getLableSimScore(s, e);
                   
//                    double s1= this.alpha * (labelsim + e.getCommonness());
//                    double s2= (1 - this.alpha) * e.getContextScore();
                    double score=(this.alpha * (labelsim + e.getCommonness())) + ((1 - this.alpha) * e.getContextScore());
                   // double score=e.getCommonness();
                    
                    e.setScore(score);
                }   
               
                eml.addAll(s.getEntities());
            }
            
        } else {
         
         globalrelatedness=new RelatednessFactory(relatedness);
   
            for (int i = 0; i < sml.size(); i++) {

                SpotMatch s = sml.get(i);
               //  System.out.println(s.getMention());

                for (int j = 0; j < s.getEntities().size(); j++) {

                    EntityMatch e = s.getEntities().get(j);

            
                    double contextsim = e.getContextScore();
                    double labelsim = localSimilarity.getLableSimScore(s, e);
                    //double contextsim=localSimilarity.getContextSimScore(s, e);
                    double localsim = alpha *(labelsim+ e.getCommonness() ) + beta * contextsim;
                  //  System.out.println("\t"+ e.getEntity().getName() + "--->" + localsim);

                    //Measure Global similarity \psi (c_i,c_j) and construct the graph between (c_i,c_j)
                    double globalsim = 0;

                    int size = sml.size();

                    int windowCenter = i;
                    int ldelta = window / 2;
                    int rdelta = window / 2;

                    if (windowCenter < ldelta) {
                        rdelta += ldelta - windowCenter;
                        ldelta = windowCenter;
                    }

                    if (rdelta + windowCenter > size) {
                        ldelta += (rdelta + windowCenter) - size;
                        rdelta = size - windowCenter;
                    }

                    int start = Math.max(windowCenter - ldelta, 0);
                    int end = Math.min(windowCenter + rdelta, size);

                    // List<Integer> neighbors=new ArrayList<Integer>();
                    for (int x = start; x < end; x++) {

                        SpotMatch b = (SpotMatch) sml.get(x);

                        if (!b.equals(s)) {
                            for (EntityMatch neighbor : b.getEntities()) {

                                MyPair p = new MyPair(e.getEntity().getId(), neighbor.getEntity().getId());
                                double rel;
                                
                                if (!visited.containsKey(p)) {
                                   // System.out.println("\t"+ neighbor.getEntity().getName());
                                    rel = globalrelatedness.getScore(e.getEntity(), neighbor.getEntity());
                                    rel = (rel > 0) ? rel : 0;
                                   // System.out.println("\t\t"+rel );
                                    visited.put(p, rel);
                                    visited.put(new MyPair(neighbor.getEntity().getId(), e.getEntity().getId()), rel);

                                } else {
    
                                    rel = (Double) visited.get(p);
                                   

                                }
                                globalsim += rel;
                            }

                        }
                    }

                   // System.out.println("\t\t" +"  "+ globalsim);
                    double gamma=Math.max(0,1-(alpha+beta));
                   // double score = localsim + gamma * globalsim;
                    double score=globalsim;
                    
                    System.out.println(sml.get(i).getEntities().get(j).getEntity().getName()+ "|| "+ score);
                    EntityMatch entityMatch = new EntityMatch(sml.get(i).getEntities().get(j).getEntity(), score, sml.get(i));
                    
                    eml.add(entityMatch);
                }
            }

            
            
        }
       
        logger.info("Linking is performed in {} millis", stopwatch.stop("Linking"));
        eml.sort();
        

        return eml;
    }

    @Override
    public void init(DexterParams dexterParams,
            DexterLocalParams dexterModuleParams) {

        if (dexterModuleParams != null) {

            if (dexterModuleParams.containsKey("window-size")) {
                window = dexterModuleParams.getIntParam("window-size");
                logger.info("init, set window = {}", Integer.valueOf(window));
            }
             if(!dexterModuleParams.getSimilarity().isEmpty())
            {
                similarity = dexterModuleParams.getSimilarity();
                logger.info("init, local similarity function = {} ", similarity);
            }
            if (dexterModuleParams.containsKey("alpha")) {
                alpha = dexterModuleParams.getDoubleParam("alpha");
                logger.info("init, set alpha = {} ", Double.valueOf(alpha));
            }
            if (dexterModuleParams.containsKey("beta")) {
                beta = dexterModuleParams.getDoubleParam("beta");
                logger.info("init, set beta = {} ", Double.valueOf(beta));
            }

            if (!dexterModuleParams.getRelatedness().isEmpty()) {
                relatedness = dexterModuleParams.getRelatedness();
                logger.info("init, relatedness function = {} ", relatedness);
            }

        }

    }

}
