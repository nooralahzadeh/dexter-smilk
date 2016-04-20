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
package it.cnr.isti.hpc.dexter;

import fr.inria.wimmics.smilk.localsimilarity.Similarity;
import it.cnr.isti.hpc.benchmark.Stopwatch;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.disambiguation.Disambiguator;
import it.cnr.isti.hpc.dexter.disambiguation.TopScoreEntityDisambiguator;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.relatedness.MilneRelatedness;
import it.cnr.isti.hpc.dexter.relatedness.Relatedness;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.spotter.DictionarySpotter;
import it.cnr.isti.hpc.dexter.spotter.Spotter;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a standard entity linker, given a document the text is processed
 * with a {@link Spotter} that returns the possible mentions detected as a
 * {@link SpotMatchList}. Each {@link SpotMatch} contains a list of candidate
 * entities for the spot (see {@link EntityMatchList}). The
 * {@link Disambiguator} takes in input the SpotMatch list and takes care to
 * select only one entity for spot (if the spot has more than one entity). The
 * Disambiguator also gives a relevance score to each EntityMatch selected and
 * rank the entity matches by relevance. The Disambiguator usually exploits a
 * {@link Relatedness} function that provides the semantic distance between two
 * entities to perform the disambiguation. Dexter takes care to remove the
 * entity matches that overlaps in the text, removing the entity matches with
 * the lower scores.
 *
 * The Standard Tagger uses external spotter/disambiguation/relatedness classes
 * set in the <code>dexter-config.xml</code> file, you only have to implement
 * the correspondent interface, to produce a jar with your code and put it in
 * the folder <code>libs</code>. <br>
 * <br>
 * By default Dexter uses the {@link DictionarySpotter} as spotter, the the
 * {@link TopScoreEntityDisambiguator} as disambiguator, and the
 * {@link MilneRelatedness} as relatedness. If you do not set different classes
 * in the property file this component will be used.
 *
 *
 * @see Tagger
 *
 *
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 *
 * Created on Sep 5, 2012
 */
public class StandardTagger implements Tagger {

    private static final Logger logger = LoggerFactory
            .getLogger(StandardTagger.class);
    private final String name;
    private final Spotter spotter;
    private final Disambiguator disambiguator;
    private String similarity;
    private String relatedness;
    private final Stopwatch stopwatch;

    DexterParams dexterParams = DexterParams.getInstance();

    public StandardTagger(String name, Spotter spotter,
         Disambiguator disambiguator) {
        this.name = name;
        this.spotter = spotter;
        this.disambiguator = disambiguator;
        
        stopwatch = new Stopwatch();

		// Relatedness r = new MilneRelatedness();
        //
        // PluginLoader pl = new PluginLoader();
        // logger.info("using the dexter tagger");
        //
        // if (properties.has("spotter.class")) {
        // spotter = pl.getSpotter(properties.get("spotter.class"));
        // } else {
        // spotter = new DictionarySpotter();
        // }
        //
        // if (properties.has("disambiguator.class")) {
        // disambiguator = pl.getDisambiguator(properties
        // .get("disambiguator.class"));
        // } else {
        // disambiguator = new TopScoreEntityDisambiguator();
        // }
        //
        // if (properties.has("relatedness.class")) {
        // r = pl.getRelatedness(properties.get("relatedness.class"));
        // RelatednessFactory.register(r);
        // }
        logger.info("Spotter: {}", spotter.getClass());
       
        logger.info("Disambiguator: {}", disambiguator.getClass());
    }

    public StandardTagger(String name, Spotter spotter,
            Disambiguator disambiguator, String similarity) {
        this.name = name;
        this.spotter = spotter;
        this.disambiguator = disambiguator;
        this.similarity = similarity;

        stopwatch = new Stopwatch();

        logger.info("Spotter: {}", spotter.getClass());

        logger.info("Disambiguator: {}", disambiguator.getClass());

        logger.info("Similarity: {}", similarity);
    }

    public SpotMatchList spot(DexterParams dexterParams,
            DexterLocalParams localParams, Document doc) {
        Spotter spotter = this.spotter;
        if (localParams != null) {
            if (localParams.hasSpotter()) {
                spotter = localParams.getSpotter();
            }
        }
        SpotMatchList sml = spotter.match(localParams, doc);
        return sml;
    }

    @Override
    public EntityMatchList tag(DexterLocalParams localParams, Document doc) {

		// TODO, perform the tag using what specified in the
        // params
        Spotter spotter = this.spotter;
        Disambiguator disambiguator = this.disambiguator;
        String similarity = this.similarity;
        String relatedness = this.relatedness;

        if (localParams != null) {
            if (localParams.hasSpotter()) {
                spotter = localParams.getSpotter();

            }
            if (localParams.hasDisambiguator()) {
                disambiguator = localParams.getDisambiguator();
            }
        }

        stopwatch.start("spotting");

        SpotMatchList sml = spotter.match(localParams, doc);

        SpotMatchList sml_not_nil = new SpotMatchList();

        EntityMatchList eml = new EntityMatchList();

        EntityMatchList eml_nil = new EntityMatchList();

        //extract NIL spots from SpotMatchList
        for (SpotMatch s : sml) {

            if (s.getEntities().size() > 0) {
                sml_not_nil.add(s);
            } else {
                EntityMatch entityMatch_nill = new EntityMatch(0, 0, s);
                eml_nil.add(entityMatch_nill);
            }
        }

        if (!sml_not_nil.isEmpty()) {
            //System.out.println(sml);
            logger.info("spotting performed in {} millis",
                    stopwatch.stop("spotting"));

            stopwatch.start("disambiguation");

            
                eml = disambiguator.disambiguate(localParams, sml_not_nil);
          
            
           

            //???????????????????
            if (!eml.isEmpty()) {
                eml = eml.removeOverlappings();

            } else {
                logger.warn("no spot identified in text");
            }
            logger.info("disambiguation performed in {} millis",
                    stopwatch.stop("disambiguation"));

        } else {
            logger.warn("no spot identified in text");
        }

        //adding NIL 
        eml.addAll(eml_nil);

        return eml;

    }

    public String stats() {
        return stopwatch.stat();
    }

    @Override
    public EntityMatchList tag(Document document) {
        return tag(null, document);
    }

    @Override
    public void init(DexterParams dexterParams,
            DexterLocalParams defaultModuleParams) {
        
       
        // TODO Auto-generated method stub

    }
}
