 
package it.cnr.isti.hpc.text;

import it.cnr.isti.hpc.dexter.util.DexterParams;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

import opennlp.tools.coref.DefaultLinker;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.util.Span;

/**
 * @author farhad Nooralahzadeh
 */
public class ParserTool {

	private Parser _parser = null;
	 
        private static DexterParams params = DexterParams.getInstance();
        private TokenSegmenter ts;
        
      
      

	public ParserTool() {
              
		InputStream modelIn = null;
                ts=new TokenSegmenter();
		try {
			// Loading tokenizer model
			//modelIn = getClass().getResourceAsStream("/nlp/fr-token.bin");
                        String path=params.getDefaultNlpModel().getPath();
                        modelIn = getClass().getResourceAsStream(path+"/parser-chunking.bin");       
                        //wordnet db path as jvm argument
                        String modelPath=params.getDefaultModel().getPath();
                        System.getProperties().setProperty("WNSEARCHDIR",modelPath+"/wordNetdb");
			final ParserModel parseModel = new ParserModel(modelIn);
                modelIn.close();
             _parser = ParserFactory.create(parseModel);

		} catch (final IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (final IOException e) {
				} // oh well!
			}
		}
	}

      
	public Parse parseSentence(String text) {
		Parse p = new Parse(text,
            // a new span covering the entire text
            new Span(0, text.length()),
            // the label for the top if an incomplete node
            AbstractBottomUpParser.INC_NODE,
            // the probability of this parse...uhhh...? 
            1,
            // the token index of the head of this parse
            0);

             final Span[] spans = ts.tokenizer.tokenizePos(text);

      for (int idx=0; idx < spans.length; idx++) {
         final Span span = spans[idx];
         // flesh out the parse with token sub-parses
         p.insert(new Parse(text, span,
               AbstractBottomUpParser.TOK_NODE,
               0,
               idx));
      }

      return parse(p);
	}

    /**
    * Parse the given parse object.
    * <p>
    * The parser is lazily initialized on first use.
    * </p>
    * 
    * @param p the parse object
    * @return the parsed parse
    */
   private Parse parse(final Parse p) {
       return _parser.parse(p);
   }
 
 
}
