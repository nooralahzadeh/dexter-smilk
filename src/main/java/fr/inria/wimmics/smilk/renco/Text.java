package fr.inria.wimmics.smilk.renco;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author Elena
 */

public class Text {

		
	/**
	 * Renvoie le nombre d'occurrences de la sous-chaine de caract�res sp�cifi�e dans la chaine de caract�res sp�cifi�e
	 * @param text chaine de caract�res initiale
	 * @param string sous-chaine de caract�res dont le nombre d'occurrences doit etre compt�
	 * @return le nombre d'occurrences du pattern sp�cifi� dans la chaine de caract�res sp�cifi�e
	 */
	 public static final int stringOccur(String text, String string) {
	    return regexOccur(text, Pattern.quote(string));
	}

	 /**
	 * Renvoie le nombre d'occurrences du pattern sp�cifi� dans la chaine de caract�res sp�cifi�e
	 * @param text chaine de caract�res initiale
	 * @param regex expression r�guli�re dont le nombre d'occurrences doit etre compt�
	 * @return le nombre d'occurrences du pattern sp�cifi� dans la chaine de caract�res sp�cifi�e
	 */
	 public static final int regexOccur(String text, String regex) {
	    Matcher matcher = Pattern.compile(regex).matcher(text);
	    int occur = 0;
	    while(matcher.find()) {
	        occur ++;
	    }
	    return occur;
	}
	
	//retourne le texte du path pass� en param�tre
	public static String getFile(String path) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(path));
		String line;
		String result = "";
		while ((line = in.readLine()) != null)
		{
			result = result + line;
		}
		in.close();
		return result;
	}
	

	//retourne le texte avec la ponctuation d�coll�e des termes
	public static String clean(String text){
		String text1=text.replace(",", " , ");
		String text2=text1.replace("."," . ");
		String text3=text2.replace(";"," ; ");
		String text4=text3.replace(":"," : ");
		String text5=text4.replace("!"," ! ");
		String text6=text5.replace("?"," ? ");
		String text7=text6.replace("/"," / ");
		String text8=text7.replace("("," ( ");
		String text9=text8.replace(")"," ) ");
		String text10=text9.replace("{"," { ");
		String text11=text10.replace("}"," } ");
		String text12=text11.replace("["," [ ");
		String text13=text12.replace("]"," ] ");
		//String text14=text13.replace("'"," ' ");
		//String text15=text14.replace("'"," ' ");
		return text13;
	}
	
	//retourne le tableau des mots du texte
	public static ArrayList<String> chunckWords(String text){
		String text2=clean(text);
		String[] resultTab=text2.split(" +");
		ArrayList<String> result=new ArrayList<String>(Arrays.asList(resultTab));
		System.out.println(result);		
		return result;
	}
	
	public static boolean uppercase(String term) {
		if(term.length()<1){return false;}//au cas ou on doive traiter un term null
		String term2=term.toLowerCase();
		char a1=term.charAt(0);
		char a2=term2.charAt(0);
		if(a1==a2){
			return false;
			
		}	
		return true; 	 
	}
	
	//return le texte avec les entit�s nomm�es potentielles marqu�es
	public static String trackEntities(String text){
		String text1=Text.clean(text);//nettoyage du texte
		ArrayList<String> tabText=Text.chunckWords(text1);//d�coupage en termes
		String annotatedText=null;
		for (int i = 0; i < tabText.size()-3; i++) {
			
			String term1=tabText.get(i);
			String term2=tabText.get(i+1);
			String term3=tabText.get(i+2);
			String term4=tabText.get(i+3);
			if(Text.uppercase(term1) && Text.uppercase(term2) && Text.uppercase(term3) && Text.uppercase(term4)){
				annotatedText = annotatedText + " <en> " + term1 + " " + term2 + " " + term3+ " " + term4 + " </en> ";
				i=i+3;
			}
			if(Text.uppercase(term1) && Text.uppercase(term2) && Text.uppercase(term3)){
				annotatedText = annotatedText + " <en> " + term1 + " " + term2 + " " + term3 + " </en> ";
				i=i+2;
			}
			else if(Text.uppercase(term1) && Text.uppercase(term2)){
				annotatedText = annotatedText + " <en> " + term1 + " " + term2 + " </en> ";
				i=i+1;
			}
			else if(Text.uppercase(term1)){
				annotatedText = annotatedText + " <en> " + term1 + " </en> ";
			}
			else{
				annotatedText = annotatedText + " " + term1;
			}
		}
		System.out.println(annotatedText);
		return annotatedText;
	}
	
	public static String concatEN(String text){//retourne le texte avec les entit�s nomm�es concat�n�es
		String textClean=clean(text);
		String[] resultTab=textClean.split(" +");
		//ArrayList<String> result=new ArrayList<String>(Arrays.asList(resultTab));
		String textConcat="";
		for (int i = 0; i < resultTab.length; i++) {
			if (Text.uppercase(resultTab[i]) && Text.uppercase(resultTab[i+1])&& Text.uppercase(resultTab[i+2])&& Text.uppercase(resultTab[i+3])) {
				textConcat=textConcat+" "+resultTab[i]+"-"+resultTab[i+1]+"-"+resultTab[i+2]+"-"+resultTab[i+3];
				i=i+3;
			}
			else if (Text.uppercase(resultTab[i]) && Text.uppercase(resultTab[i+1])&& Text.uppercase(resultTab[i+2])) {
				textConcat=textConcat+" "+resultTab[i]+"-"+resultTab[i+1]+"-"+resultTab[i+2];
				i=i+2;
			}
			else if (Text.uppercase(resultTab[i]) && Text.uppercase(resultTab[i+1])) {
				textConcat=textConcat+" "+resultTab[i]+"-"+resultTab[i+1];
				i++;
			}
			else{
				textConcat=textConcat+" "+resultTab[i];
			}
		}
		//System.out.println(textConcat);
		return textConcat;
	}
	
	public static boolean isNum(String num){
		if(num.length()>0){
			String firstCar=num.substring(0, 1);
			if(firstCar.equals("0")||firstCar.equals("1")||firstCar.equals("2")||firstCar.equals("3")||firstCar.equals("4")||firstCar.equals("5")||firstCar.equals("6")||firstCar.equals("7")||firstCar.equals("8")||firstCar.equals("9")){
				return true;
			}
			else return false;
		}
		else return false;
	}
	
	
	public static void write(String nomFic, String texte)
	{
		//on va chercher le chemin et le nom du fichier et on me tout ca dans un String
		String adressedufichier =  nomFic;
	
		//on met try si jamais il y a une exception
		try
		{
			/**
			 * BufferedWriter a besoin d un FileWriter, 
			 * les 2 vont ensemble, on donne comme argument le nom du fichier
			 * true signifie qu on ajoute dans le fichier (append), on ne marque pas par dessus 
			 
			 */
			FileWriter fw = new FileWriter(adressedufichier, true);
			
			// le BufferedWriter output auquel on donne comme argument le FileWriter fw cree juste au dessus
			BufferedWriter output = new BufferedWriter(fw);
			
			//on marque dans le fichier ou plutot dans le BufferedWriter qui sert comme un tampon(stream)
			output.write(texte);
			//on peut utiliser plusieurs fois methode write
			
			output.flush();
			//ensuite flush envoie dans le fichier, ne pas oublier cette methode pour le BufferedWriter
			
			output.close();
			//et on le ferme
			//System.out.println("fichier cr��");
		}
		catch(IOException ioe){
			System.out.print("Erreur : �criture dans le fichier impossible");
			ioe.printStackTrace();
			}

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

