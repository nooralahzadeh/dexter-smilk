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
package fr.inria.wimmics.smilk.annotation.datasets;

import fr.inria.wimmics.smilk.annotation.wikipedia.AnnotatedSpot;
import fr.inria.wimmics.smilk.eval.AssessmentRecord;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fnoorala
 */
public class datasetReader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        String input = "/user/fnoorala/home/NetBeansProjects/dexter/en.data/KORE50_AIDA/AIDA.tsv";

        BufferedReader fileReader = null;

        //Delimiter used in CSV file
        final String DELIMITER = "\t";
        try {
            String line = "";
            //Create the file reader
            fileReader = new BufferedReader(new FileReader(input));

            Map<String, Map<Integer, List<String>>> docs = new HashMap<String, Map<Integer, List<String>>>();
            Map<String, List<List<Integer>>> docs_spots = new HashMap<String, List<List<Integer>>>();

            //Read the file line by line
            String key = null;

            int i = 0;

            Map<Integer, List<String>> raws = new HashMap<Integer, List<String>>();

            while ((line = fileReader.readLine()) != null) {
                //Get all tokens available in line

                String[] tokens = line.split(DELIMITER);
                List<String> raw = new ArrayList<String>();

                for (String token : tokens) {
                    //Print all tokens
                    if (token.isEmpty()) {
                        docs.put(key, raws);
                        raws = new HashMap<>();
                        i = 0;

                    } else if (token.contains("DOCSTART")) {

                        key = token;

                    } else {

                        raw.add(token);
                        // System.out.print(token + "\t");
                    }

                }

                if (raw.size() > 0) {
                    i++;
                    raws.put(i, raw);
                }

            }

            //for last line
            if ((line = fileReader.readLine()) == null && raws.size() > 0) {
                docs.put(key, raws);
            }

            for (String k : docs.keySet()) {

                int x = 1, y = 0;

                int s = docs.get(k).keySet().size() + 1;
                String sentence = "";
                List<List<Integer>> spots = new ArrayList<List<Integer>>();
                List<Integer> spot = new ArrayList<Integer>();

                while (x < s && y < s) {

                    List<String> r = docs.get(k).get(x);
                    if (r.size() > 1) {
                        y = x + 1;

                        List<String> r_next = docs.get(k).get(y);

                        if (r_next.size() > 1 && r_next.get(1).equalsIgnoreCase("B")) {
                            spot.add(x);
                            spots.add(spot);
                            spot = new ArrayList<Integer>();
                            x = y;
                            y = 0;

                        } else if (r_next.size() > 1 && r_next.get(1).equalsIgnoreCase("I")) {
                            spot.add(x);
                            x = y;
                            y = 0;

                        } else {
                            spot.add(x);
                            x++;
                            y = 0;
                            spots.add(spot);
                            spot = new ArrayList<Integer>();
                        }

                    } else {
                        x++;
                    }

                }

                docs_spots.put(k, spots);

            }

            List<AssessmentRecord> records = new ArrayList<AssessmentRecord>();

            System.out.println(docs_spots);
            for (String k : docs.keySet()) {
                AssessmentRecord record = new AssessmentRecord();

                String sentence = "";
                List<AnnotatedSpot> spots = new ArrayList<AnnotatedSpot>();

                for (int idx : docs.get(k).keySet()) {
                    List<String> tokens = docs.get(k).get(idx);

                    if (tokens.get(0).length() > 1) {
                        sentence += tokens.get(0) + " ";
                    } else {
                        sentence = sentence.trim();
                        sentence += tokens.get(0) + " ";
                    }
                }

                record.setDocId(k);
                record.setText(sentence);

                List<List<Integer>> tokens = docs_spots.get(k);

                for (List<Integer> offset : tokens) {

                    AnnotatedSpot s = new AnnotatedSpot();

                    String txt = docs.get(k).get(offset.get(0)).get(2);

                    s.setDocId(k);
                    s.setSpot(txt);
                    if (docs.get(k).get(offset.get(0)).get(3).equalsIgnoreCase("--NME--")) {
                        s.setWikiname("NIL");
                    } else {
                        s.setWikiname(docs.get(k).get(offset.get(0)).get(3));
                    }

                    spots.add(s);

                }

                record.setAnnotatedSpot(spots);
                records.add(record);
            }

            for (AssessmentRecord r : records) {
                System.out.println(r.getDocId());
                System.out.println(r.getText());

                for (AnnotatedSpot rs : r.getAnnotatedSpot()) {
                    System.out.println(rs.getSpot() + " " + rs.getWikiname());

                }
                System.out.println("---------------");

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
