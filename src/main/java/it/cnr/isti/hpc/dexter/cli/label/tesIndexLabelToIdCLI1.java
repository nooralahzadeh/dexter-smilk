/**
 *  Copyright 2012 Diego Ceccarelli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package it.cnr.isti.hpc.dexter.cli.label;

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
import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;
import it.cnr.isti.hpc.dexter.label.IdHelperFactory;
import it.cnr.isti.hpc.dexter.label.LabelToIdWriter;
import it.cnr.isti.hpc.dexter.util.TitleRedirectId;
import it.cnr.isti.hpc.io.reader.RecordReader;
import it.cnr.isti.hpc.log.ProgressLogger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes a file containing a list of TitleRedirectId and indexes the mapping
 * <code> title -> id </code>.
 * 
 * @author Diego Ceccarelli, diego.ceccarelli@isti.cnr.it created on 02/lug/2012
 */
public class tesIndexLabelToIdCLI1 {
	

	//static LabelToIdWriter writer = IdHelperFactory.getStdLabelToIdWriter();
        
        
          private final static String sparqlEndPoint = "http://dbpedia.org/sparql";
    

	public static void main(String[] args) {
            String in="/user/fnoorala/home/NetBeansProjects/dexter/dexter-core/en.data/title-redirect-id.tsv";
		
		RecordReader<TitleRedirectId> reader = new RecordReader<TitleRedirectId>(
				in, new TitleRedirectId.Parser());
		String currentTitle = "";
		Integer currentId = -1;
		for (TitleRedirectId article: reader){
			
			

			if (!article.isRedirect()) {
				// real article
				currentTitle = article.getTitle();
                                
                                
                                getTypeFromDBpedia(article.getTitle());
                                                             
				currentId = Integer.parseInt(article.getId());
				store(currentTitle, currentId);
			}
		}
		// writer.close();
	}

	public static void store(String k, Integer v) {
                System.out.println(k+"\t"+v);
		//writer.add(k, v);
	}
        
        
        public static void getTypeFromDBpedia(String title){
            
            
            
          List<String> dbType=new ArrayList<String>();  
          
         
          String qry =  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
                  +      "PREFIX owl: <http://www.w3.org/2002/07/owl#>  "
                  +      "PREFIX dbpedia-owl	<http://dbpedia.org/ontology/>"
                  + " SELECT DISTINCT ?o WHERE { <http://dbpedia.org/resource/"+title+"> rdf:type ?o .\n"
                          + "  }\n"
                + "";
        
        Query query = QueryFactory.create(qry);
        
        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndPoint, query);
        ResultSet results = qexec.execSelect();
       
        try {
            
            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();
                
                RDFNode cnt = soln.get("o");
                
                if (cnt.isResource()) {
                    Resource rcnt = (Resource) cnt;
                    System.out.println(rcnt.getLocalName());
                    dbType.add(rcnt.getLocalName());
                    
                }
                
            }
            
        } finally {
            qexec.close();
        }
        }
                

	
}
