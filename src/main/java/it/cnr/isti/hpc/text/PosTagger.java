/**
 *  Copyright 2012 Diego Ceccarelli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package it.cnr.isti.hpc.text;

import it.cnr.isti.hpc.dexter.util.DexterParams;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PosTagger allows to annotate text with pos tag.
 * 
 * @author Diego Ceccarelli, diego.ceccarelli@isti.cnr.it created on 08/giu/2012
 */

// FIXME depends on language
public class PosTagger {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(PosTagger.class);

	private POSTaggerME posTagger = null;

	private static PosTagger instance = null;

	SentenceSegmenter ss;
	TokenSegmenter ts;
        private static DexterParams params = DexterParams.getInstance();

	private PosTagger() {

		ss = new SentenceSegmenter();
		ts = new TokenSegmenter();
		InputStream modelIn = null;
		try {
			// Loading tokenizer model
			//modelIn = getClass().getResourceAsStream("/nlp/en-pos-maxent.bin");
                       
                    String path=params.getDefaultNlpModel().getPath();
                    
                    modelIn = getClass().getResourceAsStream(path+"/pos.bin");
                       // modelIn = getClass().getResourceAsStream("/nlp/fr-pos.bin");
			final POSModel posModel = new POSModel(modelIn);
			modelIn.close();

			posTagger = new POSTaggerME(posModel);

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

	public static PosTagger getInstance() {
		if (instance == null)
			instance = new PosTagger();
		return instance;
	}

        
        
        
        
	public List<PosToken> tag(String text) {
		List<PosToken> tokens = new ArrayList<PosToken>();

		if (text == null) {
			logger.warn("text is null");
			return tokens;
		}

		if (text.isEmpty()) {
			logger.warn("text is empty");
			return tokens;
		}
		String[] sentences = ss.split(text);
		for (String sentence : sentences) {
			String[] token = ts.tokenize(sentence);
			String[] tags = posTagger.tag(token);
			for (int i = 0; i < token.length; i++) {
                            // System.out.println(token[i]+" "+ tags[i]);
				tokens.add(new PosToken(token[i], tags[i]));
			}
		}
		return tokens;
	}

        
        public List<ArrayList<PosToken>> tagWithOffSet(String text) {
                
		List<ArrayList<PosToken>> Sentence_tokens = new ArrayList<ArrayList<PosToken>>();

		if (text == null) {
			logger.warn("text is null");
			return Sentence_tokens;
		}

		if (text.isEmpty()) {
			logger.warn("text is empty");
			return Sentence_tokens;
		}
		
                List<Sentence>  sentences = ss.splitPos(text);
                
		for (Sentence sentence : sentences) {
                        int startSE=sentence.getStart();
                        int endSE=sentence.getEnd();
			//String[] token = ts.tokenize(sentence);
                        List<PosToken> tokens=new ArrayList<PosToken>();
                        String currSentence = sentence.getText();
                       // System.out.println(currSentence);
                        List<String> listToken=new LinkedList<String>();
                        int startSentence = sentence.getStart();
                        List<Token> tokensWithPositions=  ts.tokenizePos(currSentence);
                      
                        for(int i=0; i<tokensWithPositions.size();i++){
                            int start=tokensWithPositions.get(i).getStart()+startSentence;
                            int end=tokensWithPositions.get(i).getEnd()+startSentence;
                            String tokenTxt=text.substring(start,end);        
                           // System.out.println(tokenTxt +" "+start+" "+end);
                            listToken.add(tokenTxt);
                        }
                        String[] token=listToken.toArray(new String[listToken.size()]);
			String[] tags = posTagger.tag(token);
			for (int i = 0; i < token.length; i++) {
                              int start=tokensWithPositions.get(i).getStart()+startSentence;
                              int end=tokensWithPositions.get(i).getEnd()+startSentence;
                               // System.out.println(token[i]+" "+ tags[i] +" "+ start+" " +end);
				tokens.add(new PosToken(token[i], tags[i], start, end,startSE,endSE));
			}
                        Sentence_tokens.add((ArrayList<PosToken>) tokens);
		}
               
		return Sentence_tokens;
	}

        public List<MyPair> extractSentecnesOffset(String text){
           List<MyPair> Offsets =new ArrayList<MyPair>();
           List<Sentence>  sentences = ss.splitPos(text);
           for(Sentence s: sentences){
               Offsets.add(new MyPair(s.getStart(),s.getEnd()))  ;
                       }
           return Offsets;
   
        
        }
        
	public List<PosToken> getVerbs(String text) {

		List<PosToken> tokens = new ArrayList<PosToken>();
		if (text == null) {
			logger.warn("text is null");
			return tokens;
		}

		if (text.isEmpty()) {
			logger.warn("text is empty");
			return tokens;
		}
		String[] sentences = ss.split(text);
		for (String sentence : sentences) {
			String[] token = ts.tokenize(sentence);
			String[] tags = posTagger.tag(token);

			for (int i = 0; i < token.length; i++) {
				PosToken t = new PosToken(token[i], tags[i]);
				if (t.isVerb()) {
					tokens.add(t);
				}
			}
		}
		return tokens;
	}

}


 

