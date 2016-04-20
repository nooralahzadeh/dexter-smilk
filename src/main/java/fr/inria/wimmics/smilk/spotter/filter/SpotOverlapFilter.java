package fr.inria.wimmics.smilk.spotter.filter;

import fr.inria.wimmics.smilk.util.Utility;

import it.cnr.isti.hpc.dexter.spotter.filter.*;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.text.Token;
import java.util.ArrayList;

import java.util.Arrays;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fnoorala
 *
 *
 */
public class SpotOverlapFilter implements SpotMatchFilter {

    private static final Logger logger = LoggerFactory
            .getLogger(SpotOverlapFilter.class);

    // FIXME it can be implemented more efficiently if spot are sorted by
    // position
    DexterParams dexterparams = DexterParams.getInstance();
    float probability;

    Comparator<SpotMatch> comparator;

    @Override
    public SpotMatchList filter(DexterLocalParams params, SpotMatchList sml) {

        Collections.sort(sml, comparator);

        Map<Integer, List<SpotMatch>> spotmap = new HashMap<Integer, List<SpotMatch>>();
        Map<Integer, List<SpotMatch>> map_filter = new HashMap<Integer, List<SpotMatch>>();

        List<List<Integer>> viewed = new ArrayList<List<Integer>>();
        SpotMatchList filtered = new SpotMatchList();
        int i = 0;

        for (SpotMatch spot : sml) {

            if (viewed.contains(Arrays.asList(spot.getStart(), spot.getEnd()))) {
                continue;
            }

            List<SpotMatch> list = new ArrayList<SpotMatch>();

            for (SpotMatch s : sml) {

                if (viewed.contains(Arrays.asList(s.getStart(), s.getEnd()))) {
                    continue;
                }

                if (s.overlaps(spot) && s != spot) {
                    list.add(s);
                    viewed.add(Arrays.asList(s.getStart(), s.getEnd()));
                }
            }
            viewed.add(Arrays.asList(spot.getStart(), spot.getEnd()));
            list.add(spot);
            //List<SpotMatch> list=new  ArrayList<SpotMatch>(set);
            spotmap.put(i, list);
            i++;
        }

        Pattern pattern = Pattern.compile("^(NNP|NNPS)(.*)(NNP|NNPS)$");
        Pattern pattern_of = Pattern.compile("^(NNP|NNPS|NN|NNS)(,\\sIN,\\s)((NNP|NNPS),\\s)*(NNP|NNPS)$");

        //Pattern indexPattern = Pattern.compile("\\bNNP|NNPS\\b");
        for (int indx : spotmap.keySet()) {
            // System.out.println(indx + "--->");
            for (SpotMatch s : spotmap.get(indx)) {

                List<String> ne_diff = Utility.difference(s.getNeTypes(), dexterparams.TYPES);

                List<String> tag_diff = Utility.difference(s.getPos(), dexterparams.TAGS);

               // System.out.println("\t" + s.getMention() + ":" + s.getNeTypes() + "| " + s.getPos() + "#:  " + s.getEntities().size() + " " + ne_diff);
                String posTagsString = s.getPos().toString().replaceAll("\\[", "").replaceAll("\\]", "");

                Matcher matcher = pattern.matcher(posTagsString);
                Matcher matcher_of = pattern_of.matcher(posTagsString);

                // Matcher matcher_index = indexPattern.matcher(posTagsString);
                if (ne_diff.size() == 0) {
                    filtered.add(s);

                } else if (tag_diff.size() == 0 && matcher.find()) {

                    filtered.add(s);
                    //while (matcher_index.find())   {System.out.println(matcher_index.start() + ":"+ matcher_index.end()); }
                } else if (matcher_of.find()) {
                    List<String> subtokens_str = new ArrayList<String>();
                    for (Token t : s.getSubTokens()) {
                        subtokens_str.add(t.getText());

                    }
                    // matcher_of.group(2).equalsIgnoreCase(", of, ")
                    if (subtokens_str.contains("of")) {
                        filtered.add(s);
                    }
                }
            }

        }
        Collections.sort(filtered, comparator);

        //scanning 
        viewed = new ArrayList<List<Integer>>();

        i = 0;

        for (SpotMatch spot : filtered) {

            if (viewed.contains(Arrays.asList(spot.getStart(), spot.getEnd()))) {
                continue;
            }

            List<SpotMatch> list = new ArrayList<SpotMatch>();

            for (SpotMatch s : filtered) {

                if (viewed.contains(Arrays.asList(s.getStart(), s.getEnd()))) {
                    continue;
                }
                if (s.overlaps(spot) && s != spot) {
                    list.add(s);
                    viewed.add(Arrays.asList(s.getStart(), s.getEnd()));
                }
            }
            viewed.add(Arrays.asList(spot.getStart(), spot.getEnd()));
            list.add(spot);
            //List<SpotMatch> list=new  ArrayList<SpotMatch>(set);
            map_filter.put(i, list);
            i++;
        }

        SpotMatchList final_filtered = new SpotMatchList();
       
        for (int indx : map_filter.keySet()) {

            List<SpotMatch> hasEntity = new ArrayList<SpotMatch>();
            List<SpotMatch> hasNotEntity = new ArrayList<SpotMatch>();
            for (SpotMatch s : map_filter.get(indx)) {
//                System.out.println(s.getMention() + ":" + s.getNeTypes() + "| " + s.getPos() + "#:  " + s.getEntities().size() + " " + s.getProcessFiEntity());
                if (s.getEntities().size() > 0) {
                    hasEntity.add(s);
                } else {
                    hasNotEntity.add(s);
                }

            }

            if (hasEntity.size() > 0) {
                Collections.sort(hasEntity, comparator);
                final_filtered.add(hasEntity.get(0));
            }

            if (hasNotEntity.size() > 0) {
                Collections.sort(hasNotEntity, comparator);
                final_filtered.add(hasNotEntity.get(0));
            }
        }

//        for (SpotMatch s : final_filtered) {
//            System.out.println(s.getMention() + ":" + s.getNeTypes() + "| " + s.getPos() + "#:  " + s.getEntities().size());
//        }

        return final_filtered;
    }

    @Override
    public SpotMatchList filter(DexterLocalParams params, SpotMatchList eml, Document doc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static class SpotMatchLengthComparator implements
            Comparator<SpotMatch> {

        @Override
        public int compare(SpotMatch o1, SpotMatch o2) {
            int s1 = o1.getStart();
            int s2 = o2.getStart();
            int e1 = o1.getEnd();
            int e2 = o2.getEnd();

            if (s1 < s2 && e1 < e2) {
                return -1;
            } else if (s1 < s2 && e1 > e2) {
                return -1;
            } else if (s1 == s2 && e1 > e2) {
                return -1;
            } else if (s1 < s2 && e1 == e2) {
                return -1;
            } else {
                return 1;
            }
        }

    }

    private static class SpotMatchLinkProbabilityComparator implements
            Comparator<SpotMatch> {

        @Override
        public int compare(SpotMatch o1, SpotMatch o2) {
            double l1 = o1.getSpot().getLinkProbability();
            double l2 = o2.getSpot().getLinkProbability();
            if (l1 > l2) {
                return -1;
            }
            if (l1 < l2) {
                return 1;
            }
            return 0;
        }
    }

    @Override
    public void init(DexterParams dexterParams, DexterLocalParams initParams) {
        if (initParams.containsKey("filter-by")) {
            String filterBy = initParams.getParam("filter-by");
            if (filterBy.equals("length")) {
                logger.info("removes overlap selecting the longest spot");
                comparator = new SpotMatchLengthComparator();
            }
            if (filterBy.equals("probability")) {
                logger.info("removes overlap selecting the most probablespot");
                comparator = new SpotMatchLinkProbabilityComparator();
            }
        }
    }
}
