/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.smilk.cli;


import fr.inria.wimmics.smilk.annotation.wikipedia.DatasetCollection;
import fr.inria.wimmics.smilk.annotation.wikipedia.jwplWikipedia;
import fr.inria.wimmics.smilk.util.ListFilesUtil;
import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;
import java.io.File;
 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;   
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.LanguageConfigGenerator;

    /**
 *
 * @author fnoorala
 */
public class AnnotationCLI  extends AbstractCommandLineInterface {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(AnnotationCLI.class);

	private static String[] params = new String[] {OUTPUT};

	private static final String USAGE = "java -cp $jar " + AnnotationCLI.class
			+ " -output PathToSaveAnnotation ";

    public static void main(String[] args) throws Exception {
       
        // TODO code application logic here
        AnnotationCLI cli = new AnnotationCLI(args);
        List<String> targetCategories=new ArrayList<String>();
        targetCategories.add("Hygiène et soins capillaires");
        targetCategories.add("Entreprise de cosmétique ayant son siège en France");
        targetCategories.add("Marque française");
        targetCategories.add("Entreprise des Hauts-de-Seine");
        targetCategories.add("Entreprise de cosmétique");
        targetCategories.add("Entreprise du luxe");
        
        Set<String> pagesTovisit=new HashSet<String>();
        
        jwplWikipedia jwpl = new jwplWikipedia();
        WikiConfig config = LanguageConfigGenerator.generateWikiConfig("My French Wiki", "http://localhost/", "fr");

        for(String cat:targetCategories){
             Set<String> set = new HashSet<String>(jwpl.retreivePages(cat));
              pagesTovisit.addAll(set);          
        }                
        
          for(String page:pagesTovisit){
             logger.info("Annotating the page {}", page);
  //DatasetCollection.collectDocument(jwpl,"Renault",config);
             DatasetCollection.collectDocument(jwpl,page,config,cli.getOutput());
         }
          
                                 


    }
    public AnnotationCLI(String[] args) {
		super(args, params, USAGE);
	}

}
