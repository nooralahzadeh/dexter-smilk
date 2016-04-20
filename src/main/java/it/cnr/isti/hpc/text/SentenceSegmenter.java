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

import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SentenceSegmenter allows to segment text in sentences.
 *
 * @author Diego Ceccarelli, diego.ceccarelli@isti.cnr.it created on 08/giu/2012
 */
public class SentenceSegmenter {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(SentenceSegmenter.class);

    SentenceDetector sentenceDetector = null;

    private static SentenceSegmenter instance = null;
    private static DexterParams params = DexterParams.getInstance();

    public SentenceSegmenter() {
        InputStream modelIn = null;
        try {
            // Loading sentence detection model
            // modelIn = getClass().getResourceAsStream("/nlp/en-sent.bin");
            String path = params.getDefaultNlpModel().getPath();
            modelIn = getClass().getResourceAsStream(path+"/sent.bin");
            final SentenceModel sentenceModel = new SentenceModel(modelIn);
            modelIn.close();

            sentenceDetector = new SentenceDetectorME(sentenceModel);

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

    // public static SentenceSegmenter getInstance() {
    // if (instance == null)
    // instance = new SentenceSegmenter();
    // return instance;
    // }
    public String[] split(String text) {
        return sentenceDetector.sentDetect(text);
    }

    public String[] split(Reader reader) {
        BufferedReader br = new BufferedReader(reader);
        String line = "";
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            logger.error("reading the string to segment");
            System.exit(-1);
        }
        return sentenceDetector.sentDetect(sb.toString());
    }

    public List<Sentence> splitPos(String text) {
        List<Sentence> sentences = new LinkedList<Sentence>();
        for (Span s : sentenceDetector.sentPosDetect(text)) {
            sentences.add(new Sentence(
                    text.substring(s.getStart(), s.getEnd()), s.getStart(), s
                    .getEnd()));

        }
        return sentences;
    }

    public static void main(String[] args) {
        String test = "France deployed two fighter jets today to search for a missing airliner with 116 people aboard that has reportedly crashed after flying into a violent sandstorm in the Sahara desert. Air Algerie flight 5017 took off in the early hours from Ouagadougou";
        SentenceSegmenter ss = new SentenceSegmenter();
        TokenSegmenter ts = new TokenSegmenter();
        SpotManager sm = SpotManager.getStandardSpotCleaner();
        PosTagger ps = PosTagger.getInstance();
//
//        List<PosToken> tkpos = ps.tagWithOffSet(test);
//        for (PosToken pstk : tkpos) {
//            System.out.println(pstk.toStringFull());
//        }

        List<Sentence> sentences = ss.splitPos(test);
        for (Sentence sentence : sentences) {
            String currSentence = sentence.getText();
            int startSentence = sentence.getStart();

            List<Token> tokens = ts.tokenizePos(currSentence);
            List<Token> cleanTokens = new LinkedList<Token>();

            // experimental
            for (Token t : tokens) {

                t.setStart(t.getStart() + startSentence);
                t.setEnd(t.getEnd() + startSentence);

                String token = test.substring(t.getStart(), t.getEnd());
                String cleanToken = sm.clean(token);
                if (cleanToken.isEmpty()) {
                    continue;
                }

                System.out.println(token + "-> " + cleanToken);
                System.out.println("token in text: " + token);

				// Skip empty token (or tokens made only of chars cleaned
                // above)
                t.setText(cleanToken);
                cleanTokens.add(t);
            }

        }
    }

}
