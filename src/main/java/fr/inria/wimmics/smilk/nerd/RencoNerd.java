 
package fr.inria.wimmics.smilk.nerd;

import fr.inria.wimmics.smilk.renco.JDOMXMLReader;
import fr.inria.wimmics.smilk.renco.Renco;
 
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;

import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.text.Token;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spotter
 *
 *
 */
public class RencoNerd implements NameEntityRecognizer{

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(RencoNerd.class);
   
    Renco rn;
 
     List<Token> context=new ArrayList<Token>();
     
    public RencoNerd() {
      
         try {
            rn = new Renco();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(RencoNerd.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   

    @Override
    public List<Token> nerd(Document document) {
        
          List<Token> NerdTokens  = new LinkedList<Token>(); 
          
        try {
            
            
            String result = rn.rencoByWebService(document.getContent());
            System.out.println(result);
            
            org.jdom2.Document jdomDoc = JDOMXMLReader.useDOMParser(result);

            org.jdom2.Element root = jdomDoc.getRootElement();

            List<org.jdom2.Element> tokens = root.getChildren("infoEntity");

            for (org.jdom2.Element el : tokens) {
                
                Token t;
                String txt = "";
                int start = 0;
                int end = 0;
                String type = "";
                String pos = "";
                List<org.jdom2.Element> names = el.getChildren("name");
                for (org.jdom2.Element name : names) {
                    txt = name.getText();
                }
                List<org.jdom2.Element> starts = el.getChildren("start");
                for (org.jdom2.Element s : starts) {
                    start = Integer.parseInt(s.getText());
                }
                List<org.jdom2.Element> ends = el.getChildren("end");
                for (org.jdom2.Element e : ends) {
                    end = Integer.parseInt(e.getText());
                }

                List<org.jdom2.Element> types = el.getChildren("type");
                for (org.jdom2.Element tp : types) {
                    type = tp.getText().toUpperCase();
                }

                //FIXME if there is set of pos
                List<org.jdom2.Element> ps = el.getChildren("pos");
                for (org.jdom2.Element p : ps) {
                    pos = p.getText();
                }

                t = new Token(txt, start, end, pos, type);
                t.setSubTokens(Arrays.asList(t));  
                t.setNESubTokens(Arrays.asList(t));
                t.setPosTags(Arrays.asList(pos));
                NerdTokens.add(t);

            }
             
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(RencoNerd.class.getName()).log(Level.SEVERE, null, ex);
        }
          return NerdTokens;
    }

    @Override
    public void init(DexterParams dexterParams, DexterLocalParams defaultModuleParams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Token> getContext() {
       return  context;
    }
    
}
