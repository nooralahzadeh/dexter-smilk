package fr.inria.wimmics.smilk.nerd;

import fr.ho2s.holmes.ner.ws.CompactNamedEntity;
import fr.ho2s.holmes.ner.ws.HolmesNERServiceFrench;
import fr.ho2s.holmes.ner.ws.HolmesNERServiceFrenchService;
import fr.ho2s.holmes.ner.ws.HolmesServiceException;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;

import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.text.Token;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spotter
 *
 *
 */
public class HolmesNerd implements NameEntityRecognizer {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(HolmesNerd.class);

    private static final QName SERVICE_NAME = new QName("http://ws.ner.holmes.ho2s.fr/", "HolmesNERServiceFrenchService");

    List<Token> context = new ArrayList<Token>();

    @Override
    public List<Token> nerd(Document document) {
        List<Token> NerdTokens = new LinkedList<Token>();
        try {
            URL wsdlURL = HolmesNERServiceFrenchService.WSDL_LOCATION;

            HolmesNERServiceFrenchService ss = new HolmesNERServiceFrenchService(wsdlURL, SERVICE_NAME);
            HolmesNERServiceFrench port = ss.getHolmesNERServiceFrenchPort();

            List<fr.ho2s.holmes.ner.ws.CompactNamedEntity> resp = port.parse(document.getContent());

            for (CompactNamedEntity namedEntity : resp) {

                String txt = namedEntity.getEntityString();

                Token t;
                int start = namedEntity.getSpanFrom();
                int end = namedEntity.getSpanTo();
                String type = namedEntity.getEntityType().toUpperCase();
                t = new Token(txt, type, start, end);
                t.setPos("NNP");
                t.setSubTokens(Arrays.asList(t));
                t.setNESubTokens(Arrays.asList(t));

               // t.setPosTags(Arrays.asList(t.getPos()));
                NerdTokens.add(t);
            }

        } catch (HolmesServiceException ex) {
            java.util.logging.Logger.getLogger(HolmesNerd.class.getName()).log(Level.SEVERE, null, ex);
        }
        return NerdTokens;
    }

    
         
    
    
    
    @Override
    public void init(DexterParams dexterParams, DexterLocalParams defaultModuleParams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Token> getContext() {
        return context;
    }
}
