/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.smilk.annotation.wikipedia;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Title;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fnoorala
 */
public class jwplWikipedia {

    public static Wikipedia wiki;

    public jwplWikipedia() throws WikiApiException {
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost("localhost");
        dbConfig.setDatabase("SMILKWIKI");
        dbConfig.setUser("root");
        dbConfig.setPassword("Tuydfv456.>");
        dbConfig.setLanguage(Language.french);
       

        // Create a new french wikipedia object
        wiki = new Wikipedia(dbConfig);
        
        
    }
    
    
    
    public String getWikInfo(String title, List<String> outlinks) throws WikiApiException {
        Page page;
        String rawTxt = "";

        try {
            page = wiki.getPage(title);
            if (page != null) {
                rawTxt = page.getPlainText();
                Set<Page> outs = page.getOutlinks();
                for (Page out : outs) {
                    outlinks.add(out.getTitle().toString());
                    
                }
            }
            
        } catch (WikiApiException ex) {
             throw new WikiApiException("Page " + title + " does not exist");
        }

        return rawTxt;
    }

    public List<String> getWikTxt(String title)    {
        Page page;
        List<String> Txt =new ArrayList<String>();
        
        
        try {
            
            page = wiki.getPage(title);
            if (page != null) {
                
             //   System.out.println(page.getText());
                
                Txt.add(page.getText()); 
                Txt.add(page.getPlainText());
            }
            
        }catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        return Txt;
    }
    
    
    
    
    public  String getwikiData(String title)    {
        Page page;
        
        String Txt="";
        
        try {
            
            page = wiki.getPage(title);
            if (page != null) {
 
                Txt=page.getText(); 
        
            }
            
        }catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        return Txt;
    }
     public String getWikPlainTxt(String title)    {
        Page page;
        String wikiTxt = "";

        try {
            
            page = wiki.getPage(title);
            if (page != null) {
                wikiTxt = page.getPlainText();  
            }
            
        } catch (WikiApiException ex) {
              
        }

        return wikiTxt;
    }
    
    
    public String getWikInfoPage(String title, List<Page> outlinks) throws WikiApiException {
        Page page;
        String rawTxt = "";

        try {
            page = wiki.getPage(title);
            if (page != null) {
                rawTxt = page.getText();
                Set<Page> outs = page.getOutlinks();
                for (Page out : outs) {
                    outlinks.add(out);
                    
                }
            }
            
        } catch (WikiApiException ex) {
             throw new WikiApiException("Page " + title + " does not exist");
        }

        return rawTxt;
    }
    
    public   List<String> retreivePages(String category) throws WikiApiException{
        Category cat;
        List<String> pages=new ArrayList<String>();
        try {
            cat = wiki.getCategory(category);
            for (Page page : cat.getArticles()) {
                if(!page.isDisambiguation() && !page.isRedirect() && !page.isDiscussion()){
                   // System.out.println(page.getTitle().getWikiStyleTitle());
                    pages.add(page.getTitle().getWikiStyleTitle());
                }
                
            }
            
        } catch (WikiPageNotFoundException e) {
            throw new WikiApiException("Category " + category + " does not exist");
        }
        return pages;
    }
    
}
