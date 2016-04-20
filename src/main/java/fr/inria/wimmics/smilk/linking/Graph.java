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

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
 
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.relatedness.RelatednessFactory;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fnoorala
 */
public class Graph {
    
    
    UndirectedGraph<MyNode, MyLink> makeGraph(SpotMatch b, SpotMatch a, RelatednessFactory globalrelatedness, Set<MyNode> candidateNodes, Map weights) {
       // System.out.println(b.getMention() + "vise-versa" +a.getMention());
    
        UndirectedGraph< MyNode, MyLink>    graph = new UndirectedSparseGraph<MyNode,MyLink>();
        
        
        double score = 0.0D;
        EntityMatch aem;   
        List<SpotMatch> spots=new ArrayList<SpotMatch>();
        spots.add(a);
        spots.add(b);
           
        for (Iterator i = a.getEntities().iterator(); i.hasNext();) {
            aem = (EntityMatch) i.next();      
              // System.out.println("relatedness :---------:"+  aem.getEntity());
            for (Iterator j = b.getEntities().iterator(); j.hasNext();) {
                EntityMatch bem = (EntityMatch) j.next();
             
                double rel = globalrelatedness.getScore(bem.getEntity(), aem.getEntity());
              
              //  System.out.println(bem.getEntity().getName()+":" + rel);
                             
               MyLink edge_e1_e2 = new  MyLink(null, aem, bem, rel);
               
                     graph.addEdge(edge_e1_e2, findCandidateNode(candidateNodes, aem), findCandidateNode(candidateNodes, bem), EdgeType.UNDIRECTED);
                      
                
                    rel = (rel > 0) ? rel : 0;
                    weights.put(edge_e1_e2, rel);
            }
          
        }
        
        return graph;
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
    
}
