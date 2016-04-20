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
package fr.inria.wimmics.smilk.linking;

import it.cnr.isti.hpc.dexter.entity.EntityMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;

/**
 *
 * @author fnoorala
 */
public class MyNode implements Comparable<MyNode> {

        SpotMatch mention;
        EntityMatch candidate;
        String name;
        MyNode origin;
        double score;

        public MyNode(SpotMatch mention, EntityMatch candidate, MyNode origin) {

            this.candidate = candidate;
            this.origin = origin;
            this.mention = mention;
            if (mention == null) {
                this.name = candidate.getEntity().getName();

            } else {
                this.name = mention.getMention();
            }

        }

        public void setRank(double rank) {
            this.score = rank;
        }

        public double getRank(double rank) {
            return this.score;
        }

        public String toString() {
            return "V: " + name + ": " + score;
        }

        @Override
        public int compareTo(MyNode o) {
            //descending order
            int i = Double.compare(o.score, score);
            return i;
        }
    }