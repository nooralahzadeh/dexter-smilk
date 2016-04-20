 
package fr.inria.wimmics.smilk.nerd;

import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;

import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.text.Token;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.grobid.core.engines.NERParser;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spotter
 *
 *
 */
public class InriaNerd  implements  NameEntityRecognizer{

   DexterParams params = DexterParams.getInstance();
    String path;
   
    private String pGrobidHome;
    private String pGrobidProperties;

   
    

    public InriaNerd()  {

        try {
            path=params.getDefaultModel().getPath();
            pGrobidHome = path + "/grobid-home";
            pGrobidProperties = path + "/grobid-home/config/grobid.properties";
            MockContext.setInitialContext(pGrobidHome, pGrobidProperties);
            GrobidProperties.getInstance();
            LibraryLoader.load();
            
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(InriaNerd.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
 

    @Override
    public List<Token> nerd(Document document) {
         List<Token> NerdTokens = new LinkedList<Token>();
        try {

          
            NERParser nerParser = new NERParser();

            List<org.grobid.core.data.Entity> results = nerParser.extractNE(document.getContent());
           
            MockContext.destroyInitialContext();
            GrobidProperties.reset();
           
            for (org.grobid.core.data.Entity entity : results) {

                Token t;
                String txt = entity.getRawName();
                int start = entity.getOffsetStart();
                int end = entity.getOffsetEnd();
                String type =convertINRIANERDTypes(entity.getType().getName().toUpperCase());
              
                t = new Token(txt,start, end,"NNP",type);
               
                t.setPosTags(Arrays.asList("NNP"));
                t.setSubTokens(Arrays.asList(t));
                t.setNESubTokens(Arrays.asList(t));
                NerdTokens.add(t);

            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(InriaNerd.class.getName()).log(Level.SEVERE, null, ex);
        }

        return NerdTokens;
    }

    public String convertINRIANERDTypes(String NERDtype){
        String type=NERDtype;
         switch (NERDtype) {
            
            case "EVENT":
                type ="MISC"  ;
                break;
            case "ARTIFACT":
               type ="MISC"  ;
                break;
           
            case "BUSINESS":
               type ="ORGANIZATION"  ;
                break;
            
            case "ACRONYM":
               type ="MISC"  ;
                break;
            
            case "TITLE":
               type ="ROLE"  ;
                break;    
                
            case "INSTITUTION":
               type ="ORGANIZATION"  ;
                break;
            case "NATIONAL":
               type ="PERSON"  ;
                break;    
            case "WEBSITE":
               type ="ORGANIZATION"  ;
                break;
                
            
            case "ANIMAL":
               type ="MISC"  ;
                break;
           
            case "CREATION":
               type ="MISC"  ;
                break;
                
            case "AWARD":
               type ="MISC"  ;
                break;
            
            
            case "PERSON_TYPE":
               type ="PERSON"  ;
                break;
                 
            case "MEDIA":
               type ="ORGANIZATION"  ;
                break;
                 
            case "SUBSTANCE":
               type ="MISC"  ;
                break;
                    
            case "PLANT":
               type ="MISC"  ;
                break;
            
            case "SPORT_TEAM":
               type ="ORGANIZATION"  ;
                break;    
            
            case "INSTALLATION":
               type ="LOCATION"  ;
                break;
                
            case "CONCEPT":
               type ="MISC"  ;
                break;
            
              
            case "CONCEPTUAL":
               type ="MISC"  ;
                break; 
            
            case "IDENTIFIER":
               type ="MISC"  ;
                break;      
            case "UNKNOWN":
               type ="MISC"  ;
                break;
            
            case "ORGANISATION":
               type ="ORGANIZATION"  ;
                break; 
            
                     
        }
         return type;
        
    }
    
    
    

    @Override
    public void init(DexterParams dexterParams, DexterLocalParams defaultModuleParams) {
             
    }

    @Override
    public List<Token> getContext() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
