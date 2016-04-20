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

import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.common.FlatDocument;
import it.cnr.isti.hpc.text.MyPair;
import it.cnr.isti.hpc.text.Sentence;
import it.cnr.isti.hpc.text.SentenceSegmenter;
import it.cnr.isti.hpc.text.Token;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fnoorala
 */
public class Coreference {

     OpenNLPNerd ne;

    private final SentenceSegmenter sn;
    
    public Coreference() {
        ne = new OpenNLPNerd();
        sn = new SentenceSegmenter();
    }
    
    public Document replacement(Document document){
        
         List<Sentence> sentences = sn.splitPos(document.getContent());

            String[] sentences_array = new String[sentences.size()];
            for (int i = 0; i < sentences.size(); i++) {
                sentences_array[i] = sentences.get(i).getText();
            }

            Map<Integer, List<MyPair>> replacements = ne.getReplacements(sentences_array);

            StringBuffer string=new StringBuffer(document.getContent());              
              for (Map.Entry<Integer, List<MyPair>> entry : replacements.entrySet()) {
                    MyPair nominated=entry.getValue().get(0);
                    for(int i=1; i<entry.getValue().size();i++){
                        MyPair p=entry.getValue().get(i);
                        int start=p.getFirst();
                        int end=p.getSecond();
                        string.replace(start, end, string.substring(nominated.getFirst(), nominated.getSecond()));
                    }
                   
                }
              Document doc = new FlatDocument(string.toString());
           return doc;  
        
    }
    
    
    
    
}
