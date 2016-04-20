package fr.inria.wimmics.smilk.nerd;

import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.spot.ContextExtractor;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;

import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.text.MyPair;
import it.cnr.isti.hpc.text.ParserTool;
import it.cnr.isti.hpc.text.SentenceSegmenter;
import it.cnr.isti.hpc.text.Token;
import it.cnr.isti.hpc.text.TokenSegmenter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import opennlp.tools.coref.DefaultLinker;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
import opennlp.tools.coref.mention.MentionContext;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.util.Span;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Spotter
 *
 *
 */
public class OpenNLPNerd implements NameEntityRecognizer {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(OpenNLPNerd.class);

    DexterParams params = DexterParams.getInstance();

    String path;

    /**
     * The Named Entity Types of interest. Each requires a corresponding Open
     * NLP model file.
     */
    private static final String[] NAME_TYPES
            = {"person", "organization", "location"};

      
    List<Token> context=new ArrayList<Token>();
    private final TokenSegmenter ts;
    private final SentenceSegmenter sn;

    final private Map<String, TokenNameFinder> _nameFinderMap
            = new HashMap<String, TokenNameFinder>();

    private Linker _linker = null;
    private String COREF_DIR;
    String prefix;

    public OpenNLPNerd() {

        ts = new TokenSegmenter();

        sn = new SentenceSegmenter();
        path = params.getDefaultNlpModel().getPath();
        COREF_DIR = "/coref.dir";

    }

    /**
     * Find named entities in a tokenized sentence.
     * <p>
     * Must call {@link #clearNamedEntityAdaptiveData()} after finding all named
     * entities in a single document.
     * </p>
     *
     *
     * @param sentence the sentence text
     * @param tokens the sentence tokens
     * @return a collection of named entity references
     */
    public List<Span> findNamedEntities(final String[] tokens) {
        final List<Span> entities = new LinkedList<Span>();

        // use each type of finder to identify named entities 
        for (final TokenNameFinder finder : nameFinders()) {
            entities.addAll(Arrays.asList(finder.find(tokens)));
        }

        return entities;
    }

    /**
     * Must be called between documents or can negatively impact detection rate.
     */
    public void clearNamedEntityAdaptiveData() {
        for (final TokenNameFinder finder : nameFinders()) {
            finder.clearAdaptiveData();
        }
    }

    /**
     * @return the lazily-initialized token name finders
     */
    private TokenNameFinder[] nameFinders() {
        final TokenNameFinder[] finders = new TokenNameFinder[NAME_TYPES.length];
        // one for each name type
        for (int i = 0; i < NAME_TYPES.length; i++) {
            finders[i] = nameFinder(NAME_TYPES[i]);
        }
        return finders;
    }

    /**
     * @param type the name type recognizer to load
     * @return the lazily-initialized name token finder
     */
    private TokenNameFinder nameFinder(final String type) {
        if (!_nameFinderMap.containsKey(type)) {
            final TokenNameFinder finder = createNameFinder(type);
            _nameFinderMap.put(type, finder);
        }
        return _nameFinderMap.get(type);
    }

    /**
     * @param type the name type recognizer to load
     * @return the lazily-initialized name token finder
     */
    private TokenNameFinder createNameFinder(final String type) {
        InputStream modelIn = null;
        try {
            logger.info("Loading " + type + " named entity model");

            modelIn = getClass().getResourceAsStream(path
                    + String.format("/ner-%1$s.bin", type));

            final TokenNameFinderModel nameFinderModel = new TokenNameFinderModel(modelIn);
            modelIn.close();
            return new NameFinderME(nameFinderModel);
        } catch (final IOException ioe) {
            logger.error("Error loading " + type + " token name finder", ioe);
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (final IOException e) {
                }
            }
        }
        return null;
    }

   

    public DiscourseEntity[] Coref(String[] sentences) {

        try {
            // URL url =getClass().getResource(path+"/ner-person.bin");
            // ClassPathResource p1 = new ClassPathResource(path + "/ner-person.bin");     
            prefix = params.getDefaultModel().getPath();
            _linker = new DefaultLinker(
                    // LinkerMode should be TEST
                    //Note: I tried EVAL for a long time before realizing that was the problem
                    prefix + COREF_DIR, LinkerMode.TEST);

            // list of document mentions
            ParserTool parse = new ParserTool();

            List<Mention> doc = new ArrayList<Mention>();
            for (int i = 0; i < sentences.length; i++) {
                // generate the sentence parse tree
                Parse prs = parse.parseSentence(sentences[i]);

                final DefaultParse parseWrapper = new DefaultParse(prs, i);
                final Mention[] extents = _linker.getMentionFinder().getMentions(parseWrapper);

                //Note: taken from TreebankParser source...
                for (int ei = 0, en = extents.length; ei < en; ei++) {
                    // construct new parses for mentions which don't have constituents.
                    if (extents[ei].getParse() == null) {
                        // not sure how to get head index, but its not used at this point
                        final Parse snp = new Parse(prs.getText(), extents[ei].getSpan(), "NML", 1.0, 0);
                        prs.insert(snp);
                        logger.debug("Setting new parse for " + extents[ei] + " to " + snp);
                        extents[ei].setParse(new DefaultParse(snp, i));
                    }
                }
                doc.addAll(Arrays.asList(extents));
            }

            if (!doc.isEmpty()) {
                return _linker.getEntities(doc.toArray(new Mention[0]));
            }

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(OpenNLPNerd.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new DiscourseEntity[0];
    }

    public Map<Integer, List<MyPair>> getReplacements(String[] sentences) {

        DiscourseEntity[] coref_entities = Coref(sentences);

        Set<DiscourseEntity> filter_NNP = new LinkedHashSet<DiscourseEntity>();

        for (int i = 0; i < coref_entities.length; i++) {
            DiscourseEntity ent = coref_entities[i];

            if (ent.getNumMentions() > 1) {
                Iterator<MentionContext> mentions = ent.getMentions();

                while (mentions.hasNext()) {
                    MentionContext mc = mentions.next();
                    // System.out.print("[" + mc.toString() +  "]"+ mc.getHeadTokenTag() +" "+mc.getFirstTokenTag() );
                    if (mc.getHeadTokenTag().equalsIgnoreCase("NNP") || mc.getHeadTokenTag().equalsIgnoreCase("NNS") || mc.getFirstTokenTag().equalsIgnoreCase("NNP") || mc.getFirstTokenTag().equalsIgnoreCase("NNS")) {
                        filter_NNP.add(ent);
                    }
                }
                //System.out.println();

            }
        }

        Map<Integer, List<MyPair>> set_of_replacement = new HashMap<Integer, List<MyPair>>();

        int j = 0;
        for (DiscourseEntity ent : filter_NNP) {

            List<MyPair> pairs = new LinkedList<MyPair>();
            Iterator<MentionContext> mentions = ent.getMentions();
            while (mentions.hasNext()) {
                MentionContext mc = mentions.next();
                int sent_id = mc.getSentenceNumber();
                int add = 0;
                for (int i = 0; i < sent_id; i++) {
                    add += sentences[i].length() + 1;
                }
                MyPair candidates = new MyPair(mc.getSpan().getStart() + add, mc.getSpan().getEnd() + add);
                pairs.add(candidates);
            }
            set_of_replacement.put(j, pairs);
            j++;
        }
        //System.out.println(set_of_replacement);
        return set_of_replacement;
    }

    @Override
    public List<Token> nerd(Document document) {
        List<Token> NerdTokens = new LinkedList<Token>();
        
        
        String[] tokens = ts.tokenize(document.getContent());

        List<Token> tokenOffsets = ts.tokenizePos(document.getContent());
       
        List<Span> spans = findNamedEntities(ts.tokenize(document.getContent()));

        for (int i = 0; i < spans.size(); i++) {
            final Span s = spans.get(i);

            String txt = "";
           // List<Token> subTokens=new ArrayList<Token>();
           
            for (int tok = s.getStart(); tok < s.getEnd() - 1; tok++) {
              
                txt += tokens[tok] + " ";
              
            }
            txt += tokens[s.getEnd() - 1];

            Token t;
            int start = tokenOffsets.get(s.getStart()).getStart();
            int end = tokenOffsets.get(s.getEnd()).getEnd();

            String type = s.getType().toUpperCase();

            t = new Token(txt, type, start, end);
            t.setPos("NNP");
            t.setSubTokens(Arrays.asList(t));
            t.setNESubTokens(Arrays.asList(t));
            NerdTokens.add(t);
        }

        return NerdTokens;

    }

    @Override
    public void init(DexterParams dexterParams, DexterLocalParams defaultModuleParams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Token> getContext() {
        return context;
    }

}
