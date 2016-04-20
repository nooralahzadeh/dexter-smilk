/*
 * Copyright 2015 fnoorala.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.inria.wimmics.smilk.nerd;

/**
 *
 * @author fnoorala
 */
import edu.stanford.nlp.ling.CoreAnnotations;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.text.MyPair;
import it.cnr.isti.hpc.text.Token;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.collections.map.MultiValueMap;

/**
 *
 * @author fnoorala
 *
 */
public class StanfordWoutCoref implements NameEntityRecognizer {

    private static final Logger LOG = LoggerFactory.getLogger(StanfordWoutCoref.class);
    DexterParams params = DexterParams.getInstance();
    String path;

    private static StanfordCoreNLP pipeline;
    private static Properties props;
    final List<String> TYPES = params.TYPES;

    Map<Integer, MyPair> sentenceIndex_Length = new HashMap<Integer, MyPair>();
    
     List<Token> context=new ArrayList<Token>();

    public StanfordWoutCoref() {

        try {

            props = new Properties();
            props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
            props.put("tokenize.options", "ptb3Escaping=false");
            
           // props.put("ner.model", "/user/fnoorala/home/NetBeansProjects/dexter/en.data/stanford-home/classifiers/muc.7class.distsim.crf.ser.gz");
          //  props.put("ner.model", "/user/fnoorala/home/NetBeansProjects/dexter/en.data/stanford-home/classifiers/conll.4class.distsim.crf.ser.gz");
          //   props.put("ner.model", "/user/fnoorala/home/NetBeansProjects/dexter/en.data/stanford-home/classifiers/all.3class.distsim.crf.ser.gz");
            //props.put("postprocessing", false);
            pipeline = new StanfordCoreNLP(props);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(StanfordWoutCoref.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public List<Token> nerd(Document doc) {

        LOG.debug("Starting Stanford NLP");
        List<Token> filterNerdTokens = new LinkedList<Token>();
        List<Token> NerdTokens = new LinkedList<Token>();
        
        List<Token> NNPS_Tokens=new ArrayList<Token>();

        // run all Annotators on the passed-in text
        Annotation document = new Annotation(doc.getContent());
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with
        // custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        StringBuilder sb = new StringBuilder();

        List<EmbeddedToken> tokens = new ArrayList<EmbeddedToken>();

        List<Token> neTokens = new ArrayList<Token>();

        int sentenceIndex = 0;
        int firstPositionOfSentence = 0;
        int lastPositionOfSentence = 0;

       
        
        for (CoreMap sentence : sentences) {

            String prevNeToken = "O";
            String currNeToken = "O";
            boolean newToken = true;
            List<CoreLabel> core_tokens = sentence.get(TokensAnnotation.class);

            for (int x = 0; x < core_tokens.size(); x++) {
                CoreLabel token = core_tokens.get(x);

                // defining the start point of token in the 2th, 3rd ,... sentences
                if (x == 0) {
                    firstPositionOfSentence = token.beginPosition();
                }
                if (x == core_tokens.size() - 1) {
                    lastPositionOfSentence = token.endPosition();
                }

              
                currNeToken = token.get(NamedEntityTagAnnotation.class);
                String word = token.get(TextAnnotation.class);

                
                
                int start = token.beginPosition();
                int end = token.endPosition();

                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                
               
                
                Token tk = new Token(word, start, end, pos, sentenceIndex, 0);
                context.add(tk);
                tk.setType(currNeToken);

                if (currNeToken.equals("O")) {

                    if (!prevNeToken.equals("O") && (sb.length() > 0)) {
                        handleEntity(prevNeToken, sb, tokens, neTokens);
                        newToken = true;
                    }
                    continue;
                }

                if (newToken) {
                    prevNeToken = currNeToken;
                    newToken = false;
                    sb.append(word);
                    neTokens.add(tk);
                    continue;
                }

                if (currNeToken.equals(prevNeToken)) {
                    sb.append(" " + word);
                    neTokens.add(tk);
                } else {

                    handleEntity(prevNeToken, sb, tokens, neTokens);
                    sb.append(word + " ");
                    neTokens.add(tk);
                    newToken = true;
                }
                prevNeToken = currNeToken;
            }

            //sentence start and end properties
            MyPair sentenceFeatures = new MyPair(firstPositionOfSentence, lastPositionOfSentence);
            sentenceIndex_Length.put(sentenceIndex, sentenceFeatures);
            sentenceIndex++;
        }

        LOG.debug("We extracted {} tokens of interest from the input text", tokens.size());
        
        for(Token t: context){
            if (t.getPos().equalsIgnoreCase("NNP") || t.getPos().equalsIgnoreCase("NNPS"))
                NNPS_Tokens.add(t);
                   
        }

        for (EmbeddedToken t : tokens) {

            Token embedded = t.getEmbeddedToken();
            int index = embedded.getStartSE();
            MyPair pair = sentenceIndex_Length.get(index);
            int start = pair.getFirst();
            embedded.setStartSE(start);
            embedded.setEndSE(pair.getSecond());
            NerdTokens.add(embedded);

        }

        List<Token> filterNerdTokens_list=new ArrayList<Token>();
        for (Token t : NerdTokens) {
            if (TYPES.contains(t.getType())) {
                filterNerdTokens_list.addAll(t.getSubTokens());
                filterNerdTokens.add(t);
            }

        }
        
        
//        List<Token> filer_NNPS_Tokens=new ArrayList<Token>();
//        
//        for(Token t: NNPS_Tokens){
//                if(filterNerdTokens_list.contains(t)){
//                    continue;
//                 
//            }else{
//                    t.setNESubTokens(Arrays.asList(t));
//                    t.setSubTokens(Arrays.asList(t));
//                    t.setType("MISC");
//                    t.setTypes(new String[]{"MISC"});
//                    filer_NNPS_Tokens.add(t);
//                }
//        }
//        
//          
//          String prevNNPToken = "";
//          String currNNPToken = "";
//          tokens = new ArrayList<EmbeddedToken>();
          
//          for(Token t:filer_NNPS_Tokens){
//            
//          
//                boolean newToken = true;
//                int start = t.getStart();
//                int end = t.getEnd();
//                currNNPToken=t.getPos();
//                Token tk = new Token(t.getText(), start, end, t.getPos(),t.getStartSE() , t.getEndSE());
//                     if (currNNPToken.equals("NNP")) {
//
//                    if (!currNNPToken.equals("") && (sb.length() > 0)) {
//                        handleEntity(prevNNPToken, sb, tokens, neTokens);
//                        newToken = true;
//                    }
//                    continue;
//                }
//
//                if (newToken) {
//                    prevNNPToken = currNNPToken;
//                    newToken = false;
//                    sb.append(t.getText());
//                    neTokens.add(tk);
//                    continue;
//                }
//
//                if (currNNPToken.equals(prevNNPToken)) {
//                    sb.append(" " + t.getText());
//                    neTokens.add(tk);
//                } else {
//
//                    handleEntity(prevNNPToken, sb, tokens, neTokens);
//                    sb.append(t.getText() + " ");
//                    neTokens.add(tk);
//                    newToken = true;
//                }
//                prevNNPToken = currNNPToken;
//            }
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        return filterNerdTokens;
    }

    private void handleEntity(String inKey, StringBuilder inSb, List inTokens, List<Token> neTokens) {
        LOG.debug("'{}' is a {}", inSb, inKey);
        inTokens.add(new EmbeddedToken(new ArrayList<>(neTokens)));
        inSb.setLength(0);
        neTokens.clear();
    }

    @Override
    public void init(DexterParams dexterParams, DexterLocalParams defaultModuleParams) {
        
    }

    @Override
    public List<Token> getContext() {
         return context;
    }

}

class EmbeddedToken {

    private List<Token> tokens;
    private Token token;

    public EmbeddedToken(List<Token> tokens) {
        super();

        this.tokens = tokens;

    }

    public Token getEmbeddedToken() {

        StringBuilder text = new StringBuilder();
        List<String> tags = new ArrayList<String>();
        List<String> types = new ArrayList<String>();
        for (Token t : this.tokens) {
            text.append(t.getText());
            text.append(" ");
            tags.add(t.getPos());
            types.add(t.getType());

        }

        token = new Token(text.toString().trim(), this.tokens.get(0).getStart(), this.tokens.get(this.tokens.size() - 1).getEnd());

        token.setNESubTokens(tokens);
        token.setSubTokens(tokens);
        token.setPosTags(tags);
        token.setTypes(types.toArray(new String[types.size()]));
        token.setType(tokens.get(0).getType());
        token.setPos(tokens.get(0).getPos());

        return token;
    }
}
