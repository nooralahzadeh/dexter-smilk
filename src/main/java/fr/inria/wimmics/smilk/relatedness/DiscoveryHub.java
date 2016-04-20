/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.smilk.relatedness;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import java.util.ArrayList;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import fr.inria.wimmics.smilk.util.Utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fnoorala
 */
public class DiscoveryHub {

    //http://dbpedia.org/sparql //http://dbpedia-test.inria.fr/sparql
    private   String sparqlEndPoint ;
    public final   double thresholdCPD = 0.01;
    public     int importLimit;
    public     int maxPlus ;
    public final   String prefixes = "prefix rdfs: "
            + "<http://www.w3.org/2000/01/rdf-schema#>"
            + " prefix rdf: "
            + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            + " prefix dbo: "
            + "<http://dbpedia.org/ontology/>"
            + " prefix owl: "
            + "<http://www.w3.org/2002/07/owl#>"
            + "\n";

    public DiscoveryHub(String sparqlEndPoint , int maxplus, int importLimit) {
        this.sparqlEndPoint=sparqlEndPoint;
        this.maxPlus=maxplus;
        this.importLimit=importLimit;

    }

    public  HashMap<String, Double> getClassPropagationDomain(String name) throws Exception {

        HashMap<String, Double> cpd = new HashMap<>();
        String qry = ""
                + prefixes
                + "SELECT DISTINCT ?t ?tcount  ?s\n"
                + "{ ?t rdfs:subClassOf ?s\n"
                + "{ "
                + "SELECT DISTINCT ?t (count(?t) as ?tcount)   \n"
                + " WHERE {\n"
                + "{?x ?y ?z . ?z rdf:type ?t.   "
                + " FILTER (?x=<" + name + ">)\n"
                + " FILTER (regex(?z, \"http://dbpedia.org/resource\"))\n "
                + "}\n"
                + " UNION\n"
                + " {?z ?y ?x . ?z rdf:type ?t.  \n "
                + " FILTER (?x=<" + name + ">)\n"
                + "}\n"
                + " FILTER (regex(?t, \"http://dbpedia.org/ontology\"))\n "
                + " FILTER (?y!= dbo:wikiPageDisambiguates &&"
                + "         ?y!= dbo:wikiPageRedirects)"
                + " FILTER (regex(?z, \"http://dbpedia.org/resource\"))\n "
                + "  }\n"
                + "group by ?t  \n"
                + "}"
                // + "}"
                + "}";

     //  System.out.println(qry);
        //Query query = QueryFactory.create(qry);

        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndPoint, qry);
        ResultSet results = qexec.execSelect();
        // ResultSetFormatter.out(System.out, results, query);

        ArrayList<String> types = new ArrayList<>();
        ArrayList<String> superTypes = new ArrayList<>();

        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            RDFNode rel = soln.get("t");
            Resource r = (Resource) rel;

            if (soln.get("s") != null) {
                RDFNode superClass = soln.get("s");
                Resource superClass_res = (Resource) superClass;
                superTypes.add(superClass_res.getLocalName());
                // System.out.println(superClass_res.getURI());
            }

            RDFNode cnt = soln.get("tcount");

            types.add(r.getLocalName());

            Literal rcnt = (Literal) cnt;

            //cpd assignment
            cpd.put(r.getLocalName(), rcnt.getDouble());

            //   System.out.println(r.getURI() + "--- " + rcnt.getInt() + "---");
        }
        qexec.close();

        //deppest types
        Set<String> deepestTypes = new HashSet<String>(types);

        // System.out.println(deepestTypes);
        deepestTypes.removeAll(superTypes);
        ArrayList<String> deepestType = new ArrayList<>(deepestTypes);
        System.out.println("--------------------------------DeepestType starts here---------------");
        System.out.println(deepestType);

        //compute the childs of each deepestType based on the neighboor set: Class Propagation Domain CPD(O)
//        System.out.println("--------------------------------CPD (" + name + ")---------------");
        cpd.keySet().removeAll(superTypes);
//        System.out.println(cpd);
        //compute CPD(o)
        Double sum = 0.0;
        for (Double d : cpd.values()) {
            sum += d;
        }
        for (String d : cpd.keySet()) {
            cpd.put(d, cpd.get(d) * (1 / sum));
        }
//        System.out.println("--------------------------------Class Propagation Domain CPD(O) after compute and discared the ones less than threshold" + thresholdCPD + "--------------");

        List<String> discaredTypes = new ArrayList<>();
        for (String key : cpd.keySet()) {
            if (cpd.get(key) <= thresholdCPD) {
                //System.out.println(cpd.get(key));
                discaredTypes.add(key);
            }
        }
        cpd.keySet().removeAll(discaredTypes);

        System.out.println(Utility.entriesSortedByValues(cpd));
        return cpd;
    }

    public  LinkedHashMap<String, Double> computeSimilarity(Double act, String name, ArrayList<String> Neighbors)  {

      
            LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();
            
            
            try {  
           // System.out.println("-----------------------Common categories with Neighbors starts here---------------");
            
                String categoriesFilter=categoryOfcurrentNode(name);
            if (!categoriesFilter.equalsIgnoreCase("")) {
                
                Map<String, Double> tempHash = new HashMap<>();
                for (String ng : Neighbors) { 
                    
                    String simCount = "SELECT DISTINCT  (count(?prop) as ?cmtrip) \n"
                            + " WHERE {\n"
                            + "{<" + ng + ">  <http://purl.org/dc/terms/subject> ?prop\n }\n"
                            + " FILTER ( ?prop IN ( " + categoriesFilter + " ) )\n"
                            + "  }\n"
                            + "";
                    
 
                  //  System.out.println(simCount);
                   
                   // Query query_simCount = QueryFactory.create(simCount);
                    QueryExecution qexec_simCount = QueryExecutionFactory.sparqlService(sparqlEndPoint, simCount);
                    ResultSet results_simCount = qexec_simCount.execSelect();
                    
//                System.out.println("----------------  common triples with each Neighbor---------------");
//                System.out.println(ng);
              
                    
                    for (; results_simCount.hasNext();) {
                        QuerySolution soln = results_simCount.nextSolution();
                        RDFNode rel = soln.get("cmtrip");
                        Literal rcnt = (Literal) rel;
                        double Score = act * ((1 + rcnt.getDouble()) /  Neighbors.size());
                        tempHash.put(ng, Score);
//                    System.out.println(rcnt.getDouble() + "--" + Score);

                    }
                    qexec_simCount.close();
                }

//            System.out.println("---------------ActivationHashMap--------------");
                
                List<Map.Entry<String, Double>> r = Utility.entriesSortedByValues(tempHash);
                
                for (Map.Entry<String, Double> entry : r) {
                    sortedMap.put(entry.getKey(), entry.getValue());
                }
                
            }

            // System.out.println(sortedMap);
            
        } catch (Exception ex) {

            Logger.getLogger(DiscoveryHub.class.getName()).log(Level.SEVERE, null, ex);
        }
return sortedMap;
    }

    public  ArrayList<String> importNeighborhoodBasedOnCmt(String name, HashMap<String, Double> cpd) throws Exception {
        //filter type part
        //deppest types
        List<String> deepestTypes = new ArrayList<String>(cpd.keySet());
        String typeFilter = "";
        for (String typ : deepestTypes) {

            if (typ.equals(deepestTypes.get(deepestTypes.size() - 1))) {
                typeFilter += "?k1 = dbo:"
                        + typ
                        + " ";
                break;
            }

            typeFilter += "?k1= dbo:"
                    + typ
                    + " ||";

        }

        String outGoingNeighbors = ""
                + prefixes
                + " SELECT DISTINCT ?y1 \n"
                + " WHERE { \n"
                + " ?x ?p1 ?y1.\n"
               // + " OPTIONAL{?y1 rdf:type ?k1.}\n"
                + " ?y1 rdf:type ?k1. \n"
                + " FILTER (!isLiteral(?y1) && ?p1!=rdf:type\n"
                + " && ?p1!=owl:sameAs"
                + " && ?p1!=dbo:wikiPageDisambiguates"
                + " && ?p1!=dbo:wikiPageRedirects"
                
                + " && ?p1!=dbo:wikiPageInterlanguageLink)"
                + " FILTER (?x= <" + name + ">)\n"
                + " FILTER (regex(?y1, \"http://dbpedia.org/resource\"))\n "
                + " FILTER (!regex(?y1, \"http://dbpedia.org/resource/Template\"))\n "
                + " FILTER(!regex(?y1,\"http://dbpedia.org/resource/File:\") \n"
                // + " && !regex(?y1,\"http://dbpedia.org/resource/Category:\") \n"
                + " && !regex(?y1,\"http://dbpedia.org/resource/Template:\") \n"
                + " )\n"
               
                + " FILTER ("
                + typeFilter
                + ")"
               
                + "}";

        // System.out.println(outGoingNeighbors);
        //Query qry_OutNeighbors = QueryFactory.create(outGoingNeighbors);
        QueryExecution exe_OutNeighbors = QueryExecutionFactory.sparqlService(sparqlEndPoint, outGoingNeighbors);
        ResultSet RES_OutNeighbors = exe_OutNeighbors.execSelect();
        ArrayList<String> OutNeighbors = new ArrayList<>();

        for (; RES_OutNeighbors.hasNext();) {
            QuerySolution soln = RES_OutNeighbors.nextSolution();
            RDFNode rel = soln.get("y1");
            Resource r = (Resource) rel;
            OutNeighbors.add(r.getURI());
        }
        exe_OutNeighbors.close();

//           System.out.println(OutNeighbors);
        String inComingNeighbors = ""
                + prefixes
                + " SELECT DISTINCT ?y1 \n"
                + " WHERE { \n"
                + " ?y1 ?p1 ?x.\n"
             
                //+ " OPTIONAL{?y1 rdf:type ?k1} \n"
                + " ?y1 rdf:type ?k1 \n"
                + " FILTER (!isLiteral(?y1) && ?p1!=rdf:type\n"
                + " && ?p1!=owl:sameAs"
                + " && ?p1!=dbo:wikiPageDisambiguates"
                + " && ?p1!=dbo:wikiPageRedirects"
                + " && ?p1!=dbo:wikiPageInterlanguageLink)"
                + " FILTER (?x= <" + name + ">)\n"
                + " FILTER (regex(?y1, \"http://dbpedia.org/resource\"))\n "
                + " FILTER(!regex(?y1,\"http://dbpedia.org/resource/File:\") \n"
                // + " && !regex(?y1,\"http://dbpedia.org/resource/Category:\") \n"
                + " && !regex(?y1,\"http://dbpedia.org/resource/Template:\") \n"
                + " )\n"
              
                + " FILTER ("
                + typeFilter
                + ")"
                + "}";

//         System.out.println(inComingNeighbors);
       // Query qry_in_neighbors = QueryFactory.create(Utility.prepareQuery(inComingNeighbors));
        QueryExecution exe_in_neighbors = QueryExecutionFactory.sparqlService(sparqlEndPoint, inComingNeighbors);
        ResultSet RES_in_neighbors = exe_in_neighbors.execSelect();

        ArrayList<String> in_neighbors = new ArrayList<>();

        for (; RES_in_neighbors.hasNext();) {
            QuerySolution soln = RES_in_neighbors.nextSolution();
            RDFNode rel = soln.get("y1");
            Resource r = (Resource) rel;
            in_neighbors.add(r.getURI());
        }

        exe_in_neighbors.close();
       
        List<String> in_neighbors_copy = new ArrayList<>(in_neighbors);
        in_neighbors_copy.removeAll(OutNeighbors);
        OutNeighbors.addAll(in_neighbors_copy);
        
        return OutNeighbors;

    }

    public Set<String> discover(String name) {
        Set<String> finalNeighbors = new HashSet<String>();
        List<String> alreadyActivated = new ArrayList<String>();

        String uriName = "http://dbpedia.org/resource/" + name.trim().replaceAll("\\s", "_");

        LinkedHashMap<String, Double> ActivationHashMap = new LinkedHashMap<>();
        ActivationHashMap.put(uriName, 1.0);
        HashMap<String, Double> cpd = new HashMap<>();
        ArrayList<String> neighbors = new ArrayList<>();

        int i = 0;
        while (i < maxPlus) {

            LinkedHashMap<String, Double> tempMap = new LinkedHashMap<>();
           
            for (String activationNode : ActivationHashMap.keySet()) {
                
                if (alreadyActivated.contains(activationNode)) {
                    continue;
                } else {
                   
                    alreadyActivated.add(activationNode);

               System.out.println("ActivatedNode--->" + activationNode + " with score:--- " + ActivationHashMap.get(activationNode));
                   System.out.println();

                    if (ActivationHashMap.get(activationNode) > 0) {
                        try {
                            cpd = getClassPropagationDomain(activationNode);
                            if (!cpd.isEmpty()) {
                                neighbors = importNeighborhoodBasedOnCmt(activationNode, cpd);
                             
                              //  System.out.println(neighbors);
                                
                                finalNeighbors.addAll(neighbors);
                                finalNeighbors.remove(uriName);
                               
                                if (finalNeighbors.size() >= importLimit) {
                                    
                                    return finalNeighbors;
                                }
                                
                            }
                            if (!neighbors.isEmpty()) {
                                tempMap.putAll(computeSimilarity(ActivationHashMap.get(activationNode), activationNode, neighbors));
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(DiscoveryHub.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                 
                   
                } 
            }  
             i += 1;
           System.out.println("i is ------------->" + i);
            ActivationHashMap.putAll(tempMap);
             System.out.println("imported ------------->" + finalNeighbors.size());
        }
        
       System.out.println("\n Final result: "+name+ ": " + (finalNeighbors));
        
        return finalNeighbors;

    }

     public  ArrayList<String> importNeighbors(String name) throws Exception 
    {
        

        String outGoingNeighbors = ""
                + prefixes
                + " SELECT DISTINCT ?y1 \n"
                + " WHERE { \n"
                + " ?x ?p1 ?y1.\n"
                + " OPTIONAL{?y1 rdf:type ?k1.}\n"
         
                + " FILTER (!isLiteral(?y1) && ?p1!=rdf:type\n"
                + " && ?p1!=owl:sameAs"
                + " && ?p1!=dbo:wikiPageDisambiguates"
                + " && ?p1!=dbo:wikiPageRedirects"
                
                + " && ?p1!=dbo:wikiPageInterlanguageLink)"
                + " FILTER (?x= <" + name + ">)\n"
                + " FILTER (regex(?y1, \"http://dbpedia.org/resource\"))\n "
                + " FILTER (!regex(?y1, \"http://dbpedia.org/resource/Template\"))\n "
                + " FILTER(!regex(?y1,\"http://dbpedia.org/resource/File:\") \n"
                // + " && !regex(?y1,\"http://dbpedia.org/resource/Category:\") \n"
                + " && !regex(?y1,\"http://dbpedia.org/resource/Template:\") \n"
                + " )\n"
               
              
               
                + "}";

        // System.out.println(outGoingNeighbors);
        //Query qry_OutNeighbors = QueryFactory.create(outGoingNeighbors);
        QueryExecution exe_OutNeighbors = QueryExecutionFactory.sparqlService(sparqlEndPoint, outGoingNeighbors);
        ResultSet RES_OutNeighbors = exe_OutNeighbors.execSelect();
        ArrayList<String> OutNeighbors = new ArrayList<>();

        for (; RES_OutNeighbors.hasNext();) {
            QuerySolution soln = RES_OutNeighbors.nextSolution();
            RDFNode rel = soln.get("y1");
            Resource r = (Resource) rel;
            OutNeighbors.add(r.getURI());
        }
        exe_OutNeighbors.close();

//           System.out.println(OutNeighbors);
        String inComingNeighbors = ""
                + prefixes
                + " SELECT DISTINCT ?y1 \n"
                + " WHERE { \n"
                + " ?y1 ?p1 ?x.\n"
             
                + " OPTIONAL{?y1 rdf:type ?k1} \n"
                + " FILTER (!isLiteral(?y1) && ?p1!=rdf:type\n"
                + " && ?p1!=owl:sameAs"
                + " && ?p1!=dbo:wikiPageDisambiguates"
                + " && ?p1!=dbo:wikiPageRedirects"
                + " && ?p1!=dbo:wikiPageInterlanguageLink)"
                + " FILTER (?x= <" + name + ">)\n"
                + " FILTER (regex(?y1, \"http://dbpedia.org/resource\"))\n "
                + " FILTER(!regex(?y1,\"http://dbpedia.org/resource/File:\") \n"
                // + " && !regex(?y1,\"http://dbpedia.org/resource/Category:\") \n"
                + " && !regex(?y1,\"http://dbpedia.org/resource/Template:\") \n"
                + " )\n"
              
              
                + "}";

//         System.out.println(inComingNeighbors);
       // Query qry_in_neighbors = QueryFactory.create(Utility.prepareQuery(inComingNeighbors));
        QueryExecution exe_in_neighbors = QueryExecutionFactory.sparqlService(sparqlEndPoint, inComingNeighbors);
        ResultSet RES_in_neighbors = exe_in_neighbors.execSelect();

        ArrayList<String> in_neighbors = new ArrayList<>();

        for (; RES_in_neighbors.hasNext();) {
            QuerySolution soln = RES_in_neighbors.nextSolution();
            RDFNode rel = soln.get("y1");
            Resource r = (Resource) rel;
            in_neighbors.add(r.getURI());
        }

        exe_in_neighbors.close();
       
        List<String> in_neighbors_copy = new ArrayList<>(in_neighbors);
        in_neighbors_copy.removeAll(OutNeighbors);
        OutNeighbors.addAll(in_neighbors_copy);
        
        return OutNeighbors;

    }

    public  String categoryOfcurrentNode(String node) throws Exception {

        String qry = ""
                + prefixes
                + "SELECT DISTINCT ?z \n"
                + " WHERE"
                + "\n"
                + "{ "
                + "{"
                + "SELECT DISTINCT ?z  where {"
                + "?x ?y ?z \n"
                + " FILTER (?x= <" + node + ">)\n"
                + " FILTER (?y= <http://purl.org/dc/terms/subject> )"
                + "  }\n"
                + "}\n"
                + "UNION"
                + "{\n"
                + "SELECT DISTINCT ?z  where {"
                + "?x ?y ?w. \n"
                + "?z <http://www.w3.org/2004/02/skos/core#broader> ?w"
                + " FILTER (?x= <" + node + ">)\n"
                + " FILTER (?y= <http://purl.org/dc/terms/subject> )"
                + "  }\n"
                + "}\n"
                + "}";

       // System.out.println(qry);
        //Query query = QueryFactory.create(Utility.prepareQuery(qry));
        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndPoint, qry);
        ResultSet results = qexec.execSelect();
        // ResultSetFormatter.out(System.out, results, query);

        ArrayList<String> categories = new ArrayList<>();
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            RDFNode rel = soln.get("z");
            Resource r = (Resource) rel;
            // System.out.println(r.getURI());
            categories.add(r.getURI());
        }

        qexec.close();
        //construct category filter part
        String categoryFilter = "";
        for (String cat : categories) {

            if (cat.equals(categories.get(categories.size() - 1))) {
                categoryFilter += "<"
                        + cat
                        + "> ";
                break;
            }

            categoryFilter += " <"
                    + cat
                    + "> ,\n";

        }
        //System.out.println(categoryFilter);
        return (categoryFilter);

    }

   

    
}
