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
import cern.colt.matrix.linalg.Algebra;
import edu.uci.ics.jung.algorithms.matrix.GraphMatrixOperations;
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
import edu.uci.ics.jung.graph.util.EdgeType;

import fr.inria.wimmics.smilk.localsimilarity.SimilarityFactory;
import it.cnr.isti.hpc.benchmark.Stopwatch;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextGraphFork extends RecursiveTask<MyGraph> {

    private static final Logger logger = LoggerFactory.getLogger(ContextGraphFork.class);
    public static Stopwatch stopwatch = new Stopwatch();

    private SpotMatchList sml;
    private final SpotMatchList ambiguousSpots = new SpotMatchList();
    private final SpotMatchList notAmbiguousSpots = new SpotMatchList();
    private static int window;
    private static double alpha;
    private static Set<MyNode> candidateNodes;
    private static Set<MyNode> mentionNodes;

    static int edgeCount = 0;

    List<MyLink> createdLinks;

    private static RelatednessFactory globalrelatedness;
    private static SimilarityFactory localSimilarity;
    
     
    private int start;
    private int end;

    MyGraph finalMyGraph = new MyGraph();
    UndirectedGraph<MyNode, MyLink> finalGraph;
    Map finalWeights;

    static class Globals {

        static ForkJoinPool fjPool = new ForkJoinPool();
    }

    public ContextGraphFork(SpotMatchList sml, int start, int end) {
        createdLinks=new ArrayList<MyLink>();
        this.sml = sml;
        this.start = start;
        this.end = end;

    }

    public ContextGraphFork(SpotMatchList sml, int window, SimilarityFactory similarity, RelatednessFactory relatedness, Double alpha) {
       
        //initialization
        sml.sortByStartPosition();
        this.alpha = alpha;
        this.window = window;
        this.createdLinks = new ArrayList<MyLink>();
        this.candidateNodes = new HashSet<MyNode>();
        this.mentionNodes = new HashSet<MyNode>();
        this.localSimilarity = similarity;
        this.globalrelatedness = relatedness;
        
        
        for (SpotMatch m : sml) {
            MyNode mnt = new MyNode(m, null, null);
            mentionNodes.add(mnt);
            for (EntityMatch candidate : m.getEntities()) {
                MyNode cnd = new MyNode(null, candidate, mnt);
                candidateNodes.add(cnd);
            }
        }
        
        //fork
        stopwatch.start("graph");
        final ForkJoinPool pool = new ForkJoinPool(4);
        //finalMyGraph = Globals.fjPool.invoke(new Context(sml, 0, sml.size()));
        final ContextGraphFork cnt=new ContextGraphFork(sml, 0, sml.size());
        finalMyGraph=pool.invoke(cnt);
        logger.info("Graph was built in {} millis", stopwatch.stop("graph"));
        
        finalGraph = finalMyGraph.getGraph();
        
       // System.out.println(finalGraph.getVertexCount());
        //System.out.println(finalGraph.toString());
        finalWeights = finalMyGraph.getWeights();

    }

    private void makeGraph(SpotMatch b, SpotMatch a, Set<MyNode> candidateNodes,UndirectedSparseGraph<MyNode, MyLink> graph, HashMap<MyLink, Double> weights) {
      
        
     
        EntityMatch aem;

        for (Iterator i = a.getEntities().iterator(); i.hasNext();) {
            aem = (EntityMatch) i.next();
           // System.out.println("relatedness :---------:"+  aem.getEntity());
            for (Iterator j = b.getEntities().iterator(); j.hasNext();) {
                EntityMatch bem = (EntityMatch) j.next();

                double rel = globalrelatedness.getScore(bem.getEntity(), aem.getEntity());

               //  System.out.println(bem.getEntity().getName()+":" + rel);
                MyLink edge_e1_e2 = new MyLink(null, aem, bem, rel);
                if (!alreadyCreated(createdLinks, edge_e1_e2)) {
                     createdLinks.add(edge_e1_e2);
                   //  System.err.println("Edge added between "+aem.getEntity().getName() + "/ "+ bem.getEntity().getName() );
                   
                    graph.addEdge(edge_e1_e2, findCandidateNode(candidateNodes, aem), findCandidateNode(candidateNodes, bem), EdgeType.UNDIRECTED);

                }
                rel = (rel > 0) ? rel : 0;
                weights.put(edge_e1_e2, rel);
            }

        }

    }

    public EntityMatchList pageRankLike(int max_iteration, double epsilon, double lambda) {
        EntityMatchList eml = new EntityMatchList();
        //PageRankLike algorithm
        int numVertices = finalGraph.getVertexCount();
       

        DoubleMatrix2D A = GraphMatrixOperations.graphToSparseMatrix(finalGraph, finalWeights);

        
        //track the i-th element of the matrix
        List<Integer> mentionsRows = new ArrayList<Integer>();
        List<Integer> candidateRows = new ArrayList<Integer>();
        List<MyNode> vertices = new ArrayList<MyNode>(finalGraph.getVertices());

        DoubleMatrix1D S = new DenseDoubleMatrix1D(numVertices);

        for (int i = 0; i < numVertices; i++) {
            if (vertices.get(i).mention != null) {
                mentionsRows.add(i);

                /**
                 * FIX ME add value to i-th row of vector S as mention :
                 * probability to be a link to a entity for the text of this
                 * spot, it is computed dividing the number of documents in
                 * Wikipedia containing this spot as a anchor by the number of
                 * documents in wikipedia containing this spot as simple text.
                 */
                S.set(i, vertices.get(i).mention.getLinkProbability());
            } else {
                candidateRows.add(i);
                S.set(i, 0);
            }
        }

        //normalize the wieght for each row in matrix ????
        HashMap<Integer, Double> cum = new HashMap<Integer, Double>();
        for (int i = 0; i < numVertices; i++) {
            double sum = 0;
            for (int j = 0; j < numVertices; j++) {
                sum += A.get(i, j);

            }
            cum.put(i, sum);
        }

        for (int i = 0; i < numVertices; i++) {
            for (int j = 0; j < numVertices; j++) {
                A.set(i, j, (double) A.get(i, j) / (double) cum.get(j));
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
        // System.out.println(A.toString());
        double diff = 1;
        int numIteration = 0;

        while (diff > epsilon && numIteration < max_iteration) {
            //System.out.println("i:" + numIteration);

            DoubleMatrix1D s = S.copy();

            A.zMult(V, S, (1 - lambda), lambda, false); // S1=(1-lambda)*A*V+lambda*S      
            V_new = S.copy();

            S = s.copy();

            DoubleMatrix1D normvector = V_new.copy();
            normvector.assign(V, subtract);

            diff = new Algebra().norm1(normvector);
            V = V_new.copy();

            //update the S by new mentions weights in V_new
            for (int i : mentionsRows) {
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
        HashMap<MyNode, List<MyNode>> mention_candidateRanks = new HashMap<MyNode, List<MyNode>>();

        for (int i : mentionsRows) {
            vertices.get(i).score = V_new.get(i);

            List<MyNode> candidates = new ArrayList<MyNode>();

            for (int j : candidateRows) {
                if (vertices.get(j).origin == vertices.get(i)) {
                    vertices.get(j).score = V_new.get(j);
                    candidates.add(vertices.get(j));
                }
            }

            mention_candidateRanks.put(vertices.get(i), candidates);
        }

        //Assigning the last score to EntityMatch
        for (MyNode m : mention_candidateRanks.keySet()) {

            List<MyNode> list = mention_candidateRanks.get(m);
            Collections.sort(list);
            for (MyNode c : list) {
                c.candidate.setScore(c.score);
                eml.add(c.candidate);
                //System.out.println("C:" + c.candidate.getEntity().getName() + " : " + Math.log(c.score));
            }
        }

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

    @Override
    protected MyGraph compute() {
        
        int length=end-start;
        stopwatch.start("LocalFeatures");
        if (length <10000000) {

            UndirectedSparseGraph<MyNode, MyLink> graph = new UndirectedSparseGraph<MyNode, MyLink>();
            HashMap<MyLink, Double> weights = new HashMap<MyLink, Double>();

            for (int i = start; i < end; i++) {
                SpotMatch s = sml.get(i);
                System.out.println("------------------"+ s.getEntities().size());
                for (EntityMatch e : s.getEntities()) {
                    double labelsim = localSimilarity.getLableSimScore(s, e);
                    double contextsim = e.getContextScore();
                    double localsim = this.alpha * labelsim + (1 - this.alpha) * contextsim;

                    // System.out.println(s.getMention() + ": " + e.getEntity().getName() + ": " + labelsim + " - " + contextsim);
                    //  MyLink edge = new MyLink(s, null, e, e.getCommonness());
                    MyLink edge = new MyLink(s, null, e, localsim);
                    graph.addEdge(edge, findMentionNode(mentionNodes, s), findCandidateNode(candidateNodes, e), EdgeType.UNDIRECTED);

                    // weighted.put(edge,e.getCommonness());   
                    weights.put(edge, localsim);

                }
            
           logger.info("Local features calculation is performed in {} millis",
                    stopwatch.stop("LocalFeatures"));
            //Measure Global similarity \psi (c_i,c_j) and construct the graph between (c_i,c_j)
            int size =sml.size();
            int candidates = s.getEntities().size();
                
                if (candidates > 1) {
                    ambiguousSpots.add(s);
                } else {
                    notAmbiguousSpots.add(s);
                }
           
               // System.out.println("-"+ s.getMention());
                
                int windowCenter=i;
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
                
                for (int j = start; j < end; j++) {
                        SpotMatch b = (SpotMatch) sml.get(j);
                        if(!b.equals(s)){
                           // System.out.println("---"+ b.getMention());
                            makeGraph(b, s, candidateNodes, graph, weights);}
                 }
            }
            MyGraph myGraphObject = new MyGraph(graph, weights);
            return myGraphObject;
        } else {
            
            int mid = start + (end - start) / 2;
            
          //  System.out.println("fork"+start+" "+ mid+" "+end);
            ContextGraphFork left = new ContextGraphFork(sml, start, mid);
            ContextGraphFork right = new ContextGraphFork(sml, mid, end);
            
            left.fork();
            MyGraph rightAns = right.compute();
            MyGraph leftAns = left.join();
            return rightAns.merge(leftAns);
        }
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

}
