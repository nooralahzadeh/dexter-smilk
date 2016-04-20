/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.smilk.annotation.wikipedia;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import fr.inria.wimmics.smilk.eval.AssessmentRecord;
import fr.inria.wimmics.smilk.spotter.filter.POSTagFilter;
import fr.inria.wimmics.smilk.util.MyPartition;
import it.cnr.isti.hpc.benchmark.Stopwatch;
import it.cnr.isti.hpc.dexter.label.IdHelper;
import it.cnr.isti.hpc.dexter.label.IdHelperFactory;
import it.cnr.isti.hpc.text.MyPair;
import it.cnr.isti.hpc.text.PosTagger;
import it.cnr.isti.hpc.text.PosToken;

import it.cnr.isti.hpc.wikipedia.article.Article;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

/**
 *
 * @author fnoorala
 */
public class DatasetCollection {
   private static final Logger logger = LoggerFactory.getLogger(DatasetCollection.class);
    /**
     * @param args the command line arguments
     */
    static List<AnnotatedSpot> spots;
  
    public static void collectDocument(jwplWikipedia jwpl, String pageName, WikiConfig config, String directoryName) throws WikiApiException, Exception {
        spots = new ArrayList<AnnotatedSpot>();
      
        AssessmentRecord annotation=null;
       
        String title = Article.getTitleInWikistyle(pageName);
        IdHelper hash = IdHelperFactory.getStdIdHelper();
        int page_id = hash.getId(title);

        String wikitext = jwpl.getwikiData(title);
      

        final int wrapCol = Integer.MAX_VALUE;

        // Instantiate a compiler for wiki pages
        WtEngineImpl engine = new WtEngineImpl(config);

        // Retrieve a page
        PageTitle pageTitle = PageTitle.make(config, pageName);

        PageId pageId = new PageId(pageTitle, -1);

        TextConverter p = new TextConverter(config, wrapCol,false);
        EngProcessedPage cp = engine.postprocess(pageId, wikitext, null);
        String s = (String) p.go(cp.getPage());
        
        //filter the pages with less inlinks < 3
       if(p.intLinkNum > 2){
            
        
        String rawtxt = markedSpots(s);
         //extract POS tags for each spot
        
        
        
        exrtactPOStags(rawtxt,spots);
     
        IdHelper hashmap = IdHelperFactory.getStdIdHelper();
        //Filtering based on POS tags
        POSTagFilter psFilter = new POSTagFilter();
        List<AnnotatedSpot> interistingSpots = new ArrayList<AnnotatedSpot>();
        for (AnnotatedSpot sp : spots) {
            
            String titleInWikiStyle = Article.getTitleInWikistyle(sp.getWikiname());
            int enity = hashmap.getId(titleInWikiStyle);
            sp.setEntity(Integer.toString(enity));
            sp.setConfidenceScore((float) 1.0);
            if (sp.getPos() != null && psFilter.isInteresting(sp.getPos())) {
                interistingSpots.add(sp);
            }
        }
        
        
        //FIX ME: Get n=10 sentence of the text to make it simple to algorithm
        //Split the document to sentences
           PosTagger ps = PosTagger.getInstance();
           List<MyPair> sentences =ps.extractSentecnesOffset(rawtxt); 
           List<List<MyPair>> partition = MyPartition.partition(sentences,10);
           int start=partition.get(0).get(0).getFirst();
           int end=partition.get(0).get(partition.get(0).size()-1).getSecond();
           String resume=rawtxt.substring(start, end);
                   

        if (interistingSpots != null && interistingSpots.size()>2) {
            //doc = new fr.inria.wimmics.smilk.annotation.Document(page_id, rawtxt, interistingSpots);
             List<AnnotatedSpot> interistingSpots_in_resume = new ArrayList<AnnotatedSpot>();
             for(AnnotatedSpot anspot: interistingSpots){
                 if(anspot.getStart()>=start && anspot.getEnd()<=end )
                     interistingSpots_in_resume.add(anspot);
             }
          if(interistingSpots_in_resume.size()>2){
            annotation=new AssessmentRecord();
            annotation.setText(resume);
            annotation.setDocId(Integer.toString(page_id));
            annotation.setAnnotatedSpot(interistingSpots_in_resume);
            
            writeToFile(annotation,directoryName);
            }
          }
        else{
             logger.info("Page {} was not annotated becasue of few links.",title );
        }
       }
    }

    public static String markedSpots(String s){
         Pattern pattern = Pattern.compile("(<spot\\swikiname=\".*?\">)(.*?)(</spot>)");
         Pattern patternWikiName = Pattern.compile("(<spot\\swikiname=\")(.*?)(\">)");

        // Map<Couple, String> spots = new LinkedHashMap<Couple, String>();
        Set<Couple> cleaning = new HashSet<Couple>();

        Matcher matcher = pattern.matcher(s);
        int placeholder = 0;
        while (matcher.find()) {
            Couple left_cl = new Couple(matcher.start(), matcher.end(1));
            Couple right_cl = new Couple(matcher.start(3), matcher.end(3));
            cleaning.add(left_cl);
            cleaning.add(right_cl);
            placeholder += (left_cl.end - left_cl.start);

            Matcher match_wiminame = patternWikiName.matcher(matcher.group(1));

            if (match_wiminame.find()) {
                AnnotatedSpot spot = new AnnotatedSpot(match_wiminame.group(2), matcher.group(2), matcher.start(2) - placeholder, matcher.end(2) - placeholder);
                spots.add(spot);
            }
            placeholder += (right_cl.end - right_cl.start);

        }

        StringBuffer sb = new StringBuffer(s);

        List<Couple> sortedList = new ArrayList(cleaning);
        Collections.sort(sortedList, new CustomComparator());

        for (int i = sortedList.size() - 1; i >= 0; i--) {

            sb.replace(sortedList.get(i).start, sortedList.get(i).end, "");
        }

        return (new String(sb.toString()));
    }
  
    public static void exrtactPOStags(String rawtxt, List<AnnotatedSpot> spots ){
      
         PosTagger ps = PosTagger.getInstance();
        // ps.tag(text);

        List<ArrayList<PosToken>> tkpos = ps.tagWithOffSet(rawtxt);
        LinkedList<PosToken> new_tkpos= new LinkedList<PosToken>();

        for (ArrayList<PosToken> t : tkpos) {
            for (PosToken tp : t) {
                new_tkpos.add(tp);
                
            }  
        }

       
            

        //finding the POS tag set for each spot
        int x = 0, y = 0;

        while (x < spots.size() && y < new_tkpos.size()) {

            y = getTokenIndex(spots.get(x),new_tkpos);

            if (y == 0) {
                x++;
            } else {
                List<Integer> matchedPositions = new ArrayList<Integer>();
                List<String> poses = new ArrayList<String>();

                boolean match = (spots.get(x).end == new_tkpos.get(y).getEnd()) ? true : false;

                if (match) {
                    poses.add(new_tkpos.get(y).getPos());
                    spots.get(x).setPos(poses);
                    x++;

                } else {
                    if (spots.get(x).end > new_tkpos.get(y).getEnd()) {

                        while (spots.get(x).end > new_tkpos.get(y).getEnd()) {
                            // System.out.println(y + " partial match " +spots.get(x)+" " + new_tkpos.get(y));
                            matchedPositions.add(y);
                            y++;
                        }

                        matchedPositions.add(y);
                        for (int i : matchedPositions) {
                            poses.add(new_tkpos.get(i).getPos());
                        }
                        spots.get(x).setPos(poses);
                        x++;
                    } else {

                        while (spots.get(x).end < new_tkpos.get(y).getEnd()) {
                            matchedPositions.add(x);
                            x++;
                            
                            if(x>spots.size()-1){
                                break;
                            }

                        }
                         if(x<spots.size())
                                matchedPositions.add(x);
                       
                        poses.add(new_tkpos.get(y).getPos());
                       
                        for (int i : matchedPositions) {
                            spots.get(i).setPos(poses);
                        }

                        x++;

                    }
                }
            }

        }
    }
    
    public static class CustomComparator implements Comparator<Couple> {

        @Override
        public int compare(Couple o1, Couple o2) {
            return o1.getStart() - o2.getStart();
        }
    }

    public static class Couple {

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + this.start;
            hash = 53 * hash + this.end;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Couple other = (Couple) obj;
            if (this.start != other.start) {
                return false;
            }
            if (this.end != other.end) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Couple{" + "start=" + start + ", length=" + end + '}';
        }

        public int start;
        public int end;

        Couple(int start, int length) {
            this.start = start;
            this.end = length;
        }

        public int getStart() {
            return this.start;
        }

        public int getEnd() {
            return this.end;
        }

    }

    public static void writeToFile(AssessmentRecord annotation, String directoryName) {
         
        try {

            String fileName = directoryName+"/" + annotation.docId + ".json";

            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName), "UTF-8"));

            out.append(annotation.toJson());
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    public static void toJsonFile(int page_id, AssessmentRecord doc) {

        JSONObject obj = new JSONObject();
        obj.put("docId", doc.getDocId());

        obj.put("wikiTxt", doc.getText());

        JSONArray spotlist = new JSONArray();

        for (AnnotatedSpot s : doc.getAnnotatedSpot()) {
            JSONObject spots = new JSONObject();
            spots.put("docid", doc.getDocId());
            spots.put("spot", s.getSpot());
            spots.put("wikiname", s.getWikiname());

            String title = Article.getTitleInWikistyle(s.getWikiname());
            IdHelper hash = IdHelperFactory.getStdIdHelper();
            int enity = hash.getId(title);
            spots.put("entity", enity);
            spots.put("start", s.getStart());
            spots.put("end", s.getEnd());
            spotlist.put(spots);
        }

        obj.put("AnnotatedSpots", spotlist);

        try {

            String fileName = "annotation/" + page_id + ".txt";

            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName), "UTF-8"));

            out.append(obj.toString());
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String concat(List<String> T, List<Integer> pos) {
        String ret = "";
        for (int i = 0; i < pos.size(); i++) {
            ret += T.get(pos.get(i)) + " ";
        }
        return ret.trim();
    }

    public static boolean is_prefixes_match(String A, String B) {

        for (int i = 0; i < Math.min(A.length(), B.length()); i++) {
            if (A.charAt(i) != B.charAt(i)) {
                return false;
            }
        }
        return true;
    }



    public static int getTokenIndex(AnnotatedSpot ss,LinkedList<PosToken> new_tkpos) {
        for (int i = 0; i < new_tkpos.size(); i++) {

            if (new_tkpos.get(i).getStart() == ss.start) {
                return i;
            } else if (new_tkpos.get(i).getEnd() == ss.end) {
                return i;
            }
        }

        return 0;
    }

    public static int getSpotIndex(PosToken t) {
        for (int i = 0; i < spots.size(); i++) {
            if (spots.get(i).start == t.getStart()) {
                return i;
            } else if (spots.get(i).end == t.getEnd()) {
                return i;
            }
        }
        return 0;
    }

}

//    public static boolean bt(ArrayList<Integer> pa, ArrayList<Integer> pb, int ca, int cb) {
//
//        List<Spot> sps = new LinkedList<Spot>();
//        for (int s : pa) {
//            sps.add(spots.get(s));
//        }
//
//        List<PosToken> tks = new LinkedList<PosToken>();
//        for (int s : pb) {
//            tks.add(new_tkpos.get(s));
//        }
//
//        String A = concat(spots_str, pa);
//        String B = concat(new_tkpos_str, pb);
//        A = A.replaceAll("-", "");
//        B = B.replaceAll("-", "");
//
//        System.out.println(A + " / " + B + " " + pa + " " + pb + " " + ca + " " + cb);
//        boolean found = false;
//
//        if (A.replaceAll("[\\s]", "").equalsIgnoreCase(B.replaceAll("[\\s]", ""))) {
//            System.out.println("pa:" + pa);
//            System.out.println("pa:" + pb);
//            s1.add(pa);
//            s2.add(pb);
//            return true;
//        }
//
//        //chech the start point
//        if (A.length() > B.length()) {
//
//            String C = A.substring(B.trim().length()).trim();
//
//            for (int i = cb; i < Math.min(cb + 10, (int) new_tkpos.size()) && !found; i++) {
//
//                if (is_prefixes_match(C, new_tkpos.get(i).getToken())) {
//
//                    pb.add(i);
//                    found = found || bt(pa, pb, ca, i + 1);
//                    pb.remove(pb.size() - 1);
//                }
//            }
//
//        } else {
//            String C = B.substring(A.trim().length());
//
//            //ca = getSpotIndex(new_tkpos.get(pb.get(0)));
//            for (int i = ca; i < Math.min(ca + 5, (int) spots.size()) && !found; i++) {
//                System.out.println("==============" + ca + ": " + C + " / " + new_tkpos.get(pb.get(0)) + " " + spots.get(i).toString());
//                System.out.println("==============" + C.length() + " / " + spots.get(i).mention);
//
//                if (is_prefixes_match(C, spots.get(i).mention)) {
//                    pa.add(i);
//                    found = found || bt(pa, pb, i + 1, cb);
//                    pa.remove(pa.size() - 1);
//                }
//            }
//
//        }
//
//        return found;
//    }