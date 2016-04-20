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
package fr.inria.wimmics.smilk.annotation.datasets;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import fr.inria.wimmics.smilk.Smilk;
import fr.inria.wimmics.smilk.annotation.wikipedia.AnnotatedSpot;
import fr.inria.wimmics.smilk.eval.AssessmentRecord;
import it.cnr.isti.hpc.wikipedia.article.Language;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.RDFFormat;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author fnoorala
 */
public class SmilkNIF {

    private static final String itsNamespace = "http://www.w3.org/2005/11/its#";
    private static final String nifNamespace = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";
    private static final String dbpediaNamespace = "http://dbpedia.org/resource/";
    private static final String dulNamespace = "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#";
    private static final String okeNamespace = "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/";
    private static final String d0eNamespace = "http://ontologydesignpatterns.org/ont/wikipedia/d0.owl#";
    private static final String koreNamespace = "http://www.mpi-inf.mpg.de/yago-naga/aida/download/KORE50.tar.gz/AIDA.tsv/";
    

    /**
     *
     * Annotate the NIF document.
     *
     * @param filePath the path to the NIF document
     * @param rdfLang the rdf language of the NIF document
     * @param smilk an instance of smilk annotator to be used to annotate the
     * document
     * @param language the language of the document
     * @return an enriched string version of the NIF document
     * @throws IOException
     */
    public static String smilkNifFormat(String filePath, Lang rdfLang, Smilk smilk)
            throws IOException {

        OntModel corpus = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        // Reading the NIF document.
        InputStream inputStream = new FileInputStream(filePath);

        String modifiedNiFFile = preProcessing(inputStream);

        if (modifiedNiFFile != null) {
            InputStream stream = new ByteArrayInputStream(modifiedNiFFile.getBytes(StandardCharsets.UTF_8));
            RDFDataMgr.read(corpus, stream, rdfLang);
        }

        inputStream.close();

        // Extracting the texts contained in the NIF document.
        Query query = QueryFactory.create("prefix nif: "
                + "<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>"
                + "prefix itsrdf: "
                + "<http://www.w3.org/2005/11/its/rdf#>"
                + " SELECT distinct ?context ?string WHERE {"
                + "?context a nif:Context . ?context nif:isString ?string .}");

        QueryExecution qexec = QueryExecutionFactory.create(query, corpus);
        ResultSet result = qexec.execSelect();

        //ResultSetFormatter.out(System.out, result, query);
//        //Extract subModel from main Model
//        String query_string="prefix nif: "
//                + "<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>"
//                + " CONSTRUCT {?subject ?rel ?object} WHERE {"
//                + " ?subject ?rel ?object."
//                + " filter regex(?subject,?context, \"i\" )\n" 
//                +"  { SELECT distinct ?context  WHERE {"
//                + "?context a nif:Context .} } }";
//    
//        
//        Query subQuery=QueryFactory.create(query_string);
//        
//        
//        QueryExecution Sub_qexec = QueryExecutionFactory.create(subQuery, corpus);
//        Model subCorpus = Sub_qexec.execConstruct();
        OntModel newCorpus = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        newCorpus.setNsPrefixes(corpus.getNsPrefixMap());
        OntClass person = newCorpus.createClass(dulNamespace + "Person");
        OntClass organization = newCorpus.createClass(dulNamespace + "Organization");
        OntClass role = newCorpus.createClass(dulNamespace + "Role");
        OntClass place = newCorpus.createClass(dulNamespace + "Place");
        OntClass mis = newCorpus.createClass(dulNamespace + "InformationEntity");

        while (result.hasNext()) {
            QuerySolution solution = result.next();
            String text = solution.get("string").asLiteral().getString();
            String namespace = solution.get("context").toString();
            int end = Integer.parseInt(namespace.replaceAll(okeNamespace + "sentence-\\d+", "").replace("#char=0,", ""));
            Resource resource = newCorpus.createResource(namespace);

            // Set the anchor text associated with this annotation.
            newCorpus.add(resource,
                    newCorpus.createProperty(nifNamespace + "isString"),
                    newCorpus.createLiteral(text, Language.EN));

            // Set the annotation char offset.
            newCorpus.add(resource,
                    newCorpus.createProperty(nifNamespace + "beginIndex"),
                    newCorpus.createTypedLiteral(0));
            newCorpus.add(resource,
                    newCorpus.createProperty(nifNamespace + "endIndex"),
                    newCorpus.createTypedLiteral(end));

            newCorpus.add(resource, RDF.type, newCorpus.createResource(nifNamespace + "RFC5147String"));
            newCorpus.add(resource, RDF.type, newCorpus.createResource(nifNamespace + "String"));
            newCorpus.add(resource, RDF.type, newCorpus.createResource(nifNamespace + "Context"));
            System.out.println(text);
            // Annotate the documents.

            List<AnnotatedSpot> annotatedSpots = smilk.Smilk(text);
            for (AnnotatedSpot annotatedSpot : annotatedSpots) {
                addAnnotation(annotatedSpot, resource, newCorpus);
                StringWriter swriter = new StringWriter();
                RDFDataMgr.write(swriter, newCorpus, Lang.TTL);
                // System.out.println(swriter.toString());
            }
        }

        qexec.close();
        // Write the generated NIF document to a String.
        StringWriter swriter = new StringWriter();
        RDFDataMgr.write(swriter, corpus, RDFFormat.TURTLE);
        return swriter.toString();
    }

    /**
     * Adding a semantic annotation to the model.
     *
     * @param ann the Semantic Annotation to be added
     * @param text the input text given to Smilk
     * @param contextNamespace the reference id of the text
     * @param dataset the model
     */
    private static void addAnnotation(AnnotatedSpot ann, Resource rs, OntModel dataset) {
        String sentence = rs.getNameSpace().replace(okeNamespace + "sentence-", "");
        String sentenceNumber = sentence.substring(0, sentence.lastIndexOf("#"));
        int startAnn = ann.getStart();
        int endAnn = ann.getEnd() + 1;
        String uri_dbpedia = "";

        Resource dbpresource = null;

        if (!ann.getWikiname().equalsIgnoreCase("NIL")) {
            uri_dbpedia = dbpediaNamespace + ann.getWikiname();
            dbpresource = dataset.createResource(uri_dbpedia);
        }

        // Create the id of the annotation.
        String uri = okeNamespace + "sentence-" + sentenceNumber + "#char=" + startAnn + "," + endAnn;
        Resource resource = dataset.createResource(uri);

        // Set the type of the id.
        dataset.add(resource, RDF.type, dataset.createResource(nifNamespace + "RFC5147String"));
        dataset.add(resource, RDF.type, dataset.createResource(nifNamespace + "String"));

        // Set the anchor text associated with this annotation.
        Literal label = dataset.createLiteral(ann.getSpot());
        dataset.add(resource,
                dataset.createProperty(nifNamespace + "anchorOf"),
                dataset.createLiteral(ann.getSpot(), Language.EN));

        // Set the annotation char offset.
        dataset.add(resource,
                dataset.createProperty(nifNamespace + "beginIndex"),
                dataset.createTypedLiteral(startAnn));
        dataset.add(resource,
                dataset.createProperty(nifNamespace + "endIndex"),
                dataset.createTypedLiteral(endAnn));

        // A link back to the resource containing the input text.
        dataset.add(resource,
                dataset.createProperty(nifNamespace + "referenceContext"),
                rs);

//		// Adding a link to the SMILK RDF version
//		dataset.add(resource,
//				dataset.createProperty(itsNamespace, "taIdentRef"),
//				dataset.createResource(ann.getBabelNetURL()));
        //  Creat a Individual statement  for  entity  and Adding a link to the sentence
        if (dbpresource != null) {
            Individual individual = dataset.createIndividual(okeNamespace + ann.getWikiname(), dataset.getOntClass("mis"));
            individual.addProperty(OWL.sameAs, dbpresource);
            individual.addProperty(RDFS.label, label);

            dataset.add(resource,
                    dataset.createProperty(itsNamespace, "taIdentRef"),
                    individual);
        }
    }

    public static List<AssessmentRecord> readOKEAnnotation(String filePath, Lang rdfLang, boolean with) {

        List<AssessmentRecord> records = new ArrayList<AssessmentRecord>();

        OntModel corpus = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        try {

            // Reading the NIF document.
            InputStream inputStream = new FileInputStream(filePath);
            String modifiedNiFFile = preProcessing(inputStream);

            if (modifiedNiFFile != null) {
                InputStream stream = new ByteArrayInputStream(modifiedNiFFile.getBytes(StandardCharsets.UTF_8));
                RDFDataMgr.read(corpus, stream, rdfLang);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SmilkNIF.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Extracting the texts contained in the NIF document.
        Query query = QueryFactory.create("prefix nif: "
                + "<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>"
                + "prefix itsrdf: "
                + "<http://www.w3.org/2005/11/its/rdf#>"
                + " SELECT distinct ?context ?string WHERE {"
                + "?context a nif:Context . ?context nif:isString ?string .}");

        QueryExecution qexec = QueryExecutionFactory.create(query, corpus);
        ResultSet result = qexec.execSelect();

        //ResultSetFormatter.out(System.out, result, query);
        Map<Integer, String> documents = new HashMap<Integer, String>(0);
        while (result.hasNext()) {
            QuerySolution solution = result.next();
            String text = solution.get("string").asLiteral().getString();
            String namespace = solution.get("context").toString();
            String tmp = namespace.replaceAll(okeNamespace + "sentence-", "");
            int sentenceNumber = Integer.parseInt(tmp.substring(0, tmp.indexOf("#")));
            documents.put(sentenceNumber, text);
        }

        qexec.close();

        // Extracting the spots contained in the NIF document. 
        Query spot_query = QueryFactory.create("prefix nif: "
                + "<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>"
                + "prefix itsrdf: "
                + "<http://www.w3.org/2005/11/its/rdf#>"
                + " SELECT distinct ?spot ?anchor ?start ?end ?refContext ?oke WHERE {"
                + " ?spot nif:anchorOf ?anchor ."
                + " ?spot nif:beginIndex ?start ."
                + " ?spot nif:endIndex   ?end   ."
                + " ?spot nif:referenceContext ?refContext."
                + " ?spot itsrdf:taIdentRef ?oke .}");

        QueryExecution spot_qexec = QueryExecutionFactory.create(spot_query, corpus);
        ResultSet spot_result = spot_qexec.execSelect();

        Map<AnnotatedSpot, Integer> spots = new HashMap<AnnotatedSpot, Integer>();
        Map<AnnotatedSpot, String> entities = new HashMap<AnnotatedSpot, String>();

        while (spot_result.hasNext()) {
            AnnotatedSpot sp = new AnnotatedSpot();
            QuerySolution solution = spot_result.next();
            String text = solution.get("anchor").asLiteral().getString();
            int start = solution.get("start").asLiteral().getInt();
            int end = solution.get("end").asLiteral().getInt();
            String namespace = solution.get("refContext").toString();
            String tmp = namespace.replaceAll(okeNamespace + "sentence-", "");
            String reference = tmp.substring(0, tmp.indexOf("#"));
            sp.setDocId(reference);
            sp.setSpot(text);
            sp.setStart(start);
            sp.setEnd(end);
            spots.put(sp, Integer.parseInt(reference));
            //entities
            String oke = solution.get("oke").asResource().getURI().replaceAll("http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/", "");
            entities.put(sp, oke);

        }

        spot_qexec.close();

        // Extracting the entities contained in the NIF document. 
        Query entity_query = QueryFactory.create("prefix owl: "
                + "<http://www.w3.org/2002/07/owl#>"
                + "prefix oke:"
                + "<http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/>"
                + " SELECT distinct ?oke ?dul ?dbpedia  WHERE {"
                + " ?oke a ?dul . "
                + " OPTIONAL{?oke owl:sameAs ?dbpedia .} "
                + " FILTER (regex(str(?dul), \"DUL.owl#\",\"i\") || regex(str(?dul), \"d0.owl#\",\"i\"))"
                + "}");

        QueryExecution entity_qexec = QueryExecutionFactory.create(entity_query, corpus);
        ResultSet entity_result = entity_qexec.execSelect();

        Map<String, String> dbpediaLinks = new HashMap<String, String>();
        Map<String, String> entityTypes = new HashMap<String, String>();
        while (entity_result.hasNext()) {

            QuerySolution solution = entity_result.next();

            //entities
            String oke = solution.get("oke").asResource().getURI().replaceAll("http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/", "");
            String type = solution.get("dul").asResource().getURI().replaceAll("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#", "")
                    .replace("http://ontologydesignpatterns.org/ont/wikipedia/d0.owl#", "");
            String dbpedia = "NIL";
            if (solution.get("dbpedia") != null) {
                dbpedia = solution.get("dbpedia").asResource().getURI().replaceAll("http://dbpedia.org/resource/", "");
            }

            dbpediaLinks.put(oke, dbpedia);
            entityTypes.put(oke, type);

        }

        entity_qexec.close();

        //extracting the type and dbpedia resource name for each spot
        for (AnnotatedSpot sp : entities.keySet()) {

            String oke = entities.get(sp);
          
            if(with){
            sp.setType(entityTypes.get(oke));
            }
            else{
                sp.setType("MISC");
            }
            sp.setEntity(dbpediaLinks.get(oke));
        }

        //creating the document records
        for (int index : documents.keySet()) {
            AssessmentRecord record = new AssessmentRecord();
            record.setDocId("" + index);
            record.setText(documents.get(index));

            List<AnnotatedSpot> list = new ArrayList<AnnotatedSpot>();
            for (AnnotatedSpot sp : spots.keySet()) {

                if (spots.get(sp) == index) {
//                    if (with) {
                        list.add(sp);
//                    } else {
//                        if (!sp.getType().equalsIgnoreCase("Role")) {
//                            list.add(sp);
//                        }
//                    }
                }
            }
            record.setAnnotatedSpot(list);
            records.add(record);
        }
        return records;

    }
        public static List<AssessmentRecord> readKOREAnnotation(String filePath, Lang rdfLang, boolean with) {

        List<AssessmentRecord> records = new ArrayList<AssessmentRecord>();

        OntModel corpus = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        try {

            // Reading the NIF document.
            InputStream inputStream = new FileInputStream(filePath);
            String modifiedNiFFile = preProcessing(inputStream);

            if (modifiedNiFFile != null) {
                InputStream stream = new ByteArrayInputStream(modifiedNiFFile.getBytes(StandardCharsets.UTF_8));
                RDFDataMgr.read(corpus, stream, rdfLang);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SmilkNIF.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Extracting the texts contained in the NIF document.
        Query query = QueryFactory.create("prefix nif: "
                + "<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>"
                + "prefix itsrdf: "
                + "<http://www.w3.org/2005/11/its/rdf#>"
                + " SELECT distinct ?context ?string WHERE {"
                + "?context a nif:Context . ?context nif:isString ?string .}");

        QueryExecution qexec = QueryExecutionFactory.create(query, corpus);
        ResultSet result = qexec.execSelect();

        //ResultSetFormatter.out(System.out, result, query);
        Map<String, String> documents = new HashMap<String, String>(0);
        while (result.hasNext()) {
            QuerySolution solution = result.next();
            String text = solution.get("string").asLiteral().getString();
            String namespace = solution.get("context").toString();
           
           
            String tmp = namespace.replaceAll(koreNamespace, "");
            int endoftxt=(tmp.indexOf("#")>0)? tmp.indexOf("#"):tmp.length()-1;
            String sentence_id = tmp.substring(0, endoftxt);
           // System.out.println(sentence_id +"\t"+text);
            documents.put(sentence_id, text);
        }

        qexec.close();

        // Extracting the spots contained in the NIF document. 
        Query spot_query = QueryFactory.create("prefix nif: "
                + "<http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>"
                + "prefix itsrdf: "
                + "<http://www.w3.org/2005/11/its/rdf#>"
                + " SELECT distinct ?spot ?anchor ?start ?end ?refContext ?kore WHERE {"
                + " ?spot nif:anchorOf ?anchor ."
                + " ?spot nif:beginIndex ?start ."
                + " ?spot nif:endIndex   ?end   ."
                + " ?spot nif:referenceContext ?refContext."
                + " ?spot a nif:Phrase ."
                + " ?spot itsrdf:taIdentRef ?kore .}");

        QueryExecution spot_qexec = QueryExecutionFactory.create(spot_query, corpus);
        ResultSet spot_result = spot_qexec.execSelect();

        Map<AnnotatedSpot, String> spots = new HashMap<AnnotatedSpot, String>();
        Map<AnnotatedSpot, String> entities = new HashMap<AnnotatedSpot, String>();

        while (spot_result.hasNext()) {
            AnnotatedSpot sp = new AnnotatedSpot();
            QuerySolution solution = spot_result.next();
            String text = solution.get("anchor").asLiteral().getString();
            int start = solution.get("start").asLiteral().getInt();
            int end = solution.get("end").asLiteral().getInt();
            String namespace = solution.get("refContext").toString();
            String tmp = namespace.replaceAll(koreNamespace, "");
            int endoftxt=(tmp.indexOf("#")>0)? tmp.indexOf("#"):tmp.length()-1;
            String reference = tmp.substring(0, endoftxt);
            String doctype=reference.substring(0, 3);
            String docid=reference.substring(3, reference.length());
            switch (doctype) {
                        case "CEL":
                            docid="10"+docid;
                            break;
                        case "MUS":
                           docid="20"+docid;
                            break;

                        case "BUS":
                           docid="30"+docid;
                            break;

                        case "SPO":
                            docid="40"+docid;
                            break;

                        case "POL":
                           docid="50"+docid;
                            break;
                        default:
                              docid="60"+docid;
                            break;
                            

                    }
            
            sp.setDocId(docid);
            sp.setSpot(text);
            sp.setStart(start);
            sp.setEnd(end);
            spots.put(sp,reference);
            //entities
            String kore = solution.get("kore").asResource().getURI().replaceAll("http://dbpedia.org/resource/", "");
            entities.put(sp, kore);

        }

        spot_qexec.close();


        //extracting the type and dbpedia resource name for each spot
        for (AnnotatedSpot sp : entities.keySet()) {

            String oke = entities.get(sp);
            //sp.setType(entityTypes.get(oke));
            sp.setEntity(oke);
        }

        //creating the document records
        for (String index : documents.keySet()) {
            AssessmentRecord record = new AssessmentRecord();
             
            String doctype=index.substring(0, 3);
            String docid=index.substring(3, index.length());
            switch (doctype) {
                        case "CEL":
                            docid="10"+docid;
                            break;
                        case "MUS":
                           docid="20"+docid;
                            break;

                        case "BUS":
                           docid="30"+docid;
                            break;

                        case "SPO":
                            docid="40"+docid;
                            break;

                        case "POL":
                           docid="50"+docid;
                            break;
                        default:
                              docid="60"+docid;
                            break;
                            

                    }
            
            
            record.setDocId(docid);
            record.setText(documents.get(index));

            List<AnnotatedSpot> list = new ArrayList<AnnotatedSpot>();
            for (AnnotatedSpot sp : spots.keySet()) {

                if (spots.get(sp).equalsIgnoreCase(index)) {
                     
                        list.add(sp);
                               
                }
            }
            record.setAnnotatedSpot(list);
            records.add(record);
        }
        return records;

    }


    public static String preProcessing(InputStream in) {

        String s = null;
        try {

            // Reading the NIF document.
            String modifiedStr = IOUtils.toString(in, "UTF8");

            s = modifiedStr.replaceAll("(dbpedia:)([^\\s][\\w\\p{Punct}\\p{L}\\p{M}*]*)(?![.])", "<http://dbpedia.org/resource/$2>");

            //  InputStream is = new ByteArrayInputStream(modifiedStr.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(SmilkNIF.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }

    public static void writeToFile(List<AssessmentRecord> annotations, String path) {

        try {

            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path), "UTF-8"));

            for (int i = 0; i < annotations.size() - 1; i++) {
                out.append(annotations.get(i).toJson());
                out.append("\n");

            }
            out.append(annotations.get(annotations.size() - 1).toJson());
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Will proviod the TAK 2014 challenge format xml and tab files
     *
     *
     * @param path directory to save the output
     * @param xmlfilename
     * @param tabfilename
     *
     *
     */
    public static void writeToTAKformat(List<AssessmentRecord> annotations, String path, String filename, boolean docid_format) {

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path + "/" + filename + ".tab"), "UTF-8"));

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("kbpentlink");
            doc.appendChild(rootElement);

            int i = 1;

            for (AssessmentRecord record : annotations) {

                for (AnnotatedSpot spot : record.getAnnotatedSpot()) {
                    // query elements
                    Element query = doc.createElement("query");
                    rootElement.appendChild(query);

                    // set id to query element
                    Attr attr = doc.createAttribute("id");
                   // attr.setValue("EL14_EN_" + String.format("%03d", i));
                   
                    if(docid_format){
                    attr.setValue("EL14_EN_"+String.format("%03d", Integer.parseInt(spot.getDocId()))+"_"+ spot.getStart() +":"+spot.getEnd());
                        out.append("EL14_EN_"+String.format("%03d", Integer.parseInt(spot.getDocId()))+"_"+ spot.getStart() +":"+spot.getEnd());
                    }
                    else{
                         attr.setValue("EL14_EN_"+spot.getDocId()+"_"+ spot.getStart() +":"+spot.getEnd());
                         out.append("EL14_EN_"+spot.getDocId()+"_"+ spot.getStart() +":"+spot.getEnd());
                    }
                    
                    query.setAttributeNode(attr);
                    //add to tab
                    
                    out.append("\t");
                    out.append(spot.getEntity());
                    out.append("\t");

                    //convert type to TAK format
                    String type = "";
                    if (spot.getType() != null) {
                        
                        switch (spot.getType().toUpperCase()) {
                            case "PERSON":
                                type = "PER";
                                break;
                                
                            case "ORGANIZATION":
                                type = "ORG";
                                break;

                            case "PLACE":
                                type = "GPE";
                                break;
                            
                            case "LOCATION":
                                type = "GPE";
                                break;

                            case "ROLE":
                                type = "ROL";
                                break;
                                             
                            default:
                                type = "MISC";
                                break;
                        }
                    } else {
                        type = "MISC";
                    }

                    out.append(type);
                    out.append("\n");
                    // name elements
                    Element name = doc.createElement("name");
                    name.appendChild(doc.createTextNode(spot.getSpot()));
                    query.appendChild(name);

                    // docid elements
                    Element docid = doc.createElement("docid");
                       if(docid_format){
                    docid.appendChild(doc.createTextNode("doc_" + String.format("%03d", Integer.parseInt(spot.getDocId()))));
                       }else{
                              docid.appendChild(doc.createTextNode("doc_" + spot.getDocId()));
                       }
                       
                    query.appendChild(docid);

                    // offset begin elements
                    Element begin = doc.createElement("beg");
                    
                    begin.appendChild(doc.createTextNode(Integer.toString(spot.getStart())));
                    
                    query.appendChild(begin);

                    // offset end elements
                    Element end = doc.createElement("end");
                    end.appendChild(doc.createTextNode(Integer.toString(spot.getEnd())));
                    query.appendChild(end);
                    i++;

                }

            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(path + "/" + filename + ".xml"));
            //StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);
            out.flush();
            out.close();

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SmilkNIF.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(SmilkNIF.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SmilkNIF.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SmilkNIF.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(SmilkNIF.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
