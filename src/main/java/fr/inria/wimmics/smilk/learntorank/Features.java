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
package fr.inria.wimmics.smilk.learntorank;

/**
 *
 * @author fnoorala
 */
public class Features {

    
    double labelsim;// sum of the lable sim
    double label_levenshtin;
    double label_dice;
    double label_jarowinkler;
    double label_soundex;
    double globalsim;
    double discoveryhubRel;
    double milneRel_in;
    double pmiRel;
    double naviveRel;
    double jacardRel;
    double milRel_inout;

    

    public Features() {
    }

    public void setLabelsim(double labelsim) {
        this.labelsim = labelsim;
    }

    public double getLabelsim() {
        return labelsim;
    }
    
    public void setLabel_levenshtin(double label_levenshtin) {
        this.label_levenshtin = label_levenshtin;
    }

    public double getLabel_levenshtin() {
        return label_levenshtin;
    }
    
    public void setLabel_dice(double label_dice) {
        this.label_dice = label_dice;
    }

    public double getLabel_dice() {
        return label_dice;
    }
    
    public void setLabel_jaroWinkler(double label_jarowinkler) {
        this.label_jarowinkler = label_jarowinkler;
    }

    public double getLabel_jaroWinkler() {
        return label_jarowinkler;
    }
    
    
     public void setLabel_soundex(double label_soundex) {
        this.label_soundex = label_soundex;
    }

    public double getLabel_soundex() {
        return label_soundex;
    }
    public void setDiscoveryhubRel(double discoveryhubRel) {
        this.discoveryhubRel = discoveryhubRel;
    }

    public double getDiscoveryhubRel() {
        return discoveryhubRel;
    }

    
     public void setGlobalsim(double globalsim) {
        this.globalsim = globalsim;
    }

    public double getGlobalsim() {
        return globalsim;
    }
    
    public void setMilneRel_in(double milneRel_in) {
        this.milneRel_in = milneRel_in;
    }

    public double getMilneRel_in() {
        return milneRel_in;
    }
    
    
    
    public void setPmiRel(double pmiRel) {
        this.pmiRel = pmiRel;
    }

    public double getPmiRel() {
        return pmiRel;
    }
    
    
    public void setNaviveRel(double naviveRel) {
        this.naviveRel = naviveRel;
    }

    public double getNaviveRel() {
        return naviveRel;
    }
    
    
            
    public void setJacardRel(double jacardRel) {
        this.jacardRel = jacardRel;
    }

    public double getJacardRel() {
        return jacardRel;
    }
    
    
            
    public void setMilRel_inout(double milRel_inout) {
        this.milRel_inout = milRel_inout;
    }

    public double getMilRel_inout() {
        return milRel_inout;
    }
    
}
