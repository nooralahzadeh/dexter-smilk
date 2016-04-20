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
package fr.inria.wimmics.smilk.nerd;

import fr.inria.wimmics.smilk.annotation.datasets.SmilkNIF;
import static fr.inria.wimmics.smilk.annotation.datasets.SmilkNIF.readOKEAnnotation;
import fr.inria.wimmics.smilk.annotation.wikipedia.AnnotatedSpot;
import fr.inria.wimmics.smilk.eval.AssessmentRecord;
import it.cnr.isti.hpc.text.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.riot.Lang;

/**
 *
 * @author fnoorala
 */
public class ExtractedMentions {
    List<AssessmentRecord> records;
    
    public ExtractedMentions(String path,String type) {
        if(type.equalsIgnoreCase("OKE")){
            records = SmilkNIF.readOKEAnnotation(path+"/OKE/evaluation-dataset-task_1.tll", Lang.TTL, true);
        }else if(type.equalsIgnoreCase("KORE"))
          records = SmilkNIF.readKOREAnnotation(path+"/KORE50_AIDA/kore50-nif-2014-11-03.ttl", Lang.TTL, true);
        else
            System.out.println("Not define file type for annotation");
    }
    
   
    
  public  List<Token> extract(String document)  {
      
      List<Token> tokens=new ArrayList<Token>();
       for (AssessmentRecord record : records) {
           if(record.text.equalsIgnoreCase(document)){
              
               
               for(AnnotatedSpot s:record.getAnnotatedSpot()){
                   String type="MISC";
                   if(s.getType()!=null){
                     type=(s.getType().toUpperCase().equalsIgnoreCase("PLACE"))? "LOCATION": s.getType().toUpperCase();
                   }

                   Token t=new Token(s.getSpot(), s.getStart(), s.getEnd(), "NNP",type);
                    t.setPosTags(Arrays.asList("NNP"));
                    t.setSubTokens(Arrays.asList(t));
                    t.setNESubTokens(Arrays.asList(t));       
                   tokens.add(t);
                    
               }
            }
           
       }     
       return tokens;
  }
  
}
