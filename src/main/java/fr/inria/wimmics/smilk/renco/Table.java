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
public class Table {

	/**
	 * @param args
	 */
	
	public static boolean contain(String search, String[] tab){
		boolean result=false;
		for (int i=0; i < tab.length;i++) {
			if(result == false){
				if (search.equals(tab[i])) {
					result = true;
				}
			}
			
		}
		return result;
	}

	
}
