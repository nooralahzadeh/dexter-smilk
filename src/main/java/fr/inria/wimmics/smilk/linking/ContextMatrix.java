/*
 * Copyright 2015 fnoorala.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.inria.wimmics.smilk.linking;

/**
 *
 * @author fnoorala
 */
import cern.colt.function.DoubleDoubleFunction;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.uci.ics.jung.algorithms.matrix.GraphMatrixOperations;
import edu.uci.ics.jung.graph.SparseGraph;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.relatedness.RelatednessFactory;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import org.apache.commons.collections15.Factory;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import edu.uci.ics.jung.algorithms.importance.Ranking;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import fr.inria.wimmics.smilk.localsimilarity.SimilarityFactory;
import it.cnr.isti.hpc.benchmark.Stopwatch;
import it.cnr.isti.hpc.text.MyPair;
import java.util.Collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.MultiKeyMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextMatrix {

    private static final Logger logger = LoggerFactory.getLogger(ContextMatrix.class);
    public static Stopwatch stopwatch = new Stopwatch();

    public SpotMatchList sml;
    private final SpotMatchList ambiguousSpots = new SpotMatchList();
    private final SpotMatchList notAmbiguousSpots = new SpotMatchList();
    private static int window;
    private static double alpha;
   //  private static double beta;
    private static double coherence;
    private static Set<MyNode> candidateNodes;
    private static Set<MyNode> mentionNodes;

    private static List<EntityMatch> entities;

    private static DoubleMatrix2D aMATRIX;

    static int edgeCount = 0;

    List<MyLink> createdLinks;

    private static RelatednessFactory globalrelatedness;
    private static SimilarityFactory localSimilarity;

    MyGraph finalMyGraph = new MyGraph();
    UndirectedGraph<MyNode, MyLink> finalGraph;
    Map finalWeights;

    Map visited;
    MultiMap<Integer, Integer> SpotEntitiesIndex;
    Map<PairSpotEntity, Double> Spot_Match_localSimilarity;

    public ContextMatrix(SpotMatchList sml, int window, SimilarityFactory similarity, RelatednessFactory relatedness, double alpha, double coherence) {

        //initialization
        sml.sortByStartPosition();
        this.alpha = alpha;
       // this.beta = beta;
        this.window = window;
        this.coherence = coherence;
        this.createdLinks = new ArrayList<MyLink>();
        this.candidateNodes = new HashSet<MyNode>();
        this.mentionNodes = new HashSet<MyNode>();
        this.localSimilarity = similarity;
        this.globalrelatedness = relatedness;
        this.sml = sml;
        this.visited = new HashMap<MyPair, Double>();
        this.SpotEntitiesIndex = new MultiHashMap<Integer, Integer>();

        int dimension = sml.size();

        stopwatch.start("matrix");
        
        
     //   System.out.println("Alpha= "+ this.alpha +" Beta= "+ this.beta );
        SpotMatchList newSml = new SpotMatchList();

        Spot_Match_localSimilarity = new HashMap<PairSpotEntity, Double>();
        int previous = 0;
        for (int i = 0; i < sml.size(); i++) {

            SpotMatch s = sml.get(i);

            double priorSUM = 0;
            double localSimSUM = 0;
            MyPair spotOffset = new MyPair(s.getStart(), s.getEnd());
           
            for (int j = 0; j < s.getEntities().size(); j++) {

                EntityMatch e = s.getEntities().get(j);

                PairSpotEntity pair = new PairSpotEntity(spotOffset, e.getId());

                double labelsim = localSimilarity.getLableSimScore(s, e);
                // double labelsim=0;
               //double x=localSimilarity.getContextSimScore(s, e);
              
              //  System.out.println(s.getMention()+"--> "+e.getEntity().getName()+"--->"+ e.getScore());
               
                
             
                 double contextsim = e.getContextScore();
                
               
               //
                Double commenness=e.getCommonness();
                commenness=(commenness.isInfinite() || commenness.isNaN())? 0: commenness;
                
               double localsim = ((double) this.alpha * (labelsim + e.getCommonness()))  * ((1 - this.alpha) * contextsim);
              // double localsim=e.getCommonness();
                Spot_Match_localSimilarity.put(pair, localsim);

                priorSUM = +e.getCommonness();
                
             //  System.out.println(s.getMention() + ": " + e.getEntity().getName() + ": " + labelsim + " - " + contextsim);
                // MyLink edge = new MyLink(s, null, e, e.getCommonness());
                //System.err.println(s.getMention()+"----"+e.getEntity().getName());
                //int z = sml.size() + previous + j;
                //   System.out.println("i "+i+" j"+z );

                //  aMATRIX.set(i, z, localsim);
            }

           //
            double L1 = Math.abs(priorSUM - localSimSUM);

            if (L1 <= coherence) {
                EntityMatchList list = s.getEntities();
                list.sortByProbability();
                EntityMatchList enitiies = new EntityMatchList();
                enitiies.add(list.get(0));
                s.setEntities(enitiies);
            }
            newSml.add(s);
        }

        this.sml = newSml;
        
        for (SpotMatch m : newSml) {
            MyNode mnt = new MyNode(m, null, null);
            mentionNodes.add(mnt);
            for (EntityMatch candidate : m.getEntities()) {
                MyNode cnd = new MyNode(null, candidate, mnt);
                candidateNodes.add(cnd);
            }
        }
      
        entities = new ArrayList<>();
        for (SpotMatch m : newSml) {
            entities.addAll(m.getEntities());
            dimension += m.getEntities().size();
        }
        logger.info("the dimension of matrix will be {}x{}", dimension, dimension);

        aMATRIX = new SparseDoubleMatrix2D(dimension, dimension);

        for (int i = 0; i < newSml.size(); i++) {

            SpotMatch s = newSml.get(i);

            previous += (i > 0) ? newSml.get(i - 1).getEntities().size() : 0;
            for (int j = 0; j < s.getEntities().size(); j++) {

                EntityMatch e = s.getEntities().get(j);

                int z = newSml.size() + previous + j;
                MyPair pp = new MyPair(s.getStart(), s.getEnd());
                PairSpotEntity p = new PairSpotEntity(pp, e.getId());

                
                double localsim = getLocalsimilarity(p);
                aMATRIX.set(i, z, localsim);

            }

            //Measure Global similarity \psi (c_i,c_j) and construct the graph between (c_i,c_j)
            int size = newSml.size();
            int candidates = s.getEntities().size();

            if (candidates > 1) {
                ambiguousSpots.add(s);
            } else {
                notAmbiguousSpots.add(s);
            }

            // System.out.println("-"+ s.getMention());
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
            for (int j = start; j < end; j++) {

                SpotMatch b = (SpotMatch) newSml.get(j);

                if (!b.equals(s)) {

                    computeGlobalRel(b, s, i, j, aMATRIX);

                }
            }
        }

        logger.info("Matrix construction is performed in {} millis",
                stopwatch.stop("matrix"));

//     for(int i = 0; i < dimension; i++)
//     {
//      for(int j = 0; j <dimension; j++)
//      {
//         
//        System.out.printf("%5f ", aMATRIX.get(i, j));
//      }
//         System.out.println();       
//     }  
        // System.out.println(finalGraph.getVertexCount());
        //System.out.println(finalGraph.toString());
    }

    private void computeGlobalRel(SpotMatch b, SpotMatch a, int aPosition, int bPositon, DoubleMatrix2D aMatrix) {
        
        for (int i = 0; i < a.getEntities().size(); i++) {

            EntityMatch aem = a.getEntities().get(i);

            for (int j = 0; j < b.getEntities().size(); j++) {

                EntityMatch bem = b.getEntities().get(j);

                MyPair p = new MyPair(aem.getEntity().getId(), bem.getEntity().getId());
                double rel;

                if (!visited.containsKey(p)) {

                    rel = globalrelatedness.getScore(bem.getEntity(), aem.getEntity());
                    rel = (rel > 0) ? rel : 0;
                    visited.put(p, rel);
                    visited.put(new MyPair(bem.getEntity().getId(), aem.getEntity().getId()), rel);

                } else {

                    rel = (Double) visited.get(p);

                }

                int bStep = 0;

                for (int k = 0; k < bPositon; k++) {
                    bStep += sml.get(k).getEntities().size();
                }

                int aStep = 0;

                for (int k = 0; k < aPosition; k++) {
                    aStep += sml.get(k).getEntities().size();
                }

                int x = sml.size() + aStep + i;
                int y = sml.size() + bStep + j;
                // System.out.println("["+ x + "]"+"["+y+"]");

                aMatrix.set(x, y, rel);
                if (!SpotEntitiesIndex.containsValue(bPositon, y)) {
                    SpotEntitiesIndex.put(bPositon, y);
                }
                if (!SpotEntitiesIndex.containsValue(aPosition, x)) {
                    SpotEntitiesIndex.put(aPosition, x);
                }

            }

        }

    }

    public EntityMatchList pageRankLike(int max_iteration, double epsilon, double lambda) {

        EntityMatchList eml = new EntityMatchList();
        //PageRankLike algorithm 
        int numVertices = aMATRIX.viewRow(0).size();

        //DoubleMatrix2D A = new SparseDoubleMatrix2D(myMatrix);
        DoubleMatrix2D A = aMATRIX;

//          System.out.println(A.toString());
//         System.out.println("------------");
        DoubleMatrix1D S = new DenseDoubleMatrix1D(numVertices);

        for (Map.Entry< Integer, Collection<Integer>> entry : SpotEntitiesIndex.entrySet()) {
            // vertices.get(i).score = V_new.get(i);
            int i = entry.getKey();
            S.set(i, sml.get(i).getProbability());
            List<Integer> list = new ArrayList<>(entry.getValue());
            for (int j : list) {
                
                Double prior = sml.get(i).getEntities().get(list.indexOf(j)).getCommonness();
               
                prior= (prior.isInfinite() || prior.isNaN())? 0: prior;
                
                S.set(j, prior);
            }
        }
//          System.out.println("------------");
//          System.out.println(S);
        
       // normalize the wieght for each row in matrix 
        
        HashMap<Integer, Double> cum = new HashMap<Integer, Double>();
        for (int i = 0; i < numVertices; i++) {
            double sum = 0;
            for (int j = 0; j < numVertices; j++) {
                sum += A.get(i, j);

            }
            cum.put(i, sum);
        }

        for (int i = 0; i < numVertices; i++) {
            double rowSum = cum.get(i);

            for (int j = 0; j < numVertices; j++) {
                if (rowSum > 0) {
                    A.set(i, j, (double) A.get(i, j) / (double) cum.get(i));
                } else {
                    A.set(i, j, 0);
                }
            }
        }

        DoubleMatrix1D V = S.copy();
        DoubleMatrix1D V_new = new DenseDoubleMatrix1D(numVertices);

        //DoubleDoubleFunction subtract = (double a, double b) -> a - b;
        DoubleDoubleFunction subtract = new DoubleDoubleFunction() {
            public double apply(double a, double b) {
                return a - b;
            }
        };

        //Equation
       //   System.out.println(A.toString());
        double diff = 1;

        int numIteration = 0;

        while (diff > epsilon && numIteration < max_iteration) {
          
//            System.out.println("i:" + numIteration);

            DoubleMatrix1D s = S.copy();

            A.zMult(V, S, (1 - lambda), lambda, false); // S1=(1-lambda)*A*V+lambda*S      
            V_new = S.copy();
            
//            System.out.println("V_t= "+ V_new);
           
            S = s.copy();

            DoubleMatrix1D normvector = V_new.copy();
            normvector.assign(V, subtract);

            diff = new Algebra().norm1(normvector);
            V = V_new.copy();

            //update the S by new mentions weights in V_new
            for (int i : SpotEntitiesIndex.keySet()) {
                S.set(i, V_new.get(i));
            }

//            for(int i:candidateRows){
//                S.set(i, 0);
//            }
            numIteration += 1;
        }

        if (diff < epsilon) {
            logger.info("Converge after = {}", numIteration);
            //System.out.println("Final preference vector: " + V_new);
        } else {
            logger.info("Not Converge after = {}", numIteration);
            //System.out.println("Final preference vector: " + V_new);
        }

        //Final Selection
        // System.out.println(V_new);
        for (Map.Entry< Integer, Collection<Integer>> entry : SpotEntitiesIndex.entrySet()) {
            // vertices.get(i).score = V_new.get(i);
            int i = entry.getKey();
            List<Integer> list = new ArrayList<>(entry.getValue());
            for (int j : list) {
                double score = V_new.get(j);
                EntityMatch entityMatch = new EntityMatch(sml.get(i).getEntities().get(list.indexOf(j)).getEntity(), score, sml.get(i));
                
                eml.add(entityMatch);
            }

        }
        
        System.out.println(eml);
        return eml;
    }
    
    
 
    

    public EntityMatchList getNotAmbiguousEntities() {
        EntityMatchList eml = new EntityMatchList();
        Iterator i = notAmbiguousSpots.iterator();
        do {
            if (!i.hasNext()) {
                break;
            }
            SpotMatch sm = (SpotMatch) i.next();
            EntityMatchList tmp = sm.getEntities();
            if (!tmp.isEmpty()) {
                eml.add((EntityMatch) sm.getEntities().get(0));
            }
        } while (true);
        return eml;
    }

    public EntityMatchList getAmbiguousEntities(double epsilon) {
        EntityMatchList eml = new EntityMatchList();
        SpotMatch sm;
        for (Iterator i = ambiguousSpots.iterator(); i.hasNext();
                eml.add(disambiguate(sm, epsilon))) {
            sm = (SpotMatch) i.next();
        }

        return eml;
    }

    public EntityMatchList getAllEntities(double epsilon) {
        EntityMatchList eml = getNotAmbiguousEntities();
        eml.addAll(getAmbiguousEntities(epsilon));
        return eml;
    }

    public Double getLocalsimilarity(PairSpotEntity p) {
        for (Map.Entry<PairSpotEntity, Double> entry : Spot_Match_localSimilarity.entrySet()) {
           
            if (p.first.getFirst() == entry.getKey().first.getFirst() && p.first.getSecond() == entry.getKey().first.getSecond() && p.second == entry.getKey().second) {
                return entry.getValue();
            }

        }
        return 0.0;
    }

    public EntityMatchList score(EntityMatchList eml, double alpha) {
        double beta = 1.0D - alpha;
        int size = eml.size();
        for (int windowCenter = 0; windowCenter < size; windowCenter++) {
            EntityMatch e = (EntityMatch) eml.get(windowCenter);
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
            float avgRel = 0.0F;
            for (int j = start; j < end; j++) {
                if (windowCenter != j) {
                    EntityMatch c = (EntityMatch) eml.get(j);
                    avgRel = (float) ((double) avgRel + globalrelatedness.getScore(c.getEntity(), e.getEntity()));
                }
            }

            if (size == 1) {
                e.setScore(beta + alpha * e.getSpotLinkProbability());
            } else {
                e.setScore((beta * (double) avgRel) / (double) (size - 1)
                        + alpha * e.getSpotLinkProbability());
            }
        }

        return eml;
    }

    public EntityMatch disambiguate(SpotMatch sm, double epsilon) {
        EntityMatchList eml = sm.getEntities();
        eml.sort();
        EntityMatch best = (EntityMatch) eml.get(0);
        double maxScore = best.getScore();
        double minScore = maxScore - maxScore * epsilon;
        Iterator i = eml.iterator();
        do {
            if (!i.hasNext()) {
                break;
            }
            EntityMatch em = (EntityMatch) i.next();
            if (em.getScore() > minScore && best.getCommonness()
                    < em.getCommonness()) {
                best = em;
            }
        } while (true);
        return best;
    }

    public static MyNode findCandidateNode(Set<MyNode> CandidateNodes, EntityMatch candidate) {

        MyNode node = null;
        for (MyNode nd : CandidateNodes) {
            if (nd.candidate.equals(candidate)) {
                node = nd;
                break;
            }
        }
        return node;
    }

    public static MyNode findMentionNode(Set<MyNode> MentionNode, SpotMatch mention) {

        MyNode node = null;
        for (MyNode nd : MentionNode) {
            if (nd.mention.equals(mention)) {
                node = nd;
                break;
            }
        }
        return node;
    }

    public static boolean alreadyCreated(List<MyLink> createdLinks, MyLink mylink) {
        for (MyLink link : createdLinks) {
            if (link.isSimilarTo(mylink)) {
                return true;
            }
        }
        return false;
    }

    public class PairSpotEntity<F, S> {

        private MyPair first; //first member of pair
        private int second; //second member of pair

        public PairSpotEntity(MyPair first, int second) {
            this.first = first;
            this.second = second;
        }

        public void setFirst(MyPair first) {
            this.first = first;
        }

        public void setSecond(int second) {
            this.second = second;
        }

        public MyPair getFirst() {
            return first;
        }

        public int getSecond() {
            return second;
        }

        @Override
        public String toString() {
            return ("[" + first.getFirst() + "," + first.getSecond() + "]:" + second);
        }
    }

}
