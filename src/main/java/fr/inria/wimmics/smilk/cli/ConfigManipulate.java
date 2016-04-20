/**
 * Copyright 2011 Diego Ceccarelli
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.inria.wimmics.smilk.cli;

import it.cnr.isti.hpc.benchmark.Stopwatch;
import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 *
 */
public class ConfigManipulate extends AbstractCommandLineInterface {

    public static Stopwatch stopwatch = new Stopwatch();
    private static final Logger logger = LoggerFactory.getLogger(ConfigManipulate.class);

    private static String[] params = new String[]{"sp"};

    private static final String USAGE = "java -cp $jar " + ConfigManipulate.class
            + " -spotter sptter -disambiguater disamb";

    public static void main(String[] args)   {
         ConfigManipulate cli = new ConfigManipulate(args);
        try {
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse("dexter-conf.xml");
            doc.getDocumentElement().normalize();

            //update tagger
            NodeList tagger = doc.getElementsByTagName("tagger");

            Element el = null;
            for (int i = 0; i < tagger.getLength(); i++) {
                el = (Element) tagger.item(i);
                String method = el.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
                if (method.equalsIgnoreCase("dexter")) {
                  Node name = el.getElementsByTagName("spotter").item(0).getFirstChild();
                  name.setNodeValue(cli.getParam("sp"));
                }
            }

             //update disambiguator
            
            
            
            
            doc.getDocumentElement().normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("dexter-conf.xml"));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
            System.out.println("XML file updated successfully"); 

        } catch (SAXException ex) {
            java.util.logging.Logger.getLogger(ConfigManipulate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ConfigManipulate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            java.util.logging.Logger.getLogger(ConfigManipulate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            java.util.logging.Logger.getLogger(ConfigManipulate.class.getName()).log(Level.SEVERE, null, ex);
        }  catch (TransformerException ex) {
            java.util.logging.Logger.getLogger(ConfigManipulate.class.getName()).log(Level.SEVERE, null, ex);
        }

    }    
    

    public ConfigManipulate(String[] args) {
        super(args, params, USAGE);
    }

}
