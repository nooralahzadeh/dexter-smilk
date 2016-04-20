package fr.inria.wimmics.smilk.nerd;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.util.*;

import it.cnr.isti.hpc.text.Token;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import it.cnr.isti.hpc.text.MyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Spotter
 *
 *
 */
public class StanfordWthCoref implements NameEntityRecognizer {

    DexterParams params = DexterParams.getInstance();
    String path;

    private static StanfordCoreNLP pipeline;
    private static Properties props;
    final List<String> TYPES= params.TYPES;
    
    
      //AbstractSequenceClassifier classifier ;
       List<Token> context=new ArrayList<Token>();
      
    public StanfordWthCoref() {

        try {
            
            props = new Properties();
            props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
            props.put("dcoref.score", true);
            
            // disable all traditional PTB3 token transforms (like parentheses becoming -LRB-, -RRB-)
           // props.put("tokenize.ptb3Escaping", "false");
            props.put("tokenize.options", "ptb3Escaping=false");
            //props.put("postprocessing", false);
            pipeline = new StanfordCoreNLP(props);
        

//      path = params.getDefaultModel().getPath();
//      serializedClassifier = path + "/stanford-home/classifiers/conll.4class.distsim.crf.ser.gz";
//      classifier = CRFClassifier.getClassifier(serializedClassifier);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(StanfordWthCoref.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public List<Token> nerd(Document doc) {
        List<Token> filterNerdTokens = new LinkedList<Token>();
        List<Token> NerdTokens = new LinkedList<Token>();
       
        
        // create an empty Annotation just with the given text  
        Annotation document = new Annotation(doc.getContent());

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        //coref graph
        Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
        

        

        Map<Integer, MyPair> sentenceIndex_Length = new HashMap<Integer, MyPair>();
        int sentenceIndex = 0;

        StringBuilder sb = new StringBuilder();
        List<EmbeddedToken> tokens = new ArrayList<EmbeddedToken>();

        List<Token> neTokens = new ArrayList<Token>();
         
        
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

        
        //replacing the coreference tokens
        
        List<Token> coref_tokens=new ArrayList<Token>();
        sentenceIndex=0;
        for (CoreMap sentence : sentences) {

             List<CoreLabel> core_tokens = sentence.get(TokensAnnotation.class);
          
          
            
             for (int x = 0; x < tokens.size(); x++) {
                CoreLabel token = core_tokens.get(x);
                String word = token.get(TextAnnotation.class);
                //replacing with representetibe mention start 
                Integer corefClustId = token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);           
                CorefChain chain = graph.get(corefClustId);
              
                if (chain != null) {
                    
                int start = token.beginPosition();
                int end = token.endPosition();

                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
              
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);

                Token tk = new Token(word, start, end, pos, sentenceIndex, 0);
                tk.setType(ne);
                    int sentINdx = chain.getRepresentativeMention().sentNum - 1;
                    CoreMap corefSentence = sentences.get(sentINdx);
                    List<CoreLabel> corefSentenceTokens = corefSentence.get(TokensAnnotation.class);

                    String newwords = "";
                    CorefMention reprMent = chain.getRepresentativeMention();
 
                  
                    if (token.index() < reprMent.startIndex || token.index() > reprMent.endIndex) {
                        List<String> types = new ArrayList<String>();
                        
                        for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {

                            CoreLabel matchedLabel = corefSentenceTokens.get(i - 1);
                          
                            newwords += matchedLabel.word() + " ";
                            types.add(matchedLabel.ner());

                        }
                        
                  //     System.out.println("converting " + token.word() + " to " + newwords);
                        tk.setText(newwords.trim());

                        for (String t : types) {
                            if (TYPES.contains(t)) {
                                tk.setType(t);
                            }
                        }
                       
                        tk.setSentIndx(sentenceIndex);
                        tk.setTokenIndex(token.index());
                        tk.setTypes(types.toArray(new String[types.size()] ));
                        tk.setNESubTokens(Arrays.asList(tk));
                        tk.setPosTags(Arrays.asList(pos));
                        tk.setSubTokens(Arrays.asList(tk));
                        coref_tokens.add(tk);   
                      
                    }
                }
            } 
              sentenceIndex++;
        }
        
        
       for(Token t :coref_tokens){
          
            int index = t.getStartSE();
            MyPair pair = sentenceIndex_Length.get(index);
            int start=pair.getFirst();
            t.setStartSE(start);
            t.setEndSE(pair.getSecond());
            NerdTokens.add(t);
           
       }
        
         for(EmbeddedToken t: tokens){
            
            Token embedded=t.getEmbeddedToken();
            int index = embedded.getStartSE();
            MyPair pair = sentenceIndex_Length.get(index);
            int start=pair.getFirst();
            embedded.setStartSE(start);
            embedded.setEndSE(pair.getSecond());
            NerdTokens.add(embedded);
            
        }       
        
      
         
       for(Token t: NerdTokens){
           if(TYPES.contains(t.getType())){
               filterNerdTokens.add(t);
           }
               
       }
         

       
        //considering the coreference as a token and the NE inside as subtokens
        // System.out.println(filterNerdTokens);
         
       

        

        return filterNerdTokens;
    }
   
    private void handleEntity(String inKey, StringBuilder inSb, List inTokens, List<Token> tokens) {
       
        inTokens.add(new EmbeddedToken(new ArrayList<>(tokens)));
        inSb.setLength(0);
        tokens.clear();
    }

    @Override
    public void init(DexterParams dexterParams, DexterLocalParams defaultModuleParams) {

    }

    @Override
    public List<Token> getContext() {
         return context;
    }
    
    class EmbeddedToken {

    private List<Token> tokens;
    private Token token;

    public EmbeddedToken(List<Token> tokens) {
        super();

        this.tokens = tokens;

    }

    public Token getEmbeddedToken() {

        StringBuilder text=new StringBuilder();
        List<String> tags=new ArrayList<String>();
        List<String> types=new ArrayList<String>();
        for (Token t : this.tokens) {
            text.append(t.getText());
            text.append(" ");
            tags.add(t.getType());
            types.add(t.getPos());
            
        }
        
        token=new Token(text.toString().trim(), this.tokens.get(0).getStart()
                ,this.tokens.get(this.tokens.size()-1).getEnd());
        
        token.setNESubTokens(tokens);
        token.setSubTokens(tokens);
        token.setPosTags(tags);
        token.setTypes(types.toArray(new String[types.size()]));
        token.setType(tokens.get(0).getType());
        token.setPos(tokens.get(0).getPos());
  
        
        return token;
    }
}
}
