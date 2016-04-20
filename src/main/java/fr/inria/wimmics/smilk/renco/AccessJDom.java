package fr.inria.wimmics.smilk.renco;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author fnoorala
 */
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Document;
import org.xml.sax.SAXException;


import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class AccessJDom {

	public static Document setAttributeType(int id, String type, int idsentence, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{//ajoute l'attribut type et sa valeur associ�e
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int n = 0; n <sentences.size(); n++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(n);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int m = 0; m <sentence.size(); m++) {//pour chaque phrase
				if (m==idsentence) {
					org.jdom2.Element element2 = sentence.get(m);
					List<org.jdom2.Element> tokens = element2.getChildren("tokens");
					for (int s = 0; s <tokens.size(); s++) {//pour chaque tokens
						org.jdom2.Element element3 = tokens.get(s);
						List<org.jdom2.Element> token = element3.getChildren("token");
						for (org.jdom2.Element tokenElement : token) {//pour chaque tokens
							String idStr=tokenElement.getAttributeValue("id");
							int id1=Integer.parseInt(idStr);
							if(id==id1 && type!=null){
								tokenElement.setAttribute("type", type);
							}
						}
					}
				}
			}
		}
		return jdomDoc;
	}

	public static Document setAttributeRules(int id, String rule, int idsentence, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{//ajoute l'attribut type et sa valeur associ�e
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int n = 0; n <sentences.size(); n++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(n);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int m = 0; m <sentence.size(); m++) {//pour chaque phrase
				if (m==idsentence) {
					org.jdom2.Element element2 = sentence.get(m);
					List<org.jdom2.Element> tokens = element2.getChildren("tokens");
					for (int s = 0; s <tokens.size(); s++) {//pour chaque tokens
						org.jdom2.Element element3 = tokens.get(s);
						List<org.jdom2.Element> token = element3.getChildren("token");
						for (org.jdom2.Element tokenElement : token) {//pour chaque tokens
							String idStr=tokenElement.getAttributeValue("id");
							int id1=Integer.parseInt(idStr);
							String type=tokenElement.getAttributeValue("type");//type de l'entit�
							String ruleValue=tokenElement.getAttributeValue("rules"+type);//on recupere les r�gles appliqu�es
							
							if(id==id1){
								
								if(ruleValue!=null && !ruleValue.contains(rule)){//si l'attribut rules est non vide et qu'il en contient pas deja cette regle, on l'ajoute
									rule=ruleValue+" + "+rule;
									tokenElement.setAttribute("rules"+type, rule);
								}
						
								else if(ruleValue!=null){//si l'attribut rules est non vide et qu'il contient deja cette regle on laisse tel quel
									rule=ruleValue;
									tokenElement.setAttribute("rules"+type, rule);
								}
								else{//si l'attribut rules est vide => creation de l'attribut "rules"
									tokenElement.setAttribute("rules"+type, rule);
								}
							}
						}
					}
				}
			}
		}
		return jdomDoc;
	}
	
	
	
	public static Document mergeEntity(org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{//concatene les �l�ments d'entit�s nomm�es et r�indexe les attributs (id, head,...)
		org.jdom2.Element root = jdomDoc.getRootElement();
		int delete=0;
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				org.jdom2.Element element2 = sentence.get(n);
				List<org.jdom2.Element> tokens = element2.getChildren("tokens");
				for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
					org.jdom2.Element element3 = tokens.get(p);
					List<org.jdom2.Element> token = element3.getChildren("token");

					int newHead=0;
					int newId=0;
					int decalage=0;
					String newIdString="";
					for (org.jdom2.Element tokenElement : token){//pour chaque token
						if(delete==0){
							String idStr=tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
							int id=Integer.parseInt(idStr);
							
							String headStr=tokenElement.getAttributeValue("head");//on recupere le head du terme en cours
							int head=Integer.parseInt(headStr);
							
							String form1=tokenElement.getAttributeValue("form");// on recupere le form de l'id en cours
							String form2=AccessJDom.getForm(id+1, n, jdomDoc);//on recupere le form de l'id suivante
							
							String pos=AccessJDom.getPos(id, n, jdomDoc);
							String pos2=AccessJDom.getPos(id+1, n, jdomDoc);//on recupere le pos de l'id suivante
							
							if(
									(form1.equals("L'") && (Text.uppercase(form2))) 
									||
									((!Rules.allCapital(form1) || (Rules.allCapital(form1) && form1.length()<5)) && Text.uppercase(form1) && !pos.equals("DET") && !pos.equals("P") && (Text.uppercase(form2) || form2.equals("&") || pos2.equals("NC")))
									||
									((!Rules.allCapital(form1) || (Rules.allCapital(form1) && form1.length()<5)) && form1.split(" ").length>1 && (pos2.equals("NC") || pos2.equals("ADJ")))
									||
									!(!Rules.allCapital(form1) || (Rules.allCapital(form1) && form1.length()<5)) && Text.isNum(form1) && (Text.uppercase(form2) || form2.equals("&") || pos2.equals("NC"))

									){//si on a 2 termes cons�cutifs avec une capitale ou NC ou ADJ

								String end=AccessJDom.getEnd(id+1, n, jdomDoc);

								int end1=Integer.parseInt(end);	

								String newEnd= Integer.toString(end1);
								String replace=form1 +" "+ form2;
								//on met � jour les attributs
								tokenElement.setAttribute("form", replace);
								tokenElement.setAttribute("lemma", replace);
								tokenElement.setAttribute("end", newEnd);
								tokenElement.setAttribute("pos", "NPP");

								newId=id-decalage;
								//on reindexe suite aux suppressions d'id
								newIdString=Integer.toString(newId);
								tokenElement.setAttribute("id",newIdString);
								//on attribue le nouveau head a jour
								newHead=head-decalage;
								String newHeadint=Integer.toString(newHead);
								tokenElement.setAttribute("head",newHeadint);

								//on note un decalage d� � la suppression d'un token
								decalage=decalage+1;
								//on active "delete" pour supprimer le prochain token (� la prochain iteration)
								delete=1;
							}
							else{
								newId=id-decalage;
								//on reindexe suite aux suppressions d'id
								newIdString=Integer.toString(newId);
								tokenElement.setAttribute("id",newIdString);
								//on attribue le nouveau head a jour
								newHead=head-decalage;
								String newHeadint=Integer.toString(newHead);
								tokenElement.setAttribute("head",newHeadint);
							}
						}
						else{					
							//tokenElement.detach();//on supprime le token
							tokenElement.setAttribute("form", "");
							tokenElement.setAttribute("lemma", "");
							delete=0;
						}
					}
				}
			}
		}
		return jdomDoc;
	}

	
	
	public static Document mergeEntity2(org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{//concatene les �l�ments d'entit�s nomm�es et r�indexe les attributs (id, head,...)
		org.jdom2.Element root = jdomDoc.getRootElement();
		int delete=0;
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				org.jdom2.Element element2 = sentence.get(n);
				List<org.jdom2.Element> tokens = element2.getChildren("tokens");
				for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
					org.jdom2.Element element3 = tokens.get(p);
					List<org.jdom2.Element> token = element3.getChildren("token");

					int newHead=0;
					int newId=0;
					int decalage=0;
					String newIdString="";
					for (org.jdom2.Element tokenElement : token){//pour chaque token
						if(delete==0){
							String idStr=tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
							int id=Integer.parseInt(idStr);
							
							String headStr=tokenElement.getAttributeValue("head");//on recupere le head du terme en cours
							int head=Integer.parseInt(headStr);
							
							String form1=tokenElement.getAttributeValue("form");// on recupere le form de l'id en cours
							String form2=AccessJDom.getForm(id+1, n, jdomDoc);//on recupere le form de l'id suivante
							
							String type1=tokenElement.getAttributeValue("type");// on recupere le form de l'id en cours
							String type2=AccessJDom.getType(id+1, n, jdomDoc);//on recupere le form de l'id suivante
							
							
							
							if(type1!=null && type2!=null && type1.equals(type2)){//si on a 2 termes cons�cutifs du m�me type
		
								String end=AccessJDom.getEnd(id+1, n, jdomDoc);
								int end1=Integer.parseInt(end);	

								String newEnd= Integer.toString(end1);
								String replace=form1 +" "+ form2;
								//on met � jour les attributs
								tokenElement.setAttribute("form", replace);
								tokenElement.setAttribute("lemma", replace);
								tokenElement.setAttribute("end", newEnd);
								tokenElement.setAttribute("pos", "NPP");

								newId=id-decalage;
								//on reindexe suite aux suppressions d'id
								newIdString=Integer.toString(newId);
								tokenElement.setAttribute("id",newIdString);
								//on attribue le nouveau head a jour
								newHead=head-decalage;
								String newHeadint=Integer.toString(newHead);
								tokenElement.setAttribute("head",newHeadint);

								//on note un decalage d� � la suppression d'un token
								decalage=decalage+1;
								//on active "delete" pour supprimer le prochain token (� la prochain iteration)
								delete=1;
							}
							else{
								newId=id-decalage;
								//on reindexe suite aux suppressions d'id
								newIdString=Integer.toString(newId);
								tokenElement.setAttribute("id",newIdString);
								//on attribue le nouveau head a jour
								newHead=head-decalage;
								String newHeadint=Integer.toString(newHead);
								tokenElement.setAttribute("head",newHeadint);
							}
						}
						else{					
							//tokenElement.detach();//on supprime le token
							tokenElement.setAttribute("form", "");
							tokenElement.setAttribute("lemma", "");
							delete=0;
						}

					}
				}
			}
		}
		return jdomDoc;
	}
	
	public static Document reIndexation(org.jdom2.Document jdomDoc){
		String indexString="";
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> tokenListElements = root.getChildren("sentence");
		for (int m = 0; m <tokenListElements.size(); m++) {//pour chaque phrase
			org.jdom2.Element element2 = tokenListElements.get(m);
			int index=1;
			List<org.jdom2.Element> tokenListElements2 = element2.getChildren("token");
			for (org.jdom2.Element tokenElement : tokenListElements2) {//pour chaque token
				indexString=Integer.toString(index);
				tokenElement.setAttribute("id", indexString);
				index++;
			}
		}
		return jdomDoc;
	}

	public static Document reIndexationHead(org.jdom2.Document jdomDoc){
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> tokenListElements = root.getChildren("sentence");
		for (int m = 0; m <tokenListElements.size(); m++) {//pour chaque phrase
			org.jdom2.Element element2 = tokenListElements.get(m);
			int decalageHead=0;
			int headNew=0;
			int nboccurs=0;
			List<org.jdom2.Element> tokenListElements2 = element2.getChildren("token");
			for (org.jdom2.Element tokenElement : tokenListElements2) {//pour chaque token
				String headS=tokenElement.getAttributeValue("head");//on recupere le head en cours
				int head=Integer.parseInt(headS);//on le passe en entier
				headNew=head+decalageHead;
				String headString=Integer.toString(headNew);
				tokenElement.setAttribute("head",headString);

				//on met � jour les head pour le prochain tour
				String lemma=tokenElement.getAttributeValue("lemma");
				nboccurs=lemma.split(" ").length-1;//nombre d'occurences de l'espace dans le lemma en cours
				decalageHead=decalageHead-nboccurs;//on calcule la nouvelle valeur de d�calage

				System.out.println(lemma+" a un decalage de "+decalageHead);

			}
		}
		return jdomDoc;
	}


	
	
	public static Document cleanJDomDoc(org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				org.jdom2.Element element2 = sentence.get(n);
				List<org.jdom2.Element> tokens = element2.getChildren("tokens");
				for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
					org.jdom2.Element element3 = tokens.get(p);
					List<org.jdom2.Element> token = element3.getChildren("token");
					for (org.jdom2.Element tokenElement : token) {//pour chaque token
						String form=tokenElement.getAttributeValue("form");// on recupere le form de l'id en cours
						if(form.equals("")){//si on a un element vide
							tokenElement.detach();
							return jdomDoc;

						}
					}
				}
			}
		}
		return jdomDoc;
	}

	public static boolean testCleanJDomDoc(org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{//retourne true s'il existe des �l�ments vides dans le Jdom
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				org.jdom2.Element element2 = sentence.get(n);
				List<org.jdom2.Element> tokens = element2.getChildren("tokens");
				for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
					org.jdom2.Element element3 = tokens.get(p);
					List<org.jdom2.Element> token = element3.getChildren("token");
					for (org.jdom2.Element tokenElement : token) {//pour chaque token
						String form=tokenElement.getAttributeValue("form");// on recupere le form de l'id en cours
						if(form.equals("")){//si on a 2 termes cons�cutifs avec une capitale
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	
	public static String getRules(int id, int phrase, String type, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{

		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				if(n==phrase){
				org.jdom2.Element element2 = sentence.get(n);
				List<org.jdom2.Element> tokens = element2.getChildren("tokens");
				for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
					org.jdom2.Element element3 = tokens.get(p);
					List<org.jdom2.Element> token = element3.getChildren("token");
					for (org.jdom2.Element tokenElement : token) {//pour chaque token
						String idStr=tokenElement.getAttributeValue("id");
						int id1=Integer.parseInt(idStr);
						String rules="rules"+type;
						if(id1==id){
							return rules;
						}
					}
				}
				}
			}
		}
		return "";
	}
	
	public static int getHead(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				if(n==phrase){
					org.jdom2.Element element2 = sentence.get(n);
					List<org.jdom2.Element> tokens = element2.getChildren("tokens");
					for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
						org.jdom2.Element element3 = tokens.get(p);
						List<org.jdom2.Element> token = element3.getChildren("token");
						for (org.jdom2.Element tokenElement : token) {//pour chaque token
							String idStr=tokenElement.getAttributeValue("id");
							int id1=Integer.parseInt(idStr);
							if(id1==id){//
								String numHead=tokenElement.getAttributeValue("head");
								int res=Integer.parseInt(numHead);
								return res;
							}
						}
					}
				}
			}
		}
		return -1;
	}

	public static String getStart(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				if(n==phrase){
					org.jdom2.Element element2 = sentence.get(n);
					List<org.jdom2.Element> tokens = element2.getChildren("tokens");
					for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
						org.jdom2.Element element3 = tokens.get(p);
						List<org.jdom2.Element> token = element3.getChildren("token");
						for (org.jdom2.Element tokenElement : token) {//pour chaque token
							String idStr=tokenElement.getAttributeValue("id");
							int id1=Integer.parseInt(idStr);
							if(id1==id){//
								String start=tokenElement.getAttributeValue("start");
								return start;
							}
						}
					}
				}
			}
		}
		return "";
	}

	public static String getEnd(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				if(n==phrase){
				org.jdom2.Element element2 = sentence.get(n);
				List<org.jdom2.Element> tokens = element2.getChildren("tokens");
				for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
					org.jdom2.Element element3 = tokens.get(p);
					List<org.jdom2.Element> token = element3.getChildren("token");
					for (org.jdom2.Element tokenElement : token) {//pour chaque token
						String idStr=tokenElement.getAttributeValue("id");
						int id1=Integer.parseInt(idStr);
						if(id1==id){//
							String end=tokenElement.getAttributeValue("end");
							return end;
						}
						//int head=Integer.parseInt(numHead);
					}
				}
				}
			}
		}
		return "";
	}

	
	
	
	public static String getPos(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{

		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				if(n==phrase){
				org.jdom2.Element element2 = sentence.get(n);
				List<org.jdom2.Element> tokens = element2.getChildren("tokens");
				for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
					org.jdom2.Element element3 = tokens.get(p);
					List<org.jdom2.Element> token = element3.getChildren("token");
					for (org.jdom2.Element tokenElement : token) {//pour chaque token
						String idStr=tokenElement.getAttributeValue("id");
						int id1=Integer.parseInt(idStr);
						String pos=tokenElement.getAttributeValue("pos");
						if(id1==id){
							return pos;
						}
					}
				}
				}
			}
		}
		return "";
	}
	
	
	
	

	public static String getForm(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{

		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				if(n==phrase){
				org.jdom2.Element element2 = sentence.get(n);
				List<org.jdom2.Element> tokens = element2.getChildren("tokens");
				for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
					org.jdom2.Element element3 = tokens.get(p);
					List<org.jdom2.Element> token = element3.getChildren("token");
					for (org.jdom2.Element tokenElement : token) {//pour chaque token
						String idStr=tokenElement.getAttributeValue("id");
						int id1=Integer.parseInt(idStr);
						String form=tokenElement.getAttributeValue("form");
						if(id1==id){
							return form;
						}
					}
				}
				}
			}
		}
		return "";
	}

	
	
	public static String getType(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{

		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				if(n==phrase){
				org.jdom2.Element element2 = sentence.get(n);
				List<org.jdom2.Element> tokens = element2.getChildren("tokens");
				for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
					org.jdom2.Element element3 = tokens.get(p);
					List<org.jdom2.Element> token = element3.getChildren("token");
					for (org.jdom2.Element tokenElement : token) {//pour chaque token
						String idStr=tokenElement.getAttributeValue("id");
						int id1=Integer.parseInt(idStr);
						String type=tokenElement.getAttributeValue("type");
						if(id1==id){
							return type;
						}
					}
				}
				}
			}
		}
		return "";
	}
	

	
	
	public static String getDepRel(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{

		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				if(n==phrase){
					org.jdom2.Element element2 = sentence.get(n);
					List<org.jdom2.Element> tokens = element2.getChildren("tokens");
					for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
						org.jdom2.Element element3 = tokens.get(p);
						List<org.jdom2.Element> token = element3.getChildren("token");
						for (org.jdom2.Element tokenElement : token) {//pour chaque token
							String idStr=tokenElement.getAttributeValue("id");
							int id1=Integer.parseInt(idStr);
							String depRel=tokenElement.getAttributeValue("depRel");
							if(id1==id){
								return depRel;
							}
						}
					}
				}
			}
		}
		return "";
	}
	
	

	public static String getLemma(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{

		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				if(n==phrase){
					org.jdom2.Element element2 = sentence.get(n);
					List<org.jdom2.Element> tokens = element2.getChildren("tokens");
					for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
						org.jdom2.Element element3 = tokens.get(p);
						List<org.jdom2.Element> token = element3.getChildren("token");
						for (org.jdom2.Element tokenElement : token) {//pour chaque token
							String idStr=tokenElement.getAttributeValue("id");
							int id1=Integer.parseInt(idStr);
							String lemma=tokenElement.getAttributeValue("lemma");
							if(id1==id){
								return lemma;
							}
						}
					}
				}
			}
		}
		return "";
	}
	
	

	public static String getSentence(int id, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{//retourne la phrase pass�e en identifiant
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> tokenListElements = root.getChildren("sentence");
		org.jdom2.Element element2 = tokenListElements.get(id);//numero de la phrase
		List<org.jdom2.Element> tokenListElements2 = element2.getChildren("token");
		String sentence="";
		for (org.jdom2.Element tokenElement : tokenListElements2) {//pour chaque token
			String form=tokenElement.getAttributeValue("form");
			sentence = sentence +" "+ form;
		}
		return sentence;
	}


	public static String getSentence(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{
		String sortie="";
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				if(n==phrase){
					org.jdom2.Element element2 = sentence.get(n);
					List<org.jdom2.Element> tokens = element2.getChildren("tokens");
					for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
						org.jdom2.Element element3 = tokens.get(p);
						List<org.jdom2.Element> token = element3.getChildren("token");
						
						for (org.jdom2.Element tokenElement : token) {//pour chaque token
							String form=tokenElement.getAttributeValue("form");
							sortie = sortie +" "+ form;
						}
					}
				}
			}
		}
		return sortie;
	}
	
	
	
	public static String getValue(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException{
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				if(n==phrase){
					org.jdom2.Element element2 = sentence.get(n);
					List<org.jdom2.Element> tokens = element2.getChildren("tokens");
					for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
						org.jdom2.Element element3 = tokens.get(p);
						List<org.jdom2.Element> token = element3.getChildren("token");
						for (org.jdom2.Element tokenElement : token) {//pour chaque token
							String idStr=tokenElement.getAttributeValue("id");
							int id1=Integer.parseInt(idStr);
							if(id1==id){
								List<org.jdom2.Element> features=tokenElement.getChildren("features");
								for (int h = 0; h <features.size();h++) {//pour chaque features
									org.jdom2.Element feature = features.get(h);
									List<org.jdom2.Element> featureElement = feature.getChildren("feature");
									for (org.jdom2.Element feat : featureElement) {//pour chaque token
										String value=feat.getAttributeValue("value");//au cas o�...
										if(value==null){value="";}
										return value;
									}
								}
							}
						}
					}
				}
			}
		}
		return "";
	}

	/*   <token depRel="obj" end="3823" form="cible" head="3" id="5" lemma="cible" pos="NC" start="3818">
	      <feature attribute="MorphoAnnotation" value="fs" />
	    </token>*/

	public static int nbSentence(String text) throws ParserConfigurationException, SAXException, IOException{
		org.jdom2.Document jdomDoc = JDOMXMLReader.useDOMParser(text);
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size();) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			
			return sentence.size(); 
		}
		
		/*org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				org.jdom2.Element element2 = sentence.get(n);
				List<org.jdom2.Element> tokens = element2.getChildren("tokens");
				for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
					org.jdom2.Element element3 = tokens.get(p);
					List<org.jdom2.Element> token = element3.getChildren("token");
					
				*/	
		return 0;
	}

	public static void afficher(Document document){
		try{
			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(document, System.out);
		}
		catch (java.io.IOException e){
			e.printStackTrace();
		}
	}

	public static void sortieHTML(Document jdomDoc){
		//File MyFile = new File("sortie.html");//mettre en commentaire les deux lignes pour ne pas supprimer � chaque fois le contenu du fichier
		//MyFile.delete(); 
		/*String result="<font size=5><span style=\"background:#E4FD01\">Product</span>  " +
				"<span style=\"background:#AAAFF6\">Range</span> " +
				"<span style=\"background:#0112FE\">Brand</span> " +
				"<span style=\"background:#FE7070\">Division</span> " +
				"<span style=\"background:#FE0101\">Group</span>" +
				"</font><br/><br/><br/><br/>";*/
		String result="";


		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				org.jdom2.Element element2 = sentence.get(n);
				List<org.jdom2.Element> tokens = element2.getChildren("tokens");
				for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
					org.jdom2.Element element3 = tokens.get(p);
					List<org.jdom2.Element> token = element3.getChildren("token");
					for (org.jdom2.Element tokenElement : token) {//pour chaque token



						String form=tokenElement.getAttributeValue("form");
						String type=tokenElement.getAttributeValue("type");
						if(type!=null){
							if(type.equals("product")){
								result=result+" <span style=\"background:#E4FD01\">"+form+"</span>";
							}
							else if(type.equals("brand")){
								result=result+" <span style=\"background:#0112FE\">"+form+"</span>";
							}
							else if(type.equals("range")){
								result=result+" <span style=\"background:#AAAFF6\">"+form+"</span>";
							}
							else if(type.equals("division")){
								result=result+" <span style=\"background:#FE7070\">"+form+"</span>";
							}
							else if(type.equals("group")){
								result=result+" <span style=\"background:#FE0101\">"+form+"</span>";
							}
							else if(type.equals("not_identified")){
								result=result+" <span style=\"background:#EFEFEF\">"+form+"</span>";
							}
						}
						else{
							result=result+" "+form;
						}
					}
				}
			}
		}
		Text.write("sortie.html", result+"<hr/>");
	}

	public static void sortieXML(Document jdomDoc){
		String result="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
		result = result + "<units_list>";
		result = result + "<unit>";
		org.jdom2.Element root = jdomDoc.getRootElement();
		List<org.jdom2.Element> sentences = root.getChildren("sentences");
		for (int m = 0; m <sentences.size(); m++) {//pour chaque sentences
			org.jdom2.Element element1 = sentences.get(m);
			List<org.jdom2.Element> sentence = element1.getChildren("sentence");
			for (int n = 0; n <sentence.size(); n++) {//pour chaque sentence
				org.jdom2.Element element2 = sentence.get(n);
				List<org.jdom2.Element> tokens = element2.getChildren("tokens");
				for (int p = 0; p <tokens.size(); p++) {//pour chaque tokens
					org.jdom2.Element element3 = tokens.get(p);
					List<org.jdom2.Element> token = element3.getChildren("token");
					for (org.jdom2.Element tokenElement : token) {//pour chaque token
						String form=tokenElement.getAttributeValue("form");
						String type=tokenElement.getAttributeValue("type");
						if(type!=null){
							String rulesProduct=tokenElement.getAttributeValue("rulesproduct");
							String rulesRange=tokenElement.getAttributeValue("rulesrange");
							String rulesBrand=tokenElement.getAttributeValue("rulesbrand");
							String rulesDivision=tokenElement.getAttributeValue("rulesdivision");
							String rulesGroup=tokenElement.getAttributeValue("rulesgroup");
							result=result+" <named_entity typeRenco=\""+type+"\" rulesproduct=\""+rulesProduct+ "\" rulesrange=\""+rulesRange+ "\" rulesbrand=\""+rulesBrand+ "\" rulesdivision=\""+rulesDivision + "\" rulesgroup=\""+rulesGroup+"\">"+form+"</named_entity>";
						}
						else{
							result=result+" "+form;
						}
					}
				}
			}
		}
		result = result + "</unit>";
		Text.write("sortie.xml", "\n"+result);
	}

	
}