/*
 * Copyright University of Orleans - ENSI de Bourges
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 */
package fr.inria.wimmics.smilk.linking;

/**
 * This class provides a set of methods used in graph algorithms.
 * @author V. Levorato
 */
import com.google.common.collect.Sets;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.collections15.Factory;

/**
 * This class provides a set of methods used in graph algorithms.
 * @author V. Levorato
 */
public class Operations {
    
    /**
       * Insert into subG nodes and edges of N[x] in G.
       * @param G original graph
       * @param subG subgraph of G
       * @param x node to add with its neighborhood in Gtarget
       */
      public static<V,E> void subGraph(Graph<V,E> G, Graph<V,E> subG, V x)
      {
          subG.addVertex(x);

          Collection<E> edges=G.getIncidentEdges(x);
          if(edges!=null)
              for(E e : edges)
                  if(G.getEdgeType(e)==EdgeType.UNDIRECTED)
                        subG.addEdge(e, G.getEndpoints(e).getFirst(), G.getEndpoints(e).getSecond());
                  else
                        subG.addEdge(e, G.getSource(e), G.getDest(e));
      }

     /**
       * Insert into subG nodes and edges of N[S] in G. S is a set of nodes.
       * @param G original graph
       * @param subG subgraph of G
       * @param S nodes to add with its neighborhoods in subG
      */
      public static<V,E> void subGraph(Graph<V,E> G, Graph<V,E> subG, Set<V> S)
      {
          for(V x : S)
          {
              subG.addVertex(x);

              Collection<E> edges=G.getIncidentEdges(x);
              if(edges!=null)
                  for(E e : edges)
                      if(!subG.containsEdge(e))
                      subG.addEdge(e, G.getEndpoints(e).getFirst(), G.getEndpoints(e).getSecond());
          }
      }

      /**
       * Merge graph subG into G.
       * @param G result graph of G ∪ subG
       * @param subG graph to merge into G
       */
      public static<V,E>  void mergeGraph(Graph<V,E> G, Graph<V,E> subG)
      {
          for(V v : subG.getVertices())
            G.addVertex(v);

          for(E e : subG.getEdges())
               if(subG.getEdgeType(e)==EdgeType.UNDIRECTED)
                    G.addEdge(e,subG.getEndpoints(e).getFirst(),subG.getEndpoints(e).getSecond());
                else
                    G.addEdge(e, subG.getSource(e), subG.getDest(e));
      }

    /**
     * Full copy (edges + vertices) of the Graph. (Undirected version)
     * @param G Graph to copy
     * @return copy of the Graph G
     */
    public static<V,E> Graph<V,E> copyUndirectedSparseGraph(Graph<V,E> G)
    {
        Graph<V,E> newG = new UndirectedSparseGraph<V,E>();

        for(V x : G.getVertices())
            newG.addVertex(x);

         for(E e : G.getEdges())
            newG.addEdge(e, G.getEndpoints(e).getFirst(), G.getEndpoints(e).getSecond());

        return newG;
    }

    /**
     * Full copy (arcs + vertices) of the Graph. (Directed version)
     * @param G Graph to copy
     * @return copy of the Graph G
     */
    public static<V,E> Graph<V,E> copyDirectedSparseGraph(Graph<V,E> G)
    {
        Graph<V,E> newG = new DirectedSparseGraph<V,E>();

        for(V x : G.getVertices())
            newG.addVertex(x);

         for(E e : G.getEdges())
            newG.addEdge(e, G.getEndpoints(e).getFirst(), G.getEndpoints(e).getSecond());

        return newG;
    }
    
    /**
     * Full copy (arcs + vertices) of a graph using the given factory.
     * @param <V>
     * @param <E>
     * @param G
     * @param f
     * @return a full copy a a graph
     */
    public static<V,E> Graph<V,E> copyGraph(Graph<V,E> G, Factory<Graph<V,E>> f)
    {
    	Graph<V,E> newG = f.create();

    	for(V x : G.getVertices())    	{
    		newG.addVertex(x);
    	}

    	for(E e : G.getEdges()) {
    		newG.addEdge(e, G.getEndpoints(e).getFirst(), G.getEndpoints(e).getSecond());
    	}
    	return newG;
    }


    /**
     * Returns the diameter of a graph.
     * @param G graph
     * @return diameter of G
     */
    public static<V,E> int getDiameter(Graph<V,E> G)
    {
        int diameter=0;

        Distance d=new DijkstraDistance(G);

        for(V s:G.getVertices())
            for(V t:G.getVertices())
                if(s!=t && d.getDistance(s, t)!=null)
                    if(d.getDistance(s, t).intValue()>diameter)
                        diameter=d.getDistance(s, t).intValue();


        return diameter;
    }


    /**
     * Returns a vertex of G with minimum degree.
     * @param G Graph in which vertex has to be found
     * @return a vertex with min degree.
     */
    public static<V,E> V getMinDegVertex(Graph<V,E> G)
    {
        int deg=java.lang.Integer.MAX_VALUE;
        V v = null;

        for(V x : G.getVertices())
            if(G.degree(x)<deg)
            {
                v=x;
                deg=G.degree(x);
            }

        return v;
    }

    /**
     * Returns a set containing vertices of G with lowest degree.
     * @param G Graph in which all vertices of min degree has to be found
     * @return a set with all vertices of min degree
     */
    public static<V,E> Set<V> getAllMinDegVertex(Graph<V,E> G)
    {
        Set<V> allDM=new HashSet();

        int degmin=getMinDeg(G);
        for(V x : G.getVertices())
            if(G.degree(x)==degmin)
                allDM.add(x);

        return allDM;
    }

     /**
     * Returns a vertex of G with maximum degree.
     * @param G Graph in which vertex has to be found
     * @return a vertex with max degree.
     */
    public static<V,E> V getMaxDegVertex(Graph<V,E> G)
    {
        int deg=0;
        V v = null;

        for(V x : G.getVertices())
            if(G.degree(x)>deg)
            {
                v=x;
                deg=G.degree(x);
            }

        return v;
    }

    /**
     * Returns a set containing vertices of G with highest degree.
     * @param G Graph in which all vertices of max degree has to be found
     * @return a set with all vertices of max degree
     */
    public static<V,E> Set<V> getAllMaxDegVertex(Graph<V,E> G)
    {
        Set<V> allDM=new HashSet();

        int degmax=getMaxDeg(G);
        for(V x : G.getVertices())
            if(G.degree(x)==degmax)
                allDM.add(x);

        return allDM;
    }


    /**
     * Returns the maximum degree of G.
     * @param G Graph in which max degree has to be found
     * @return max degree.
     */
    public static<V,E> int getMaxDeg(Graph<V,E> G)
    {
        int deg=0;

        for(V x : G.getVertices())
            if(G.degree(x)>deg)
                deg=G.degree(x);


        return deg;
    }

     /**
     * Returns the minimum degree of G.
     * @param G Graph in which min degree has to be found
     * @return min degree.
     */
    public static<V,E> int getMinDeg(Graph<V,E> G)
    {
        int deg=java.lang.Integer.MAX_VALUE;

        for(V x : G.getVertices())
            if(G.degree(x)<deg)
                deg=G.degree(x);

        return deg;
    }

    /**
     * Returns a vertex of degree 'deg' for the graph G.
     * @param G graph
     * @param deg degree
     * @return a vertex of degree 'deg'
     */
    public static<V,E> V getDegVertex(Graph<V,E> G, int deg)
    {
        V v=null;
        for(V x : G.getVertices())
            if(G.degree(x)==deg)
            {   v=x; break;   }

        return v;
    }

    /**
     * Retunrs the set of all the vertices of degree 'deg' for the graph G.
     * @param G graph
     * @param deg degree
     * @return the set of all the vertices of degree 'deg'
     */
    public static<V,E> Set<V> getAllDegVertex(Graph<V,E> G, int deg)
    {
        Set<V> Sdeg=new HashSet();
        for(V x : G.getVertices())
            if(G.getNeighborCount(x)==deg)
                Sdeg.add(x);

        return Sdeg;
    }


    /**
     * Returns the amount of edges which are linking vertices of A.
     * @param G Graph where edges appear
     * @param A Set containing vertices to test
     * @return the number of edges linking elements of A
     */
    public static<V,E> int getNbEdges(Graph<V,E> G, Set<V> A)
    {
        int nbe=0;
        for(E e : G.getEdges())
            if(A.contains(G.getEndpoints(e).getFirst()) && A.contains(G.getEndpoints(e).getSecond()))
                nbe++;

        return nbe;
    }


    /**
     * Returns all neighbors of S without S (usually denoted as N(S)).
     * @param G graph
     * @param S set of vertices
     * @return neighbors of S
     */
    public static<V,E> Set<V> getNeighbors(Graph<V,E> G, Set<V> S)
    {
        Set<V> N=new HashSet();
        for(V v : S)
            N.addAll(G.getNeighbors(v));

        N.removeAll(S);
        
        return N;
    }


    /**
     * Returns the neighbors vertices of a set of v at a given distance. For instance,
     * with dist=2, the method returns N²(v).
     * @param G Graph
     * @param v vertex
     * @param dist distance of the neighbors
     * @return neighbors vertices of v at a given distance
     */
    public static<V,E> Set<V> getNeighbors(Graph<V,E> G, V v, int dist)
    {
        Set<V> N=new HashSet();
        Set<V> done=new HashSet();


        N.addAll(G.getNeighbors(v));
        done.addAll(N);
        done.add(v);

        for(int i=1;i<dist;i++)
        {
            Set<V> Nn=new HashSet();
            for(V x : N)
            {
                Sets.difference(new HashSet(G.getNeighbors(x)), done).copyInto(Nn);

            }

            done.addAll(Nn);
            N=Nn;
        }

        return N;
    }
    
    /**
     * Tests if a graph is regular.
     * @param G graph
     * @return true if G is regular, else false.
     */
    public static<V,E> boolean isRegular(Graph<V,E> G)
    {
        int deg=G.degree(G.getVertices().iterator().next());
        for(V v : G.getVertices())
           if(G.degree(v)!=deg)
              return false;

        return true;
       
    }

    /**
     * Tests if a graph is d-regular.
     * @param G graph
     * @param deg degree
     * @return true if G is regular, else false.
     */
    public static<V,E> boolean isRegular(Graph<V,E> G, int deg)
    {
        for(V v : G.getVertices())
           if(G.degree(v)!=deg)
              return false;

        return true;

    }

    /**
     * Returns true if a directed graph has no circuit.
     * @param G graph
     * @return true if G has no circuit, else false.
     */
    public static<V,E> boolean isAcyclic(Graph<V,E> G)
    {
        boolean b=false;
        Graph<V,E> Gp=copyDirectedSparseGraph(G);
        ArrayList<V> L=new ArrayList();
        ArrayList<V> Q=new ArrayList();
        for(V v:Gp.getVertices())
            if(Gp.getPredecessorCount(v)==0)
                Q.add(v);

        while(!Q.isEmpty())
        {
            V n=Q.remove(0);
            L.add(n);
            Set<V> succ=new HashSet(Gp.getSuccessors(n));
            Gp.removeVertex(n);
            for(V m : succ)
                if(Gp.getPredecessorCount(m)==0)
                    Q.add(m);
        }

        if(Gp.getEdgeCount()==0)
            b=true;

        return b;
    }

    /**
     * Return true if the subset C of G contains at least one cycle.
     * @param G graph
     * @param C subset of G
     * @return true if C contains a cycle, false else.
     */
    public static<V,E> boolean isCycle(Graph<V,E> G, Set<V> C)
    {
        ArrayList<V> visited=new ArrayList();
        V vstart=C.iterator().next();
        visited.add(vstart);
        boolean b=true;
        V v=vstart;

        while(b && visited.size()<C.size())
        {
            Set<V> neigh=new HashSet();
            if(!G.getNeighbors(v).isEmpty())
                Sets.intersection(C, new HashSet(G.getNeighbors(v))).copyInto(neigh);

            neigh.removeAll(visited);
            if(!neigh.isEmpty())
            {
                v=neigh.iterator().next();
                visited.add(v);
            }
            else
                b=false;

        }

        if(b && !G.getNeighbors(visited.get(visited.size()-1)).contains(vstart))
            b=false;

        return b;
    }
    
    /**
     * Tests if an edge exists between vertices v1 and v2.
     * @param G Graph in which edge has to be found
     * @param v1 1st vertex of the edge
     * @param v2 2nd vertex of the edge
     * @return true if the edge v1-v2 exits, false if not.
     */
    public static<V,E> boolean isEdge(Graph<V,E> G, V v1, V v2)
    {
        if(G.findEdge(v1, v2)==null)
            return false;
        else
            return true;
    }


    /**
     * Tests if a set of vertices is a clique for the Graph G.
     * @param G Graph where the set has to be tested.
     * @param K Set of vertices to test
     * @return true if K is a clique in G, if not returns false.
     */
    public static<V,E> boolean isClique(Graph<V,E> G, Set<V> K)
    {
        boolean b=true;

        if(K.isEmpty() || K.size()==1)
            return true;

        ArrayList<Set<V>> N=new ArrayList();

        for(V v : K)
        {
            Set Nv=new HashSet();
            Nv.addAll(G.getNeighbors(v));
            Nv.add(v);
            N.add(Nv);
        }

        Set Ni=N.get(0);
        int i=1;

        while(b && i<N.size())
        {
            Set temp=new HashSet();
            Sets.intersection(Ni, N.get(i)).copyInto(temp);
            Ni=temp;
            if(!Ni.containsAll(K))
                b=false;
            
            i++;
        }

        return b;
    }

    /**
     * Add all the vertices of set A to the graph G.
     * @param G graph
     * @param A set of vertices to add to G
     */
    public static<V,E> void addAllVertices(Graph<V,E> G, Set<V> A)
    {
        for(V v : A)
            G.addVertex(v);
    }



    /**
     * Remove all the vertices of set R of the graph G.
     * @param G graph
     * @param R set of vertices to remove of G
     */
    public static<V,E> void removeAllVertices(Graph<V,E> G, Set<V> R)
    {
        for(V v : R)
            G.removeVertex(v);
    }

    /**
     * Remove all the edges of set R of the graph G.
     * @param G graph
     * @param R set of edges to remove of G
     */
    public static<V,E> void removeAllEdges(Graph<V,E> G, Set<E> R)
    {
        for(E e : R)
            G.removeEdge(e);
    }


    /**
     * Ascending sort of a list of sets of vertices following their cardinality.
     * @param tab list of sets
     * @param p min boundary
     * @param r max boundary
     */
    public static<V> void quickSortSet(ArrayList<Set<V>> tab, int p, int r)
    {

        int q;
        if(p<r)
        {
            q = partitionSet(tab, p, r);
            quickSortSet(tab, p, q);
            quickSortSet(tab, q+1, r);
        }
    }

    private static<V> int partitionSet(ArrayList<Set<V>> tab, int p, int r)
    {
        int pivot = tab.get(p).size(), i = p-1, j = r+1;
        Set<V> temp;
        while(true)
        {
                do
                    j--;
                while(tab.get(j).size()> pivot);

                do
                    i++;
                while(tab.get(i).size()  < pivot);

                if(i<j) {
                        temp = new HashSet() ;
                        temp.addAll(tab.get(i));
                        //temp.setName(tab.get(i).getName());
                        tab.set(i, tab.get(j));
                        tab.set(j,temp);
                }
                else
                        return j;
        }
   }
    
    /**
     * Return a vertex of G choosen randomly.
     * @param G graph
     * @return a vertex choosen randomly.
     */
    public static<V,E> V chooseRandVertex(Graph<V,E> G)
    {
        V vertex=null;
        while(vertex==null)
            for(V v : G.getVertices())
                if(Math.random()<0.3)
                    vertex=v;

        return vertex;
    }

    /**
     * Returns density of a graph G.
     * @param G graph
     * @return density of G.
     */
    public static<V,E> double getDensity(Graph<V,E> G)
    {
        return (double)(2*G.getEdgeCount())/(G.getVertexCount()*(G.getVertexCount()-1));
    }
    
    /**
     * Randomly rewire edges in a graph.
     * @param G graph
     * @param amount number of edges to rewire.
     * @param edgeFactory factory used to create edge.
     */
    public static<V,E> void rewireEdgesRand(Graph<V,E> G, int amount, Factory<E> edgeFactory)
    {
        ArrayList<E> edgesR=new ArrayList();

        while(edgesR.size()<amount)
            for(E e : G.getEdges())
                if(Math.random()<0.3)
                    edgesR.add(e);

        for(E e : edgesR)
        {
            V v1=null;
            if(Math.random()<0.5)
                v1=G.getEndpoints(e).getFirst();
            else
                v1=G.getEndpoints(e).getSecond();

            V v2=null;
            Iterator<V> itr=G.getVertices().iterator();
            while(v2==null || v2.equals(G.getEndpoints(e).getFirst()) || v2.equals(G.getEndpoints(e).getSecond()) || G.findEdge(v1, v2)!=null)
               v2=itr.next();
            
            G.removeEdge(e);
            G.addEdge(edgeFactory.create(), v1, v2);
            
        }

     }

     /**
     * Randomly rewire a given edge in a graph.
     * @param G graph
     * @param edge edge to rewire.
     * @param edgeFactory factory used to create edge.
     */
    public static<V,E> void rewireEdge(Graph<V,E> G, E edge, Factory<E> edgeFactory)
    {
            V v1=null;
            if(Math.random()<0.5)
                v1=G.getEndpoints(edge).getFirst();
            else
                v1=G.getEndpoints(edge).getSecond();

            V v2=null;
            Iterator<V> itr=G.getVertices().iterator();
            while(v2==null || v2.equals(G.getEndpoints(edge).getFirst()) || v2.equals(G.getEndpoints(edge).getSecond()) || G.findEdge(v1, v2)!=null)
               v2=itr.next();

            G.removeEdge(edge);
            G.addEdge(edgeFactory.create(), v1, v2);
    }
    
}