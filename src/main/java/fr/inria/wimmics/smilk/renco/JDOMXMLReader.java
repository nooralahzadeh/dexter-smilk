package fr.inria.wimmics.smilk.renco;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Elena
 * 
 */
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
 

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
 

import org.jdom2.*;
import org.jdom2.input.DOMBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
 

public class JDOMXMLReader {
	
	
	
public static String entityToContext(String entity, String sentenceTag) throws ParserConfigurationException, SAXException, IOException{//return the context of a given entity
		org.jdom2.Document jdomDoc;
		jdomDoc = useDOMParser(sentenceTag);
		Element root = jdomDoc.getRootElement();
		List<Element> tokenListElements = root.getChildren("sentence");
        Element element2 = tokenListElements.get(0);
        List<Element> tokenListElements2 = element2.getChildren("token");
        //System.out.println(sentenceTag);
        for (Element tokenElement : tokenListElements2) {
        	String form=tokenElement.getAttributeValue("form");
        	if(form.equals(entity)) {//if we find the entity within the token
        		//System.out.println(form);
        		//on r�cup�re l'id du head
        		String headStr=tokenElement.getAttributeValue("head");//we extract the head of this entity
            	int head = Integer.parseInt(headStr);
            	//we parse all the token until retrieving this head
            	 for (Element tokenElement2 : tokenListElements2) {
            		 String idStr=tokenElement2.getAttributeValue("id");
            		 int id=Integer.parseInt(idStr);
            		 if(id==head && tokenElement2.getAttributeValue("pos").equals("NC")){
            			String lemma=tokenElement2.getAttributeValue("lemma");
         				return lemma;
            		 }
            	 }
        	}
        }
        
		String warn="Aucun contexte trouv� pour l'entit� donn�e";
		return warn;
	}
	

	
	//return an entity of a given context
	public static String contextToEntity(String context, String sentenceTag, ArrayList<String> listEntities) throws ParserConfigurationException, SAXException, IOException{
		org.jdom2.Document jdomDoc;
		jdomDoc = useDOMParser(sentenceTag);
		Element root = jdomDoc.getRootElement();
		List<Element> tokenListElements = root.getChildren("sentence");
        Element element3 = tokenListElements.get(0);
        List<Element> tokenListElements3 = element3.getChildren("token");
        // System.out.println(sentenceTag);
        for(Element tokenElement3 : tokenListElements3){
	        String lemmaMatching=tokenElement3.getAttributeValue("lemma");
			if(lemmaMatching.equals(context)){
	    		 //on recupere son id
	    		 String idStr1=tokenElement3.getAttributeValue("id");//id of the context
	    		 int id1=Integer.parseInt(idStr1);
	    		 for(Element tokenElement4 : tokenListElements3){
	    			 String headStr2=tokenElement4.getAttributeValue("head");
	    			 int head2 = -1;
	    			 String form=tokenElement4.getAttributeValue("form");
	    			 if (headStr2 != null){
	    				 head2=Integer.parseInt(headStr2);
	    			 }
	    	
	    			 String depRel=tokenElement4.getAttributeValue("depRel");
	    			 if(head2==id1 && capitale(form) && depRel.equals("mod") && !listEntities.contains(form)){//on ne garde que les capitales
	    				 String form3=tokenElement4.getAttributeValue("form");
	    				 return form3; 
	    			 }
	    		 }
			}
        }

		return "";//in the case we found nothing
	}
	
	public static boolean letter(String term){
		String tab[]={"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		char a1=term.charAt(0);
		for (String l : tab) {
			if(l.equals(a1)){
				System.out.println("ok");
				return true;
			}
		}
		System.out.println("false");

		return false;
	}
	
	public static boolean capitale(String term) {
		if(term.length()<1){return false;}//au cas ou on doive traiter un term null
		String term2=term.toLowerCase();
		char a1=term.charAt(0);
		char a2=term2.charAt(0);
		if(a1==a2){
			return false;
			
		}	
		return true; 	 
	}
	

    //Get JDOM document from DOM Parser
    public static org.jdom2.Document useDOMParser(String sentenceTag)
            throws ParserConfigurationException, SAXException, IOException {
        //creating DOM Document
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        InputStream  is = new ByteArrayInputStream(sentenceTag.getBytes("UTF-8"));
        Document doc = dBuilder.parse(is);
        DOMBuilder domBuilder = new DOMBuilder();
        return domBuilder.build(doc);
    }
    
    public static String entityNotTreated(ArrayList<String> listTreatedEntities, ArrayList<String> listEntities){
		String result_prob=null;
		String result=null;
		System.out.println("liste entit�s deja vues : " +listTreatedEntities);
		System.out.println("liste entit�s : " +listTreatedEntities);
    	for (int i = 0; i < listEntities.size(); i++) {
			 result_prob=listEntities.get(i);
			 if(!listTreatedEntities.contains(result_prob)){
				result=result_prob;
			 }
		}
    	if(result.isEmpty()){
    		System.out.println("Fin");
    		return "-1";
    	}
    	else{
    		System.out.println("result :" + result);
    		
    		return result;
    	}
	}




}