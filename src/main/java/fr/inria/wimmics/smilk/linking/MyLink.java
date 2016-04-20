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
 public  class MyLink {

        double weight;
        
        SpotMatch from1;
        EntityMatch from2;
        EntityMatch to;

        public MyLink(SpotMatch from1, EntityMatch from2, EntityMatch to, double weight) {
           
            this.weight = weight;
            this.from1 = from1;
            this.from2 = from2;
            this.to = to;

        }

        public boolean isSimilarTo(MyLink mylink) {
            if (this.from2.equals(mylink.to) && this.to.equals(mylink.from2)) {
                return true;
            } else {
                return false;
            }
        }

        public String toString() {

            String from = (from1 != null) ? from1.getMention() : from2.getMention();
            return "E: " + from + "-->" + to;
        }

    }