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
package fr.inria.wimmics.smilk.annotation.wikipedia;

import java.util.Comparator;
import java.util.List;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 *
 * Created on Feb 16, 2014
 */
public class AnnotatedSpot {

    String docId;
    String spot;
    int start;
    int end;
    String entity;
    String wikiname;
    String type;
    float confidenceScore;
    String entityFinderProcess;

    //<fnoorala>
    public List<String> pos;

    public void setPos(List<String> pos) {
        this.pos = pos;
    }

    public List<String> getPos() {
        return this.pos;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
    
     public void setEntityFinderProcess(String process) {
        this.entityFinderProcess = process;
    }

    public String getEntityFinderProcess() {
        return this.entityFinderProcess;
    }
    //</fnoorala>

    public AnnotatedSpot() {

    }

    public AnnotatedSpot(String spot, String entity) {
        super();
        this.spot = spot;
        this.entity = entity;
    }

    public AnnotatedSpot(String spot) {
        super();
        this.spot = spot;
    }
    
     public AnnotatedSpot(String wikiName,String spot, int start, int end) {
        super();
        this.spot = spot;
        this.wikiname = wikiName;
        this.start = start;
        this.end    = end;
        }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getSpot() {
        return spot;
    }

    public void setSpot(String spot) {
        this.spot = spot;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * the length of the spot *
     */
    public int getLength() {
        return getEnd() - getStart();
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public float getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(float confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getWikiname() {
        return wikiname;
    }

    public String asString() {
        return spot + "<" + start + "," + end + ">[" + entity + "]";
    }

    public void setWikiname(String wikiname) {
        this.wikiname = wikiname;
    }

    @Override
    public String toString() {
        return "AnnotatedSpot [docId=" + docId + ", spot=" + spot + ", start="
                + start + ", end=" + end + ", entity=" + entity + ", wikiname="
                + wikiname + ", confidenceScore=" + confidenceScore + ", pos=" + pos+"]";
    }

    /**
     * Sorts from in decreasing length order of the spots
     *
     * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
     *
     * Created on Feb 18, 2014
     */
    public static class SortByLength implements Comparator<AnnotatedSpot> {

        public int compare(AnnotatedSpot o1, AnnotatedSpot o2) {
            return o2.getLength() - o1.getLength();
        }

    }

    public static class SortByStart implements Comparator<AnnotatedSpot> {

        public int compare(AnnotatedSpot o1, AnnotatedSpot o2) {
            return o1.getStart() - o2.getStart();
        }

    }

    /**
     * Sorts from in decreasing confidence score order of the spots
     *
     * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
     *
     * Created on Feb 18, 2014
     */
    public static class SortByConfidence implements Comparator<AnnotatedSpot> {

        public int compare(AnnotatedSpot o1, AnnotatedSpot o2) {
            if (o1.getConfidenceScore() > o2.getConfidenceScore()) {
                return -1;
            }
            if (o1.getConfidenceScore() < o2.getConfidenceScore()) {
                return 1;
            }
            return 0;
        }

    }

}
