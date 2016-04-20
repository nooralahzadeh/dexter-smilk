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

import fr.inria.wimmics.smilk.localsimilarity.SimilarityFactory;
import it.cnr.isti.hpc.benchmark.Stopwatch;
import it.cnr.isti.hpc.dexter.graph.IncomingNodes;
import it.cnr.isti.hpc.dexter.graph.NodeFactory;
import it.cnr.isti.hpc.dexter.graph.OutcomingNodes;
import it.cnr.isti.hpc.text.MyPair;
import java.util.Collection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NaiveContextMatrix {

    private static final Logger logger = LoggerFactory.getLogger(NaiveContextMatrix.class);
    public static Stopwatch stopwatch = new Stopwatch();

    private SpotMatchList sml;
    private final SpotMatchList ambiguousSpots = new SpotMatchList();
    private final SpotMatchList notAmbiguousSpots = new SpotMatchList();
    private static int window;
    private static double alpha;
    private static double beta;
    private static double gamma;

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
    private static IncomingNodes in = NodeFactory
            .getIncomingNodes(NodeFactory.STD_TYPE);

    private static final int W = in.size();

    public NaiveContextMatrix(SpotMatchList sml, int window, double alpha, double beta, double gamma, RelatednessFactory relatedness) {

        //initialization
        sml.sortByStartPosition();
        this.alpha = alpha;
        this.beta = beta;

        this.gamma = gamma;

        this.window = window;

        this.createdLinks = new ArrayList<MyLink>();
        this.candidateNodes = new HashSet<MyNode>();
        this.mentionNodes = new HashSet<MyNode>();

        this.globalrelatedness = relatedness;
        this.sml = sml;

        this.SpotEntitiesIndex = new MultiHashMap<Integer, Integer>();

        int dimension = 0;

        stopwatch.start("matrix");

        Spot_Match_localSimilarity = new HashMap<PairSpotEntity, Double>();
        int previous = 0;

        for (SpotMatch m : sml) {
            MyNode mnt = new MyNode(m, null, null);
            mentionNodes.add(mnt);
            for (EntityMatch candidate : m.getEntities()) {
                MyNode cnd = new MyNode(null, candidate, mnt);
                candidateNodes.add(cnd);
            }
        }
        entities = new ArrayList<>();
        for (SpotMatch m : sml) {
            entities.addAll(m.getEntities());
            dimension += m.getEntities().size();
        }
        logger.info("the dimension of matrix will be {}x{}", dimension, dimension);

        aMATRIX = new SparseDoubleMatrix2D(dimension, dimension);

        for (int i = 0; i < sml.size(); i++) {

            SpotMatch s = sml.get(i);

            previous += (i > 0) ? sml.get(i - 1).getEntities().size() : 0;

            //Measure Global similarity \psi (c_i,c_j) and construct the graph between (c_i,c_j)
            int size = sml.size();

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

                SpotMatch b = (SpotMatch) sml.get(j);

                if (!b.equals(s)) {

                    computeGlobalRel(b, s, i, j, aMATRIX);

                }
            }
        }

        logger.info("Matrix construction is performed in {} millis",
                stopwatch.stop("matrix"));

    }

    private void computeGlobalRel(SpotMatch b, SpotMatch a, int aPosition, int bPositon, DoubleMatrix2D aMatrix) {
        for (int i = 0; i < a.getEntities().size(); i++) {

            EntityMatch aem = a.getEntities().get(i);

            for (int j = 0; j < b.getEntities().size(); j++) {

                EntityMatch bem = b.getEntities().get(j);

                MyPair p = new MyPair(aem.getEntity().getId(), bem.getEntity().getId());

                double rel = globalrelatedness.getScore(bem.getEntity(), aem.getEntity());
                rel = (rel != 0) ? rel : 0;

                int bStep = 0;

                for (int k = 0; k < bPositon; k++) {
                    bStep += sml.get(k).getEntities().size();
                }

                int aStep = 0;

                for (int k = 0; k < aPosition; k++) {
                    aStep += sml.get(k).getEntities().size();
                }

                int x = aStep + i;
                int y = bStep + j;

                //System.out.println("["+ x + "]"+"["+y+"]" + "<"+rel+">");
                aMatrix.set(x, y, gamma*rel);
                if (!SpotEntitiesIndex.containsValue(bPositon, y)) {
                    SpotEntitiesIndex.put(bPositon, y);
                }
                if (!SpotEntitiesIndex.containsValue(aPosition, x)) {
                    SpotEntitiesIndex.put(aPosition, x);
                }

            }

        }

    }

    public EntityMatchList Rank() {

        EntityMatchList eml = new EntityMatchList();
        //PageRankLike algorithm 
        int numVertices = aMATRIX.viewRow(0).size();

        //DoubleMatrix2D A = new SparseDoubleMatrix2D(myMatrix);
        DoubleMatrix2D A = aMATRIX;

//         System.out.println(A.toString());
//         System.out.println("------------");
        DoubleMatrix1D S = new DenseDoubleMatrix1D(numVertices);

        for (Map.Entry< Integer, Collection<Integer>> entry : SpotEntitiesIndex.entrySet()) {
            // vertices.get(i).score = V_new.get(i);
            int i = entry.getKey();
//       System.out.println(sml.get(i).getMention());
            List<Integer> list = new ArrayList<>(entry.getValue());
            for (int j : list) {
                double prior =  (double) sml.get(i).getEntities().get(list.indexOf(j)).getCommonness();
               
                double contextsim = sml.get(i).getEntities().get(list.indexOf(j)).getContextScore();
//      System.out.println(sml.get(i).getEntities().get(list.indexOf(j)).getEntity().getName()+"--"+contextsim +"/"+ prior);
                
                S.set(j,alpha*prior+ beta*contextsim);
            }
        }

        // System.out.println(S);
        //normalize the wieght for each row in matrix 
        HashMap<Integer, Double> cum = new HashMap<Integer, Double>();
        DoubleMatrix1D marginal_Sum = new DenseDoubleMatrix1D(numVertices);

        for (int i = 0; i < numVertices; i++) {
            double sum = 0;
            for (int j = 0; j < numVertices; j++) {
                sum += A.get(i, j);

            }
            marginal_Sum.set(i, sum);
        }

        //DoubleDoubleFunction subtract = (double a, double b) -> a - b;
        DoubleDoubleFunction sum = new DoubleDoubleFunction() {
            public double apply(double a, double b) {
                return a + b;
            }
        };

        //Equation
        DoubleMatrix1D rank_vector = S.copy();
        rank_vector.assign(marginal_Sum, sum);

        //Final Selection
//        System.out.println(rank_vector);
        for (Map.Entry< Integer, Collection<Integer>> entry : SpotEntitiesIndex.entrySet()) {
            // vertices.get(i).score = V_new.get(i);
            int i = entry.getKey();
            List<Integer> list = new ArrayList<>(entry.getValue());
            for (int j : list) {
                double score = rank_vector.get(j);
                EntityMatch entityMatch = new EntityMatch(sml.get(i).getEntities().get(list.indexOf(j)).getEntity(), score, sml.get(i));
                
                eml.add(entityMatch);
            }

        }
        // System.out.println(eml);
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
