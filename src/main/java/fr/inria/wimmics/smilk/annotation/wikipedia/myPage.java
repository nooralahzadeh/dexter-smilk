/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.smilk.annotation.wikipedia;

import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;

import de.fau.cs.osr.ptk.common.AstVisitor;
import static de.tudarmstadt.ukp.wikipedia.api.WikiConstants.SWEBLE_CONFIG;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.config.WikiConfigurationInterface;
import org.sweble.wikitext.engine.utils.LanguageConfigGenerator;
import org.xml.sax.SAXException;

/**
 *
 * @author fnoorala
 */
public class myPage {

    public myPage(String title, String text) {
        this.title = title;
        this.text = text;
    }
    
    
    String title;
    String text;
      WikiConfig config;
      /**
	 * <p>Returns the Wikipedia article as plain text using the SwebleParser with
	 * a SimpleWikiConfiguration and the PlainTextConverter. <br/>
	 * If you have different needs regarding the plain text, you can use
	 * getParsedPage(Visitor v) and provide your own Sweble-Visitor. Examples
	 * are in the <code>de.tudarmstadt.ukp.wikipedia.api.sweble</code> package
	 * or on http://www.sweble.org </p>
	 *
	 * <p>Alternatively, use Page.getText() to return the Wikipedia article
	 * with all Wiki markup. You can then use the old JWPL MediaWiki parser for
	 * creating a plain text version. The JWPL parser is now located in a
	 * separate project <code>de.tudarmstad.ukp.wikipedia.parser</code>.
	 * Please refer to the JWPL Google Code project page for further reference.</p>
	 *
	 * @return The plain text of a Wikipedia article
	 * @throws WikiApiException
	 */
	public String getPlainText()
		throws WikiApiException
                
	{
        try {
            //Configure the PlainTextConverter for plain text parsing
            config= LanguageConfigGenerator.generateWikiConfig("My French Wiki", "http://localhost/", "fr");
            return (String) parsePage(new PlainTextConverter(config,  Integer.MAX_VALUE));
        }catch(Exception e){
			throw new WikiApiException(e);
		}
        }

	/**
	 * Parses the page with the Sweble parser using a SimpleWikiConfiguration
	 * and the provided visitor. For further information about the visitor
	 * concept, look at the examples in the
	 * <code>de.tudarmstadt.ukp.wikipedia.api.sweble</code> package, or on
	 * <code>http://www.sweble.org</code> or on the JWPL Google Code project
	 * page.
	 *
	 * @return the parsed page. The actual return type depends on the provided
	 *         visitor. You have to cast the return type according to the return
	 *         type of the go() method of your visitor.
	 * @throws WikiApiException
	 */
	public Object parsePage(AstVisitor v) throws WikiApiException
	{
		// Use the provided visitor to parse the page
		return v.go(getCompiledPage().getPage());
	}

	/**
	 * Returns CompiledPage produced by the SWEBLE parser using the
	 *  WikiConfiguration.
	 *
	 * @return the parsed page
	 * @throws WikiApiException
	 */
	public CompiledPage getCompiledPage() throws WikiApiException
	{
		CompiledPage cp;
		try{
		   config= LanguageConfigGenerator.generateWikiConfig("My French Wiki", "http://localhost/", "fr");

			PageTitle pageTitle = PageTitle.make(config, title.toString());
    			PageId pageId = new PageId(pageTitle, -1);

			// Compile the retrieved page
			Compiler compiler = new Compiler((WikiConfigurationInterface) config);
			cp = compiler.postprocess(pageId, text, null);
		}catch(Exception e){
			throw new WikiApiException(e);
		}
		return cp;
	}

   
     
}
