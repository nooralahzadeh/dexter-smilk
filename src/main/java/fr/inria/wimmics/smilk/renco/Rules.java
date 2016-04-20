package fr.inria.wimmics.smilk.renco;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Elena
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Document;
import org.springframework.core.io.ClassPathResource;

import org.xml.sax.SAXException;

public class Rules {

    private static   String gammes;

    private static   String marques;
    private static   String produits;
    private static   String divisions;
    private static   String groupes;
    private static   String ressource;
    private static   String tripletsCOSM;

    static {
        ClassPathResource gammes_resource = new ClassPathResource("gammes.xml");
        ClassPathResource marques_resource = new ClassPathResource("marques.xml");
        ClassPathResource produits_resource = new ClassPathResource("produits.xml");
        ClassPathResource division_resource = new ClassPathResource("divisions.xml");
        ClassPathResource groupes_resource = new ClassPathResource("groupes.xml");
        ClassPathResource resource = new ClassPathResource("ressource.xml");
        ClassPathResource triplesCOSM_resource = new ClassPathResource("tripletsCOSM.txt");
        try {
            gammes = gammes_resource.getFile().getAbsolutePath();
            marques = marques_resource.getFile().getAbsolutePath();
            produits = produits_resource.getFile().getAbsolutePath();
            divisions = division_resource.getFile().getAbsolutePath();
            groupes = groupes_resource.getFile().getAbsolutePath();
            ressource = resource.getFile().getAbsolutePath();
            tripletsCOSM=triplesCOSM_resource.getFile().getAbsolutePath();
        } catch (IOException ex) {
            Logger.getLogger(Rules.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



    /**
     * @param args
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static void learning(Document jdomDoc, float threshold) throws IOException {//ajoute les entit�s nomm�es trouv�s dans les lexiques � condition que le score soit sup�rieur au seuil
        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");
        for (int n = 0; n < sentences.size(); n++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(n);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int m = 0; m < sentence.size(); m++) {//pour chaque phrase
                org.jdom2.Element element2 = sentence.get(m);
                List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                for (int s = 0; s < tokens.size(); s++) {//pour chaque tokens
                    org.jdom2.Element element3 = tokens.get(s);
                    List<org.jdom2.Element> token = element3.getChildren("token");
                    for (org.jdom2.Element tokenElement : token) {//pour chaque tokens
                        String type = tokenElement.getAttributeValue("type");
                        String form = tokenElement.getAttributeValue("form");
                        if (tokenElement.getAttributeValue("confidence") != null) {
                            float confidence = Float.valueOf(tokenElement.getAttributeValue("confidence"));

                            if (confidence > threshold) {
                                //ajout au dico:
                                if (type.equals("group")) {
                                    String search = "name=\"" + form + "\"";
                                    String ajout = "<groupLearned name=\"" + form + "\"></group>\n";
                                    String dicoBrand = Text.getFile(groupes);
                                    if (!dicoBrand.contains(search)) {
                                        Text.write(groupes, ajout);
                                    }
                                } else if (type.equals("division")) {
                                    String search = "name=\"" + form + "\"";
                                    String ajout = "<divisionLearned name=\"" + form + "\"></division>\n";
                                    String dicoBrand = Text.getFile(divisions);
                                    if (!dicoBrand.contains(search)) {
                                        Text.write(divisions, ajout);
                                    }
                                } else if (type.equals("brand")) {
                                    String search = "name=\"" + form + "\"";
                                    String ajout = "<brandLearned name=\"" + form + "\"></brand>\n";
                                    String dicoBrand = Text.getFile(marques);
                                    if (!dicoBrand.contains(search)) {
                                        Text.write(marques, ajout);
                                    }
                                } else if (type.equals("range")) {
                                    String search = "name=\"" + form + "\"";
                                    String ajout = "<rangeLearned name=\"" + form + "\"></range>\n";
                                    String dicoBrand = Text.getFile(gammes);
                                    if (!dicoBrand.contains(search)) {
                                        Text.write(gammes, ajout);
                                    }
                                } else if (type.equals("product")) {
                                    String search = "name=\"" + form + "\"";
                                    String ajout = "<productLearned name=\"" + form + "\"></product>\n";
                                    String dicoBrand = Text.getFile(produits);
                                    if (!dicoBrand.contains(search)) {
                                        Text.write(produits, ajout);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void compute(int id, int phrase, org.jdom2.Document jdomDoc, float threshold) {//r�gle de calcul pour l'attribution d'un type d'entit� nomm�e final
        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");

        //R�gles d�finitoires fond�es sur la syntaxe 0,87
        // R�gles de coordination 0,90
        // R�gles de coordination 0,90
        // R�gles hi�rarchiques 0,68
        // R�gles internes 0,77
        // R�gles internes avec hi�rarchie
        String[][] tabCoef = new String[8][2];//ligne + colonne
        tabCoef[0][0] = "DICO";
        tabCoef[0][1] = "0.97";
        tabCoef[1][0] = "DEF";
        tabCoef[1][1] = "0.87";
        tabCoef[2][0] = "LEARNED";
        tabCoef[2][1] = "0.50";
        tabCoef[3][0] = "COORD";
        tabCoef[3][1] = "0.80";
        tabCoef[4][0] = "HIE";
        tabCoef[4][1] = "0.80";
        tabCoef[5][0] = "INT";
        tabCoef[5][1] = "0.88";
        tabCoef[6][0] = "HIN";
        tabCoef[6][1] = "0.33";
        tabCoef[7][0] = "VER";
        tabCoef[7][1] = "0.59";

        for (int n = 0; n < sentences.size(); n++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(n);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int m = 0; m < sentence.size(); m++) {//pour chaque sentence
                if (m == phrase) {
                    org.jdom2.Element element2 = sentence.get(m);
                    List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                    for (int s = 0; s < tokens.size(); s++) {//pour chaque tokens
                        org.jdom2.Element element3 = tokens.get(s);
                        List<org.jdom2.Element> token = element3.getChildren("token");
                        for (org.jdom2.Element tokenElement : token) {//pour chaque tokens
                            String idStr = tokenElement.getAttributeValue("id");
                            int idTerm = Integer.parseInt(idStr);

                            float scoreGroup = 0;
                            float scoreDivision = 0;
                            float scoreBrand = 0;
                            float scoreRange = 0;
                            float scoreProduct = 0;

                            if (idTerm == id) {
                                String type = tokenElement.getAttributeValue("type");
                                if (type != null) {

                                    String group = tokenElement.getAttributeValue("rulesgroup");
                                    String division = tokenElement.getAttributeValue("rulesdivision");
                                    String brand = tokenElement.getAttributeValue("rulesbrand");
                                    String range = tokenElement.getAttributeValue("rulesrange");
                                    String product = tokenElement.getAttributeValue("rulesproduct");

                                    if (group != null) {
                                        String[] tabGroup = group.split(" + ");
                                        for (String string : tabGroup) {
                                            for (int i = 0; i < tabCoef.length; i++) {//for each value possible, we add the local score into the global score
                                                if (string.contains(tabCoef[i][0])) {
                                                    scoreGroup = scoreGroup + Float.valueOf(tabCoef[i][1]);
                                                }
                                            }
                                        }
                                    }
                                    if (division != null) {
                                        String[] tabDivision = division.split(" + ");
                                        for (String string : tabDivision) {
                                            for (int i = 0; i < tabCoef.length; i++) {//for each value possible, we add the local score into the global score
                                                if (string.contains(tabCoef[i][0])) {
                                                    scoreDivision = scoreDivision + Float.valueOf(tabCoef[i][1]);
                                                }
                                            }
                                        }
                                    }
                                    if (brand != null) {
                                        String[] tabBrand = brand.split(" + ");
                                        for (String string : tabBrand) {
                                            for (int i = 0; i < tabCoef.length; i++) {//for each value possible, we add the local score into the global score
                                                if (string.contains(tabCoef[i][0])) {
                                                    scoreBrand = scoreBrand + Float.valueOf(tabCoef[i][1]);
                                                }
                                            }
                                        }
                                    }
                                    if (range != null) {
                                        String[] tabRange = range.split(" + ");
                                        for (String string : tabRange) {
                                            for (int i = 0; i < tabCoef.length; i++) {//for each value possible, we add the local score into the global score
                                                if (string.contains(tabCoef[i][0])) {
                                                    scoreRange = scoreRange + Float.valueOf(tabCoef[i][1]);
                                                }
                                            }
                                        }
                                    }
                                    if (product != null) {
                                        String[] tabProduct = product.split(" + ");
                                        for (String string : tabProduct) {
                                            for (int i = 0; i < tabCoef.length; i++) {//for each value possible, we add the local score into the global score
                                                if (string.contains(tabCoef[i][0])) {
                                                    scoreProduct = scoreProduct + Float.valueOf(tabCoef[i][1]);
                                                }
                                            }
                                        }
                                    }

                                    //
                                    //on attribue le type de plus haut score:
                                    float max = Math.max(Math.max(Math.max(Math.max(scoreGroup, scoreDivision), scoreBrand), scoreRange), scoreProduct);
                                    String score = String.valueOf(max);
                                    tokenElement.setAttribute("confidence", score);

                                    if (max >= threshold) {
                                        if (scoreBrand == max) {
                                            tokenElement.setAttribute("type", "brand");
                                        } else if (scoreProduct == max) {
                                            tokenElement.setAttribute("type", "product");
                                        } else if (scoreGroup == max) {
                                            tokenElement.setAttribute("type", "group");
                                        } else if (scoreDivision == max) {
                                            tokenElement.setAttribute("type", "division");
                                        } else if (scoreRange == max) {
                                            tokenElement.setAttribute("type", "range");
                                        }
                                    } else {
                                        tokenElement.setAttribute("type", "not_identified");
                                      //  System.out.println("!!!!!!!!!!!!!!!!!!!NOT IDENTIFIED: " + tokenElement.getAttributeValue("lemma") + " (score=" + max + ")");
                                    }

                                    //tokenElement.setAttribute("type", type);
                                    //Attribute lemma=tokenElement.getAttribute("lemma");
                                    //System.out.println(lemma+">>>>>>"+scoreBrand+" "+scoreDivision+" "+scoreGroup+" "+scoreProduct+" "+scoreRange);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean person(String entity, String dicoPrenoms) throws IOException {
        String[] tab = entity.split(" +");
        if (tab.length == 2) {
            if (dicoPrenoms.contains("<prenom>" + tab[0] + "</prenom>")) {
                return true;
            }
        }
        return false;
    }

    public static boolean mult_EN_bool(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//retourne TRUE si le nombre d'entit�s en coordination apr�s une entit� donn�e est sup�rieur � 1
        int result = 0;
        String term0 = AccessJDom.getPos(id, phrase, jdomDoc);
        String term1 = AccessJDom.getPos(id + 1, phrase, jdomDoc);
        String term2 = AccessJDom.getPos(id + 2, phrase, jdomDoc);
        String term3 = AccessJDom.getPos(id + 3, phrase, jdomDoc);
        String term4 = AccessJDom.getPos(id + 4, phrase, jdomDoc);
        if (term0.equals("NPP")) {
            result = result + 1;
        }
        if (term1.equals("NPP")) {
            result = result + 1;
        }
        if (term2.equals("NPP")) {
            result = result + 1;
        }
        if (term3.equals("NPP")) {
            result = result + 1;
        }
        if (term4.equals("NPP")) {
            result = result + 1;
        }
        if (result > 1) {
            return true;
        } else {
            return false;
        }
    }

    public static void DEF(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles d�finitoires
        //if(type==null){
        //lexiques de contextes
        String group[] = {"groupe"};
        String division[] = {"division", "p�le"};
        String brand[] = {"marque", "signature", "boutique", "maison", "laboratoire"};
        String range[] = {"gamme", "ligne", "saga", "collection", "franchise"};
        String product[] = {"opus", "gel-cr�me", "solution", "parfum", "�dition", "spray", "lisseur", "d�maquillant", "floral-fruit�", "floral", "fruit�", "f�minin", "masculin", "masque", "savon", "gel", "palette", "eau", "gel�e", "tonique", "vaporisateur", "beurre", "creme", "cr�me", "applicateur", "gloss", "mascara", "parfum", "fond", "FDT", "d�odorant", "vernis", "baume", "shampoing", "rouge", "l�vre", "shampooing", "eyeliner", "eye-liner", "crayon", "poudre", "blush", "dissolvant", "durcisseur", "serum", "s�rum", "masque", "gommage", "huiles", "huile", "apr�s-shampooing", "d�m�lant", "d�frisant", "gel", "spray", "laque", "lotion", "d�maquillant", "kajal", "kh�l", "jus", "cologne", "dentifrice", "savon", "lait", "mousse"};

        //on recupere le terme en cours de traitement
        String term = AccessJDom.getForm(id, phrase, jdomDoc);
        String term1 = AccessJDom.getForm(id + 1, phrase, jdomDoc);
        String term2 = AccessJDom.getForm(id + 2, phrase, jdomDoc);
        String term3 = AccessJDom.getForm(id + 3, phrase, jdomDoc);

        //on recupere la t�te de id
        int head = AccessJDom.getHead(id, phrase, jdomDoc);//25: marques
        String lemma = AccessJDom.getLemma(head, phrase, jdomDoc);

        String form = AccessJDom.getForm(head, phrase, jdomDoc);

        if (Table.contain(lemma, group) | Table.contain(form, group)) {
           // System.out.println("DEF-SYNTAX(1): " + term + " => group (contexte : " + lemma + ")");
            Text.write("sortie.html", "DEF-SYNTAX(1): " + term + " => group (contexte : " + lemma + ")" + "<br/>");
            String typeEN = "group";
            String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
            if (!rules.contains("DEF1")) {
                AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                AccessJDom.setAttributeRules(id, "DEF1", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
            }
            //ajout au dico:
		/*String search="name=\""+term+"\"";
             String ajout="<groupLearned name=\""+term+"\"></group>\n";
             String dicoBrand=Text.getFile(groupes);

             if(!dicoBrand.contains(search)){

             Text.write(groupes, ajout);
             }*/
        } else if (Table.contain(lemma, division) | Table.contain(form, division)) {
          //  System.out.println("DEF-SYNTAX(2) : " + term + " => range (contexte : " + lemma + ")");
            Text.write("sortie.html", "DEF-SYNTAX(2) : " + term + " => range (contexte : " + lemma + ")" + "<br/>");
            String typeEN = "division";
            String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
            if (!rules.contains("DEF2")) {
                AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                AccessJDom.setAttributeRules(id, "DEF2", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
            }
            //ajout au dico:
		/*String search="name=\""+term+"\"";
             String ajout="<divisionLearned name=\""+term+"\"></division>\n";
             String dicoBrand=Text.getFile(divisions);
             if(!dicoBrand.contains(search)){
             Text.write(divisions, ajout);
             }*/
        } else if (Table.contain(lemma, brand) | Table.contain(form, brand)) {
          //  System.out.println("DEF-SYNTAX(3) : " + term + " => brand (contexte : " + lemma + ")");
            Text.write("sortie.html", "DEF-SYNTAX(3) : " + term + " => brand (contexte : " + lemma + ")" + "<br/>");
            String typeEN = "brand";
            String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
            if (!rules.contains("DEF3")) {
                AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                AccessJDom.setAttributeRules(id, "DEF3", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
            }
            //ajout au dico:
		/*String search="name=\""+term+"\"";
             String ajout="<brandLearned name=\""+term+"\"></brand>\n";
             String dicoBrand=Text.getFile(marques);
             if(!dicoBrand.contains(search)){
             Text.write(marques, ajout);
             }*/
        } else if (Table.contain(lemma, range) | Table.contain(form, range)) {
           // System.out.println("DEF-SYNTAX(4) : " + term + " => range (contexte : " + lemma + ")");
            Text.write("sortie.html", "DEF-SYNTAX(4) : " + term + " => range (contexte : " + lemma + ")" + "<br/>");
            String typeEN = "range";
            String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
            if (!rules.contains("DEF4")) {
                AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                AccessJDom.setAttributeRules(id, "DEF4", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
            }
            //ajout au dico:
		/*String search="name=\""+term+"\"";
             String ajout="<rangeLearned name=\""+term+"\"></range>\n";
             String dicoBrand=Text.getFile(gammes);
             if(!dicoBrand.contains(search)){
             Text.write(gammes, ajout);
             }*/
        } else if (Table.contain(lemma, product) | Table.contain(form, product)) {

            String value = AccessJDom.getValue(head, phrase, jdomDoc);
            boolean nb_EN = mult_EN_bool(head, phrase, jdomDoc);

            if (value.equals("ms") || value.equals("fs") || value.equals("s")) {//si le contexte est au singulier

                if (!nb_EN) {//et si une seule entit� le succ�de alors l'entit� nomm�e est un produit
                  //  System.out.println("DEF-SYNTAX(5) : " + term + " => product (contexte : " + lemma + ")");
                    Text.write("sortie.html", "DEF-SYNTAX(5) : " + term + " => product (contexte : " + lemma + ")" + "<br/>");
                    String typeEN = "product";
                    String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                    if (!rules.contains("DEF5")) {
                        AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                        AccessJDom.setAttributeRules(id, "DEF5", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                    }
                    //ajout au dico:
				/*String search="name=\""+term+"\"";
                     String ajout="<productLearned name=\""+term+"\"></product>\n";
                     String dicoBrand=Text.getFile(produits);
                     if(!dicoBrand.contains(search)){
                     Text.write(produits, ajout);
                     }
                     */
                }
            }
            if (value.equals("mp") || value.equals("fp") || value.equals("p")) {//si le contexte est au pluriel
                if (!nb_EN) {//et si une seule entit� le succ�de alors l'entit� nomm�e est un produit
                   // System.out.println("DEF-SYNTAX(6) : " + term + " => brand (contexte : " + lemma + ")");
                    Text.write("sortie.html", "DEF-SYNTAX(6) : " + term + " => brand (contexte : " + lemma + ")" + "<br/>");
                    String typeEN = "brand";
                    String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                    if (!rules.contains("DEF6")) {
                        AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                        AccessJDom.setAttributeRules(id, "DEF6", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                    }
                    //ajout au dico:
				/*String search="name=\""+term+"\"";
                     String ajout="<brandLearned name=\""+term+"\"></brand>\n";
                     String dicoBrand=Text.getFile(marques);
                     if(!dicoBrand.contains(search)){
                     Text.write(marques, ajout);
                     }*/
                } else {//et si plusieurs entit�s le succ�dent alors l'entit� nomm�e est une marque
                   // System.out.println("DEF-SYNTAX(7) : " + term + " => product (contexte : " + lemma + ")");
                    Text.write("sortie.html", "DEF-SYNTAX(7) : " + term + " => product (contexte : " + lemma + ")" + "<br/>");
                    String typeEN = "product";
                    String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                    if (!rules.contains("DEF7")) {
                        AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                        AccessJDom.setAttributeRules(id, "DEF7", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                    }
                    //ajout au dico:
				/*String search="name=\""+term+"\"";
                     String ajout="<productLearned name=\""+term+"\"></product>\n";
                     String dicoBrand=Text.getFile(produits);
                     if(!dicoBrand.contains(search)){
                     Text.write(produits, ajout);
                     }*/
                }
            }

        } else if (term1.equals("est") && (term2.equals("un") | term2.equals("une")) && (Table.contain(term3, product))) {
            //System.out.println("DEF-is-a : " + term + " => product");
            Text.write("sortie.html", "DEF-is-a : " + term + " => product" + "<br/>");
            String typeEN = "product";
            String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
            if (!rules.contains("DEF8")) {
                AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                AccessJDom.setAttributeRules(id, "DEF8", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
            }
            //ajout au dico:
		/*String search="name=\""+term+"\"";
             String ajout="<productLearned name=\""+term+"\"></product>\n";
             String dicoBrand=Text.getFile(produits);
             if(!dicoBrand.contains(search)){
             Text.write(produits, ajout);
             }*/
        } else if (term1.equals("est") && (term2.equals("un") | term2.equals("une")) && (Table.contain(term3, range))) {
           // System.out.println("DEF-is-a : " + term + " => range");
            Text.write("sortie.html", "DEF-is-a : " + term + " => range" + "<br/>");
            String typeEN = "range";
            String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
            if (!rules.contains("DEF9")) {
                AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                AccessJDom.setAttributeRules(id, "DEF9", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
            }
            //ajout au dico:
		/*String search="name=\""+term+"\"";
             String ajout="<rangeLearned name=\""+term+"\"></range>\n";
             String dicoBrand=Text.getFile(gammes);
             if(!dicoBrand.contains(search)){
             Text.write(gammes, ajout);
             }*/
        } else if (term1.equals("est") && (term2.equals("un") | term2.equals("une")) && (Table.contain(term3, brand))) {
           // System.out.println("DEF-is-a : " + term + " => brand");
            Text.write("sortie.html", "DEF-is-a : " + term + " => brand" + "<br/>");
            String typeEN = "brand";
            String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
            if (!rules.contains("DEF10")) {
                AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                AccessJDom.setAttributeRules(id, "DEF10", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
            }
            //ajout au dico:
		/*String search="name=\""+term+"\"";
             String ajout="<brandLearned name=\""+term+"\"></brand>\n";
             String dicoBrand=Text.getFile(marques);
             if(!dicoBrand.contains(search)){
             Text.write(marques, ajout);
             }*/
        } else if (term1.equals("est") && (term2.equals("un") | term2.equals("une")) && (Table.contain(term3, division))) {
            //System.out.println("DEF-is-a : " + term + " => division");
            Text.write("sortie.html", "DEF-is-a : " + term + " => division" + "<br/>");
            String typeEN = "division";
            String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
            if (!rules.contains("DEF11")) {
                AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                AccessJDom.setAttributeRules(id, "DEF11", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
            }
            //ajout au dico:
		/*String search="name=\""+term+"\"";
             String ajout="<divisionLearned name=\""+term+"\"></division>\n";
             String dicoBrand=Text.getFile("division.xml");
             if(!dicoBrand.contains(search)){
             Text.write("division.xml", ajout);
             }*/
        } else if (term1.equals("est") && (term2.equals("un") | term2.equals("une")) && (Table.contain(term3, group))) {
            //System.out.println("DEF-is-a : " + term + " => group");
            Text.write("sortie.html", "DEF-is-a : " + term + " => group" + "<br/>");
            String typeEN = "group";
            String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
            if (!rules.contains("DEF12")) {
                AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                AccessJDom.setAttributeRules(id, "DEF12", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
            }
            //ajout au dico:
		/*String search="name=\""+term+"\"";
             String ajout="<groupLearned name=\""+term+"\"></group>\n";
             String dicoBrand=Text.getFile(groupes);
             if(!dicoBrand.contains(search)){
             Text.write(groupes, ajout);
             }*/
        }
        //}
    }

    public static boolean EXCL(String term) {//r�gles d'exclusion
        //lexiques de contextes
        String loc[] = {"Londres", "Tokyo", "Berlin", "Zurich", "Madrid", "Mexique", "Guatemala", "New-York", "Suisse", "Gen�ve", "Sud", "Nord", "Est", "Ouest", "USA", "Europe", "Afrique", "Am�rique", "Asie", "Moyen-Orient", "Afghanistan", "Albanie", "Alg�rie", "Allemagne", "Andorre", "Angola", "Anguilla", "Argentine", "Arm�nie", "Aruba", "Australie", "Autriche", "Azerba�djan", "Bahamas", "Bahrein", "Bangladesh", "Barbade", "Belgique", "Belize", "B�nin", "Bermudes", "Bhoutan", "Birmanie", "Bolivie", "Botswana", "Br�sil", "Bulgarie", "Burundi", "Cambodge", "Cameroun", "Canada", "Cayman", "Centre-Afrique", "Chili", "Chine", "Chypre", "Colombie", "Comores", "Congo", "Croatie", "Cuba", "Danemark", "Djibouti", "Dominique", "Egypte", "Equateur", "Erythr�e", "Espagne", "Estonie", "�tats-Unis", "Etats-Unis", "Ethiopie", "ex-Yougoslavie", "Falkland", "F�ro�", "Fidji", "Finlande", "France", "Gabon", "Gambie", "G�orgie", "Ghana", "Gibraltar", "Grande-Bretagne", "Gr�ce", "Grenade", "Groenland", "Guadeloupe", "Guatemala", "Guin�e", "Guyane", "Ha�ti", "Honduras", "Hongrie", "Inde", "Indon�sie", "Irak", "Iran", "Irlande", "Islande", "Isra�l", "Italie", "Jama�que", "Japon", "Jordanie", "Kazakhstan", "Kenya", "Kirghizistan", "Kiribati", "Kowe�t", "Laos", "Lesotho", "Lettonie", "Liban", "Lib�ria", "Libye", "Liechtenstein", "Lituanie", "Luxembourg", "Macao", "Madagascar", "Malaisie", "Malawi", "Maldives", "Mali", "Malte", "Maroc", "Martinique", "Mauritanie", "Mayotte", "Mexice", "Micron�sie", "Moldavie", "Monaco", "Mongolie", "Montserrat", "Mozambique", "Namibie", "Nauru", "N�pal", "Nicaragua", "Niger", "Nig�ria", "Niue", "Norfolk", "Norv�ge", "Oman", "Ouganda", "Ouzb�kistan", "Pakistan", "Palau", "Panama", "Paraguay", "Pays-Bas", "P�rou", "Philippines", "Pitcairn", "Plogne", "Porto-Rico", "Portugal", "Qatar", "R�union", "Roumanie", "Russie", "Rwanda", "S�n�gal", "Seychelles", "Singapour", "Slov�nie", "Somalie", "Soudan", "Su�de", "Suisse", "Suriname", "Swaziland", "Syrie", "Ta�wan", "Tanzanie", "Tchad", "Tha�lande", "Togo", "Tokelau", "Tonga", "Tunisie", "Turkm�nistan", "Turquie", "Tuvalu", "Ukraine", "Uruguay", "Vanuatu", "Vatican", "V�n�zuela", "Vietnam", "Yemen", "Zambie", "Zimbabwe"};
        String func[] = {"RH", "DRH", "PDG", "DG", "DGA", "DGPA", "DPGP", "CA", "CEO", "R & D", "PDM", "EdT"};
        String other[] = {"GMS"};
        //String outils[]={"Cette","Avec","Ma","Ta","Sa","Mon","Ton","Son","L","Notre","Nous","Certaines","Leur","Leurs","Peu","Chaque","�","Enfin","Une","Depuis", "Dans","Nos","Leur","Leurs","Apr�s","Avant","Au","Aux","Quand","Quant","Elles","Ils","Il","Elle","On","Mais","Donc","Ni","Car","Si","Que","Cependant","Alors","Pourtant","Dans","Outre","C'","L'","Le", "La", "Les", "Des", "Sa","Son", "Ses", "Ce", "Ces","En","Pour","Un","Sans","Certains","Chez","Aucun", "Plusieurs","Un","Deux","Trois","Quatre","Cinq","Six","Sept","Huit","Neuf"}; 

        String[] tabMots = term.split(" ");
        for (String term1 : tabMots) {
            if (Table.contain(term1, loc) || Table.contain(term1, func) || Table.contain(term1, other)) {//|| Table.contain(term1, outils)){
                //System.out.println("DEF-EXCL: "+term);
                return true;
            }
        }
        return false;
    }

    public static void HIE(int id, int phrase, String dicoPrenoms, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles de hi�rarchie
        //id = mot en cours ; id-1 = mot avant
        //on recupere le terme en cours de traitement
        String term = AccessJDom.getForm(id, phrase, jdomDoc);
        //ATTENTION id>2 est conseill� mais dans ce cas les 2 premiers mots de la phrase ne peuvent pas etre pris en compte
        if (id > 0) {//pour ne pas avoir un identifiant negatif quand on fait i-2 ci-apres
            String form = AccessJDom.getForm(id - 1, phrase, jdomDoc);
            String type = AccessJDom.getType(id - 2, phrase, jdomDoc);
            //String typeCourant=AccessJDom.getType(id, phrase, jdomDoc);
            //if( typeCourant==null){
            if (form.equals("de") || form.equals("d'") || form.equals("(") || form.equals("/")) {//si le mot avant est "de" et que le mot encore avant est une entit� typ�e

                //si le type est nul, on cr�� un type par d�faut type=0
                if (type != null) {

                    if (type.equals("product")) {
                        String typeEN = "brand";
                    //    System.out.println("HIE (1) : " + term + " => brand");
                        Text.write("sortie.html", "HIE (1) : " + term + " => brand" + "<br/>");
                        String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                        if (!rules.contains("HIE1")) {
                            AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                            AccessJDom.setAttributeRules(id, "HIE1", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                        }
                        /*String search="name=\""+term+"\"";
                         String ajout="<brandLearned name=\""+term+"\"></brand>";
                         String dicoBrand=Text.getFile(marques);
                         if(!dicoBrand.contains(search)){
                         Text.write(marques, ajout+"\n");
                         }*/
                        //on ajoute l'info � la ressource
                        String ressourceCosmetique = Text.getFile(ressource);
                        String inf = AccessJDom.getForm(id - 2, phrase, jdomDoc);//mot apres le "de"
                        String hierarchie = "<" + typeEN + " name=\"" + term + "\" inf=\"" + inf + "\">";
                        if (!ressourceCosmetique.contains(hierarchie)) {
                            Text.write(ressource, hierarchie + "\n");
                        }
                    } else if (type.equals("brand")) {
                        String typeEN = "group";
                        //System.out.println("HIE (2) : " + term + " => group");
                        Text.write("sortie.html", "HIE (2) : " + term + " => group" + "<br/>");

                        String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                        if (!rules.contains("HIE2")) {
                            AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                            AccessJDom.setAttributeRules(id, "HIE2", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                        }
                        /*String search="name=\""+term+"\"";
                         String ajout="<groupLearned name=\""+term+"\"></group>";
                         String dicoBrand=Text.getFile(groupes);
                         if(!dicoBrand.contains(search)){
                         Text.write(groupes, ajout+"\n");
                         }*/
                        //on ajoute l'info � la ressource
                        String ressourceCosmetique = Text.getFile(ressource);
                        String inf = AccessJDom.getForm(id - 2, phrase, jdomDoc);//mot apres le "de"
                        String hierarchie = "<" + typeEN + " name=\"" + term + "\" inf=\"" + inf + "\">";
                        if (!ressourceCosmetique.contains(hierarchie)) {
                            Text.write(ressource, hierarchie + "\n");
                        }
                    } else if (type.equals("range")) {
                        String typeEN = "brand";
                        //System.out.println("HIE (3) : " + term + " => brand");
                        Text.write("sortie.html", "HIE (3) : " + term + " => brand" + "<br/>");
                        String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                        if (!rules.contains("HIE3")) {
                            AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                            AccessJDom.setAttributeRules(id, "HIE3", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                        }
                        /*String search="name=\""+term+"\"";
                         String ajout="<brandLearned name=\""+term+"\"></brand>";						
                         String dicoBrand=Text.getFile(marques);
                         if(!dicoBrand.contains(search)){
                         Text.write(marques, ajout+"\n");
                         }*/
                        //on ajoute l'info � la ressource
                        String ressourceCosmetique = Text.getFile(ressource);
                        String inf = AccessJDom.getForm(id - 2, phrase, jdomDoc);//mot apres le "de"
                        String hierarchie = "<" + typeEN + " name=\"" + term + "\" inf=\"" + inf + "\">";
                        if (!ressourceCosmetique.contains(hierarchie)) {
                            Text.write(ressource, hierarchie + "\n");
                        }
                    } else if (type.equals("division")) {
                        String typeEN = "group";
                        //System.out.println("HIE (4) : " + term + " => group");
                        Text.write("sortie.html", "HIE (4) : " + term + " => group" + "<br/>");
                        String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                        if (!rules.contains("HIE4")) {
                            AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                            AccessJDom.setAttributeRules(id, "HIE4", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                        }
                        /*String search="name=\""+term+"\"";
                         String ajout="<groupLearned name=\""+term+"\"></group>";
                         String dicoBrand=Text.getFile(groupes);
                         if(!dicoBrand.contains(search)){
                         Text.write(groupes, ajout+"\n");
                         }*/
                        //on ajoute l'info � la ressource
                        String ressourceCosmetique = Text.getFile(ressource);
                        String inf = AccessJDom.getForm(id - 2, phrase, jdomDoc);//mot apres le "de"
                        String hierarchie = "<" + typeEN + " name=\"" + term + "\" inf=\"" + inf + "\">";
                        if (!ressourceCosmetique.contains(hierarchie)) {
                            Text.write(ressource, hierarchie + "\n");
                        }
                    } else if (type.equals("group")) {
                        String typeEN = "group";
                        //System.out.println("HIE (5) : " + term + " => group");
                        Text.write("sortie.html", "HIE (5) : " + term + " => group" + "<br/>");
                        String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                        if (!rules.contains("HIE5")) {
                            AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                            AccessJDom.setAttributeRules(id, "HIE5", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                        }
                        /*String search="name=\""+term+"\"";
                         String ajout="<groupLearned name=\""+term+"\"></group>";
                         String dicoBrand=Text.getFile(groupes);
                         if(!dicoBrand.contains(search)){
                         Text.write(groupes, ajout+"\n");
                         }*/
                        //on ajoute l'info � la ressource
                        String ressourceCosmetique = Text.getFile(ressource);
                        String inf = AccessJDom.getForm(id - 2, phrase, jdomDoc);//mot apres le "de"
                        String hierarchie = "<" + typeEN + " name=\"" + term + "\" inf=\"" + inf + "\">";
                        if (!ressourceCosmetique.contains(hierarchie)) {
                            Text.write(ressource, hierarchie + "\n");
                        }
                    } else {

                    }
                }
            }
            String formAfter = AccessJDom.getForm(id + 1, phrase, jdomDoc);
            String typeAfter = AccessJDom.getType(id + 2, phrase, jdomDoc);

            if (typeAfter != null) {
                if (formAfter.equals("de") || formAfter.equals("d'") || formAfter.equals("(")) {//si le mot apres est "de" et que le mot encore apres est une entit� typ�e

                    if (typeAfter.equals("product")) {
                        String typeEN = "product";
                       // System.out.println("HIE (6): " + term + " => product");
                        Text.write("sortie.html", "HIE (6): " + term + " => product" + "<br/>");
                        String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                        if (!rules.contains("HIE6")) {
                            AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                            AccessJDom.setAttributeRules(id, "HIE6", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                        }
                        /*String search="name=\""+term+"\"";
                         String ajout="<productLearned name=\""+term+"\"></product>";
                         String dicoBrand=Text.getFile(produits);
                         if(!dicoBrand.contains(search)){
                         Text.write(produits, ajout+"\n");
                         }*/
                        //on ajoute l'info � la ressource
                        String ressourceCosmetique = Text.getFile(ressource);
                        String sup = AccessJDom.getForm(id + 2, phrase, jdomDoc);//mot apres le "de"
                        String hierarchie = "<" + typeEN + " name=\"" + term + "\" sup=\"" + sup + "\">";
                        if (!ressourceCosmetique.contains(hierarchie)) {
                            Text.write(ressource, hierarchie + "\n");
                        }
                    } else if (typeAfter.equals("brand")) {//si le type apr�s est une marque et que l'EN � annoter n'est pas une personne
                        if (!person(term, dicoPrenoms)) {//si le terme n'est pas une personne
                            String typeEN = "product";
                           // System.out.println("HIE (7) : " + term + " => product");
                            Text.write("sortie.html", "HIE (7): " + term + " => product" + "<br/>");
                            String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                            if (!rules.contains("HIE7")) {
                                AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                                AccessJDom.setAttributeRules(id, "HIE7", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                            }
                            /*String search="name=\""+term+"\"";
                             String ajout="<productLearned name=\""+term+"\"></product>";
                             String dicoBrand=Text.getFile(produits);
                             if(!dicoBrand.contains(search)){
                             Text.write(produits, ajout+"\n");
                             }*/
                            //on ajoute l'info � la ressource
                            String ressourceCosmetique = Text.getFile(ressource);
                            String sup = AccessJDom.getForm(id + 2, phrase, jdomDoc);//mot apres le "de"
                            String hierarchie = "<" + typeEN + " name=\"" + term + "\" sup=\"" + sup + "\">";
                            if (!ressourceCosmetique.contains(hierarchie)) {
                                Text.write(ressource, hierarchie + "\n");
                            }
                        }
                    } else if (typeAfter.equals("range")) {
                        String typeEN = "product";
                     //   System.out.println("HIE (8) : " + term + " => product");
                        Text.write("sortie.html", "HIE (8) : " + term + " => product" + "<br/>");
                        String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                        if (!rules.contains("HIE8")) {
                            AccessJDom.setAttributeRules(id, "HIE8", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                            AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                        }
                        /*String search="name=\""+term+"\"";
                         String ajout="<productLearned name=\""+term+"\"></product>";
                         String dicoBrand=Text.getFile(produits);
                         if(!dicoBrand.contains(search)){
                         Text.write(produits, ajout+"\n");
                         }*/
                        //on ajoute l'info � la ressource
                        String ressourceCosmetique = Text.getFile(ressource);
                        String sup = AccessJDom.getForm(id + 2, phrase, jdomDoc);//mot apres le "de"
                        String hierarchie = "<" + typeEN + " name=\"" + term + "\" sup=\"" + sup + "\">";
                        if (!ressourceCosmetique.contains(hierarchie)) {
                            Text.write(ressource, hierarchie + "\n");
                        }
                    } else if (typeAfter.equals("division")) {
                        String typeEN = "brand";
                       // System.out.println("HIE (9) : " + term + " => brand");
                        Text.write("sortie.html", "HIE (9) : " + term + " => brand" + "<br/>");
                        String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                        if (!rules.contains("HIE9")) {
                            AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                            AccessJDom.setAttributeRules(id, "HIE9", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                        }
                        /*String search="name=\""+term+"\"";
                         String ajout="<brandLearned name=\""+term+"\"></brand>";
                         String dicoBrand=Text.getFile(marques);
                         if(!dicoBrand.contains(search)){
                         Text.write(marques, ajout+"\n");
                         }*/
                        //on ajoute l'info � la ressource
                        String ressourceCosmetique = Text.getFile(ressource);
                        String sup = AccessJDom.getForm(id + 2, phrase, jdomDoc);//mot apres le "de"
                        String hierarchie = "<" + typeEN + " name=\"" + term + "\" sup=\"" + sup + "\">";
                        if (!ressourceCosmetique.contains(hierarchie)) {
                            Text.write(ressource, hierarchie + "\n");
                        }
                    }
                }
            }
        }
    }

    public static void VERsuj(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles sur les verbes
        String depRel = AccessJDom.getDepRel(id, phrase, jdomDoc);
        if (depRel.equals("suj")) {//sinon si EN est identifi�e comme un sujet

            org.jdom2.Element root = jdomDoc.getRootElement();
            List<org.jdom2.Element> sentences = root.getChildren("sentences");
            for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
                org.jdom2.Element element1 = sentences.get(m);
                List<org.jdom2.Element> sentence = element1.getChildren("sentence");
                for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                    if (n == phrase) {
                        org.jdom2.Element element2 = sentence.get(n);
                        List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                        for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                            org.jdom2.Element element3 = tokens.get(p);
                            List<org.jdom2.Element> token = element3.getChildren("token");
                            //	for (org.jdom2.Element tokenElement : token) {//pour chaque token

                            String term = AccessJDom.getForm(id, phrase, jdomDoc);

                            String predicat = "";
                            String objet = "";
                            int id1 = -1;
                            Map<String, Integer> scores = new HashMap<String, Integer>();
                            for (org.jdom2.Element tokenElement : token) {//pour chaque token
                                String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                                id1 = Integer.parseInt(idStr);
                                int head = AccessJDom.getHead(id1, phrase, jdomDoc);//on cherche la t�te du sujet
                                for (int i = id; i < id + 10; i++) {//on cherche le verbe qui a pour t�te ce sujet !!! � voir pour le 10 !!!
                                    String pos2 = AccessJDom.getPos(i, phrase, jdomDoc);
                                    if (i == head && pos2.equals("V")) {//quand on a trouv� le verbe qui correspond au sujet
                                        String lemmaVerbe = AccessJDom.getLemma(i, phrase, jdomDoc);//on recupere son lemme
                                        predicat = lemmaVerbe;//on stocke le lemme dans le predicat
                                        for (int k = id; k < id + 10; k++) {//on cherche l'objet du verbe
                                            String depRel2 = AccessJDom.getDepRel(k, phrase, jdomDoc);
                                            if (depRel2.equals("obj")) {//quand on a trouv� l'objet
                                                String typeObjet = AccessJDom.getType(k, phrase, jdomDoc);//on recupere son lemme
                                                objet = typeObjet;
                                                if (objet == null) {//si l'objet n'a pas de type, 
                                                    objet = AccessJDom.getForm(k, phrase, jdomDoc);//on garde son lemme
                                                } else {
                                                    objet = typeObjet.toUpperCase();//on stocke le lemme dans l'objet

                                                }
                                                //on regarde si l'objet et predicat sont connus dans notre base de connaissance
                                                String baseConnaissance = Text.getFile(tripletsCOSM);
                                                String[] triplets = baseConnaissance.split(";");//on stocke la BdC dans un arraylist
                                                //on cherche le triplet le plus fr�quent dans la BdC
                                                scores.put("PROD", 0);
                                                scores.put("RANGE", 0);
                                                scores.put("BRAND", 0);
                                                scores.put("DIV", 0);
                                                scores.put("GROUP", 0);
                                                scores.put("CHIM", 0);
                                                scores.put("PERS", 0);
                                                int newScorePROD = 0;
                                                int newScoreRANGE = 0;
                                                int newScoreBRAND = 0;
                                                int newScoreDIV = 0;
                                                int newScoreGROUP = 0;
                                                int newScoreCHIM = 0;
                                                int newScorePERS = 0;

                                                for (int j = 0; j < triplets.length; j++) {
                                                    if (objet != null && predicat != null) {
                                                        if (triplets[j].contains(predicat) && triplets[j].contains(objet)) {//si le triplet contient l'objet et le predicat										
                                                            String[] triplet = triplets[j].split(",");//on splite notre tableau
                                                            if (triplet.length > 2) {
                                                                String sujetBdC = triplet[0];//et on r�cupere le sujet de la base de Connaissance qui correspond

                                                                if (sujetBdC.equals("PRODUCT")) {
                                                                    newScorePROD = scores.get("PROD") + 1;
                                                                    scores.put("PROD", newScorePROD);
                                                                }
                                                                if (sujetBdC.equals("RANGE")) {
                                                                    newScoreRANGE = scores.get("RANGE") + 1;
                                                                    scores.put("RANGE", newScoreRANGE);
                                                                }
                                                                if (sujetBdC.equals("BRAND")) {
                                                                    newScoreBRAND = scores.get("BRAND") + 1;
                                                                    scores.put("BRAND", newScoreBRAND);
                                                                }
                                                                if (sujetBdC.equals("DIVISION")) {
                                                                    newScoreDIV = scores.get("DIV") + 1;
                                                                    scores.put("DIV", newScoreDIV);
                                                                }
                                                                if (sujetBdC.equals("GROUP")) {
                                                                    newScoreGROUP = scores.get("GROUP") + 1;
                                                                    scores.put("GROUP", newScoreGROUP);
                                                                }
                                                                if (sujetBdC.equals("CHIM")) {
                                                                    newScoreCHIM = scores.get("CHIM") + 1;
                                                                    scores.put("CHIM", newScoreCHIM);
                                                                }
                                                                if (sujetBdC.equals("PERS")) {
                                                                    newScorePERS = scores.get("PERS") + 1;
                                                                    scores.put("PERS", newScorePERS);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                            }

                            String label = max(scores);//on a trouv� le type de l'EN
                            if (label.length() > 0) {
                               // System.out.println("VER (1)(EN + " + predicat + " + " + objet + ") : " + term + " => " + label + " " + scores);
                                Text.write("sortie.html", "VER (1)(EN + " + predicat + " + " + objet + ") : " + term + " => " + label + " " + scores + "<br/>");

                                String rules = AccessJDom.getRules(id, phrase, label, jdomDoc);
                                if (!rules.contains("VER1")) {
                                    AccessJDom.setAttributeType(id, label, phrase, jdomDoc);
                                    AccessJDom.setAttributeRules(id, "VER1", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void VERpred1(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles sur les verbes
        String depRel = AccessJDom.getDepRel(id, phrase, jdomDoc);

        if (depRel.equals("suj")) {//sinon si EN est identifi�e comme un sujet
            org.jdom2.Element root = jdomDoc.getRootElement();
            List<org.jdom2.Element> sentences = root.getChildren("sentences");
            for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
                org.jdom2.Element element1 = sentences.get(m);
                List<org.jdom2.Element> sentence = element1.getChildren("sentence");
                for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                    if (n == phrase) {
                        org.jdom2.Element element2 = sentence.get(n);
                        List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                        for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                            org.jdom2.Element element3 = tokens.get(p);
                            List<org.jdom2.Element> token = element3.getChildren("token");
                            //	for (org.jdom2.Element tokenElement : token) {//pour chaque token

                            String term = AccessJDom.getForm(id, phrase, jdomDoc);

                            String predicat = "";
                            String objet = "";
                            int id1 = -1;
                            Map<String, Integer> scores = new HashMap<String, Integer>();
                            for (org.jdom2.Element tokenElement : token) {//pour chaque token
                                String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                                id1 = Integer.parseInt(idStr);
                                int head = AccessJDom.getHead(id1, phrase, jdomDoc);//on cherche la t�te du sujet
                                for (int i = id; i < id + 10; i++) {//on cherche le verbe qui a pour t�te ce sujet !!! � voir pour le 10 !!!
                                    String pos2 = AccessJDom.getPos(i, phrase, jdomDoc);
                                    if (i == head && pos2.equals("V")) {//quand on a trouv� le verbe qui correspond au sujet
                                        String lemmaVerbe = AccessJDom.getLemma(i, phrase, jdomDoc);//on recupere son lemme
                                        predicat = lemmaVerbe;//on stocke le lemme dans le predicat
                                        for (int k = id; k < id + 10; k++) {//on cherche l'objet du verbe
                                            String depRel2 = AccessJDom.getDepRel(k, phrase, jdomDoc);
                                            if (depRel2.equals("obj")) {//quand on a trouv� l'objet
                                                String typeObjet = AccessJDom.getType(k, phrase, jdomDoc);//on recupere son lemme
                                                objet = typeObjet;
                                                if (objet == null) {//si l'objet n'a pas de type, 
                                                    objet = AccessJDom.getForm(k, phrase, jdomDoc);//on garde son lemme
                                                } else {
                                                    objet = typeObjet.toUpperCase();//on stocke le lemme dans l'objet

                                                }
                                                //on regarde si l'objet et predicat sont connus dans notre base de connaissance
                                                String baseConnaissance = Text.getFile(tripletsCOSM);
                                                String[] triplets = baseConnaissance.split(";");//on stocke la BdC dans un arraylist
                                                //on cherche le triplet le plus fr�quent dans la BdC
                                                scores.put("PROD", 0);
                                                scores.put("RANGE", 0);
                                                scores.put("BRAND", 0);
                                                scores.put("DIV", 0);
                                                scores.put("GROUP", 0);
                                                scores.put("CHIM", 0);
                                                scores.put("PERS", 0);
                                                int newScorePROD = 0;
                                                int newScoreRANGE = 0;
                                                int newScoreBRAND = 0;
                                                int newScoreDIV = 0;
                                                int newScoreGROUP = 0;
                                                int newScoreCHIM = 0;
                                                int newScorePERS = 0;

                                                for (int j = 0; j < triplets.length; j++) {
                                                    if (objet != null && predicat != null) {
                                                        if (triplets[j].contains(objet)) {//si le triplet contient l'objet et le predicat										

                                                            String[] triplet = triplets[j].split(",");//on splite notre tableau
                                                            if (triplet.length > 2) {
                                                                String sujetBdC = triplet[0];//et on r�cupere le sujet de la base de Connaissance qui correspond

                                                                if (sujetBdC.equals("PRODUCT")) {
                                                                    newScorePROD = scores.get("PROD") + 1;
                                                                    scores.put("PROD", newScorePROD);
                                                                }
                                                                if (sujetBdC.equals("RANGE")) {
                                                                    newScoreRANGE = scores.get("RANGE") + 1;
                                                                    scores.put("RANGE", newScoreRANGE);
                                                                }
                                                                if (sujetBdC.equals("BRAND")) {
                                                                    newScoreBRAND = scores.get("BRAND") + 1;
                                                                    scores.put("BRAND", newScoreBRAND);
                                                                }
                                                                if (sujetBdC.equals("DIVISION")) {
                                                                    newScoreDIV = scores.get("DIV") + 1;
                                                                    scores.put("DIV", newScoreDIV);
                                                                }
                                                                if (sujetBdC.equals("GROUP")) {
                                                                    newScoreGROUP = scores.get("GROUP") + 1;
                                                                    scores.put("GROUP", newScoreGROUP);
                                                                }
                                                                if (sujetBdC.equals("CHIM")) {
                                                                    newScoreCHIM = scores.get("CHIM") + 1;
                                                                    scores.put("CHIM", newScoreCHIM);
                                                                }
                                                                if (sujetBdC.equals("PERS")) {
                                                                    newScorePERS = scores.get("PERS") + 1;
                                                                    scores.put("PERS", newScorePERS);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            String label = max(scores);//on a trouv� le type de l'EN
                            if (label.length() > 0) {
                               // System.out.println("VER (2)(EN" + " + ? + " + objet + ") : " + term + " => " + label + " " + scores);
                                Text.write("sortie.html", "VER (2)(EN" + " + ? + " + objet + ") : " + term + " => " + label + " " + scores + "<br/>");
                                String rules = AccessJDom.getRules(id, phrase, label, jdomDoc);
                                if (!rules.contains("VER2")) {
                                    AccessJDom.setAttributeType(id, label, phrase, jdomDoc);
                                    AccessJDom.setAttributeRules(id, "VER2", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void VERsuj2(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles de hi�rarchie
        String depRel = AccessJDom.getDepRel(id, phrase, jdomDoc);
        if (depRel.equals("suj")) {//sinon si EN est identifi�e comme un sujet

            org.jdom2.Element root = jdomDoc.getRootElement();
            List<org.jdom2.Element> sentences = root.getChildren("sentences");
            for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
                org.jdom2.Element element1 = sentences.get(m);
                List<org.jdom2.Element> sentence = element1.getChildren("sentence");
                for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                    if (n == phrase) {
                        org.jdom2.Element element2 = sentence.get(n);
                        List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                        for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                            org.jdom2.Element element3 = tokens.get(p);
                            List<org.jdom2.Element> token = element3.getChildren("token");
                            //for (org.jdom2.Element tokenElement : token) {//pour chaque token

                            String term = AccessJDom.getForm(id, phrase, jdomDoc);

                            String predicat = "";
                            int id1 = -1;
                            Map<String, Integer> scores = new HashMap<String, Integer>();
                            for (org.jdom2.Element tokenElement : token) {//pour chaque token
                                String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                                id1 = Integer.parseInt(idStr);
                                int head = AccessJDom.getHead(id1, phrase, jdomDoc);//on cherche la t�te du sujet
                                for (int i = id; i < id + 10; i++) {//on cherche le verbe qui a pour t�te ce sujet !!! � voir pour le 10 !!!
                                    String pos2 = AccessJDom.getPos(i, phrase, jdomDoc);
                                    if (i == head && pos2.equals("V")) {//quand on a trouv� le verbe qui correspond au sujet
                                        String lemmaVerbe = AccessJDom.getLemma(i, phrase, jdomDoc);//on recupere son lemme
                                        predicat = lemmaVerbe;//on stocke le lemme dans le predicat

                                        //on regarde si le predicat est connus dans notre base de connaissance
                                        String baseConnaissance = Text.getFile(tripletsCOSM);
                                        String[] triplets = baseConnaissance.split(";");//on stocke la BdC dans un arraylist
                                        //on cherche le triplet le plus fr�quent dans la BdC
                                        scores.put("PROD", 0);
                                        scores.put("RANGE", 0);
                                        scores.put("BRAND", 0);
                                        scores.put("DIV", 0);
                                        scores.put("GROUP", 0);
                                        scores.put("CHIM", 0);
                                        scores.put("PERS", 0);
                                        int newScorePROD = 0;
                                        int newScoreRANGE = 0;
                                        int newScoreBRAND = 0;
                                        int newScoreDIV = 0;
                                        int newScoreGROUP = 0;
                                        int newScoreCHIM = 0;
                                        int newScorePERS = 0;

                                        for (int j = 0; j < triplets.length; j++) {
                                            if (predicat != null && !predicat.equals("�tre") && !predicat.equals("avoir")) {
                                                if (triplets[j].contains(predicat)) {//si le triplet contient le predicat										

                                                    String[] triplet = triplets[j].split(",");//on splite notre tableau
                                                    if (triplet.length > 2) {
                                                        String sujetBdC = triplet[0];//et on r�cupere le sujet de la base de Connaissance qui correspond

                                                        if (sujetBdC.equals("PRODUCT")) {
                                                            newScorePROD = scores.get("PROD") + 1;
                                                            scores.put("PROD", newScorePROD);
                                                        }
                                                        if (sujetBdC.equals("RANGE")) {
                                                            newScoreRANGE = scores.get("RANGE") + 1;
                                                            scores.put("RANGE", newScoreRANGE);
                                                        }
                                                        if (sujetBdC.equals("BRAND")) {
                                                            newScoreBRAND = scores.get("BRAND") + 1;
                                                            scores.put("BRAND", newScoreBRAND);
                                                        }
                                                        if (sujetBdC.equals("DIVISION")) {
                                                            newScoreDIV = scores.get("DIV") + 1;
                                                            scores.put("DIV", newScoreDIV);
                                                        }
                                                        if (sujetBdC.equals("GROUP")) {
                                                            newScoreGROUP = scores.get("GROUP") + 1;
                                                            scores.put("GROUP", newScoreGROUP);
                                                        }
                                                        if (sujetBdC.equals("CHIM")) {
                                                            newScoreCHIM = scores.get("CHIM") + 1;
                                                            scores.put("CHIM", newScoreCHIM);
                                                        }
                                                        if (sujetBdC.equals("PERS")) {
                                                            newScorePERS = scores.get("PERS") + 1;
                                                            scores.put("PERS", newScorePERS);
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                            String label = max(scores);//on a trouv� le type de l'EN
                            if (label.length() > 0) {
                              //  System.out.println("VER (3)(EN + " + predicat + ") : " + term + " => " + label + " " + scores);
                                Text.write("sortie.html", "VER (3)(EN + " + predicat + ") : " + term + " => " + label + " " + scores + "<br/>");
                                String rules = AccessJDom.getRules(id, phrase, label, jdomDoc);
                                if (!rules.contains("VER3")) {
                                    AccessJDom.setAttributeType(id, label, phrase, jdomDoc);
                                    AccessJDom.setAttributeRules(id, "VER3", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void VERobj(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles de hi�rarchie

        String depRel = AccessJDom.getDepRel(id, phrase, jdomDoc);

        if (depRel.equals("obj")) {//sinon si EN est identifi�e comme un objet
            org.jdom2.Element root = jdomDoc.getRootElement();
            List<org.jdom2.Element> sentences = root.getChildren("sentences");
            for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
                org.jdom2.Element element1 = sentences.get(m);
                List<org.jdom2.Element> sentence = element1.getChildren("sentence");
                for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                    if (n == phrase) {
                        org.jdom2.Element element2 = sentence.get(n);
                        List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                        for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                            org.jdom2.Element element3 = tokens.get(p);
                            List<org.jdom2.Element> token = element3.getChildren("token");
                            String term = AccessJDom.getForm(id, phrase, jdomDoc);

                            String sujet = "";
                            String predicat = "";
                            int id1 = -1;
                            Map<String, Integer> scores = new HashMap<String, Integer>();
                            for (org.jdom2.Element tokenElement : token) {//pour chaque token de la phrase
                                String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                                id1 = Integer.parseInt(idStr);
                                int head = AccessJDom.getHead(id1, phrase, jdomDoc);//on cherche la t�te de l'objet
                                for (int i = 0; i < id; i++) {//on cherche le verbe qui a pour t�te cet objet !!!
                                    String pos2 = AccessJDom.getPos(i, phrase, jdomDoc);
                                    if (i == head && pos2.equals("V")) {//quand on a trouv� le verbe qui correspond � l'objet
                                        String lemmaVerbe = AccessJDom.getLemma(i, phrase, jdomDoc);//on recupere son lemme
                                        predicat = lemmaVerbe;//on stocke le lemme dans le predicat
                                        for (int k = 0; k < id; k++) {//on cherche le sujet du verbe
                                            String depRel2 = AccessJDom.getDepRel(k, phrase, jdomDoc);
                                            if (depRel2.equals("suj")) {//quand on a trouv� le sujet
                                                String typeSujet = AccessJDom.getType(k, phrase, jdomDoc);//on recupere son lemme
                                                sujet = typeSujet;
                                                if (sujet == null) {//si l'objet n'a pas de type, 
                                                    sujet = AccessJDom.getForm(k, phrase, jdomDoc);//on garde son lemme
                                                } else {
                                                    sujet = typeSujet.toUpperCase();//on stocke le lemme dans l'objet
                                                }

                                                //on regarde si le sujet et predicat sont connus dans notre base de connaissance
                                                String baseConnaissance = Text.getFile(tripletsCOSM);
                                                String[] triplets = baseConnaissance.split(";");//on stocke la BdC dans un arraylist
                                                //on cherche le triplet le plus fr�quent
                                                scores.put("PROD", 0);
                                                scores.put("RANGE", 0);
                                                scores.put("BRAND", 0);
                                                scores.put("DIV", 0);
                                                scores.put("GROUP", 0);
                                                scores.put("CHIM", 0);
                                                scores.put("PERS", 0);
                                                int newScorePROD = 0;
                                                int newScoreRANGE = 0;
                                                int newScoreBRAND = 0;
                                                int newScoreDIV = 0;
                                                int newScoreGROUP = 0;
                                                int newScoreCHIM = 0;
                                                int newScorePERS = 0;
                                                for (int j = 0; j < triplets.length; j++) {
                                                    if (sujet != null && predicat != null) {
                                                        if (triplets[j].contains(sujet.toUpperCase()) && triplets[j].contains(predicat)) {//si le triplet contient le sujet et le predicat
                                                            String[] triplet = triplets[j].split(",");//on splite notre tableau
                                                            if (triplet.length > 2) {
                                                                String objetBdC = triplet[2];//et on r�cupere l'objet de la base de Connaissance qui correspond
                                                                if (objetBdC.equals("PRODUCT")) {
                                                                    newScorePROD = scores.get("PROD") + 1;
                                                                    scores.put("PROD", newScorePROD);
                                                                }
                                                                if (objetBdC.equals("RANGE")) {
                                                                    newScoreRANGE = scores.get("RANGE") + 1;
                                                                    scores.put("RANGE", newScoreRANGE);
                                                                }
                                                                if (objetBdC.equals("BRAND")) {
                                                                    newScoreBRAND = scores.get("BRAND") + 1;
                                                                    scores.put("BRAND", newScoreBRAND);
                                                                }
                                                                if (objetBdC.equals("DIV")) {
                                                                    newScoreDIV = scores.get("DIV") + 1;
                                                                    scores.put("DIV", newScoreDIV);
                                                                }
                                                                if (objetBdC.equals("GROUP")) {
                                                                    newScoreGROUP = scores.get("GROUP") + 1;
                                                                    scores.put("GROUP", newScoreGROUP);
                                                                }
                                                                if (objetBdC.equals("CHIM")) {
                                                                    newScoreCHIM = scores.get("CHIM") + 1;
                                                                    scores.put("CHIM", newScoreCHIM);
                                                                }
                                                                if (objetBdC.equals("PERS")) {
                                                                    newScorePERS = scores.get("PERS") + 1;
                                                                    scores.put("PERS", newScorePERS);
                                                                }

                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            String label = max(scores);//on a trouv� le type de l'EN
                            if (label.length() > 0) {
                             //   System.out.println("VER (4)(" + sujet + " + " + predicat + " + EN) : " + term + " => " + label + " " + scores);
                                Text.write("sortie.html", "VER (4)(" + sujet + " + " + predicat + " + EN) : " + term + " => " + label + " " + scores + "<br/>");
                                String rules = AccessJDom.getRules(id, phrase, label, jdomDoc);
                                if (!rules.contains("VER4")) {
                                    AccessJDom.setAttributeType(id, label, phrase, jdomDoc);
                                    AccessJDom.setAttributeRules(id, "VER4", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void VERpred2(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles de hi�rarchie
        String depRel = AccessJDom.getDepRel(id, phrase, jdomDoc);
        if (depRel.equals("obj")) {//sinon si EN est identifi�e comme un objet
            org.jdom2.Element root = jdomDoc.getRootElement();
            List<org.jdom2.Element> sentences = root.getChildren("sentences");
            for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
                org.jdom2.Element element1 = sentences.get(m);
                List<org.jdom2.Element> sentence = element1.getChildren("sentence");
                for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                    if (n == phrase) {
                        org.jdom2.Element element2 = sentence.get(n);
                        List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                        for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                            org.jdom2.Element element3 = tokens.get(p);
                            List<org.jdom2.Element> token = element3.getChildren("token");
                            //	for (org.jdom2.Element tokenElement : token) {//pour chaque token
                            String term = AccessJDom.getForm(id, phrase, jdomDoc);

                            String sujet = "";
                            String predicat = "";
                            int id1 = -1;
                            Map<String, Integer> scores = new HashMap<String, Integer>();
                            for (org.jdom2.Element tokenElement : token) {//pour chaque token de la phrase
                                String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                                id1 = Integer.parseInt(idStr);
                                int head = AccessJDom.getHead(id1, phrase, jdomDoc);//on cherche la t�te de l'objet
                                for (int i = 0; i < id; i++) {//on cherche le verbe qui a pour t�te cet objet !!!
                                    String pos2 = AccessJDom.getPos(i, phrase, jdomDoc);
                                    if (i == head && pos2.equals("V")) {//quand on a trouv� le verbe qui correspond � l'objet
                                        String lemmaVerbe = AccessJDom.getLemma(i, phrase, jdomDoc);//on recupere son lemme
                                        predicat = lemmaVerbe;//on stocke le lemme dans le predicat
                                        for (int k = 0; k < id; k++) {//on cherche le sujet du verbe
                                            String depRel2 = AccessJDom.getDepRel(k, phrase, jdomDoc);
                                            if (depRel2.equals("suj")) {//quand on a trouv� le sujet
                                                String typeSujet = AccessJDom.getType(k, phrase, jdomDoc);//on recupere son lemme
                                                sujet = typeSujet;
                                                if (sujet == null) {//si l'objet n'a pas de type, 
                                                    sujet = AccessJDom.getForm(k, phrase, jdomDoc);//on garde son lemme
                                                } else {
                                                    sujet = typeSujet.toUpperCase();//on stocke le lemme dans l'objet
                                                }

                                                //on regarde si le sujet et predicat sont connus dans notre base de connaissance
                                                String baseConnaissance = Text.getFile(tripletsCOSM);
                                                String[] triplets = baseConnaissance.split(";");//on stocke la BdC dans un arraylist
                                                //on cherche le triplet le plus fr�quent
                                                scores.put("PROD", 0);
                                                scores.put("RANGE", 0);
                                                scores.put("BRAND", 0);
                                                scores.put("DIV", 0);
                                                scores.put("GROUP", 0);
                                                scores.put("CHIM", 0);
                                                scores.put("PERS", 0);
                                                int newScorePROD = 0;
                                                int newScoreRANGE = 0;
                                                int newScoreBRAND = 0;
                                                int newScoreDIV = 0;
                                                int newScoreGROUP = 0;
                                                int newScoreCHIM = 0;
                                                int newScorePERS = 0;
                                                for (int j = 0; j < triplets.length; j++) {
                                                    if (sujet != null && predicat != null) {
                                                        if (triplets[j].contains(sujet.toUpperCase())) {//si le triplet contient le sujet et le predicat
                                                            String[] triplet = triplets[j].split(",");//on splite notre tableau
                                                            if (triplet.length > 2) {
                                                                String objetBdC = triplet[2];//et on r�cupere l'objet de la base de Connaissance qui correspond
                                                                if (objetBdC.equals("PRODUCT")) {
                                                                    newScorePROD = scores.get("PROD") + 1;
                                                                    scores.put("PROD", newScorePROD);
                                                                }
                                                                if (objetBdC.equals("RANGE")) {
                                                                    newScoreRANGE = scores.get("RANGE") + 1;
                                                                    scores.put("RANGE", newScoreRANGE);
                                                                }
                                                                if (objetBdC.equals("BRAND")) {
                                                                    newScoreBRAND = scores.get("BRAND") + 1;
                                                                    scores.put("BRAND", newScoreBRAND);
                                                                }
                                                                if (objetBdC.equals("DIV")) {
                                                                    newScoreDIV = scores.get("DIV") + 1;
                                                                    scores.put("DIV", newScoreDIV);
                                                                }
                                                                if (objetBdC.equals("GROUP")) {
                                                                    newScoreGROUP = scores.get("GROUP") + 1;
                                                                    scores.put("GROUP", newScoreGROUP);
                                                                }
                                                                if (objetBdC.equals("CHIM")) {
                                                                    newScoreCHIM = scores.get("CHIM") + 1;
                                                                    scores.put("CHIM", newScoreCHIM);
                                                                }
                                                                if (objetBdC.equals("PERS")) {
                                                                    newScorePERS = scores.get("PERS") + 1;
                                                                    scores.put("PERS", newScorePERS);
                                                                }

                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            String label = max(scores);//on a trouv� le type de l'EN
                            if (label.length() > 0) {
                             //   System.out.println("VER (5)(" + sujet + " + ? + " + "EN) : " + term + " => " + label + " " + scores);
                                Text.write("sortie.html", "VER (5)(" + sujet + " + ? + " + "EN) : " + term + " => " + label + " " + scores + "<br/>");
                                String rules = AccessJDom.getRules(id, phrase, label, jdomDoc);
                                if (!rules.contains("VER5")) {
                                    AccessJDom.setAttributeType(id, label, phrase, jdomDoc);
                                    AccessJDom.setAttributeRules(id, "VER5", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void VERobj2(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles de hi�rarchie
        String depRel = AccessJDom.getDepRel(id, phrase, jdomDoc);
        if (depRel.equals("obj")) {//sinon si EN est identifi�e comme un objet
            org.jdom2.Element root = jdomDoc.getRootElement();
            List<org.jdom2.Element> sentences = root.getChildren("sentences");
            for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
                org.jdom2.Element element1 = sentences.get(m);
                List<org.jdom2.Element> sentence = element1.getChildren("sentence");
                for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                    if (n == phrase) {
                        org.jdom2.Element element2 = sentence.get(n);
                        List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                        for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                            org.jdom2.Element element3 = tokens.get(p);
                            List<org.jdom2.Element> token = element3.getChildren("token");
                            //	for (org.jdom2.Element tokenElement : token) {//pour chaque token
                            String term = AccessJDom.getForm(id, phrase, jdomDoc);

                            String sujet = "";
                            String predicat = "";
                            int id1 = -1;
                            Map<String, Integer> scores = new HashMap<String, Integer>();
                            for (org.jdom2.Element tokenElement : token) {//pour chaque token de la phrase
                                String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                                id1 = Integer.parseInt(idStr);
                                int head = AccessJDom.getHead(id1, phrase, jdomDoc);//on cherche la t�te de l'objet
                                for (int i = 0; i < id; i++) {//on cherche le verbe qui a pour t�te cet objet !!!
                                    String pos2 = AccessJDom.getPos(i, phrase, jdomDoc);
                                    if (i == head && pos2.equals("V")) {//quand on a trouv� le verbe qui correspond � l'objet
                                        String lemmaVerbe = AccessJDom.getLemma(i, phrase, jdomDoc);//on recupere son lemme
                                        predicat = lemmaVerbe;//on stocke le lemme dans le predicat
                                        for (int k = 0; k < id; k++) {//on cherche le sujet du verbe
                                            //on regarde si le sujet et predicat sont connus dans notre base de connaissance
                                            String baseConnaissance = Text.getFile(tripletsCOSM);
                                            String[] triplets = baseConnaissance.split(";");//on stocke la BdC dans un arraylist
                                            //on cherche le triplet le plus fr�quent
                                            scores.put("PROD", 0);
                                            scores.put("RANGE", 0);
                                            scores.put("BRAND", 0);
                                            scores.put("DIV", 0);
                                            scores.put("GROUP", 0);
                                            scores.put("CHIM", 0);
                                            scores.put("PERS", 0);
                                            int newScorePROD = 0;
                                            int newScoreRANGE = 0;
                                            int newScoreBRAND = 0;
                                            int newScoreDIV = 0;
                                            int newScoreGROUP = 0;
                                            int newScoreCHIM = 0;
                                            int newScorePERS = 0;
                                            for (int j = 0; j < triplets.length; j++) {
                                                if (sujet != null && predicat != null && !predicat.equals("�tre") && !predicat.equals("avoir")) {
                                                    if (triplets[j].contains(sujet.toUpperCase()) && triplets[j].contains(predicat)) {//si le triplet contient le sujet et le predicat
                                                        String[] triplet = triplets[j].split(",");//on splite notre tableau
                                                        if (triplet.length > 2) {
                                                            String objetBdC = triplet[2];//et on r�cupere l'objet de la base de Connaissance qui correspond
                                                            if (objetBdC.equals("PRODUCT")) {
                                                                newScorePROD = scores.get("PROD") + 1;
                                                                scores.put("PROD", newScorePROD);
                                                            }
                                                            if (objetBdC.equals("RANGE")) {
                                                                newScoreRANGE = scores.get("RANGE") + 1;
                                                                scores.put("RANGE", newScoreRANGE);
                                                            }
                                                            if (objetBdC.equals("BRAND")) {
                                                                newScoreBRAND = scores.get("BRAND") + 1;
                                                                scores.put("BRAND", newScoreBRAND);
                                                            }
                                                            if (objetBdC.equals("DIV")) {
                                                                newScoreDIV = scores.get("DIV") + 1;
                                                                scores.put("DIV", newScoreDIV);
                                                            }
                                                            if (objetBdC.equals("GROUP")) {
                                                                newScoreGROUP = scores.get("GROUP") + 1;
                                                                scores.put("GROUP", newScoreGROUP);
                                                            }
                                                            if (objetBdC.equals("CHIM")) {
                                                                newScoreCHIM = scores.get("CHIM") + 1;
                                                                scores.put("CHIM", newScoreCHIM);
                                                            }
                                                            if (objetBdC.equals("PERS")) {
                                                                newScorePERS = scores.get("PERS") + 1;
                                                                scores.put("PERS", newScorePERS);
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }

                            String label = max(scores);//on a trouv� le type de l'EN
                            if (label.length() > 0) {
                              //  System.out.println("VER (6)(" + predicat + " + EN) : " + term + " => " + label + " " + scores);
                                Text.write("sortie.html", "VER (6)(" + predicat + " + EN) : " + term + " => " + label + " " + scores + "<br/>");

                                String rules = AccessJDom.getRules(id, phrase, label, jdomDoc);
                                if (!rules.contains("VER6")) {
                                    AccessJDom.setAttributeType(id, label, phrase, jdomDoc);
                                    AccessJDom.setAttributeRules(id, "VER6", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static String max(Map<String, Integer> scores) {//renvoi le max de la map
        if (!scores.isEmpty()) {
            String result = "";
            int maximum = 0;
            int prod = scores.get("PROD");
            int range = scores.get("RANGE");
            int brand = scores.get("BRAND");
            int div = scores.get("DIV");
            int group = scores.get("GROUP");
            int pers = scores.get("PERS");
            int chim = scores.get("CHIM");

            if (prod > maximum) {
                result = "product";
                maximum = prod;
            }
            if (range > maximum) {
                result = "range";
                maximum = range;
            }
            if (brand > maximum) {
                result = "brand";
                maximum = brand;
            }
            if (div > maximum) {
                result = "division";
                maximum = div;
            }
            if (group > maximum) {
                result = "group";
                maximum = group;
            }
            if (pers > maximum) {
                result = "person";
                maximum = pers;
            }
            if (chim > maximum) {
                result = "component";
                maximum = chim;
            }
            return result;
        } else {
            return "";
        }
    }

    public static void COORD1(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles de coordination
        //cas ou l'entit� � annoter est plac� avant les autres entit�s

        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");
        for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(m);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                if (n == phrase) {
                    org.jdom2.Element element2 = sentence.get(n);
                    List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                    for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                        org.jdom2.Element element3 = tokens.get(p);
                        List<org.jdom2.Element> token = element3.getChildren("token");
                        for (org.jdom2.Element tokenElement : token) {//pour chaque token
                            String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                            int id1 = Integer.parseInt(idStr);
                            int head = AccessJDom.getHead(id1, n, jdomDoc);//t�te //head=4
                            String depRel = AccessJDom.getDepRel(id1, n, jdomDoc);

                            if (head == id && depRel.equals("coord")) {//id=5 head=5
                                int head2 = AccessJDom.getHead(id1, n, jdomDoc);//head=4 = id du terme � annoter
                                //on refait une boucle jusqu'� trouver id=4

                                org.jdom2.Element root1 = jdomDoc.getRootElement();
                                List<org.jdom2.Element> sentences1 = root1.getChildren("sentences");
                                for (int a = 0; a < sentences1.size(); a++) {//pour chaque sentences
                                    org.jdom2.Element element4 = sentences1.get(a);
                                    List<org.jdom2.Element> sentence2 = element4.getChildren("sentence");
                                    for (int b = 0; b < sentence2.size(); b++) {//pour chaque sentence
                                        if (b == phrase) {
                                            org.jdom2.Element element5 = sentence2.get(b);
                                            List<org.jdom2.Element> tokens2 = element5.getChildren("tokens");
                                            for (int c = 0; c < tokens2.size(); c++) {//pour chaque tokens
                                                org.jdom2.Element element7 = tokens2.get(c);
                                                List<org.jdom2.Element> token3 = element7.getChildren("token");
                                                for (org.jdom2.Element tokenElement8 : token3) {//pour chaque token
                                                    String idStr2 = tokenElement8.getAttributeValue("id");//on recupere l'id du terme en cours
                                                    int id2 = Integer.parseInt(idStr2);

                                                    if (head2 == id2) {//on a trouver le terme � annoter
                                                        //head2=10 ; id1=10 ; id2=11;
                                                        String typeTerm2 = AccessJDom.getType(id2, b, jdomDoc);//on recupere le type

                                                        if (typeTerm2 != null) {
                                                            String term = AccessJDom.getForm(id, phrase, jdomDoc);
                                                            //String typeEN="product";
                                                            Text.write("sortie.html", "COORD0 : " + term + " => " + typeTerm2 + "<br/>");
                                                            String rules = AccessJDom.getRules(id, phrase, typeTerm2, jdomDoc);
                                                            if (!rules.contains("COORD1")) {
                                                                AccessJDom.setAttributeType(id, typeTerm2, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                                                                AccessJDom.setAttributeRules(id, "COORD1", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                                                            }
                                                            /*String search="name=\""+term+"\"";
                                                             String ajout="<"+typeTerm2+"Learned name=\""+term+"\"></product>";

                                                             if(typeTerm2.equals("product")){
                                                             String dicoProduct=Text.getFile(produits);
                                                             if(!dicoProduct.contains(search)){
                                                             Text.write(produits, ajout+"\n");
                                                             }
                                                             }
                                                             else if(typeTerm2.equals("range")){
                                                             String dicoRange=Text.getFile(gammes);
                                                             if(!dicoRange.contains(search)){
                                                             Text.write(gammes, ajout+"\n");
                                                             }
                                                             }
                                                             else if(typeTerm2.equals("brand")){
                                                             String dicoBrand=Text.getFile(marques);
                                                             if(!dicoBrand.contains(search)){
                                                             Text.write(marques, ajout+"\n");
                                                             }
                                                             }
                                                             else if(typeTerm2.equals("division")){
                                                             String dicoDivision=Text.getFile(divisions);
                                                             if(!dicoDivision.contains(search)){
                                                             Text.write(divisions, ajout+"\n");
                                                             }
                                                             }
                                                             else if(typeTerm2.equals("group")){
                                                             String dicoGroup=Text.getFile(groupes);
                                                             if(!dicoGroup.contains(search)){
                                                             Text.write(groupes, ajout+"\n");
                                                             }
                                                             }*/
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void COORD2(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles de coordination
        //cas ou l'entit� � annoter est plac� apr�s les autres entit�s
        String depRel1 = AccessJDom.getDepRel(id, phrase, jdomDoc);
        //id=5
        //depRel1=dep_coord
        if (depRel1.equals("dep_coord")) {
            int head = AccessJDom.getHead(id, phrase, jdomDoc);//t�te
            org.jdom2.Element root = jdomDoc.getRootElement();
            List<org.jdom2.Element> sentences = root.getChildren("sentences");
            for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
                org.jdom2.Element element1 = sentences.get(m);
                List<org.jdom2.Element> sentence = element1.getChildren("sentence");
                for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                    if (n == phrase) {
                        org.jdom2.Element element2 = sentence.get(n);
                        List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                        for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                            org.jdom2.Element element3 = tokens.get(p);
                            List<org.jdom2.Element> token = element3.getChildren("token");
                            for (org.jdom2.Element tokenElement : token) {//pour chaque token
                                String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours

                                int id1 = Integer.parseInt(idStr);
                                //id1=4;
                                int head2 = AccessJDom.getHead(id1, phrase, jdomDoc);//t�te
                                //head=1;
                                String depRel = AccessJDom.getDepRel(id1, phrase, jdomDoc);

                                if (id1 == head && depRel.equals("coord")) {

                                    org.jdom2.Element root1 = jdomDoc.getRootElement();
                                    List<org.jdom2.Element> sentences1 = root1.getChildren("sentences");
                                    for (int a = 0; a < sentences1.size(); a++) {//pour chaque sentences
                                        org.jdom2.Element element4 = sentences1.get(a);
                                        List<org.jdom2.Element> sentence2 = element4.getChildren("sentence");
                                        for (int b = 0; b < sentence2.size(); b++) {//pour chaque sentence
                                            if (b == phrase) {
                                                org.jdom2.Element element5 = sentence2.get(b);
                                                List<org.jdom2.Element> tokens2 = element5.getChildren("tokens");
                                                for (int c = 0; c < tokens2.size(); c++) {//pour chaque tokens
                                                    org.jdom2.Element element7 = tokens2.get(c);
                                                    List<org.jdom2.Element> token3 = element7.getChildren("token");
                                                    for (org.jdom2.Element tokenElement8 : token3) {//pour chaque token

                                                        String idStr2 = tokenElement8.getAttributeValue("id");//on recupere l'id du terme en cours
                                                        int id2 = Integer.parseInt(idStr2);
                                                        //id2=1;

                                                        if (head2 == id2) {
                                                            //head2=10 ; id1=10 ; id2=11;
                                                            String typeTerm2 = AccessJDom.getType(id2, phrase, jdomDoc);

                                                            if (typeTerm2 != null) {
                                                                String term = AccessJDom.getForm(id, phrase, jdomDoc);
                                                                //String typeEN="product";
                                                                //System.out.println("COORD : "+term+" => product");
                                                                Text.write("sortie.html", "COORD2 : " + term + " => " + typeTerm2 + "<br/>");
                                                                String rules = AccessJDom.getRules(id, phrase, typeTerm2, jdomDoc);
                                                                if (!rules.contains("COORD2")) {
                                                                    AccessJDom.setAttributeType(id, typeTerm2, b, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                                                                    AccessJDom.setAttributeRules(id, "COORD2", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                                                                }
                                                                /*String search="name=\""+term+"\"";
                                                                 String ajout="<"+typeTerm2+"Learned name=\""+term+"\"></product>";

                                                                 if(typeTerm2.equals("product")){
                                                                 String dicoProduct=Text.getFile(produits);
                                                                 if(!dicoProduct.contains(search)){
                                                                 Text.write(produits, ajout+"\n");
                                                                 }
                                                                 }
                                                                 else if(typeTerm2.equals("range")){
                                                                 String dicoRange=Text.getFile(gammes);
                                                                 if(!dicoRange.contains(search)){
                                                                 Text.write(gammes, ajout+"\n");
                                                                 }
                                                                 }
                                                                 else if(typeTerm2.equals("brand")){
                                                                 String dicoBrand=Text.getFile(marques);
                                                                 if(!dicoBrand.contains(search)){
                                                                 Text.write(marques, ajout+"\n");
                                                                 }
                                                                 }
                                                                 else if(typeTerm2.equals("division")){
                                                                 String dicoDivision=Text.getFile(divisions);
                                                                 if(!dicoDivision.contains(search)){
                                                                 Text.write(divisions, ajout+"\n");
                                                                 }
                                                                 }
                                                                 else if(typeTerm2.equals("group")){
                                                                 String dicoGroup=Text.getFile(groupes);
                                                                 if(!dicoGroup.contains(search)){
                                                                 Text.write(groupes, ajout+"\n");
                                                                 }
                                                                 }
                                                                 */
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void COORD3(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles de coordination

        String type2 = AccessJDom.getType(id - 2, phrase, jdomDoc);
        String type4 = AccessJDom.getType(id + 2, phrase, jdomDoc);
        String form = AccessJDom.getForm(id, phrase, jdomDoc);

        //cas de l'�num�ration vers l'arri�re (, MARQUE , EN)
        if (type2 != null) {
            String form1 = AccessJDom.getForm(id - 1, phrase, jdomDoc);//on recupere le form du terme pr�c�dent
            String form3 = AccessJDom.getForm(id - 3, phrase, jdomDoc);//et le form du terme id-3
            if (form1.equals(",") && form3.equals(",")) {
                String rules = AccessJDom.getRules(id, phrase, type2, jdomDoc);
                if (!rules.contains("COORD3")) {
                    AccessJDom.setAttributeType(id, type2, phrase, jdomDoc);
                    AccessJDom.setAttributeRules(id, "COORD3", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                }
               // System.out.println("COORD3 (enum. arriere) : " + form + " => " + type2);
                Text.write("sortie.html", "COORD3 (enum. arriere) : " + form + " => " + type2 + "<br/>");
            }
        } //cas de l'�num�ration vers l'avant (EN, MARQUE,)
        else if (type4 != null) {
            String form10 = AccessJDom.getForm(id + 1, phrase, jdomDoc);//on recupere le form du terme suivant
            String form11 = AccessJDom.getForm(id + 3, phrase, jdomDoc);//on recupere le form du terme suivant
            if (form10.equals(",") && form11.equals(",")) {
                String rules = AccessJDom.getRules(id, phrase, type4, jdomDoc);
                if (!rules.contains("COORD4")) {
                    AccessJDom.setAttributeType(id, type4, phrase, jdomDoc);
                    AccessJDom.setAttributeRules(id, "COORD4", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                }
               // System.out.println("COORD4 (enum. avant) : " + form + " => " + type4);
                Text.write("sortie.html", "COORD3 (enum. avant) : " + form + " => " + type4 + "<br/>");
            }

        }
    }

    public static void COMPL(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles de completion : si une entit� est appos� � une entit� d�j� typ�e, alors elle recupere son type
        if (id > 1) {

            String type1 = AccessJDom.getType(id - 1, phrase, jdomDoc);
            String type2 = AccessJDom.getType(id + 1, phrase, jdomDoc);
            String term = AccessJDom.getForm(id, phrase, jdomDoc);

            String pos = AccessJDom.getPos(id, phrase, jdomDoc);
            //String lemma=AccessJDom.getLemma(id, phrase, jdomDoc);
            String depRel = AccessJDom.getDepRel(id, phrase, jdomDoc);

            if (type2 != null && type2.equals(type1) && !pos.equals("PONCT") && !depRel.equals("coord")) {//si le token avant et le token apres ont le m�me type et que ce mot n'est ni une prep ni une ponctuation
                String rules = AccessJDom.getRules(id, phrase, type1, jdomDoc);
                if (!rules.contains("COMPL1")) {
                    AccessJDom.setAttributeType(id, type1, phrase, jdomDoc);//on compl�te
                    AccessJDom.setAttributeRules(id, "COMPL1", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                }
               // System.out.println("COMPL (coord111) : " + term + " => " + type1);
            } else if (pos != null && type1 != null) {//on compl�te l'entit� avec les adjectifs et noms successifs
                if (type1.equals("product") && (pos.equals("ADJ") || pos.equals("NC") || pos.equals("NPP"))) {
                    String rules = AccessJDom.getRules(id, phrase, type1, jdomDoc);
                    if (!rules.contains("COMPL2")) {
                        AccessJDom.setAttributeType(id, type1, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                        AccessJDom.setAttributeRules(id, "COMPL2", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                    }
                  //  System.out.println("COMPL (adj/nc) : " + term + " => " + type1);
                } else if (type1.equals("range") && (pos.equals("ADJ") || pos.equals("NC"))) {
                    String rules = AccessJDom.getRules(id, phrase, type1, jdomDoc);
                    if (!rules.contains("COMPL3")) {
                        AccessJDom.setAttributeType(id, type1, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                        AccessJDom.setAttributeRules(id, "COMPL3", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                    }
                  //  System.out.println("COMPL (adj/nc) : " + term + " => " + type1);
                } else if (type1.equals("division") && (pos.equals("ADJ") || pos.equals("NC"))) {
                  //  System.out.println("COMPL (adj/nc) : " + term + " => " + type1);
                    String rules = AccessJDom.getRules(id, phrase, type1, jdomDoc);
                    if (!rules.contains("COMPL4")) {
                        AccessJDom.setAttributeType(id, type1, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"	
                        AccessJDom.setAttributeRules(id, "COMPL4", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                    }
                }
            }
        }
    }

    public static void HIN2(int id, int phrase, String dicoGammes, String dicoMarques, String dicoGroupes, String dicoProduits, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {
	//on considere les lexiques comme des contextes internes
        //ex: le nom d'un groupe pourra se trouver � l'int�rieur d'une marque, mais pas l'inverse

        //String type=AccessJDom.getType(id, phrase, jdomDoc);
        //if(type==null){
        String termComplet = AccessJDom.getForm(id, phrase, jdomDoc);

        String[] tab = termComplet.split(" +");
        String premierMot = tab[0];

        int n = tab.length;
        String dernierMot = tab[n - 1];

        if (tab.length > 1) {

            String deuxPremiersMots = tab[0] + " " + tab[1];
            if (isRange(premierMot, dicoGammes) || isRange(dernierMot, dicoGammes) || (isRange(deuxPremiersMots, dicoGammes) && tab.length > 2)) {//si une partie de l'entit� est une gamme alors l'entit� est un produit
               // System.out.println("INT2a : " + termComplet + " => produit (contexte : " + deuxPremiersMots + " / " + premierMot + ")");
                Text.write("sortie.html", "INT2a : " + termComplet + " => produit (contexte : " + deuxPremiersMots + ")<br/>");
                String typeEN = "product";
                String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                if (!rules.contains("INT1")) {
                    AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                    AccessJDom.setAttributeRules(id, "HIN1", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                }
                /*String search="product name=\""+termComplet+"\"";
                 String ajout="<productLearned name=\""+termComplet+"\"></product>";
                 String dicoProduct=Text.getFile(produits);
                 if(!dicoProduct.contains(search)){
                 Text.write(produits, ajout+"\n");
                 }*/
            } else if (isBrand(premierMot, dicoMarques) || isBrand(dernierMot, dicoMarques) || (isBrand(deuxPremiersMots, dicoMarques) && tab.length > 2)) {//si une partie de l'entit� est une marque alors l'entit� est un produit
               // System.out.println("INT2b : " + termComplet + " => produit (contexte : " + deuxPremiersMots + " / " + premierMot + ")");
                Text.write("sortie.html", "HIN2b : " + termComplet + " => produit (contexte : " + deuxPremiersMots + ")<br/>");
                String typeEN = "product";
                String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                if (!rules.contains("INT2")) {
                    AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                    AccessJDom.setAttributeRules(id, "HIN2", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                }
                /*String search="range name=\""+termComplet+"\"";
                 String ajout="<rangeLearned name=\""+termComplet+"\"></range>";
                 String dicoProduct=Text.getFile(gammes);
                 if(!dicoProduct.contains(search)){
                 Text.write(gammes, ajout+"\n");
                 }*/
            } /*else if(isDivision(premierMot) || isDivision(dernierMot)|| (isDivision(deuxPremiersMots) && tab.length>2)){//si une partie de l'entit� est une division alors l'entit� est une marque
             System.out.println("INT2 : "+termComplet+" => brand (contexte : "+deuxPremiersMots+")");
             Text.write("sortie.html", "INT2c : "+termComplet+" => brand (contexte : "+deuxPremiersMots+")<br/>");
             String typeEN = "brand";
             AccessJDom.setAttributeType(id, typeEN, phrase,jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
             String search="brand name=\""+termComplet+"\"";
             String ajout="<brandLearned name=\""+termComplet+"\"></brand>";
             String dicoBrand=Text.getFile(marques);
             if(!dicoBrand.contains(search)){
             Text.write(marques, ajout+"\n");
             }
             }*/ else if (isGroup(premierMot, dicoGroupes) || isGroup(dernierMot, dicoGroupes) || (isGroup(deuxPremiersMots, dicoGroupes) && tab.length > 2)) {//si une partie de l'entit� est un groupe alors l'entit� est une division
                //System.out.println("INT2 : " + termComplet + " => division (contexte : " + deuxPremiersMots + " / " + premierMot + ")");
                Text.write("sortie.html", "INT2d : " + termComplet + " => division (contexte : " + deuxPremiersMots + ")<br/>");
                String typeEN = "division";
                String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                if (!rules.contains("INT3")) {
                    AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                    AccessJDom.setAttributeRules(id, "HIN3", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                }
                /*String search="division name=\""+termComplet+"\"";
                 String ajout="<divisionLearned name=\""+termComplet+"\"></division>";
                 String dicoDiv=Text.getFile(divisions);
                 if(!dicoDiv.contains(search)){
                 Text.write(divisions, ajout+"\n");
                 }*/
            } else if (isProduct(premierMot, dicoProduits) || isProduct(dernierMot, dicoProduits) || (isProduct(deuxPremiersMots, dicoProduits) && tab.length > 2)) {//si une partie de l'entit� est un groupe alors l'entit� est une division
               // System.out.println("INT2 : " + termComplet + " => produit (contexte : " + deuxPremiersMots + " / " + premierMot + ")");
                Text.write("sortie.html", "INT2d : " + termComplet + " => produit (contexte : " + deuxPremiersMots + ")<br/>");
                String typeEN = "product";
                String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                if (!rules.contains("INT4")) {
                    AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                    AccessJDom.setAttributeRules(id, "HIN4", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                }
                /*String search="product name=\""+termComplet+"\"";
                 String ajout="<productLearned name=\""+termComplet+"\"></division>";
                 String dicoDiv=Text.getFile(produits);
                 if(!dicoDiv.contains(search)){
                 Text.write(produits, ajout+"\n");
                 }*/
            }
        }
        //}
    }

    public static boolean isRange(String entity, String dicoGammes) throws IOException {

        entity = "range name=\"" + entity + "\"";
        if (dicoGammes.contains(entity)) {
            return true;
        }
        return false;
    }

    public static boolean isBrand(String entity, String dicoMarques) throws IOException {

        entity = "brand name=\"" + entity + "\"";
        if (dicoMarques.contains(entity)) {
            return true;
        }
        return false;
    }

    public static boolean isDivision(String entity, String dicoDivisions) throws IOException {

        entity = "division name=\"" + entity + "\"";
        if (dicoDivisions.contains(entity)) {
            return true;
        }
        return false;
    }

    public static boolean isGroup(String entity, String dicoGroupes) throws IOException {

        entity = "group name=\"" + entity + "\"";
        if (dicoGroupes.contains(entity)) {
            return true;
        }
        return false;
    }

    public static boolean isProduct(String entity, String dicoProduits) throws IOException {

        entity = "product name=\"" + entity + "\"";
        if (dicoProduits.contains(entity)) {
            return true;
        }
        return false;
    }

    public static void INT(int id, int phrase, org.jdom2.Document jdomDoc) throws ParserConfigurationException, SAXException, IOException {//r�gles d�finitoires
        //String type=AccessJDom.getType(id, phrase, jdomDoc);
        String pos2 = AccessJDom.getPos(id + 1, phrase, jdomDoc);
        //if(type==null){
        //lexiques de contextes
        String group[] = {"Group", "Foundation", "Fondation", "Enterprise", "Entreprise", "Groupe", "Company", "Studio", "Business", "Agency", "Company"};
        String division[] = {"Produit", "Produits", "Beaut�", "Beauty", "Product", "Products", "Lab", "Laboratoire", "Cosmetics", "Laboratoires", "Espagne", "International", "Italie", "Monde", "France", "Allemagne", "USA", "�tats-Unis", "Etats-Unis", "Europe"};
        String brand[] = {"Men", "Man"};
        String range[] = {"Color", "Collection"};
        String product[] = {"elixir", "opus", "produit", "extrait", "lisseur", "parfum", "spray", "by", "fond", "d�maquillant", "savon", "gel", "palette", "eau", "gel�e", "tonique", "vaporisateur", "beurre", "creme", "cr�me", "applicateur", "gloss", "Eyes", "mascara", "teint", "masque", "parfum", "fond", "FDT", "d�odorant", "vernis", "eau", "baume", "jour", "nuit", "shampoing", "shampooing", "eyeliner", "eye-liner", "crayon", "poudre", "blush", "dissolvant", "durcisseur", "serum", "s�rum", "masque", "gommage", "huiles", "huile", "apr�s-shampooing", "d�m�lant", "d�frisant", "gel", "spray", "laque", "lotion", "d�maquillant", "kajal", "kh�l", "jus", "cologne", "dentifrice", "savon", "lait", "mousse", "serum", "soin", "cream", "creams", "aqua", "acqua", "man_", "rouge", "l�vres", "women_", "woman_", "eyes", "mousse", "cr�me", "s�rum", "huile", "male", "baume", "eau", "elixir", "blonde", "brune", "rousse", "Serum", "Cream", "Nail", "Aqua", "Acqua", "Rouge", "Women", "Woman", "Eye", "Eyes", "Mousse", "Cr�me", "S�rum", "Huile", "Male", "Baume", "Eau", "Elixir", "Blonde", "Brune", "Rousse", "Teint", "Eclat", "by", "By", "Yeux", "Cils", "cil", "Ongles", "ongle", "Gel", "Gel�e", "Her"};

        //on recupere le terme en cours de traitement
        String termComplet = AccessJDom.getForm(id, phrase, jdomDoc);
        String[] tabTerm = termComplet.split(" +");
        String dicoBrand = Text.getFile(marques);
        if (tabTerm.length > 1 || pos2.equals("ADJ") || pos2.equals("NC")) {//si le terme est au  moins un bigramme

            for (String term : tabTerm) {

                /*if(dicoBrand.contains(term)){//si le terme contient une marque, alors il s'agit d'une division
                 System.out.println("INT : "+termComplet+" => division");
                 String typeEN = "division";
                 AccessJDom.setAttributeType(id, typeEN, phrase,jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                 String search="name=\""+term+"\"";
                 String ajout="<divisionLearned name=\""+termComplet+"\"></division>\n";
                 if(!dicoBrand.contains(search)){
                 Text.write(divisions, ajout+"\n");
                 }
                 }*/
                if (Table.contain(term, group)) {
                   // System.out.println("INT : " + termComplet + " => group");
                    Text.write("sortie.html", "INT : " + termComplet + " => group" + "<br/>");
                    String typeEN = "group";
                    String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                    if (!rules.contains("INT1")) {
                        AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                        AccessJDom.setAttributeRules(id, "INT1", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                    }
                    /*String search="name=\""+termComplet+"\"";
                     String ajout="<groupLearned name=\""+termComplet+"\"></group>";
                     String dicoGroup=Text.getFile(groupes);
                     if(!dicoGroup.contains(search)){
                     Text.write(groupes, ajout+"\n");
                     }*/
                } else if (Table.contain(term, division)) {
                  //  System.out.println("INT : " + termComplet + " => division");
                    Text.write("sortie.html", "INT : " + termComplet + " => division" + "<br/>");
                    String typeEN = "division";
                    String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                    if (!rules.contains("INT2")) {
                        AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                        AccessJDom.setAttributeRules(id, "INT2", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                    }
                    /*String search="name=\""+termComplet+"\"";
                     String ajout="<divisionLearned name=\""+termComplet+"\"></division>";
                     String dicoDiv=Text.getFile(divisions);
                     if(!dicoDiv.contains(search)){
                     Text.write(divisions, ajout+"\n");
                     }*/
                } else if (Table.contain(term, brand)) {
                   // System.out.println("INT : " + termComplet + " => brand");
                    Text.write("sortie.html", "INT : " + termComplet + " => brand" + "<br/>");
                    String typeEN = "brand";
                    String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                    if (!rules.contains("INT3")) {
                        AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                        AccessJDom.setAttributeRules(id, "INT3", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                    }
                    /*String search="name=\""+termComplet+"\"";
                     String ajout="<brandLearned name=\""+termComplet+"\"></brand>";
                     if(!dicoBrand.contains(search)){
                     Text.write(marques, ajout+"\n");
                     }*/
                } else if (Table.contain(term, range)) {
                  //  System.out.println("INT : " + termComplet + " => range");
                    Text.write("sortie.html", "INT : " + termComplet + " => range" + "<br/>");
                    String typeEN = "range";
                    String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                    if (!rules.contains("INT4")) {
                        AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                        AccessJDom.setAttributeRules(id, "INT4", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                    }
                    /*String search="name=\""+termComplet+"\"";
                     String ajout="<rangeLearned name=\""+termComplet+"\"></range>";
                     String dicoRange=Text.getFile(gammes);
                     if(!dicoRange.contains(search)){
                     Text.write(gammes, ajout+"\n");
                     }*/
                } else if (Table.contain(term, product)) {
                  //  System.out.println("INT : " + termComplet + " => product");
                    Text.write("sortie.html", "INT : " + termComplet + " => product" + "<br/>");
                    String typeEN = "product";
                    String rules = AccessJDom.getRules(id, phrase, typeEN, jdomDoc);
                    if (!rules.contains("INT5")) {
                        AccessJDom.setAttributeType(id, typeEN, phrase, jdomDoc);//ajoute l'atttribut type au Jdom avec la valeur par d�faut "null"
                        AccessJDom.setAttributeRules(id, "INT5", phrase, jdomDoc);//ajoute l'atttribut rules au Jdom	
                    }
                    /*String search="name=\""+termComplet+"\"";
                     String ajout="<productLearned name=\""+termComplet+"\"></product>";
                     String dicoProduct=Text.getFile(produits);
                     if(!dicoProduct.contains(search)){
                     Text.write(produits, ajout+"\n");
                     }*/
                }
            }
            //}
        }
    }

    public static void enrichBrand(String text, org.jdom2.Document jdomDoc, int nbSentences, String dicoPrenoms) throws ParserConfigurationException, SAXException, IOException {//on type les entit�s selon le dictionnaire des marques
        //ouverture du dictionnaire de produits
        String dicoBrand = Text.getFile(marques);
        String typeEN = "brand";
        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");
        for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(m);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                org.jdom2.Element element2 = sentence.get(n);
                List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                    org.jdom2.Element element3 = tokens.get(p);
                    List<org.jdom2.Element> token = element3.getChildren("token");
                    for (org.jdom2.Element tokenElement : token) {//pour chaque token
                        String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                        int id = Integer.parseInt(idStr);
                        String form = tokenElement.getAttributeValue("form");//on r�cup�re le form			
                        String pos = tokenElement.getAttributeValue("pos");
                        String typeEN1 = tokenElement.getAttributeValue("typeEN");
                        if (typeEN1 == null && (pos.equals("NPP") || pos.equals("NC"))) {//si le terme a une majuscule on cherche son type
                            String search = "brand name=\"" + form + "\"";

                            if (dicoBrand.contains(search)) {//si le terme est dans le lexique des marques
                                AccessJDom.setAttributeType(id, typeEN, n, jdomDoc);//ajoute l'attribut type au Jdom avec la valeur par d�faut "null"	
                               // System.out.println("Dictionnary : " + form + " => brand");
                                Text.write("sortie.html", "Dictionnary : " + form + " => brand" + "<br/>");
                                AccessJDom.setAttributeRules(id, "DICO3", n, jdomDoc);//ajoute l'atttribut rules au Jdom	
                            }
                        }
                    }
                }
            }
        }
    }

    public static void enrichBrandWithLearning(String text, org.jdom2.Document jdomDoc, int nbSentences, String dicoPrenoms) throws ParserConfigurationException, SAXException, IOException {//on type les entit�s selon le dictionnaire des marques
        //ouverture du dictionnaire de produits
        String dicoBrand = Text.getFile(marques);
        String typeEN = "brand";
        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");
        for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(m);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                org.jdom2.Element element2 = sentence.get(n);
                List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                    org.jdom2.Element element3 = tokens.get(p);
                    List<org.jdom2.Element> token = element3.getChildren("token");
                    for (org.jdom2.Element tokenElement : token) {//pour chaque token
                        String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                        int id = Integer.parseInt(idStr);
                        String form = tokenElement.getAttributeValue("form");//on r�cup�re le form
                        String pos = tokenElement.getAttributeValue("pos");
                        String typeEN1 = tokenElement.getAttributeValue("typeEN");
                        if (typeEN1 == null && (pos.equals("NPP"))) {//si le terme a une majuscule on cherche son type
                            String search = "brandLearned name=\"" + form + "\"";

                            if (dicoBrand.contains(search)) {//si le terme est dans le lexique des marques
                                AccessJDom.setAttributeType(id, typeEN, n, jdomDoc);//ajoute l'attribut type au Jdom avec la valeur par d�faut "null"	
                               // System.out.println("Dictionnary (learned) : " + form + " => brand");
                                Text.write("sortie.html", "Dictionnary (learned) : " + form + " => brand" + "<br/>");
                                AccessJDom.setAttributeRules(id, "LEARNED4", n, jdomDoc);//ajoute l'atttribut rules au Jdom	
                            }
                        }
                    }
                }
            }
        }
    }

    public static void enrichRange(String text, org.jdom2.Document jdomDoc, int nbSentences, String dicoPrenoms) throws ParserConfigurationException, SAXException, IOException {//on type les entit�s selon le dictionnaire des marques
        //ouverture du dictionnaire de produits
        String dicoRange = Text.getFile(gammes);
        String typeEN = "range";
        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");
        for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(m);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                org.jdom2.Element element2 = sentence.get(n);
                List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                    org.jdom2.Element element3 = tokens.get(p);
                    List<org.jdom2.Element> token = element3.getChildren("token");
                    for (org.jdom2.Element tokenElement : token) {//pour chaque token
                        String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                        int id = Integer.parseInt(idStr);
                        String form = tokenElement.getAttributeValue("form");//on r�cup�re le form
                        String pos = tokenElement.getAttributeValue("pos");
                        String typeEN1 = tokenElement.getAttributeValue("typeEN");
                        if (!Rules.person(form, dicoPrenoms) && typeEN1 == null && (pos.equals("NPP") || pos.equals("NC"))) {//si le terme a une majuscule on cherche son type
                            String search = "range name=\"" + form + "\"";

                            if (dicoRange.contains(search)) {//si le terme est dans le lexique des marques
                                AccessJDom.setAttributeType(id, typeEN, n, jdomDoc);//ajoute l'attribut type au Jdom avec la valeur par d�faut "null"	
                              //  System.out.println("Dictionnary : " + form + " => range");
                                Text.write("sortie.html", "Dictionnary : " + form + " => range" + "<br/>");
                                AccessJDom.setAttributeRules(id, "DICO5", n, jdomDoc);//ajoute l'atttribut rules au Jdom	
                            }
                        }
                    }

                }
            }
        }
    }

    public static void enrichRangeWithLearning(String text, org.jdom2.Document jdomDoc, int nbSentences, String dicoPrenoms) throws ParserConfigurationException, SAXException, IOException {//on type les entit�s selon le dictionnaire des marques
        //ouverture du dictionnaire de produits
        String dicoRange = Text.getFile(gammes);
        String typeEN = "range";
        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");
        for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(m);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                org.jdom2.Element element2 = sentence.get(n);
                List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                    org.jdom2.Element element3 = tokens.get(p);
                    List<org.jdom2.Element> token = element3.getChildren("token");
                    for (org.jdom2.Element tokenElement : token) {//pour chaque token
                        String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                        int id = Integer.parseInt(idStr);
                        String form = tokenElement.getAttributeValue("form");//on r�cup�re le form
                        String pos = tokenElement.getAttributeValue("pos");
                        String typeEN1 = tokenElement.getAttributeValue("typeEN");
                        if (typeEN1 == null && (pos.equals("NPP"))) {//si le terme a une majuscule on cherche son type
                            String search = "rangeLearned name=\"" + form + "\"";

                            if (dicoRange.contains(search)) {//si le terme est dans le lexique des marques
                                AccessJDom.setAttributeType(id, typeEN, n, jdomDoc);//ajoute l'attribut type au Jdom avec la valeur par d�faut "null"	
                              //  System.out.println("Dictionnary (learned) : " + form + " => range");
                                Text.write("sortie.html", "Dictionnary (learned) : " + form + " => range" + "<br/>");
                                AccessJDom.setAttributeRules(id, "LEARNED6", n, jdomDoc);//ajoute l'atttribut rules au Jdom	
                            }
                        }
                    }
                }
            }
        }
    }

    public static void enrichProduct(String text, org.jdom2.Document jdomDoc, int nbSentences, String dicoPrenoms) throws ParserConfigurationException, SAXException, IOException {//on type les entit�s selon le dictionnaire des marques
        //ouverture du dictionnaire de produits
        String dicoProduct = Text.getFile(produits);
        String typeEN = "product";
        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");
        for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(m);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                org.jdom2.Element element2 = sentence.get(n);
                List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                    org.jdom2.Element element3 = tokens.get(p);
                    List<org.jdom2.Element> token = element3.getChildren("token");

                    for (org.jdom2.Element tokenElement : token) {//pour chaque token
                        String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                        int id = Integer.parseInt(idStr);
                        String form = tokenElement.getAttributeValue("form");//on r�cup�re le form
                        String pos = tokenElement.getAttributeValue("pos");
                        String typeEN1 = tokenElement.getAttributeValue("typeEN");
                        if (typeEN1 == null && (pos.equals("NPP") || pos.equals("NC"))) {//si le terme a une majuscule on cherche son type
                            String search = "product name=\"" + form + "\"";

                            if (dicoProduct.contains(search)) {//si le terme est dans le lexique des marques
                                AccessJDom.setAttributeType(id, typeEN, n, jdomDoc);//ajoute l'attribut type au Jdom avec la valeur par d�faut "null"	
                               // System.out.println("Dictionnary : " + form + " => product");
                                Text.write("sortie.html", "Dictionnary : " + form + " => product" + "<br/>");
                                AccessJDom.setAttributeRules(id, "DICO7", n, jdomDoc);//ajoute l'atttribut rules au Jdom	
                            }
                        }
                    }

                }
            }

        }
    }

    public static void enrichProductWithLearning(String text, org.jdom2.Document jdomDoc, int nbSentences, String dicoPrenoms) throws ParserConfigurationException, SAXException, IOException {//WITH Learning
        //ouverture du dictionnaire de produits
        String dicoProduct = Text.getFile(produits);
        String typeEN = "product";
        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");
        for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(m);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                org.jdom2.Element element2 = sentence.get(n);
                List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                    org.jdom2.Element element3 = tokens.get(p);
                    List<org.jdom2.Element> token = element3.getChildren("token");
                    for (org.jdom2.Element tokenElement : token) {//pour chaque token
                        String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                        int id = Integer.parseInt(idStr);
                        String form = tokenElement.getAttributeValue("form");//on r�cup�re le form
                        String pos = tokenElement.getAttributeValue("pos");
                        String typeEN1 = tokenElement.getAttributeValue("typeEN");
                        if (typeEN1 == null && (pos.equals("NPP"))) {//si le terme a une majuscule on cherche son type
                            String search = "productLearned name=\"" + form + "\"";

                            if (dicoProduct.contains(search)) {//si le terme est dans le lexique des marques
                                AccessJDom.setAttributeType(id, typeEN, n, jdomDoc);//ajoute l'attribut type au Jdom avec la valeur par d�faut "null"	
                             //   System.out.println("Dictionnary (learned) : " + form + " => product");
                                Text.write("sortie.html", "Dictionnary (learned) : " + form + " => product" + "<br/>");
                                AccessJDom.setAttributeRules(id, "LEARNED8", n, jdomDoc);//ajoute l'atttribut rules au Jdom	
                            }
                            //}
                        }
                    }
                }
            }
        }
    }

    public static void enrichGroup(String text, org.jdom2.Document jdomDoc, int nbSentences, String dicoPrenoms) throws ParserConfigurationException, SAXException, IOException {//on type les entit�s selon le dictionnaire des marques
        //ouverture du dictionnaire de produits
        String dicoGroup = Text.getFile(groupes);
        String typeEN = "group";
        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");
        for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(m);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                org.jdom2.Element element2 = sentence.get(n);
                List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                    org.jdom2.Element element3 = tokens.get(p);
                    List<org.jdom2.Element> token = element3.getChildren("token");
                    for (org.jdom2.Element tokenElement : token) {//pour chaque token
                        String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                        int id = Integer.parseInt(idStr);
                        String form = tokenElement.getAttributeValue("form");//on r�cup�re le form
                        String pos = tokenElement.getAttributeValue("pos");
                        String typeEN1 = tokenElement.getAttributeValue("typeEN");
                        if (typeEN1 == null && (pos.equals("NPP") || pos.equals("NC"))) {//si le terme a une majuscule on cherche son type
                            String search = "group name=\"" + form + "\"";

                            if (dicoGroup.contains(search)) {//si le terme est dans le lexique des marques
                                AccessJDom.setAttributeType(id, typeEN, n, jdomDoc);//ajoute l'attribut type au Jdom avec la valeur par d�faut "null"	
                              //  System.out.println("Dictionnary : " + form + " => group");
                                Text.write("sortie.html", "Dictionnary : " + form + " => group" + "<br/>");
                                AccessJDom.setAttributeRules(id, "DICO9", n, jdomDoc);//ajoute l'atttribut rules au Jdom	
                            }
                        }
                    }
                }
            }
        }
    }

    public static void enrichGroupWithLearning(String text, org.jdom2.Document jdomDoc, int nbSentences, String dicoPrenoms) throws ParserConfigurationException, SAXException, IOException {//on type les entit�s selon le dictionnaire des marques
        //ouverture du dictionnaire de produits
        String dicoGroup = Text.getFile(groupes);
        String typeEN = "group";
        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");
        for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(m);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                org.jdom2.Element element2 = sentence.get(n);
                List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                    org.jdom2.Element element3 = tokens.get(p);
                    List<org.jdom2.Element> token = element3.getChildren("token");
                    for (org.jdom2.Element tokenElement : token) {//pour chaque token
                        String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                        int id = Integer.parseInt(idStr);
                        String form = tokenElement.getAttributeValue("form");//on r�cup�re le form
                        String pos = tokenElement.getAttributeValue("pos");
                        String typeEN1 = tokenElement.getAttributeValue("typeEN");
                        if (typeEN1 == null && (pos.equals("NPP"))) {//si le terme a une majuscule on cherche son type
                            String search = "groupLearned name=\"" + form + "\"";

                            if (dicoGroup.contains(search)) {//si le terme est dans le lexique des marques
                                AccessJDom.setAttributeType(id, typeEN, n, jdomDoc);//ajoute l'attribut type au Jdom avec la valeur par d�faut "null"	
                               // System.out.println("Dictionnary (learned) : " + form + " => group");
                                Text.write("sortie.html", "Dictionnary (learned) : " + form + " => group" + "<br/>");
                                AccessJDom.setAttributeRules(id, "LEARNED10", n, jdomDoc);//ajoute l'atttribut rules au Jdom	
                            }
                        }
                    }
                }
            }
        }
    }

    public static void enrichDivision(String text, org.jdom2.Document jdomDoc, int nbSentences, String dicoPrenoms) throws ParserConfigurationException, SAXException, IOException {//on type les entit�s selon le dictionnaire des marques
        //ouverture du dictionnaire de produits
        String dicoDivision = Text.getFile(divisions);
        String typeEN = "division";
        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");
        for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(m);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                org.jdom2.Element element2 = sentence.get(n);
                List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                    org.jdom2.Element element3 = tokens.get(p);
                    List<org.jdom2.Element> token = element3.getChildren("token");
                    for (org.jdom2.Element tokenElement : token) {//pour chaque token
                        String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                        int id = Integer.parseInt(idStr);
                        String form = tokenElement.getAttributeValue("form");//on r�cup�re le form
                        String pos = tokenElement.getAttributeValue("pos");
                        String typeEN1 = tokenElement.getAttributeValue("typeEN");
                        if (typeEN1 == null && (pos.equals("NPP") || pos.equals("NC"))) {//si le terme a une majuscule on cherche son type
                            String search = "division name=\"" + form + "\"";

                            if (dicoDivision.contains(search)) {//si le terme est dans le lexique des marques
                                AccessJDom.setAttributeType(id, typeEN, n, jdomDoc);//ajoute l'attribut type au Jdom avec la valeur par d�faut "null"	
                               // System.out.println("Dictionnary : " + form + " => division");
                                Text.write("sortie.html", "Dictionnary : " + form + " => division" + "<br/>");
                                AccessJDom.setAttributeRules(id, "DICO11", n, jdomDoc);//ajoute l'atttribut rules au Jdom	
                            }
                        }
                    }
                }
            }
        }
    }

    public static void enrichDivisionWithLearning(String text, org.jdom2.Document jdomDoc, int nbSentences, String dicoPrenoms) throws ParserConfigurationException, SAXException, IOException {//on type les entit�s selon le dictionnaire des marques
        //ouverture du dictionnaire de produits
        String dicoDivision = Text.getFile(divisions);
        String typeEN = "division";
        org.jdom2.Element root = jdomDoc.getRootElement();
        List<org.jdom2.Element> sentences = root.getChildren("sentences");
        for (int m = 0; m < sentences.size(); m++) {//pour chaque sentences
            org.jdom2.Element element1 = sentences.get(m);
            List<org.jdom2.Element> sentence = element1.getChildren("sentence");
            for (int n = 0; n < sentence.size(); n++) {//pour chaque sentence
                org.jdom2.Element element2 = sentence.get(n);
                List<org.jdom2.Element> tokens = element2.getChildren("tokens");
                for (int p = 0; p < tokens.size(); p++) {//pour chaque tokens
                    org.jdom2.Element element3 = tokens.get(p);
                    List<org.jdom2.Element> token = element3.getChildren("token");
                    for (org.jdom2.Element tokenElement : token) {//pour chaque token
                        String idStr = tokenElement.getAttributeValue("id");//on recupere l'id du terme en cours
                        int id = Integer.parseInt(idStr);
                        String form = tokenElement.getAttributeValue("form");//on r�cup�re le form
                        String pos = tokenElement.getAttributeValue("pos");
                        String typeEN1 = tokenElement.getAttributeValue("typeEN");
                        if (typeEN1 == null && (pos.equals("NPP"))) {//si le terme a une majuscule on cherche son type
                            String search = "divisionLearned name=\"" + form + "\"";

                            if (dicoDivision.contains(search)) {//si le terme est dans le lexique des marques
                                AccessJDom.setAttributeType(id, typeEN, n, jdomDoc);//ajoute l'attribut type au Jdom avec la valeur par d�faut "null"	
                              //  System.out.println("Dictionnary (learned): " + form + " => division");
                                Text.write("sortie.html", "Dictionnary (learned) : " + form + " => division" + "<br/>");
                                AccessJDom.setAttributeRules(id, "LEARNED12", n, jdomDoc);//ajoute l'atttribut rules au Jdom	
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean allCapital(String term) {

        if (term.equals(term.toUpperCase())) {
            return true;
        } else {
            return false;
        }
    }

}
