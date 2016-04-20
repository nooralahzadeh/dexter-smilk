/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.smilk.spotter;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author fnoorala
 */
public class CandidateGen {

    private String sparqlEndPoint;
    private String lang;
    private String category;
    private String disambiguation;
    private final String uri;
    private final String langFilter;
    private final String resourceFilter;
    private final String filterNoise;
    private final String prefixes = "prefix rdfs: "
            + "<http://www.w3.org/2000/01/rdf-schema#>"
            + " prefix rdf: "
            + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            + " prefix dbo: "
            + "<http://dbpedia.org/ontology/>"
            + " prefix owl: "
            + "<http://www.w3.org/2002/07/owl#>"
            + " prefix foaf: "
            + "<http://xmlns.com/foaf/0.1/>"
            + " prefix fn:"
            + "<http://www.w3.org/2005/xpath-functions/#>"
            + "\n";

    public CandidateGen(String sparqlEndPoint, String lang) {
        this.sparqlEndPoint = sparqlEndPoint;
        this.lang = lang;
        uri = sparqlEndPoint.replaceAll("sparql", "") + "resource";
        langFilter = " FILTER (lang(?s) =" + "\"" + lang + "\") ";

        if (lang.equalsIgnoreCase("en")) {
            category = "Category";
            disambiguation="(disambiguation)";

        } else if (lang.equalsIgnoreCase("fr")) {
            category = "Catégorie";
            disambiguation="(homonymie)";
        }
        
        resourceFilter = " FILTER (regex(str(?o), \"" + uri + "\"))\n"
                + " FILTER(!regex(str(?o),\"" + uri + "/File:\") \n"
                + " && !regex(str(?o),\"" + uri + "/" + category + ":\") \n"
                + " && !regex(str(?o),\"" + uri + "/Template:\")\n"
                + " && !regex(str(?o),\"" + uri + "/List\")\n"
                + " && !regex(str(?o),\""+disambiguation + "\"))\n";

        filterNoise = " FILTER(fn:string-length(fn:substring-after(?o,\"" + uri + "/\"))>1)";

      
    }

    //   private final String resourceFilter10 = " FILTER (fn:starts-with(fn:string(?o), \"http://fr.dbpedia.org/resource\"))\n";
    // + " FILTER(!fn:starts-with(?o,\"http://fr.dbpedia.org/resource/File:\") \n"
//            + " && !fn:starts-with(?o,\"http://fr.dbpedia.org/resource/Catégorie:\") \n"
//            + " && !fn:starts-with(?o,\"http://fr.dbpedia.org/resource/Template:\"))\n";
    //private final static String noisFilter=" BIND(REPLACE(str(?o), '^.*(#|/)', \"\") AS ?localname).\n"
    //         +" BIND(STRLEN(STR(?localname)) as ?len ).\n"
    //         + "FILTER(?len>1)";
    public Set<Resource> candidates = new HashSet<>();

    public void LabelBase(String name) {
        //label
        String querystr_Label = " SELECT DISTINCT ?s    WHERE { ?s rdfs:label ?o."
                //  + " FILTER (regex(?o, \"" + "^" + name + "\",'i' ) " + langFilter)+
                + " FILTER (regex(?o, \"" + name + "\",'i' ) " + ")" + resourceFilter
                + "  }\n"
                //  + "LIMIT   10\n"
                + "";

        //System.out.println(querystr_Label);
        Query query = QueryFactory.create(prepareQuery(querystr_Label));

        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndPoint, query);
        ResultSet results = qexec.execSelect();
//        ResultSetFormatter.out(System.out, results, query);
        try {

            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();
                RDFNode rel = soln.get("s");
                Resource r = (Resource) rel;

                candidates.add(r);

            }

        } finally {
            qexec.close();
        }

    }

    public void disambiguationBase(String name) {
        //disambiguation
        String querystr_dsm = " SELECT DISTINCT ?o WHERE { ?s dbo:wikiPageDisambiguates ?o."
                //  + " FILTER (regex(?o, \"" + "^" + name + "\",'i' ) " + langFilter)+
                + " FILTER (regex(str(?o), \"" + name + "\",'i' ) )" + resourceFilter
                + "  }\n"
                //  + "LIMIT   10\n"
                + "";

        Query query = QueryFactory.create(prepareQuery(querystr_dsm));

        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndPoint, query);
        ResultSet results = qexec.execSelect();
//        ResultSetFormatter.out(System.out, results, query);
        try {

            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();

                RDFNode cnt = soln.get("o");

                if (cnt.isResource()) {
                    Resource rcnt = (Resource) cnt;
                    candidates.add(rcnt);

                }

            }

        } finally {
            qexec.close();
        }
    }

    public void redirectBase(String name) {
        //redirect
        String querystr_redrct = " SELECT DISTINCT  ?o WHERE { ?s dbo:wikiPageRedirects ?o."
                //  + " FILTER (regex(?o, \"" + "^" + name + "\",'i' ) " + langFilter)+
                + " FILTER (regex(?o, \"" + name + "\",'i' )) " + resourceFilter
                + "  }\n"
                // + "LIMIT   10\n"
                + "";
        Query query = QueryFactory.create(prepareQuery(querystr_redrct));

        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndPoint, query);
        ResultSet results = qexec.execSelect();
//        ResultSetFormatter.out(System.out, results, query);
        try {

            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();

                RDFNode cnt = soln.get("o");

                if (cnt.isResource()) {
                    Resource rcnt = (Resource) cnt;
                    candidates.add(rcnt);
                    // System.out.println(r.getURI() + "--- " + rcnt.getURI());
                }

            }

        } finally {
            qexec.close();
        }
    }

    public Set<String> canndidateGeneration(String name) throws Exception {

        String label = Normalizer.normalize(name, Normalizer.Form.NFD);
        label = label.replaceAll("[^\\p{ASCII}]", "");
        label = label.replaceAll("'", "_");
        Set<String> candidates = new HashSet<>();

        ///should be implemented the dbCanonicalName
        String dbCanocicalName = FormatAsDbpediaNaming(label);
        name = name.replaceAll("'", "`");

        //label
        String querystr_Label = "SELECT DISTINCT ?o  "
                + " WHERE {?o rdfs:label ?s. \n"
                + " ?s <bif:contains> " + "\"\'" + name + "\'\"\n"
                + langFilter + "\n"
                + " }\n"
                + "";

        //name
        String querystr_name = "SELECT DISTINCT ?o  "
                + " WHERE {?o foaf:name ?s. \n"
                + " ?s <bif:contains> " + "\"\'" + name + "\'\"\n"
                + langFilter + "\n"
                + " }\n"
                + "";

        //redirect
        String querystr_redrct = " SELECT DISTINCT ?o WHERE { ?o dbo:wikiPageRedirects  <" + uri + "/" + dbCanocicalName + "> .\n"
               // + "?disPage dbo:wikiPageRedirects ?o .\n"
                + filterNoise
                + "  }\n"
                + "";

        String querystr_redrct2 = " SELECT DISTINCT  ?o WHERE {\n"
                + "<" + uri + "/" + dbCanocicalName + ">  dbo:wikiPageRedirects ?o ."
                + "  }\n"
                + "";

     // disambiguation
        String querystr_dsm = " SELECT DISTINCT  ?o WHERE { ?disPage dbo:wikiPageDisambiguates <" + uri + "/" + dbCanocicalName + "> .\n"
                + "?disPage dbo:wikiPageDisambiguates ?o .\n"
                + filterNoise
                + "  }\n"
                + "";

        String querystr_dsm2 = " SELECT DISTINCT  ?o WHERE {\n"
                + "<" + uri + "/" + dbCanocicalName + ">  dbo:wikiPageDisambiguates ?o .\n"
                + "  }\n"
                + "";

        String unionQry = prepareUnionQuery(querystr_Label, querystr_name);
        //apply filtering

     // System.out.println(unionQry);
      //  Query query = QueryFactory.create(unionQry);

        //  ResultSetFormatter.out(System.out, results, query);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndPoint, unionQry);
        try {

            ResultSet results = qexec.execSelect();
            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();
                RDFNode rel = soln.get("o");
                Resource r = (Resource) rel;

                if (r.isResource()) {
                    candidates.add(r.getURI());
                    //System.out.println(r.getURI());
                }

            }

        } finally {
            qexec.close();
        }
        
        
        return candidates;
    }

    /**
     *
     * @param name
     * @return
     * @throws Exception
     */
    public List<Resource> canndidateResourceGeneration(String name) throws Exception {

//        String  label = Normalizer.normalize(name, Normalizer.Form.NFD);
//        label= label.replaceAll("[^\\p{ASCII}]", "");
//        label=label.replaceAll("'", "_");
        List<Resource> candidates = new ArrayList<>();

        ///should be implemented the dbCanonicalName
        String dbCanocicalName = name;

        //label
        String querystr_Label = "SELECT DISTINCT ?o  "
                + " WHERE {?o rdfs:label ?s. \n"
                + " ?s <bif:contains> " + "\"\'" + name + "\'\"\n"
                + langFilter + "\n"
                + " }\n"
                + "";

        //redirect
        String querystr_redrct = " SELECT DISTINCT  ?o WHERE { ?disPage dbo:wikiPageRedirects  <http://fr.dbpedia.org/resource/" + dbCanocicalName + "> .\n"
                + "?disPage dbo:wikiPageDisambiguates ?o ."
                + "  }\n"
                + "";

        String querystr_redrct2 = " SELECT DISTINCT  ?o WHERE {\n"
                + "<http://fr.dbpedia.org/resource/" + dbCanocicalName + ">  dbo:wikiPageRedirects ?o ."
                + "  }\n"
                + "";

        //disambiguation
        String querystr_dsm = " SELECT DISTINCT  ?o WHERE { ?disPage dbo:wikiPageDisambiguates <" + "http://fr.dbpedia.org/resource/" + dbCanocicalName + "> .\n"
                + "?disPage dbo:wikiPageDisambiguates ?o ."
                + "  }\n"
                + "";

        String querystr_dsm2 = " SELECT DISTINCT  ?o WHERE {\n"
                + "<http://fr.dbpedia.org/resource/" + dbCanocicalName + ">  dbo:wikiPageDisambiguates ?o ."
                + "  }\n"
                + "";

        String unionQry = prepareUnionQuery(querystr_Label, querystr_dsm, querystr_dsm2, querystr_redrct, querystr_redrct2);
        //apply filtering

         System.out.println(unionQry);
        Query query = QueryFactory.create(unionQry);

        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndPoint, query);
        ResultSet results = qexec.execSelect();
        //  ResultSetFormatter.out(System.out, results, query);

        try {

            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();
                RDFNode rel = soln.get("o");
                Resource r = (Resource) rel;

                if (r.isResource()) {
                    candidates.add(r);
                    //System.out.println(r.getURI());
                }

            }

        } finally {
            qexec.close();
        }
        return candidates;
    }

    private String prepareUnionQuery(String... args) throws Exception {
        String qry = "";

        for (String arg : args) {
            if (arg.equals(args[args.length - 1])) {
                qry += "{"
                        + arg
                        + "}";
                break;
            }
            qry += "{"
                    + arg
                    + "} UNION ";
        }
        return (prefixes +"\n SELECT DISTINCT * WHERE { " + qry + "\n"
                + resourceFilter + "\n"
                + "}");
    }

    public List<String> candidate(String name) throws Exception {

        //label
        String querystr_Label = "SELECT DISTINCT ?s   WHERE {?s rdfs:label ?o. \n"
                + " ?o <bif:contains> " + "\"" + name + "\"\n"
                + langFilter + "\n"
                + resourceFilter + "\n"
                + "  }\n"
                + "";

        //System.out.println(querystr_Label);
        Query query = QueryFactory.create(prepareQuery(querystr_Label));

        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndPoint, query);
        ResultSet results = qexec.execSelect();
        // ResultSetFormatter.out(System.out, results, query);
        try {
            List<String> candidates = new ArrayList<>();
            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();
                RDFNode rel = soln.get("s");
                Resource r = (Resource) rel;

                candidates.add(r.getURI());

            }
            //  System.out.println(candidates);
            return candidates;

        } finally {
            qexec.close();

        }

    }

    public static String FormatAsDbpediaNaming(String string) {

        string = unCamelCase(string.trim());
        string = string.replace(" ", "_");
        string = string.replaceAll("_+", "_");

        return string.trim();

    }

    public static String unCamelCase(String string) {
        List<String> tokens = new ArrayList<String>();

        Collections.addAll(tokens, string.split("\\s+"));
        StringBuilder newLabel = new StringBuilder();

        for (String token : tokens) {

            //the first token must be CamleCase even it is not noun, the other token must be camelcase if they are noun
            if (tokens.get(0).equalsIgnoreCase(token)) {
                newLabel.append(Character.toUpperCase(token.charAt(0)) + token.substring(1) + " ");

                continue;
            } else {
                if (isNoun(token)) {
                    newLabel.append(Character.toUpperCase(token.charAt(0)) + token.substring(1) + " ");

                } else {
                    newLabel.append(token.toLowerCase() + " ");
                }
            }
        }
        return newLabel.toString().trim();

    }

    public static boolean isNoun(String token) {

        return true;
    }

    public String prepareQuery(String query) {
        String q = "";
        try {
            q = prefixes
                    + " " + query;
        } catch (Exception ex) {
            Logger.getLogger(CandidateGen.class.getName()).log(Level.SEVERE, null, ex);
        }
        return q;
    }

}
