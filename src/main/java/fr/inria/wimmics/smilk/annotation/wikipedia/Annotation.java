/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.smilk.annotation.wikipedia;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;   
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.sweble.wikitext.engine.config.WikiConfig;
 import org.sweble.wikitext.engine.utils.LanguageConfigGenerator;

    /**
 *
 * @author fnoorala
 */
public class Annotation {

    /**
     * @param args the command line arguments
     */
    private static final Logger logger = LoggerFactory.getLogger(Annotation.class);
     
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        String directoryName="annotation";
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
             DatasetCollection.collectDocument(jwpl,page,config,directoryName);
         }
       

    }

}