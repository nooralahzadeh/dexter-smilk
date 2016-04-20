/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.smilk.util;

import com.hp.hpl.jena.rdf.model.Model;
import java.util.ArrayList;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import java.io.*;
import java.net.URL;
import javax.xml.parsers.*;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.*;
import org.xml.sax.*;
 
import org.apache.commons.validator.routines.UrlValidator;
/**
 *
 * @author fnoorala
 */
public class Utility {

    public static void prefixes(Model mainModel) throws Exception {

        //define prefixes
        ArrayList<String> prefixes = getContent("http://dbpedia.org/sparql?nsdecl");
        int i = 0;
        UrlValidator defaultValidator = new UrlValidator();
        while (i < prefixes.size()) {
            String prfx = prefixes.get(i);
            if (defaultValidator.isValid(prefixes.get(i + 1))) {
                mainModel.setNsPrefix(prfx, prefixes.get(i + 1));
            }
            i = i + 2;
        }

    }

    public static String prefixes() throws Exception {
        String prfxes = "";
        //define prefixes
        ArrayList<String> prefixes = getContent("http://dbpedia.org/sparql?nsdecl");
        int i = 0;
        UrlValidator defaultValidator = new UrlValidator();
        while (i < prefixes.size()) {
            String prfx = prefixes.get(i);
            if (defaultValidator.isValid(prefixes.get(i + 1))) {
                prfxes = prfxes + " PREFIX " + prfx + ":<" + prefixes.get(i + 1) + ">\n";
            }
            i = i + 2;
        }

        return prfxes;
    }

    public static String prepareQuery(String query) throws Exception {
        return Utility.prefixes()
                + " " + query;
    }

    public static String getLocalNameOfURI(String txt) throws URISyntaxException {
        URI uri = new URI(txt);
        String path = uri.getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        return idStr;
    }

    public static <K, V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K, V> map) {

        List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Entry<K, V>>() {
                    @Override
                    public int compare(Entry<K, V> e1, Entry<K, V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );

        return sortedEntries;
    }
    public static <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<T>(set);
    }

    public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }
    
    
    public static <T> List<T> difference (List<T> list1, List<T> list2) {
        List<T> c = new ArrayList<T>(list1.size());      
            c.addAll(list1);
            c.removeAll(list2);
         return c;
    }
    
    public static Document stringToDom(String xmlSource) 
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
    }
    
    public static String getHTML(URL url) throws Exception {
        String lines = null;
        try {
 
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
 
            String inputLine;
            while ((inputLine = in.readLine()) != null) //System.out.println(inputLine);
            {
                lines += inputLine + "\n";
            }
            in.close();
        } catch (Exception e) {
            System.out.println("Unable to get cookie using CookieHandler");
            e.printStackTrace();
        }
        return lines;
    }
 public static ArrayList<String> getContent(String a) throws MalformedURLException, Exception {
 
            URL my_url = new URL(a);
            String html = getHTML(my_url);
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            Elements rows = doc.getElementsByTag("td");
            ArrayList<String> prefixes=new ArrayList<String> ();
 
            for(org.jsoup.nodes.Element r:rows){
              prefixes.add(r.text());
            }
return prefixes;
    }
}
