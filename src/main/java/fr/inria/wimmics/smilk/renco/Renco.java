package fr.inria.wimmics.smilk.renco;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.ws.BindingProvider;

import fr.ho2s.holmes.client.parser.jaxws.HolmesOutput;
import fr.ho2s.holmes.client.parser.jaxws.HolmesServiceFrench;
import fr.ho2s.holmes.client.parser.jaxws.HolmesServiceFrenchService;
import fr.ho2s.holmes.client.parser.jaxws.ObjectFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import org.jdom2.output.XMLOutputter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
 
import org.apache.commons.lang.StringEscapeUtils;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
import it.cnr.isti.hpc.benchmark.Stopwatch;
import java.net.URI;
 
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fnoorala
 */
public class Renco {

   private static final Logger logger = LoggerFactory   .getLogger(Renco.class);
   private final Stopwatch stopwatch;
    public String prenom_dico;
    public String gammes;
    public String marques;
    public String produits;
    public String divisions;
    public String groupes;
    public String ressource;

    //private static final String webServiceURI = "https://demo-innovation-projets-groupe.viseo.net/renco-rest/rest/renco";

    public Renco() throws IOException {
        stopwatch = new Stopwatch();
        
       
    }

  

   
    public String rencoByWebService(String in) throws Exception {
        String xmlEntity="";
        try {

           
            //ClientConfig clientConfig = new ClientConfig();
	   //  Client client = ClientBuilder.newClient(clientConfig);
           // URI serviceURI = UriBuilder.fromUri(webServiceURI).build();
	   // WebTarget webTarget = client.target(serviceURI);
            Client client = Client.create();		
	    WebResource webResource = client.resource("https://demo-innovation-projets-groupe.viseo.net/renco-rest/rest/renco/getRenco");	 	      	    
	   
	    ClientResponse response = webResource.type("text/plain").post(ClientResponse.class, in);   
	    
	 	    
	     
            
       // String sortie = webTarget.path("rest").path("getRenco").request(in)
				//.accept(MediaType.TEXT_PLAIN).get(String.class);
	 String sortie = response.getEntity(String.class);
          
          
          
       // System.out.println(sortie);
          
        int nbSentences = AccessJDom.nbSentence(sortie);
        org.jdom2.Document jdomDoc = JDOMXMLReader.useDOMParser(sortie);
        
        
         
           
         org.jdom2.Element root = jdomDoc.getRootElement();
         List<org.jdom2.Element> sentences = root.getChildren("sentences");
            
         xmlEntity = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><RENCO>\n";

        for (int r = 0; r < sentences.size(); r++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(r);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int phrase = 0; phrase < nbSentences; phrase++) { //pour chaque phrase 
                org.jdom2.Element element2 = sentence.get(phrase);//numero de la phrase
                List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                    org.jdom2.Element element3 = tokens.get(p);
                    List<org.jdom2.Element> token = element3.getChildren("token");
                    for (org.jdom2.Element tokenElement : token) {
                        if (tokenElement.getAttributeValue("pos").contentEquals("NPP") ) {
                            String spot;
                            xmlEntity += "<infoEntity>\n";   
                              if (!tokenElement.getAttributeValue("end").equalsIgnoreCase("-1")) {
                                 spot=in.substring(Integer.parseInt(tokenElement.getAttributeValue("start")), Integer.parseInt(tokenElement.getAttributeValue("end")));
                             } else {
                                  spot=in.substring(Integer.parseInt(tokenElement.getAttributeValue("start")), in.length() - 1);
                                 }
                              
                              System.out.println(spot+ "|----|"+tokenElement.getAttributeValue("form"));
                            xmlEntity += "\t<name>" + StringEscapeUtils.escapeXml(spot) + "</name>\n";
                         
                            xmlEntity += "\t<start>" + tokenElement.getAttributeValue("start") + "</start>\n";
                           
                            xmlEntity += "\t<end>" + tokenElement.getAttributeValue("end") + "</end>\n";
                            
                            xmlEntity += "\t<pos>" + tokenElement.getAttributeValue("pos") + "</pos>\n";
                          
                            xmlEntity += "\t<type>" + tokenElement.getAttributeValue("type") + "</type>\n";
                          
                            xmlEntity += "</infoEntity>\n";
                            
                        }
                    }
                }
            }
        }

         
        xmlEntity += "</RENCO>";  
 

        } catch (Exception e) {

            e.printStackTrace();

        }        return  xmlEntity ;
    }

   
}
