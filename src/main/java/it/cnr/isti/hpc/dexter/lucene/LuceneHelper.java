package it.cnr.isti.hpc.dexter.lucene;

/**
 * Copyright 2012 Salvatore Trani
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
import it.cnr.isti.hpc.dexter.entity.Entity;
import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.QuotesCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.UnderscoreCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.UnicodeCleaner;

import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.log.ProgressLogger;
import it.cnr.isti.hpc.text.Text;
import it.cnr.isti.hpc.wikipedia.article.Article;
import it.cnr.isti.hpc.wikipedia.article.ArticleSummarizer;
import it.cnr.isti.hpc.wikipedia.article.Link;
import it.cnr.isti.hpc.wikipedia.article.Template;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVectorFormat;
import org.apache.commons.math3.linear.SparseRealVector;
import org.apache.lucene.analysis.core.StopAnalyzer;

import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.lucene.store.RAMDirectory;

/**
 * LuceneHelper provides utilities for indexing, retrieving, and ranking
 * Wikipedia articles.
 *
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 *
 * Created on Aug 27, 2013
 */
public class LuceneHelper {

    private static final String LUCENE_ARTICLE_DEFAULT_FIELD = "content";

    private static final String LUCENE_ARTICLE_ID = "wiki-id";
    private static final String LUCENE_ARTICLE_WIKI_TITLE = "wiki-title";
    private static final String LUCENE_ARTICLE_TITLE = "title";
    private static final String LUCENE_ARTICLE_TYPE = "type";
    private static final String LUCENE_ARTICLE_LIST = "list";
    private static final String LUCENE_ARTICLE_INFOBOX = "infobox";
    private static final String LUCENE_ARTICLE_EMPH = "emph";
    private static final String LUCENE_ARTICLE_SECTIONS = "sections";
    private static final String LUCENE_ARTICLE_DESCRIPTIONS = "desc";
    private static final String LUCENE_ARTICLE_LINKS = "link";
    private static final String LUCENE_ARTICLE_CONTENT = "content";
    private static final String LUCENE_ARTICLE_SUMMARY = "summary";

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(LuceneHelper.class);

    /**
     * The Lucene analyzer
     */
    private StandardAnalyzer EN_ANALYZER = new StandardAnalyzer(Version.LUCENE_41, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    //<fnoorala>
    private static String model;

    private FrenchAnalyzer FR_ANALYZER = new FrenchAnalyzer(Version.LUCENE_41, FrenchAnalyzer.getDefaultStopSet());
    //</fnoorala>
    /**
     * Singleton
     */
    private static LuceneHelper dexterHelper;

    private Directory index;
    private IndexWriter writer;
    private IndexWriter writerRAM;
    private IndexSearcher searcher;
    private final IndexWriterConfig config;
    private final ArticleSummarizer summarizer;

    // Construct a RAMDirectory to hold the in-memory representation
    // of the index.
    private Directory idx;

    private static DexterParams params = DexterParams.getInstance();

    /**
     * number of documents indexed
     */
    private final int collectionSize;

    public static final FieldType STORE_TERM_VECTORS = new FieldType();
    public static final FieldType STORE_TERM_VECTORS_NOT_STORED = new FieldType();

    String uri = params.getSparqlEndPointPath().replaceAll("sparql", "") + "resource";
    static {
        STORE_TERM_VECTORS.setIndexed(true);
        STORE_TERM_VECTORS.setTokenized(true);
        STORE_TERM_VECTORS.setStored(true);
        STORE_TERM_VECTORS.setStoreTermVectors(true);
        STORE_TERM_VECTORS.freeze();

        STORE_TERM_VECTORS_NOT_STORED.setIndexed(true);
        STORE_TERM_VECTORS_NOT_STORED.setTokenized(true);
        STORE_TERM_VECTORS_NOT_STORED.setStored(false);
        STORE_TERM_VECTORS_NOT_STORED.setStoreTermVectors(true);
        STORE_TERM_VECTORS_NOT_STORED.freeze();
    }

    private static SpotManager cleaner = new SpotManager();
    //<fnoorala>
    private static SpotManager content_cleaner = new SpotManager();
    private static SpotManager sm = SpotManager.getStandardSpotManager();

    static {
        content_cleaner.add(new UnderscoreCleaner());
        content_cleaner.add(new QuotesCleaner());
    }
    //</fnoorala>
    private final File wikiIdtToLuceneIdSerialization;
    private static Map<Integer, Integer> wikiIdToLuceneId;

    static {
        cleaner.add(new UnicodeCleaner());
        cleaner.add(new UnderscoreCleaner());
        cleaner.add(new QuotesCleaner());
    }

    /**
     * Opens or creates a lucene index in the given directory
     *
     * @param wikiIdtToLuceneIdSerialization - the file containing the
     * serialized mapping between wiki-id and Lucene documents ids
     *
     * @param indexPath - the path of the directory with the Lucene's index
     */
    protected LuceneHelper(File wikiIdtToLuceneIdSerialization, File indexPath) {

        logger.info("opening lucene index in folder {}", indexPath);

        if (model.equalsIgnoreCase("fr")) {

            config = new IndexWriterConfig(Version.LUCENE_41, FR_ANALYZER);

        } else {
            config = new IndexWriterConfig(Version.LUCENE_41, EN_ANALYZER);
        }

        this.wikiIdtToLuceneIdSerialization = wikiIdtToLuceneIdSerialization;

        BooleanQuery.setMaxClauseCount(1000);

        try {
            
            index = FSDirectory.open(indexPath);

            //<fnoorala>
            idx = new RAMDirectory();
            //</fnoorala>

            // writer.commit();
        } catch (Exception e) {
            logger.error("opening the index: {}", e.toString());
            System.exit(1);
        }

        summarizer = new ArticleSummarizer();
        writer = getWriter();

        collectionSize = writer.numDocs();
        wikiIdToLuceneId = Collections.emptyMap();
    }

    /**
     * @return an index reader
     */
    private IndexReader getReader() {
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(index);
        } catch (Exception e) {
            logger.error("reading the index: {} ", e.toString());
            System.exit(1);
        }
        return reader;
    }

    /**
     * @return an index reader from RAMDirectory
     *
     * <fnoorala> </fnoorala>
     */
    private IndexReader getReaderFromRAM() {
        IndexReader readerRAM = null;
        try {
            readerRAM = DirectoryReader.open(idx);
        } catch (Exception e) {
            logger.error("reading the index: {} ", e.toString());
            System.exit(1);
        }
        return readerRAM;
    }

    private IndexSearcher getSearcher() {
        if (searcher != null) {
            return searcher;
        }
        IndexReader reader = getReader();
        searcher = new IndexSearcher(reader);
        return searcher;
    }

    /**
     * @return true if the dexter lucene index exists, false otherwise
     */
    public static boolean hasDexterLuceneIndex() {
        File luceneFolder = params.getIndexDir();
        return luceneFolder.exists();
    }

    /**
     * Returns an instance of the Dexter's Lucene index.
     *
     * @return an instance of the Dexter's Lucene index
     */
    public static LuceneHelper getDexterLuceneHelper() {
        if (dexterHelper == null) {
            model = params.getDefaultModel().getName();
            System.out.println(params.getIndexDir().getPath());
            File luceneFolder = params.getIndexDir();
            File serializedWikiFile = params.getWikiToIdFile();
            System.out.println(params.getWikiToIdFile().getPath());
            dexterHelper = new LuceneHelper(serializedWikiFile, luceneFolder);
        }
        return dexterHelper;
    }

    /**
     * Loads the map containing the conversion from the Wikipedia ids to the
     * Lucene Ids.
     */
    protected void parseWikiIdToLuceneId() {
        logger.warn("no index wikiID -> lucene found - I'll generate");
        IndexReader reader = getReader();
        wikiIdToLuceneId = new HashMap<Integer, Integer>(reader.numDocs());
        ProgressLogger pl = new ProgressLogger(
                "creating wiki2lucene, readed {} docs", 100000);
        int numDocs = reader.numDocs();
        for (int i = 0; i < numDocs; i++) {
            pl.up();
            try {
                Document doc = reader.document(i);
                IndexableField f = doc.getField(LUCENE_ARTICLE_ID);
                Integer wikiId = new Integer(f.stringValue());
                wikiIdToLuceneId.put(wikiId, i);
            } catch (CorruptIndexException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    /**
     * Dumps the map containing the conversion from the Wikipedia ids to the
     * Lucene Ids.
     */
    protected void dumpWikiIdToLuceneId() {

        try {
            // Serializes to a file
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(
                    wikiIdtToLuceneIdSerialization));
            out.writeObject(wikiIdToLuceneId);
            out.close();
        } catch (IOException e) {
            logger.info("dumping incoming links in a file ({})", e.toString());
            System.exit(-1);
        }
    }

    /**
     * Loads the map containing the conversion from the Wikipedia ids to the
     * Lucene Ids.
     */
    @SuppressWarnings("unchecked")
    public void loadWikiIdToLuceneId() {

        if (!wikiIdtToLuceneIdSerialization.exists()) {
            logger.info("{} not exists, generating",
                    wikiIdtToLuceneIdSerialization);
            parseWikiIdToLuceneId();
            logger.info("storing");
            dumpWikiIdToLuceneId();
            return;
        }

        logger.info("loading wiki id to lucene id ");
        try {

            InputStream is = new BufferedInputStream(new FileInputStream(
                    wikiIdtToLuceneIdSerialization));
            @SuppressWarnings("resource")
            ObjectInput oi = new ObjectInputStream(is);
            wikiIdToLuceneId = (Map<Integer, Integer>) oi.readObject();

        } catch (Exception e) {
            logger.info("reading serialized object ({})", e.toString());
            System.exit(-1);
        }
        logger.info("done ");
    }

    /**
     * @return the Lucene id of an article, given its wikiId
     */
    public int getLuceneId(int wikiId) {
       
        if (wikiIdToLuceneId.isEmpty()) {
            loadWikiIdToLuceneId();
        }

        if (!wikiIdToLuceneId.containsKey(wikiId)) {
            return -1;
        }
        return wikiIdToLuceneId.get(wikiId);
    }

    /**
     * Returns the TFIDF-similarity between a given string and an article
     *
     * @param query - the query containing the query to compare with the article
     * @param wikiId - the id of the article to compare with the query
     * @return the TFIDF-similarity between the query and wikiId
     */
    public float getSimilarity(Query query, int wikiId) {
        searcher = getSearcher();
        int docId = getLuceneId(wikiId);
        Explanation e = null;
        try {
            e = searcher.explain(query, docId);
        } catch (IOException e1) {
            logger.error("getting similarity between text and doc {} ", wikiId);
            return 0;
        }
        return e.getValue();
    }

    /**
     * Returns the cosine similarity between two documents
     *
     * @param x - the WikiId of the first document
     * @param y - the WikiId of the second document
     *
     * @return a double between 0 (not similar) and 1 (same content),
     * representing the similarity between the 2 documents
     */
    public double getCosineSimilarity(int x, int y) {
        return getCosineSimilarity(x, y, LUCENE_ARTICLE_DEFAULT_FIELD);
    }

    /**
     * Returns the cosine similarity between two documents
     *
     * @param x - the WikiId of the first document
     * @param y - the WikiId of the second document
     * @param field - the field on which to compute the similarity
     *
     * @return a double between 0 (not similar) and 1 (same content),
     * representing the similarity between the 2 documents
     */
    public double getCosineSimilarity(int x, int y, String field) {

        IndexReader reader = getReader();
        Terms tfvX = null;
        Terms tfvY = null;
        try {
            tfvX = reader.getTermVector(getLuceneId(x), field);
            tfvY = reader.getTermVector(getLuceneId(y), field);

            // try {
            // tfvX = reader.document(idX).getBinaryValue("asd")
            // getTermFreqVectors(idX);
            // tfvY = reader.getTermFreqVectors(idY);
        } catch (IOException e) {
            logger.error("computing cosine similarity ({}) ", e.toString());
            System.exit(-1);
        }

        Map<String, Integer> xfrequencies = new HashMap<String, Integer>();
        Map<String, Integer> yfrequencies = new HashMap<String, Integer>();
        TermsEnum xtermsEnum = null;
        try {

            xtermsEnum = tfvX.iterator(null);

            BytesRef text;

            while ((text = xtermsEnum.next()) != null) {
                String term = text.utf8ToString();
                int freq = (int) xtermsEnum.totalTermFreq();
                xfrequencies.put(term, freq);
            }

            TermsEnum ytermsEnum = tfvY.iterator(null);
            while ((text = ytermsEnum.next()) != null) {
                String term = text.utf8ToString();
                int freq = (int) ytermsEnum.totalTermFreq();
                yfrequencies.put(term, freq);
            }

        } catch (IOException e) {
            logger.error("computing cosine similarity ({}) ", e.toString());
            System.exit(-1);
        }
        Map<String, Double> xTfidf = new HashMap<String, Double>();
        Map<String, Double> yTfidf = new HashMap<String, Double>();
        double xnorm = tfidfVector(xTfidf, xfrequencies, field, reader);
        double ynorm = tfidfVector(yTfidf, yfrequencies, field, reader);

        double dotproduct = 0;

        for (Map.Entry<String, Double> k : xTfidf.entrySet()) {
            if (yTfidf.containsKey(k.getKey())) {
                logger.info("key {} ", k.getKey());
                logger.info("key x {} y {} ", k.getValue(),
                        yTfidf.get(k.getKey()));
                dotproduct += k.getValue() * yTfidf.get(k.getKey());
                logger.info("dotproduct {} ", dotproduct);
            }

        }
        return dotproduct / (xnorm * ynorm);

    }

    /**
     * Builds the TFIDF vector and its norm2
     *
     * @param tfidf - the vector containing for each term its TFIDF score, it
     * will be populated by this method
     * @param freq - the vector containing for each term its frequency
     * @param field - the field on which to compute the inverse document
     * frequency
     *
     * @return the norm of the TFIDF vector
     *
     */
    private double tfidfVector(Map<String, Double> tfidf,
            Map<String, Integer> freq, String field, IndexReader reader) {

        double norm = 0;
        for (Map.Entry<String, Integer> entry : freq.entrySet()) {
            Term t = new Term(field, entry.getKey());
            int df = 0;
            try {
                df = reader.docFreq(t);
            } catch (IOException e) {
                logger.error("computing tfidfVector ({}) ", e.toString());
                System.exit(-1);
            }
            double idf = Math.log(collectionSize / (double) df + 1)
                    / Math.log(2) + 1;
            double tfidfValue = entry.getValue() * idf;
            norm += tfidfValue * tfidfValue;
            tfidf.put(entry.getKey(), tfidfValue);
        }
        return Math.sqrt(norm);

    }

    /**
     * Converts an article to a Lucene Index
     *
     * @param a - a Wikipedia Article to index
     * @return the Lucene Document representing the Wikipedia Article
     */
    private Document toLuceneDocument(Article a) {
        Document d = new Document();
        d.add(new TextField(LUCENE_ARTICLE_TITLE, a.getTitle(), Field.Store.YES));
        d.add(new IntField(LUCENE_ARTICLE_ID, a.getWid(), Field.Store.YES));
        d.add(new StringField(LUCENE_ARTICLE_WIKI_TITLE, a.getWikiTitle(),
                Field.Store.YES));
        d.add(new StringField(LUCENE_ARTICLE_TYPE, String.valueOf(a.getType()),
                Field.Store.YES));
        for (List<String> l : a.getLists()) {
            for (String e : l) {
                d.add(new TextField(LUCENE_ARTICLE_LIST, e, Field.Store.NO));
            }
        }
        Template t = a.getInfobox();
        d.add(new TextField(LUCENE_ARTICLE_INFOBOX, t.getName(),
                Field.Store.YES));
        for (String e : t.getDescription()) {
            d.add(new TextField(LUCENE_ARTICLE_INFOBOX, e, Field.Store.YES));
        }
        for (String e : a.getHighlights()) {
            d.add(new Field(LUCENE_ARTICLE_EMPH, e, STORE_TERM_VECTORS));
        }
        for (String e : a.getSections()) {
            d.add(new TextField(LUCENE_ARTICLE_SECTIONS, e, Field.Store.NO));
        }

        for (Link e : a.getLinks()) {
            d.add(new Field(LUCENE_ARTICLE_DESCRIPTIONS, cleaner.clean(e
                    .getDescription()), STORE_TERM_VECTORS));
            d.add(new Field(LUCENE_ARTICLE_LINKS, cleaner.clean(e.getCleanId()
                    .replace('_', ' ')), STORE_TERM_VECTORS));

        }

        d.add(new Field(LUCENE_ARTICLE_CONTENT, content_cleaner.clean(a.getText()),
                STORE_TERM_VECTORS));
        d.add(new Field(LUCENE_ARTICLE_SUMMARY, summarizer.getSummary(a),
                STORE_TERM_VECTORS));
        return d;
    }

    /**
     * Indexes a Wikipedia Article
     *
     * @param a the article to index
     */
    public void addDocument(Article a) {
        writer = getWriter();
        logger.debug("add doc {} ", a.getTitle());
        try {
            writer.addDocument(toLuceneDocument(a));
            // writer.addDocument(doc);
        } catch (Exception e) {
            logger.error("exception indexing a document: {} ({})",
                    a.getTitle(), e.toString());
            e.printStackTrace();
            System.exit(1);
        }
        logger.debug("added doc {}", a.getWid());
    }

    /**
     * Adds a Wikipedia Article (added just for testing)
     *
     * @param id - the id of the Wikipedia Article
     * @param content - the text of the Wikipedia Article
     */
    protected void addDocument(int id, String content) {
        Article a = new Article();
        a.setWid(id);
        a.setParagraphs(Arrays.asList(content));
        addDocument(a);
    }

    /**
     * Clears the index
     */
    public void clearIndex() {
        logger.info("delete all the documents indexed");
        try {
            writer.deleteAll();
            writer.commit();
        } catch (IOException e) {
            logger.error("deleting the index: {}", e.toString());
            System.exit(1);
        }
    }

    /**
     * Clears the index on RAM
     */
    public void clearRAMIndex() {
        logger.info("delete all the documents indexed");
        try {
            writerRAM = new IndexWriter(idx, config);
            writerRAM.deleteAll();
            writerRAM.commit();
            writerRAM.close();
        } catch (IOException e) {
            logger.error("deleting the index: {}", e.toString());
            System.exit(1);
        }
    }

    public void commit() {
        try {
            writer.commit();
            // logger.info("commited, index contains {} documents", writer
            // .getReader().numDocs());
        } catch (Exception e) {
            logger.error("committing: {}", e.toString());
            System.exit(1);
        }

    }

    public Document getDoc(int wikiId) {
        IndexReader reader = getReader();

        // System.out.println("get docId "+pos);
        if (wikiId <= 0) {
            return null;
        }
        int docId = getLuceneId(wikiId);
        if (docId < 0) {
            logger.warn("no id for wikiId {}", wikiId);

            return null;
        }
        logger.debug("get wikiId {}  ->  docId {}", wikiId, docId);
        Document doc = null;
        try {
            doc = reader.document(docId);
        } catch (Exception e) {
            logger.error("retrieving doc in position {} {}", docId,
                    e.toString());
            System.exit(-1);
        }

        return doc;
    }

    /**
     * @param query - a query
     * @param field - the field where to search the query
     * @return number of documents containing the text in query in the given
     * fields
     */
    public int getFreq(String query, String field) {
        Query q = null;
        searcher = getSearcher();
        TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);

        // try {
        Text t = new Text(query).disableStopwords();
        PhraseQuery pq = new PhraseQuery();
        int i = 0;
        for (String term : t.getTerms()) {
            pq.add(new Term(field, term), i++);
        }
        q = pq;
        logger.debug(q.toString());
        // } catch (ParseException e) {
        // logger.error("querying the index: {} ", e.toString());
        // return -1;
        // }
        try {
            searcher.search(q, collector);
        } catch (IOException e) {
            logger.error("querying the index: {} ", e.toString());
            return -1;
        }
        return collector.getTotalHits();
    }

    /**
     * @param query - a query
     * @param field - the field on which to perform the query
     * @return number of documents containing the text in query in the given
     * fields
     */
    public int getFreq(String query) {
        return getFreq(query, LUCENE_ARTICLE_DEFAULT_FIELD);
    }

    public int getFreqFromSummary(String query) {
        return getFreq(query, LUCENE_ARTICLE_SUMMARY);
    }

    //<fnoorala>
    public int getFreqFromContent(String query) {
        return getFreq(query, LUCENE_ARTICLE_CONTENT);
    }

    private IndexWriter getWriterRAM() {
        if (writerRAM == null) {
            try {
                writerRAM = new IndexWriter(idx, config);
            } catch (CorruptIndexException e1) {
                logger.error("creating the index: {}", e1.toString());
                System.exit(-1);
            } catch (LockObtainFailedException e1) {
                logger.error("creating the index: {}", e1.toString());
                System.exit(-1);
            } catch (IOException e1) {
                logger.error("creating the index: {}", e1.toString());
                System.exit(-1);
            }
        }
        return writerRAM;
    }

    //</fnoorala>
    private IndexWriter getWriter() {
        if (writer == null) {
            try {
                writer = new IndexWriter(index, config);
            } catch (CorruptIndexException e1) {
                logger.error("creating the index: {}", e1.toString());
                System.exit(-1);
            } catch (LockObtainFailedException e1) {
                logger.error("creating the index: {}", e1.toString());
                System.exit(-1);
            } catch (IOException e1) {
                logger.error("creating the index: {}", e1.toString());
                System.exit(-1);
            }
        }
        return writer;
    }

    /**
     * @return the number of documents indexed
     */
    public int numDocs() {
        IndexReader reader = getReader();

        return reader.numDocs();

    }

    public void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            logger.error("closing the writer: {}", e.toString());
            System.exit(-1);
        }
    }

    /**
     * @param query entities containing the text of the query as a phrase (terms
     * consecutive) will be be returned.
     * @param field the field where the query must be performed (summary,
     * content, title ..).
     * @param n the max number of results to produce.
     * @return the top wiki-id matching the query
     */
    public List<Integer> query(String query, String field, int n) {
        searcher = getSearcher();
        TopScoreDocCollector collector = TopScoreDocCollector.create(n, true);
        List<Integer> results = new ArrayList<Integer>();
        Query q = null;

        try {
            
            
            if (model.equalsIgnoreCase("fr")) {
                q = new QueryParser(Version.LUCENE_41, field, new FrenchAnalyzer(Version.LUCENE_41, FrenchAnalyzer.getDefaultStopSet())).parse("\"" + query + "\"");
            } else {
                q = new QueryParser(Version.LUCENE_41, field, new StandardAnalyzer(Version.LUCENE_41, StopAnalyzer.ENGLISH_STOP_WORDS_SET)).parse("\"" + query + "\"");
                    //parse(QueryParser.escape(query));
            }
            
        } catch (ParseException e) {
            logger.error("querying the index: {} ", e.toString());
            return results;
        }

        try {
            searcher.search(q, collector);
        } catch (IOException e) {
            logger.error("querying the index: {} ", e.toString());
            return results;
        }

        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            results.add(getWikiId(docId));
        }

        logger.debug("query {} docs {}", query, results);
        return results;
    }

    
    
    
    /**
     * @param query entities containing the text of the query as a phrase (terms
     * consecutive) will be be returned.
     * @param field the field where the query must be performed (summary,
     * content, title ..).
     * @param n the max number of results to produce.
     * @return the top wiki-id matching the query (returns max 10000 wiki-id );
     */
    public List<Integer> query(String query, String field) {
        return query(query, field, 10000);
    }

    /**
     * @return the top Lucene Document ids matching the query
     */
    public List<Integer> query(String query) {
        return query(query, LUCENE_ARTICLE_DEFAULT_FIELD);
    }

    
   
    
    /**
     * Retrieves an article from the index
     *
     * @param id - the Wikipedia Id of the Article
     * @return the document from the index
     */
    public Article getArticle(int id) {
        Article a = new Article();
        a.setWikiId(id);

        Document d = getDoc(id);
        if (d != null) {
            List<String> paragraphs = new ArrayList<String>();
            paragraphs.add(d.getField(LUCENE_ARTICLE_CONTENT).stringValue());
            a.setTitle(d.getField(LUCENE_ARTICLE_TITLE).stringValue());
            a.setWikiTitle(d.getField(LUCENE_ARTICLE_WIKI_TITLE).stringValue());
            a.setSummary(d.getField(LUCENE_ARTICLE_SUMMARY).stringValue());

            a.setParagraphs(paragraphs);
        }
        //
        return a;

    }

    
    
    
    /**
     * Retrieves only the article summary and the title from the index
     *
     * @param id - the Wikipedia Id of the Article
     * @return the document from the index
     */
    public Article getArticleSummary(int id) {
        Article a = new Article();
        a.setWikiId(id);

        Document d = getDoc(id);
        if (d != null) {
            a.setWikiTitle(d.getField(LUCENE_ARTICLE_WIKI_TITLE).stringValue());
            a.setTitle(d.getField(LUCENE_ARTICLE_TITLE).stringValue());
            a.setSummary(d.getField(LUCENE_ARTICLE_SUMMARY).stringValue());

        }
        //
        return a;

    }

    public int getWikiId(int luceneId) {
        IndexReader reader = getReader();

        // System.out.println("get docId "+pos);
        Document doc = null;
        try {
            doc = reader.document(luceneId);
        } catch (Exception e) {
            logger.error("retrieving doc in position {} {}", luceneId,
                    e.toString());
            System.exit(-1);
        }
        return Integer.parseInt(doc.get(LUCENE_ARTICLE_ID));
    }

    /**
     *
     * Sorts a list of entities by their similarity with the string context.
     *
     * @param spot - the spot for which the entities are sorted
     * @param eml - the entity list to sort
     * @param context - the context text, entities are sorted based on their
     * similarity with the context.
     * @param field - sort the entity based on the similarity between their text
     * in this field and the context.
     *
     */
    @SuppressWarnings("null")
    public void rankBySimilarity(SpotMatch spot, EntityMatchList eml,
            String context, String field) {

        if (context.trim().isEmpty()) {
            logger.warn("no context for spot {}", spot.getMention());
            return;
        }

        Query q = null;

        try {
            // removing all not alphanumerical chars
            context = context.replaceAll("[^A-Za-z0-9 ]", " ");

            // q = new QueryParser(Version.LUCENE_41, "content",
            //new StandardAnalyzer(Version.LUCENE_41)).parse(QueryParser
            //.escape(context));
            //<fnoorala>
            q = new QueryParser(Version.LUCENE_41, "content",
                    new FrenchAnalyzer(Version.LUCENE_41, FrenchAnalyzer.getDefaultStopSet())).parse(QueryParser
                            .escape(context));
            //</fnoorala>

        } catch (ParseException e) {
            logger.error("querying the index: {} ", e.toString());
            logger.error("clauses = {} ",
                    ((BooleanQuery) q).getClauses().length);
            return;
        }

        for (EntityMatch e : eml) {
            Integer luceneId = getLuceneId(e.getId());
            float score = 0.5f;
            // smoothing
            if (luceneId == null || luceneId < 0) {
                // logger.warn("no docs in lucene for wiki id {}, ignoring",
                // e.id());
            } else {
                score += getSimilarity(q, e.getId());

            }
            e.setScore(score);
        }

        return;

    }

    /**
     *
     * Sorts a list of entities by their similarity (full text) with the string
     * context.
     *
     * @param spot - the spot for which the entities are sorted
     * @param eml - the entity list to sort
     * @param context - the context text, entities are sorted based on their
     * similarity with the context.
     *
     *
     */
    public void rankBySimilarity(SpotMatch spot, EntityMatchList eml,
            String context) {
        rankBySimilarity(spot, eml, context, LUCENE_ARTICLE_DEFAULT_FIELD);
        return;

    }

    public void getContextSimilarity(SpotMatch spot, EntityMatchList eml,
            String context) {

        String field = LUCENE_ARTICLE_DEFAULT_FIELD;
        if (context.trim().isEmpty()) {
            logger.warn("no context for spot {}", spot.getMention());
            return;
        }

        Query q = null;

        try {

            q = new QueryParser(Version.LUCENE_41, "content",
                    new FrenchAnalyzer(Version.LUCENE_41, FrenchAnalyzer.getDefaultStopSet())).parse(QueryParser
                            .escape(context));

        } catch (ParseException e) {
            logger.error("querying the index: {} ", e.toString());
            logger.error("clauses = {} ",
                    ((BooleanQuery) q).getClauses().length);
            return;
        }

        for (EntityMatch e : eml) {
            Integer luceneId = getLuceneId(e.getId());
            float score = 0.5f;
            // smoothing
            if (luceneId == null || luceneId < 0) {
                // logger.warn("no docs in lucene for wiki id {}, ignoring",
                // e.id());
            } else {
                score += getSimilarity(q, e.getId());

            }
            e.setContextScore(score);
        }

        return;

    }

    public double getContextSimilarity(SpotMatch spot, EntityMatch e,
            String context) {

        if (context.trim().isEmpty()) {
            logger.warn("no context for spot {}", spot.getMention());
            return 0;
        }

        Query q = null;

        try {

            // removing all not alphanumerical chars
            //context = context.replaceAll("[^A-Za-z0-9 ]", " ");
            if (model.equalsIgnoreCase("fr")) {
                q = new QueryParser(Version.LUCENE_41, "content",
                        new FrenchAnalyzer(Version.LUCENE_41, FrenchAnalyzer.getDefaultStopSet())).parse(QueryParser
                                .escape(context));
            } else {
                q = new QueryParser(Version.LUCENE_41, "content",
                        new StandardAnalyzer(Version.LUCENE_41, StopAnalyzer.ENGLISH_STOP_WORDS_SET)).parse(QueryParser
                                .escape(context));
            }

        } catch (ParseException exeption) {
            logger.error("querying the index: {} ", exeption.toString());
            logger.error("clauses = {} ",
                    ((BooleanQuery) q).getClauses().length);
            return 0;
        }

        Integer luceneId = getLuceneId(e.getId());
        float score = 0.5f;
        // smoothing
        if (luceneId == null || luceneId < 0) {
            // logger.warn("no docs in lucene for wiki id {}, ignoring",
            // e.id());
        } else {
            score += getSimilarity(q, e.getId());

        }

        return score;

    }

    public SpotMatch getConsinSimilarity(SpotMatch spot) {

        SpotMatch s = null;

        String field = LUCENE_ARTICLE_SUMMARY;
        EntityMatchList eml = new EntityMatchList();

        if (spot.getContext().trim().isEmpty()) {
            logger.warn("no context for spot {}", spot.getMention());
            return s;
        }

        //index the spot context on RAM 
        clearRAMIndex();
        addDocumentRAM(spot);

        BytesRef text;

        IndexReader readerRAM = getReaderFromRAM();

        Terms tfvX = null;
        try {
            tfvX = readerRAM.getTermVector(0, "context");

        } catch (IOException e) {
            logger.error("computing cosine similarity ({}) ", e.toString());
            System.exit(-1);
        }

        Map<String, Integer> xfrequencies = new HashMap<String, Integer>();

        TermsEnum xtermsEnum = null;

        try {
            xtermsEnum = tfvX.iterator(null);

            while ((text = xtermsEnum.next()) != null) {
                String term = text.utf8ToString();
                int freq = (int) xtermsEnum.totalTermFreq();
                xfrequencies.put(term, freq);
            }

        } catch (IOException e) {
            logger.error("computing cosine similarity ({}) ", e.toString());
            System.exit(-1);
        }

        Map<String, Double> xTfidf = new HashMap<String, Double>();

        double xnorm = tfidfVector(xTfidf, xfrequencies, "context", readerRAM);

        IndexReader reader = getReader();

        for (Entity entity : spot.getSpot().getEntities()) {

            Terms tfvY = null;
            EntityMatch match = null;

            try {

                tfvY = reader.getTermVector(getLuceneId(entity.getId()), field);

                if (tfvY == null) {
                    continue;
                }

            } catch (IOException e) {
                logger.error("computing cosine similarity ({}) ", e.toString());
                System.exit(-1);
            }

            Map<String, Integer> yfrequencies = new HashMap<String, Integer>();

            try {

                TermsEnum ytermsEnum = tfvY.iterator(null);
                while ((text = ytermsEnum.next()) != null) {
                    String term = text.utf8ToString();
                    int freq = (int) ytermsEnum.totalTermFreq();
                  
                    yfrequencies.put(term, freq);
                }

            } catch (IOException e) {
                logger.error("computing cosine similarity ({}) ", e.toString());
                System.exit(-1);
            }

            Map<String, Double> yTfidf = new HashMap<String, Double>();

            double ynorm = tfidfVector(yTfidf, yfrequencies, field, reader);

            double dotproduct = 0;

            for (Map.Entry<String, Double> k : xTfidf.entrySet()) {
                if (yTfidf.containsKey(k.getKey())) {
                    logger.info("key {} ", k.getKey());
                    logger.info("key x {} y {} ", k.getValue(),
                            yTfidf.get(k.getKey()));
                    dotproduct += k.getValue() * yTfidf.get(k.getKey());
                    logger.info("dotproduct {} ", dotproduct);
                }
            }

            double sim = dotproduct / (xnorm * ynorm);

            match = new EntityMatch(entity, spot.getEntityCommonness(entity), sim, spot);
            eml.add(match);

        }

        s = new SpotMatch(spot.getSpot(), spot.getField(), spot.getStart(), spot.getEnd(), spot.getPos(), spot.getContext());
        s.setEntities(eml);

        return s;

    }

    /**
     * Indexes a SpotMatch based on RAMDirectory indexing
     *
     * @param spot the SpotMatch to index
     */
    public void addDocumentRAM(SpotMatch spot) {
        try {
            writerRAM = new IndexWriter(idx, config);

            logger.debug("add doc in RAM {} ", spot.getMention());

            writerRAM.addDocument(toLuceneDocument(spot));
            //close the writer to finish building the index
            writerRAM.close();
            // writer.addDocument(doc);
        } catch (Exception e) {
            logger.error("exception indexing a SpotMatch: {} ({})",
                    spot.getMention(), e.toString());
            e.printStackTrace();
            System.exit(1);
        }
        logger.debug("added doc in RAM {} ", spot.getMention());
    }

    private Document toLuceneDocument(SpotMatch spot) {
        Document d = new Document();
        d.add(new IntField("id", 0, Field.Store.YES));
        d.add(new TextField("spot", spot.getMention(), Field.Store.NO));
        d.add(new Field("context", content_cleaner.clean(spot.getContext()),
                STORE_TERM_VECTORS_NOT_STORED));

        return d;
    }

    private double getTFIDFCosineSimilarity(DocVector d1, DocVector d2) {

        return (d1.tfidf.dotProduct(d2.tfidf))
                / (d1.tfidf.getNorm() * d2.tfidf.getNorm());

    }

    private double getCosineSimilarity(DocVector d1, DocVector d2) {

        return (d1.vector.dotProduct(d2.vector))
                / (d1.vector.getNorm() * d2.vector.getNorm());

    }

    private class DocVector {

        public Map<String, Integer> terms;
        public SparseRealVector vector;
        public Map<String, Integer> targetTerms;

        public SparseRealVector tfidf;

        public DocVector(Map<String, Integer> terms, Map<String, Integer> targetTerms) {
            this.terms = terms;
            this.targetTerms = targetTerms;

            if (terms.size() < targetTerms.size()) {

                this.vector = new OpenMapRealVector(targetTerms.size());
                this.tfidf = new OpenMapRealVector(terms.size());

            } else {

                this.vector = new OpenMapRealVector(terms.size());
                this.tfidf = new OpenMapRealVector(targetTerms.size());
            }
        }

        public void setEntry(String term, int freq) {
            if (targetTerms.containsKey(term)) {
                int pos = targetTerms.get(term);
                vector.setEntry(pos, (double) freq);
            }
        }

        public void normalize() {
            double sum = vector.getL1Norm();

            vector = (SparseRealVector) vector.mapDivide(sum);

            if (terms.size() < targetTerms.size()) {
                int i = terms.size();
                while (i < targetTerms.size()) {
                    vector.setEntry(i, (double) 0);
                    i++;
                }
            }

            vector = (SparseRealVector) vector.getSubVector(0, targetTerms.size());

        }

        public void tfidfVector(SparseRealVector idf) {
            this.tfidf = (SparseRealVector) this.vector.ebeMultiply(idf);

        }

        public String toString() {
            RealVectorFormat formatter = new RealVectorFormat();
            return formatter.format(vector);
        }

        public String toStringtfidf() {
            RealVectorFormat formatter = new RealVectorFormat();
            return formatter.format(tfidf);
        }
    }

    public SpotMatch getConsinSimilarity(SpotMatch spotmatch,String field) {

        String surfaceForm = spotmatch.getMention();

        SpotMatch s = null;
        
         switch (field) {
            case "summary":
               field=LUCENE_ARTICLE_SUMMARY;
                break;
            case "content":
               field=LUCENE_ARTICLE_CONTENT;
                break;

            case "links":
                field=LUCENE_ARTICLE_LINKS;
                break;

            case "infobox":
                field=LUCENE_ARTICLE_INFOBOX;
                break;
            
            case "discovery":
                field="discovery";
                break;
         
        }

        

        if (spotmatch.getContext().trim().isEmpty()) {
            logger.warn("no context for spot {}", spotmatch.getMention());
            return spotmatch;
        }

        Map<Integer, DocVector> docsVec = new HashMap<Integer, DocVector>();
        int i = 0;

        //index the spot context on RAM 
        clearRAMIndex();
        addDocumentRAM(spotmatch);

        BytesRef text;

        IndexReader readerRAM = getReaderFromRAM();

        Terms tfvX = null;

        try {
            tfvX = readerRAM.getTermVector(0, "context");

        } catch (IOException e) {
            logger.error("computing cosine similarity ({}) ", e.toString());
            System.exit(-1);
        }

        Map<String, Integer> xfrequencies = new HashMap<String, Integer>();

        Map<String, Integer> terms = new HashMap<String, Integer>();

        TermsEnum xtermsEnum = null;

        try {
            xtermsEnum = tfvX.iterator(null);

            int pos = 0;

            while ((text = xtermsEnum.next()) != null) {
                String term = text.utf8ToString();
                int freq = (int) xtermsEnum.totalTermFreq();

                //remove surface form from the terms
               
                if (!term.equalsIgnoreCase(surfaceForm)) {
                    xfrequencies.put(term, freq);
                    terms.put(term, pos++);
                }
            }

        } catch (IOException e) {
            logger.error("computing cosine similarity ({}) ", e.toString());
            System.exit(-1);
        }

        docsVec.put(i, new DocVector(xfrequencies, terms));
        for (Map.Entry<String, Integer> k : xfrequencies.entrySet()) {
            String termText = k.getKey();
            int termFreq = k.getValue();
            
            docsVec.get(i).setEntry(termText, termFreq);
        }

        docsVec.get(i).normalize();

        // double xnorm = tfidfVector(xTfidf, xfrequencies, "context",readerRAM);
        IndexReader reader = getReader();
        List<Integer> spotDocs = new ArrayList<Integer>();

        for (int x = 0; x < spotmatch.getEntities().size(); x++) {

            Entity entity = spotmatch.getEntities().get(x).getEntity();

            Terms tfvY = null;
            Map<String, Integer> yfrequencies = new HashMap<String, Integer>();
            
            try {
                
               // System.out.println(entity.getName() +" "+ entity.getId());
                int id=getLuceneId(entity.getId());
                
                if(id<0)
                    continue;
               if(!field.equalsIgnoreCase("discovery")){
                tfvY = reader.getTermVector(getLuceneId(entity.getId()), field);

                if (tfvY == null) {

                    continue;
                }

           
        
            TermsEnum ytermsEnum = tfvY.iterator(null);

             while ((text = ytermsEnum.next()) != null) {
                    String term = text.utf8ToString();
                    int freq = (int) ytermsEnum.totalTermFreq();
                     
                    yfrequencies.put(term, freq);

                }
                
               } else{
                   
                  EntityMatch emacth= spotmatch.getEntities().get(x);
                  List<String> neighboursTerms=new ArrayList<String>();
                  
                  for(String neighbour:emacth.getNeighboursDiscovered()){
                          String n=sm.clean(neighbour.replaceAll(uri,""));
                          
                          for(String t:n.split(" ")){
                              neighboursTerms.add(t.toLowerCase().replaceAll("'", ""));
                          } 
                      }
                 
                   
                  for(String neighbour:neighboursTerms){
                       Integer n = yfrequencies.get(neighbour);
                        n = (n == null) ? 1 : ++n;
                        yfrequencies.put(neighbour, n);                      
                  }
                   
               }
                
                
            } catch (IOException e) {
                
                logger.error("computing cosine similarity ({}) ", e.toString());
                 
            }

            
            
            Map<String, Integer> yyfrequencies = new HashMap<String, Integer>();

            for (Map.Entry<String, Integer> k : xfrequencies.entrySet()) {

                if (yfrequencies.containsKey(k.getKey())) {
                    yyfrequencies.put(k.getKey(), yfrequencies.get(k.getKey()));
                }

            }
//              System.out.println("--------------");
//              System.out.println(yyfrequencies);

            i = x + 1;
            // docs[i] = new DocVector(terms);
            docsVec.put(i, new DocVector(yfrequencies, terms));
            spotDocs.add(i);
           

            for (Map.Entry<String, Integer> k : yfrequencies.entrySet()) {
                String termText = k.getKey();
                int termFreq = k.getValue();
                docsVec.get(i).setEntry(termText, termFreq);
            }
            docsVec.get(i).normalize();
        }

               
        SparseRealVector idfVector = new OpenMapRealVector(terms.size());

        int N = spotDocs.size();

        for (Map.Entry<String, Integer> k : terms.entrySet()) {
            int count = 0;
            double idf = 0;
            String term = k.getKey();
            Integer pos = k.getValue();
            for (int x : spotDocs) {
                if (docsVec.get(x).terms.containsKey(term)) {
                    if (docsVec.get(x).terms.get(term) > 0) {
                        count++;
                    }
                }
            }

            if (count != 0) {
                idf = 1 + Math.log(N / count);
            }

            idfVector.setEntry(pos, idf);

        }
     
        for (int ii : docsVec.keySet()) {
            docsVec.get(ii).tfidfVector(idfVector);
        }

        
        
        for (int x : spotDocs) {
            //Entity entity = spotmatch.getEntities().get(x - 1).getEntity();
            double cosin = getTFIDFCosineSimilarity(docsVec.get(0), docsVec.get(x));
            
            spotmatch.getEntities().get(x-1).setContextScoreTFIDF(cosin);
           // EntityMatch match = new EntityMatch(entity, 0, spotmatch);
           // match.setCommonness(spotmatch.getEntityCommonness(entity));
           // match.setContextScoreTFIDF(cosin);
             
           // eml.add(match);
        }

//        spotmatch.setEntities(eml);
//        s = new SpotMatch(spotmatch.getSpot(), spotmatch.getField(), spotmatch.getStart(), spotmatch.getEnd(), spotmatch.getPos(), spotmatch.getContext());
//        s.setEntities(eml);
        return spotmatch;

    }
}
