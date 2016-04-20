/**
 * Copyright 2012 Diego Ceccarelli
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
package it.cnr.isti.hpc.text;

import it.cnr.isti.hpc.dexter.util.DexterParams;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Diego Ceccarelli, diego.ceccarelli@isti.cnr.it created on 08/giu/2012
 */
public class Chunker {

    private Tokenizer tokenizer = null;
    private final Chunker instance = null;
    private static DexterParams params = DexterParams.getInstance();
    ChunkerME chunkerME;
    POSTaggerME tagger;
    
    Properties _prop = new Properties();
    public Chunker() {
        InputStream modelIn = null;
        try {
			// Loading tokenizer model
            //modelIn = getClass().getResourceAsStream("/nlp/fr-token.bin");

            String path = params.getDefaultNlpModel().getPath();
         
            modelIn = getClass().getResourceAsStream(path + "/pos.bin");
           
                      
           
            POSModel model = new POSModel(modelIn);
            modelIn.close();

            tagger = new POSTaggerME(model);

             
            modelIn = getClass().getResourceAsStream(path + "/chunker.bin");

            ChunkerModel cModel = new ChunkerModel(modelIn);
            modelIn.close();
            chunkerME = new ChunkerME(cModel);

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

    public Span[] chunk(String input) {
    
            ObjectStream<String> lineStream = new PlainTextByLineStream(
                    new StringReader(input));

            String[] tags = null;

            String line;
            String whitespaceTokenizerLine[] = null;
            try {
                while ((line = lineStream.read()) != null) {
                    whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
                            .tokenize(line);
                    
                    tags = tagger.tag(whitespaceTokenizerLine);
                    
                }
            } catch (IOException ex) {
                Logger.getLogger(Chunker.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);
            

      
        return span;
        
    }
    

    public List<Token> chunkPos(String sentence) {
        Span[] spans = tokenizer.tokenizePos(sentence);
        List<Token> tokens = new LinkedList<Token>();
        for (Span s : spans) {
            tokens.add(new Token(s.getStart(), s.getEnd()));
        }
        return tokens;
    }

}
