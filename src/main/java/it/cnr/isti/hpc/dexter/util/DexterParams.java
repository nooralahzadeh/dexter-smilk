/**
 * Copyright 2014 Diego Ceccarelli
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
/**
 * Copyright 2014 Diego Ceccarelli
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
package it.cnr.isti.hpc.dexter.util;

import fr.inria.wimmics.smilk.localsimilarity.Similarity;
import it.cnr.isti.hpc.dexter.StandardTagger;
import it.cnr.isti.hpc.dexter.Tagger;
import it.cnr.isti.hpc.dexter.disambiguation.Disambiguator;
import it.cnr.isti.hpc.dexter.graph.NodeStar.Direction;
import it.cnr.isti.hpc.dexter.plugin.PluginLoader;
import it.cnr.isti.hpc.dexter.relatedness.Relatedness;
import it.cnr.isti.hpc.dexter.spotter.Spotter;
import it.cnr.isti.hpc.dexter.spotter.filter.SpotMatchFilter;
import it.cnr.isti.hpc.dexter.util.DexterParamsXMLParser.Param;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 *
 * Created on Jan 2, 2014
 */
public class DexterParams {

    private static final Logger logger = LoggerFactory
            .getLogger(DexterParams.class);

    private static DexterParams dexterParams;
    Map<String, DexterParamsXMLParser.Tagger> taggers;
    Map<String, DexterParamsXMLParser.Spotter> spotters;
    Map<String, DexterParamsXMLParser.SpotFilter> spotFilters;
    Map<String, DexterParamsXMLParser.Disambiguator> disambiguators;
    Map<String, String> relatednessFunctions;
    //<fnoorala>
    Map<String, DexterParamsXMLParser.Similarity> similarities;
    Map<String, DexterParamsXMLParser.LabelSimilarity> labelsimilarities;
    Map<String, DexterParamsXMLParser.ContextSimilarity> contextsimilarities;

    String defaultSimilarity;
    String defaultLabelSimilarity;
    String defaultContextSimilarity;
    File wordModel;
    File entityModel;
    File entityFreebaseModel;
    String sparqlEndPoint;
    String lang;

    FTPClient ftpClient = null;
     //</fnoorala>

    Map<String, Map<Direction, String>> graphs;
    Map<String, String> nlpmodels;
    Map<String, List<String>> models;
    Map<String, Integer> cacheSize;
    Map<String, Float> thresholds;

    File defaultModel;
    File graphDir;
    File labelDir;
    File indexDir;

    File spotsData;
    File spotsEliasFano;
    File spotsOffsetData;
    File spotsPerfectHash;
    File plainSpots;
    File entityToSpots;

    File defaultnlpModel;

    public final List<String> TYPES = Arrays.asList("PERSON", "ORGANIZATION", "LOCATION", "MISC", "ROLE", "LOC", "PER", "ORG");
    public final List<String> TAGS = Arrays.asList("NNP", "NNPS", ",", "-", ":", "_", ".");
    //Arrays.asList("NNP","NNPS","NN","NNS","FW","JJ","VBN","VBP","VB","VBG","VBZ");
    public final List<String> INTERESTING_TAGS_Context = Arrays.asList("NNP", "NNPS", "NN", "NNS", "FW");
    public final List<String> INTERESTING_TAGS_NE = Arrays.asList("NNP", "NNPS", "JJ", "NN", "NNS");

    private static final String DEFAULT = "___default";

    String defaultRelatedness;
    String accessmode;
    String ftpServer;

    public String getFtpServer() {
        return ftpServer;
    }

    public void setFtpServer(String ftpServer) {
        this.ftpServer = ftpServer;
    }

    public String getFtpUsername() {
        return ftpUsername;
    }

    public void setFtpUsername(String ftpUsername) {
        this.ftpUsername = ftpUsername;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public void setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }
    String ftpUsername;
    String ftpPassword;
 

    private DexterParamsXMLParser params;

    private PluginLoader loader;

    private File wikiToIdFile;

    private DexterParams() {

        taggers = new HashMap<String, DexterParamsXMLParser.Tagger>();
        spotters = new HashMap<String, DexterParamsXMLParser.Spotter>();
        spotFilters = new HashMap<String, DexterParamsXMLParser.SpotFilter>();

        disambiguators = new HashMap<String, DexterParamsXMLParser.Disambiguator>();
        relatednessFunctions = new HashMap<String, String>();

        //<fnoorala>
        similarities = new HashMap<String, DexterParamsXMLParser.Similarity>();
        labelsimilarities = new HashMap<String, DexterParamsXMLParser.LabelSimilarity>();
        contextsimilarities = new HashMap<String, DexterParamsXMLParser.ContextSimilarity>();

        //</fnoorala>
        graphs = new HashMap<String, Map<Direction, String>>();
        models = new HashMap<String, List<String>>();
        nlpmodels = new HashMap<String, String>();
        cacheSize = new HashMap<String, Integer>();
        thresholds = new HashMap<String, Float>();
    }

    private DexterParams(String xmlConfig) {
        this();
        params = DexterParamsXMLParser.load(xmlConfig);

        loader = new PluginLoader(new File(params.getLibs().getLib()));

        for (DexterParamsXMLParser.Graph graph : params.getGraphs().getGraphs()) {
            Map<Direction, String> names = new HashMap<Direction, String>();
            names.put(Direction.IN, graph.getIncoming());
            names.put(Direction.OUT, graph.getOutcoming());
            graphs.put(graph.getName(), names);
            logger.info("registering graph {} in: {}", graph.getName(),
                    graph.getIncoming());
            logger.info("registering graph {} out: {}", graph.getName(),
                    graph.getOutcoming());
        }

        for (DexterParamsXMLParser.Model model : params.getModels().getModels()) {
            models.put(model.getName(), Arrays.asList(model.getPath(), model.getSparqlEndPoint(), model.getAccess()));
        }

        for (DexterParamsXMLParser.nlpModel nlpmodel : params.getNlpModels().getModels()) {
            nlpmodels.put(nlpmodel.getName(), nlpmodel.getPath());
        }

        for (DexterParamsXMLParser.Cache cache : params.getCaches().getCaches()) {
            cacheSize.put(cache.getName(), cache.getSize());
        }

        for (DexterParamsXMLParser.Threshold threshold : params.getThresholds()
                .getThresholds()) {
            thresholds.put(threshold.getName(), threshold.getValue());
        }

        String access = models.get(params.getModels().getDefaultModel()).get(2);
       
        if (access.equalsIgnoreCase("ftp")) {
            ftpClient = new FTPClient();
            try {
                ftpServer = params.getftpServer().getServer();
                ftpUsername = params.getftpServer().getUser();
                ftpPassword = params.getftpServer().getPass();
                ftpClient.connect(ftpServer);
                ftpClient.login(ftpUsername, ftpPassword);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                String ftpPath = "ftp://" + ftpUsername + ":" + ftpPassword + "@" + ftpServer;

                defaultModel = new File(
                        ftpPath + models.get(params.getModels().getDefaultModel()).get(0));

            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(DexterParams.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {

            defaultModel = new File(
                    models.get(params.getModels().getDefaultModel()).get(0));

        }

        sparqlEndPoint = models.get(params.getModels().getDefaultModel()).get(1);
        lang = params.getModels().getDefaultModel();

        defaultnlpModel = new File(nlpmodels.get(params.getNlpModels().getDefaultModel()));

        graphDir = new File(defaultModel, params.getGraphs().getDir());

        labelDir = new File(defaultModel, params.getLabels().getDir());
        indexDir = new File(defaultModel, params.getIndex().getDir());
        wikiToIdFile = new File(indexDir, params.getIndex().getWikiIdMap());
        defaultRelatedness = params.getRelatednessFunctions()
                .getDefaultFunction();
        //<fnoorala>
        defaultSimilarity = params.getSimilarities().getDefaultSimilairty();
        defaultContextSimilarity = params.getConetxSimilarities().getDefaultFunction();
        defaultLabelSimilarity = params.getLabelSimilarities().getDefaultFunction();
        //</fnoorala>
        File spotsDir = new File(defaultModel, params.getSpotRepository()
                .getDir());
        
        spotsData = new File(spotsDir, params.getSpotRepository()
                .getSpotsData());
        
        //<fnoorala>
        File ent2vecDir = new File(defaultModel, params.getEnt2VecRepositoy()
                .getDir());
        wordModel = new File(ent2vecDir, params.getEnt2VecRepositoy()
                .getWordModel());
        entityModel = new File(ent2vecDir, params.getEnt2VecRepositoy()
                .getEnityModel());

        entityFreebaseModel = new File(ent2vecDir, params.getEnt2VecRepositoy()
                .getEnityFreebaseModel());
        //</fnoorala>

        spotsOffsetData = new File(spotsDir, params.getSpotRepository()
                .getOffsets());
        spotsEliasFano = new File(spotsDir, params.getSpotRepository()
                .getEliasFanoOffsets());
        spotsPerfectHash = new File(spotsDir, params.getSpotRepository()
                .getPerfectHash());
        plainSpots = new File(spotsDir, params.getSpotRepository()
                .getPlainSpots());

        entityToSpots = new File(spotsDir, params.getSpotRepository()
                .getEntityToSpots());

    }

    private DexterParams(String xmlConfig, String lang) {
        this();
        params = DexterParamsXMLParser.load(xmlConfig);

        params.getModels().setDefaultModel(lang);
        params.getNlpModels().setDefaultModel(lang);

        loader = new PluginLoader(new File(params.getLibs().getLib()));

        for (DexterParamsXMLParser.Graph graph : params.getGraphs().getGraphs()) {
            Map<Direction, String> names = new HashMap<Direction, String>();
            names.put(Direction.IN, graph.getIncoming());
            names.put(Direction.OUT, graph.getOutcoming());
            graphs.put(graph.getName(), names);
            logger.info("registering graph {} in: {}", graph.getName(),
                    graph.getIncoming());
            logger.info("registering graph {} out: {}", graph.getName(),
                    graph.getOutcoming());
        }

        for (DexterParamsXMLParser.Model model : params.getModels().getModels()) {
            models.put(model.getName(), Arrays.asList(model.getPath(), model.getSparqlEndPoint(), model.getAccess()));
        }

        for (DexterParamsXMLParser.nlpModel nlpmodel : params.getNlpModels().getModels()) {
            nlpmodels.put(nlpmodel.getName(), nlpmodel.getPath());
        }

        for (DexterParamsXMLParser.Cache cache : params.getCaches().getCaches()) {
            cacheSize.put(cache.getName(), cache.getSize());
        }

        for (DexterParamsXMLParser.Threshold threshold : params.getThresholds()
                .getThresholds()) {
            thresholds.put(threshold.getName(), threshold.getValue());
        }

        accessmode = models.get(params.getModels().getDefaultModel()).get(2);
        if (accessmode.equalsIgnoreCase("ftp")) {
            ftpClient = new FTPClient();

            try {
                  ftpServer = params.getftpServer().getServer();
                  ftpUsername = params.getftpServer().getUser();
                  ftpPassword = params.getftpServer().getPass();
                ftpClient.connect(ftpServer);
                ftpClient.login(ftpUsername, ftpPassword);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                String ftpPath = "ftp://" + ftpUsername + ":" + ftpPassword + "@" + ftpServer;

                defaultModel = new File(
                        ftpPath + models.get(params.getModels().getDefaultModel()).get(0));

            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(DexterParams.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {

            defaultModel = new File(
                    models.get(params.getModels().getDefaultModel()).get(0));

        }

        sparqlEndPoint = models.get(params.getModels().getDefaultModel()).get(1);

        defaultnlpModel = new File(nlpmodels.get(params.getNlpModels().getDefaultModel()));

        graphDir = new File(defaultModel, params.getGraphs().getDir());

        labelDir = new File(defaultModel, params.getLabels().getDir());
        indexDir = new File(defaultModel, params.getIndex().getDir());
        wikiToIdFile = new File(indexDir, params.getIndex().getWikiIdMap());
        defaultRelatedness = params.getRelatednessFunctions()
                .getDefaultFunction();
        //<fnoorala>
        defaultSimilarity = params.getSimilarities().getDefaultSimilairty();
        defaultContextSimilarity = params.getConetxSimilarities().getDefaultFunction();
        defaultLabelSimilarity = params.getLabelSimilarities().getDefaultFunction();
        //</fnoorala>
        File spotsDir = new File(defaultModel, params.getSpotRepository()
                .getDir());
        spotsData = new File(spotsDir, params.getSpotRepository()
                .getSpotsData());
        System.out.println( spotsData.getTotalSpace());
        //<fnoorala>
        File ent2vecDir = new File(defaultModel, params.getEnt2VecRepositoy()
                .getDir());
        
        wordModel = new File(ent2vecDir, params.getEnt2VecRepositoy()
                .getWordModel());
        entityModel = new File(ent2vecDir, params.getEnt2VecRepositoy()
                .getEnityModel());

        entityFreebaseModel = new File(ent2vecDir, params.getEnt2VecRepositoy()
                .getEnityFreebaseModel());
        //</fnoorala>

        spotsOffsetData = new File(spotsDir, params.getSpotRepository()
                .getOffsets());
        spotsEliasFano = new File(spotsDir, params.getSpotRepository()
                .getEliasFanoOffsets());
        spotsPerfectHash = new File(spotsDir, params.getSpotRepository()
                .getPerfectHash());
        plainSpots = new File(spotsDir, params.getSpotRepository()
                .getPlainSpots());

        entityToSpots = new File(spotsDir, params.getSpotRepository()
                .getEntityToSpots());

    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    private void loadDisambiguators() {
        for (DexterParamsXMLParser.Disambiguator function : params
                .getDisambiguators().getDisambiguators()) {

            logger.info("registering disambiguator {} -> {} ",
                    function.getName(), function.getClazz());
            disambiguators.put(function.getName(), function);
        }
        String defaultName = params.getDisambiguators()
                .getDefaultDisambiguator();
        DexterParamsXMLParser.Disambiguator dis = disambiguators
                .get(defaultName);
        disambiguators.put(DEFAULT, dis);
    }

    private void loadSpotters() {
        for (DexterParamsXMLParser.Spotter function : params.getSpotters()
                .getSpotters()) {
            logger.info("registering spotter {} -> {} ", function.getName(),
                    function.getClazz());
            spotters.put(function.getName(), function);

        }
        String defaultName = params.getSpotters().getDefaultSpotter();
        DexterParamsXMLParser.Spotter spotter = spotters.get(defaultName);
        spotters.put(DEFAULT, spotter);
    }

    private void loadTaggers() {
        for (DexterParamsXMLParser.Tagger tagger : params.getTaggers()
                .getTaggers()) {
            // TODO add tagger from class
            // TODO check if components exist

            taggers.put(tagger.getName(), tagger);
        }

        String defaultName = params.getTaggers().getDefaultTagger();
        DexterParamsXMLParser.Tagger tagger = taggers.get(defaultName);
        taggers.put(DEFAULT, tagger);

    }

    //<fnoorala

    private void loadSimilarities() {
        for (DexterParamsXMLParser.Similarity similarity : params.getSimilarities().getSimilarities()) {
            // TODO add tagger from class
            // TODO check if components exist

            similarities.put(similarity.getName(), similarity);
        }

        String defaultName = params.getSimilarities().getDefaultSimilairty();
        DexterParamsXMLParser.Similarity similarity = similarities.get(defaultName);
        similarities.put(DEFAULT, similarity);

    }

    //</fnoorala>

    private void loadSpotFilters() {
        for (DexterParamsXMLParser.SpotFilter function : params
                .getSpotFilters().getSpotFilters()) {
            logger.info("registering spot filter {} -> {} ",
                    function.getName(), function.getClazz());
            spotFilters.put(function.getName(), function);

        }
    }

    private void loadRelatednessFunctions() {
        for (DexterParamsXMLParser.RelatednessFunction function : params
                .getRelatednessFunctions().getRelatednessFunctions()) {
            logger.info("registering relatedness {} -> {} ",
                    function.getName(), function.getClazz());
            relatednessFunctions.put(function.getName(), function.getClazz());
            // FIXME remove relatedness factory??
            // RelatednessFactory.register(relatedness);
        }
        String defaultName = defaultRelatedness;
        String clazz = relatednessFunctions.get(defaultName);
        relatednessFunctions.put(DEFAULT, clazz);
    }

    //<fnoorala>
    private void loadLabelSimilarities() {
        for (DexterParamsXMLParser.LabelSimilarity function : params
                .getLabelSimilarities().getLabelSimilarities()) {
            logger.info("registering similarity {} -> {} ",
                    function.getName(), function.getClazz());
            labelsimilarities.put(function.getName(), function);

        }
        String defaultName = defaultLabelSimilarity;
        DexterParamsXMLParser.LabelSimilarity sim = labelsimilarities
                .get(defaultName);
        labelsimilarities.put(DEFAULT, sim);

    }

    private void loadContextSimilarities() {
        for (DexterParamsXMLParser.ContextSimilarity function : params
                .getConetxSimilarities().getContextSimilarities()) {
            logger.info("registering similarity {} -> {} ",
                    function.getName(), function.getClazz());
            contextsimilarities.put(function.getName(), function);

        }
        String defaultName = defaultContextSimilarity;
        DexterParamsXMLParser.ContextSimilarity sim = contextsimilarities
                .get(defaultName);
        contextsimilarities.put(DEFAULT, sim);

    }

    public String getModelPath(String key) {
        return models.get(key).get(0);
    }

    public String getSparqlEndPointPath(String key) {
        System.out.println(key + "  " + models.get(key).get(1));
        return models.get(key).get(1);
    }

    public String getSparqlEndPointPath() {
        return sparqlEndPoint;
    }

    public String getDefaulyModelLang() {
        return lang;
    }

    //</fnoorala
    public File getSpotsData() {
        return spotsData;
    }

    public File getWordModelData() {
        return wordModel;
    }

    public File getEntModelData() {
        return entityModel;
    }

    public File getEntFreebaseModelData() {
        return entityFreebaseModel;
    }

    public String getDefaultRelatedness() {
        return defaultRelatedness;
    }

    public String getDefaultSimilarity() {

        return defaultSimilarity;
    }

    public String getDefaultLabelSimilarity() {

        return defaultLabelSimilarity;
    }

    public String getDefaultContextSimilarity() {

        return defaultContextSimilarity;
    }

    public String getAccessMode() {
        return accessmode;

    }
    
    public String getftpServerInfo() {
        return "ftp:/"+ftpUsername+":"+ftpPassword+"@"+ftpServer;

    }
    

    public static DexterParams getInstance() {
        if (dexterParams == null) {
            String confFile = System.getProperty("conf");
            if (confFile == null) {
                confFile = "dexter-conf.xml";
            }
            logger.info("loading configuration from {} ", confFile);
            dexterParams = new DexterParams(confFile);
            dexterParams.loadDisambiguators();
            dexterParams.loadRelatednessFunctions();

            //<fnoorala>
            dexterParams.loadContextSimilarities();
            dexterParams.loadLabelSimilarities();
            dexterParams.loadSimilarities();

            //</fnoorala>
            dexterParams.loadSpotFilters();
            dexterParams.loadSpotters();

            dexterParams.loadTaggers();
        }
        return dexterParams;
    }

    public static DexterParams getInstance(String confFile, String lang) {

        logger.info("loading configuration for {} from {} ", lang, confFile);
        dexterParams = new DexterParams(confFile, lang);
        dexterParams.loadDisambiguators();
        dexterParams.loadRelatednessFunctions();

        //<fnoorala>
        dexterParams.loadContextSimilarities();
        dexterParams.loadLabelSimilarities();
        dexterParams.loadSimilarities();

        //</fnoorala>
        dexterParams.loadSpotFilters();
        dexterParams.loadSpotters();
        dexterParams.loadTaggers();

        return dexterParams;
    }

    public int getCacheSize(String name) {
        if (!cacheSize.containsKey(name)) {
            logger.warn(
                    "cannot find cache size for {}, use default value (1000) ",
                    name);
            return 1000;
        }
        return cacheSize.get(name);
    }

    public boolean hasSpotter(String name) {
        return spotters.containsKey(name);
    }

    public SpotMatchFilter getSpotMatchFilter(String name) {
        if ((name == null) || (name.isEmpty())) {
            logger.warn("empty name for spot match filter");
            return null;
        }
        DexterParamsXMLParser.SpotFilter spotter = spotFilters.get(name);
        if (spotter == null) {
            logger.warn("cannot find spot filter named {}, skipping", name);
            return null;
        }
        SpotMatchFilter s = loader.getSpotFilter(spotter.getClazz());

        DexterLocalParams initParams = new DexterLocalParams();

        for (Param p : spotter.getParams().getParams()) {
            logger.info("adding param {} -> {}", p.getName(), p.getValue());
            initParams.addParam(p.getName(), p.getValue());
        }
        s.init(dexterParams, initParams);
        return s;
    }

    public Spotter getSpotter(String name) {
        if ((name == null) || (name.isEmpty())) {
            name = DEFAULT;
        }
        DexterParamsXMLParser.Spotter spotter = spotters.get(name);
        Spotter s = loader.getSpotter(spotter.getClazz());
        List<SpotMatchFilter> filters = new ArrayList<SpotMatchFilter>();
        for (DexterParamsXMLParser.Filter f : spotter.getFilters()) {
            SpotMatchFilter smf = getSpotMatchFilter(f.getName());
            filters.add(smf);
        }

        DexterLocalParams initParams = new DexterLocalParams();
        for (Param p : spotter.getParams().getParams()) {
            logger.info("adding param {} -> {}", p.getName(), p.getValue());
            initParams.addParam(p.getName(), p.getValue());
        }
        s.init(dexterParams, initParams);
        s.setFilters(filters);
        return s;
    }

    public Spotter getSpotter(String name, String tool) {
        if ((name == null) || (name.isEmpty())) {
            name = DEFAULT;
        }
        DexterParamsXMLParser.Spotter spotter = spotters.get(name);
        Spotter s = loader.getSpotter(spotter.getClazz());
        List<SpotMatchFilter> filters = new ArrayList<SpotMatchFilter>();
        for (DexterParamsXMLParser.Filter f : spotter.getFilters()) {
            SpotMatchFilter smf = getSpotMatchFilter(f.getName());
            filters.add(smf);
        }

        DexterLocalParams initParams = new DexterLocalParams();
        for (Param p : spotter.getParams().getParams()) {
            logger.info("adding param {} -> {}", p.getName(), p.getValue());
            if (p.getName().equalsIgnoreCase("tools")) {
                initParams.addParam("tools", tool);
            } else {
                initParams.addParam(p.getName(), p.getValue());
            }
        }
        s.init(dexterParams, initParams);
        s.setFilters(filters);
        return s;
    }

    public boolean hasDisambiguator(String name) {
        return disambiguators.containsKey(name);
    }

    public Disambiguator getDisambiguator(String name) {
        if ((name == null) || (name.isEmpty())) {
            name = DEFAULT;
        }
        DexterParamsXMLParser.Disambiguator disambiguator = disambiguators
                .get(name);
        Disambiguator s = loader.getDisambiguator(disambiguator.getClazz());

        DexterLocalParams initParams = new DexterLocalParams();
        for (Param p : disambiguator.getParams().getParams()) {
            logger.info("adding param {} -> {}", p.getName(), p.getValue());
            initParams.addParam(p.getName(), p.getValue());
        }
        s.init(dexterParams, initParams);
        return s;
    }

    //<fnoorala>
    public Disambiguator getDisambiguator(String name, String similarity) {
        if ((name == null) || (name.isEmpty())) {
            name = DEFAULT;
        }
        DexterParamsXMLParser.Disambiguator disambiguator = disambiguators
                .get(name);
        Disambiguator s = loader.getDisambiguator(disambiguator.getClazz());

        DexterLocalParams initParams = new DexterLocalParams();

        //<fnoorala>
        initParams.setSimilarity(similarity);
        //</fnoorala>

        for (Param p : disambiguator.getParams().getParams()) {
            logger.info("adding param {} -> {}", p.getName(), p.getValue());
            initParams.addParam(p.getName(), p.getValue());
        }
        s.init(dexterParams, initParams);
        return s;
    }

    public Disambiguator getDisambiguator(String name, String similarity, String relatdeness) {
        if ((name == null) || (name.isEmpty())) {
            name = DEFAULT;
        }
        DexterParamsXMLParser.Disambiguator disambiguator = disambiguators
                .get(name);
        Disambiguator s = loader.getDisambiguator(disambiguator.getClazz());

        DexterLocalParams initParams = new DexterLocalParams();

        //<fnoorala>
        initParams.setSimilarity(similarity);
        initParams.setRelatedness(relatdeness);
        //</fnoorala>

        for (Param p : disambiguator.getParams().getParams()) {
            logger.info("adding param {} -> {}", p.getName(), p.getValue());
            initParams.addParam(p.getName(), p.getValue());
        }
        s.init(dexterParams, initParams);
        return s;
    }
    //</fnoorala>

    public Float getThreshold(String name) {
        return thresholds.get(name);
    }

    public boolean hasTagger(String name) {
        return taggers.containsKey(name);
    }

    public Tagger getTagger(String name) {
        if ((name == null) || (name.isEmpty())) {
            name = DEFAULT;
        }

        DexterParamsXMLParser.Tagger t = taggers.get(name);

       // Tagger tagger = new StandardTagger(name, getSpotter(t.getSpotter()),getDisambiguator(t.getDisambiguator()));
        //<fnoorla>
        // Tagger tagger = new StandardTagger(name, getSpotter(t.getSpotter()),getDisambiguator(t.getDisambiguator(),t.similarity));
        Tagger tagger = new StandardTagger(name, getSpotter(t.getSpotter()),
                getDisambiguator(t.getDisambiguator(), t.getSimilarities(), t.getRelatedness()));
        //<fnoorla>

        DexterLocalParams initParams = new DexterLocalParams();

        for (Param p : t.getParams().getParams()) {
            logger.info("adding param {} -> {}", p.getName(), p.getValue());
            initParams.addParam(p.getName(), p.getValue());
        }
        tagger.init(dexterParams, initParams);
        return tagger;
    }

    public DexterParamsXMLParser.Similarity getSimilarity(String name) {
        if ((name == null) || (name.isEmpty())) {
            name = DEFAULT;
        }
        DexterParamsXMLParser.Similarity sim = similarities.get(name);

        return sim;
    }

    public Relatedness getRelatedness(String name) {
        if ((name == null) || (name.isEmpty())) {
            name = DEFAULT;
        }
        logger.info("Selected relatedness function: {}", relatednessFunctions.get(name));
        return loader.getRelatedness(relatednessFunctions.get(name));
    }

    //<fnoorala>
    public Relatedness getRelatedness(String name, boolean True) {
        if ((name == null) || (name.isEmpty())) {
            name = DEFAULT;
        }

        logger.info("Selected relatedness function: {}", relatednessFunctions.get(name));
        return loader.getRelatedness(relatednessFunctions.get(name));

    }

    public Similarity getLabelSimilarity(String name) {
        if ((name == null) || (name.isEmpty())) {
            name = DEFAULT;
        }

        logger.info("Selected label similarity function: {}", labelsimilarities.get(name).getName());

        return loader.getSimilarity(labelsimilarities.get(name).getClazz());
    }

    public Similarity getContextSimilarity(String name) {
        if ((name == null) || (name.isEmpty())) {
            name = DEFAULT;
        }

        logger.info("Selected context similarity function: {}", contextsimilarities.get(name).getName());

        return loader.getSimilarity(contextsimilarities.get(name).getClazz());
    }
    //</fnoorala>

    public File getDefaultModel() {
        return defaultModel;
    }

    public File getDefaultNlpModel() {
        return defaultnlpModel;
    }

    public File getGraphDir() {
        return graphDir;
    }

    public File getLabelDir() {
        return labelDir;
    }

    public File getGraph(String string, Direction direction) {
        return new File(getGraphDir(), graphs.get(string).get(direction));
    }

    public File getIndexDir() {
        return indexDir;
    }

    public File getWikiToIdFile() {
        return wikiToIdFile;
    }

    public File getSpotsOffsetData() {
        return spotsOffsetData;
    }

    public File getEntityToSpots() {
        return entityToSpots;
    }

    public File getSpotsEliasFano() {
        return spotsEliasFano;
    }

    public File getSpotsPerfectHash() {
        return spotsPerfectHash;
    }

    public File getPlainSpots() {
        return plainSpots;

    }

}
