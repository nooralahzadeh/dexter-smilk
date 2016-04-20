package fr.inria.wimmics.smilk.nerd;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
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
public class StanfordCoref implements NameEntityRecognizer {

    DexterParams params = DexterParams.getInstance();
    String path;

    private static StanfordCoreNLP pipeline;
    private static Properties props;
    final List<String> TYPES= params.TYPES;
    public StanfordCoref() {

        try {

            props = new Properties();
            props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
            props.put("dcoref.score", true);
            //props.put("postprocessing", false);
            pipeline = new StanfordCoreNLP(props);

//      path = params.getDefaultModel().getPath();
//      serializedClassifier = path + "/stanford-home/classifiers/conll.4class.distsim.crf.ser.gz";
//      classifier = CRFClassifier.getClassifier(serializedClassifier);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(StanfordCoref.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public List<Token> nerd(Document doc) {
        List<Token> NerdTokens = new LinkedList<Token>();
        List<Token> FilteredCorefTokens = new LinkedList<Token>();
        List<Token> FinalTokens = new LinkedList<Token>();
        List<Token> FilterNerdTokens = new LinkedList<Token>();
        Set<String> ners = new HashSet<String>();
        // create an empty Annotation just with the given text  
        Annotation document = new Annotation(doc.getContent());

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        //coref graph
        Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
        Map<MyPair, Integer> mentionBoundry = new HashMap<>();
        Map<MyPair, String> mentionMap_repMen = new HashMap<>();
        Map<MyPair, List<Token>> mentionTokens = new HashMap<>();

        for (int i : graph.keySet()) {
            CorefChain chain = graph.get(i);
            LinkedList<MyPair> mentions = new LinkedList<MyPair>();
            int sentId = 0;

            for (CorefMention men : chain.getMentionsInTextualOrder()) {
                MyPair p = new MyPair(men.startIndex, men.endIndex);
                mentions.add(p);
                mentionBoundry.put(p, men.sentNum);
                mentionMap_repMen.put(p, chain.getRepresentativeMention().mentionSpan);
            }

        }

        Map<Integer, MyPair> sentenceIndex_Length = new HashMap<Integer, MyPair>();
        int sentenceIndex = 0;

        int lastIndexOfSentence = 0;

        for (CoreMap sentence : sentences) {

            List<String> originalSenetnce = new ArrayList<String>();
            List<String> resolved = new ArrayList<String>();

            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);

            int firstPositionOfSentence = 0;
            int lastPositionOfSentence = 0;
            for (int x = 0; x < tokens.size(); x++) {

                CoreLabel token = tokens.get(x);
                String word = token.get(TextAnnotation.class);

                // defining the start point of token in the 2th, 3rd ,... sentences
                if (x == 0) {
                    firstPositionOfSentence = token.beginPosition();
                }
                if (x == tokens.size() - 1) {
                    lastPositionOfSentence = token.endPosition();
                }
              //  int additive = (sentenceIndex == 0) ? 0 : 1;

                int start = token.beginPosition();
                int end = token.endPosition();

                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);

                Token tk = new Token(word, start, end, pos, sentenceIndex, 0);
                tk.setType(ne);

                //replacing with representetibe mention start 
                Integer corefClustId = token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);

                //System.out.println(token.word() + " --> corefClusterID = " + corefClustId);
                CorefChain chain = graph.get(corefClustId);
                // System.out.println("matched chain = " + chain);
                originalSenetnce.add(token.word());

                if (chain == null) {
                    resolved.add(token.word());

                } else {

                    int sentINdx = chain.getRepresentativeMention().sentNum - 1;
                    CoreMap corefSentence = sentences.get(sentINdx);
                    List<CoreLabel> corefSentenceTokens = corefSentence.get(TokensAnnotation.class);

                    String newwords = "";
                    CorefMention reprMent = chain.getRepresentativeMention();

                    // System.out.println(reprMent.mentionSpan + ":[" + sentenceIndex + " " + sentINdx + "]" + " [" + reprMent.startIndex + ":" + reprMent.endIndex + "]");
                    // Token tk_mention=new Token(reprMent.mentionSpan, reprMent., end, pos, startSE, endSE)
//                    if (sentINdx != sentenceIndex) {
//                        token.setIndex(lastIndexOfSentence + token.index());
//                    }

                    if (token.index() < reprMent.startIndex || token.index() > reprMent.endIndex) {
                        List<String> types = new ArrayList<String>();
                        for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {

                            CoreLabel matchedLabel = corefSentenceTokens.get(i - 1);

                            resolved.add(matchedLabel.word());

                            newwords += matchedLabel.word() + " ";
                            types.add(matchedLabel.ner());

                        }
                        //   System.out.println("converting " + token.word() + " to " + newwords);
                        tk.setText(newwords);

                        for (String t : types) {
                            if (TYPES.contains(t)) {
                                tk.setType(t);
                            }
                        }

                       // token.setIndex(token.index() - lastIndexOfSentence);
                    } else {
                        resolved.add(token.word());
                    }

                }

                tk.setSentIndx(sentenceIndex);
                tk.setTokenIndex(token.index());
                NerdTokens.add(tk);

            }

            lastIndexOfSentence += tokens.size() - 1;

            //sentence start and end properties
            MyPair sentenceFeatures = new MyPair(firstPositionOfSentence, lastPositionOfSentence);
            sentenceIndex_Length.put(sentenceIndex, sentenceFeatures);
            sentenceIndex++;
        }
        
        
      
//        System.out.println(mentionBoundry);
//        System.out.println(mentionMap_repMen);
//        System.out.println(sentenceIndex_Length);
        
        for (Token t : NerdTokens) {
            int index = t.getStartSE();
            MyPair pair = sentenceIndex_Length.get(index);
            int start=pair.getFirst();
            t.setStartSE(start);
            t.setEndSE(pair.getSecond());

        }

        for (MyPair p : mentionBoundry.keySet()) {
            List<Token> tt = new ArrayList<Token>();
            for (Token t : NerdTokens) {
                if (t.getSentIndx() == mentionBoundry.get(p) - 1 && t.getTokenIndx() >= p.getFirst() && t.getTokenIndx() < p.getSecond()) {
                    tt.add(t);
                }
               
            }
             mentionTokens.put(p, tt);
        }

        
        //filtering the interesitin Named entity types

        for (Token tkn : NerdTokens) {
            if (TYPES.contains(tkn.getType())) {
                ners.add(tkn.getType());
                FilterNerdTokens.add(tkn);
            }
        }
        
        

        List<Token> corefTokens = new ArrayList<Token>();
        for (MyPair mntks : mentionTokens.keySet()) {
            List<Token> tokenList = mentionTokens.get(mntks);
            int s = tokenList.get(0).getStart();
            int e = tokenList.get(tokenList.size() - 1).getEnd();
            String mention = "";
            String[] types = new String[tokenList.size()];
            List<String> pos = new ArrayList<String>();
            List<Token> tokens=new ArrayList<Token>();
            
            for (int i = 0; i < tokenList.size(); i++) {
                mention += tokenList.get(i).getText() + " ";
                types[i] = tokenList.get(i).getType();
                pos.add(tokenList.get(i).getPos());
                tokens.add(tokenList.get(i));
            }

            Token t = new Token(mention.trim(), s, e);
            t.setPosTags(pos);
            t.setTypes(types);
            t.setStartSE(tokenList.get(0).getStartSE());
            t.setEndSE(tokenList.get(0).getEndSE());
            t.setTypes(types);
            t.setSubTokens(tokens);
            corefTokens.add(t);

        }

        for (Token tc : corefTokens) {
            List<String> typeList = new ArrayList<String>(Arrays.asList(tc.getTypes()));
            if (!Collections.disjoint(ners, typeList)) {
                FilteredCorefTokens.add(tc);
            }
        }

        Map<Token, List<Token>> joinTokens = new HashMap<Token, List<Token>>();
        List<Token> removableTokens=new ArrayList<Token>();

        for (Token coref : FilteredCorefTokens) {
            List<Token> join = new ArrayList<Token>();

            if (coref.getTypes().length > 0) {
                for (Token tc : FilterNerdTokens) {
                    if (tc.getStart() >= coref.getStart() && tc.getEnd() <= coref.getEnd()) {
                        join.add(tc);
                        removableTokens.add(tc);
                    }
                }

                joinTokens.put(coref, join);
            }
        }

        //considering the coreference as a token and the NE inside as subtokens
         
        List<Token> finaljoins = new ArrayList<Token>();
//        for(Token t:FilterNerdTokens){
//            if(!removableTokens.contains(t))
//                finaljoins.add(t);
//        }

        List<List<Integer>> alreadyadded = new ArrayList<List<Integer>>();
        
        
        for (Token set : joinTokens.keySet()) {
            List<Token> subset = set.getSubTokens();
            int start = set.getStart();
            int end = set.getEnd();
            String text = set.getText();
            List<String> postags = new ArrayList<String>();
            String[] types = new String[subset.size()];
            
            List<Token> NESubset=joinTokens.get(set);
           
//            
            int i=0;
            for (Token s : subset) {
               // text += s.getText() + " ";
                postags.add(s.getPos());
                types[i] = s.getType();
                i++;

            }
       
            
            Token newToken = new Token(text.trim(), start, end);
            newToken.setType(NESubset.get(0).getType());
            newToken.setPos(NESubset.get(0).getPos());
            newToken.setPosTags(postags);
            newToken.setTypes(types);
            newToken.setStartSE(set.getStartSE());
            newToken.setEndSE(set.getEndSE());
            newToken.setSubTokens(subset);
            newToken.setNESubTokens(NESubset);
            
            List<Integer> list=Arrays.asList(start,end);
           /// Pair p = new Pair(start, end);

            if (alreadyadded.contains(list)) {
                continue;
            } else {
                alreadyadded.add(list);
                finaljoins.add(newToken);
            }
        }
         
       
        //finaljoins.addAll(FilterNerdTokens);

        return finaljoins;
    }

    @Override
    public void init(DexterParams dexterParams, DexterLocalParams defaultModuleParams) {

    }

    @Override
    public List<Token> getContext() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
