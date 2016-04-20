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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author fnoorala
 */
public class MyGraph{
    
       UndirectedGraph<MyNode, MyLink>    graph ;
       HashMap<MyLink,Double> weights;
       Operations op=new Operations();
       
       public MyGraph(UndirectedSparseGraph<MyNode, MyLink> graph, HashMap<MyLink,Double> weights) {
            this.graph = graph;
            this.weights = weights;
        }
       
       public MyGraph merge(MyGraph b){
           
            this.getWeights().putAll(b.getWeights());
            Operations.mergeGraph(this.getGraph(), b.getGraph());
           
           return this;
       }

    public MyGraph() {
           graph =new  UndirectedSparseGraph<MyNode, MyLink>();
      weights=new  HashMap<MyLink,Double>();
    }
       
       public UndirectedGraph<MyNode, MyLink> getGraph(){
           return this.graph;
       }
       
       
        public HashMap<MyLink,Double> getWeights(){
           return this.weights;
       }
   }     