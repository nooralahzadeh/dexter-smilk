/**
 *  Copyright 2012 Diego Ceccarelli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package it.cnr.isti.hpc.dexter.spot;

import it.cnr.isti.hpc.dexter.common.Field;
import it.cnr.isti.hpc.dexter.shingle.Shingle;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.QuotesCleaner;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.cleaner.UnderscoreCleaner;
import it.cnr.isti.hpc.text.MyPair;
import it.cnr.isti.hpc.text.PosTagger;
import it.cnr.isti.hpc.text.Token;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ContextExtractor extract the context of a spot (i.e., text around the the
 * spot)
 * 
 * 
 * @author Diego Ceccarelli, diego.ceccarelli@isti.cnr.it created on 24/lug/2012
 */
public class ContextExtractor {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(ContextExtractor.class);

	private int windowSize ;
	protected String text;
        private int textSize ;
	protected List<Integer> positions = new ArrayList<Integer>();
        List<MyPair> sentenceOffsets=new ArrayList<MyPair>();
        
	  private static SpotManager content_cleaner = new SpotManager();

    static {
        content_cleaner.add(new UnderscoreCleaner());
        content_cleaner.add(new QuotesCleaner());
    }
	private final int MAX_TERM_NUMBER=1000;

	protected ContextExtractor() {

	}

	public ContextExtractor(Field field) {
		//this.text = SpotManager.cleanText(field.getValue());
                this.text = field.getValue();
		init(text);
	}
	
	public ContextExtractor(String text){
		this.text = text;
		init(text);
	}
        
        
        public ContextExtractor(String text, Boolean t){
		this.text = text;
		initialize(text);
	}
	
	private String cleanContext(String context){
		context = context.replaceAll("[\\[\\]]"," ");
		return context;
	}

	protected int closest(int key, List<Integer> list) {
		int size = list.size();
		int delta = size / 2;
		int i = size / 2;
		while (delta > 1) {
			logger.debug("i = {} \t delta = {} ", i, delta);

			int elem = list.get(i);
			delta = (delta % 2 == 0) ? (delta) / 2 : (delta + 1) / 2;
			if (elem == key)
				return i;
			if (elem > key) {
				i = Math.max(i - delta, 0);
			} else {
				i = Math.min(delta + i, size - 1);
			}

		}
		int elem = list.get(i);
		if (elem > key)
			return i - 1;
		return i;
	}

	public String getContext(int l, int r) {
		int start = Math.max(0, l - windowSize / 2);
		int end = Math.min(positions.size() - 1, r + windowSize / 2);
                 String context=text.substring(positions.get(start), positions.get(end)).trim();
                logger.info("context from {} to {} is {} ",l,r);
                System.out.println(context);
		return context;
	}
        
        //<fnoorla>
        public String getCntxt(int l, int r) {
		int start = Math.max(0, l - windowSize / 2);
		int end = Math.min(text.length()- 1, r + windowSize / 2);
                String context=text.substring(start,  end).trim();
               
		return context;
	}
         
        public String getCntxtSentence(SpotMatch match) {
             int windowCenter=0;
              for(int i=0;i<sentenceOffsets.size();i++){
                  if(sentenceOffsets.get(i).getFirst()==match.getStartSE() && sentenceOffsets.get(i).getSecond()==match.getEndSE()){
                      windowCenter=i;
                      break;
                    }
              }
              
              
              
                int size=sentenceOffsets.size()-1;
                int ldelta = windowSize / 2;
                int rdelta = windowSize / 2;
               
                if (windowCenter < ldelta) {
                    rdelta += ldelta - windowCenter;
                    ldelta = windowCenter;
                }
                
                if (rdelta + windowCenter > size) {
                    ldelta += (rdelta + windowCenter) - size;
                    rdelta = size - windowCenter;
                }
                
                int start = Math.max(windowCenter - ldelta, 0);
                int end = Math.min(windowCenter + rdelta, size);    
                
                int contextStart=sentenceOffsets.get(start).getFirst();
                int contextEnd=sentenceOffsets.get(end).getSecond();
                
                return content_cleaner.clean(text.substring(contextStart, contextEnd)).replaceAll("\\p{Punct}", " ");
                
                
         }
        
        //</fnoorla>
	public String getContext(String label) {
		StringBuilder sb = new StringBuilder();
		int terms = 0;
		for (int i = -1; (i = text.indexOf(label, i + 1)) != -1;) {
			sb.append(getContext(termPos(i), termPos(i + label.length() + 1)));
			sb.append(" ");
			terms += windowSize;
			if (terms >= MAX_TERM_NUMBER){
				break;
			}
			
		}
		String context = cleanContext(sb.toString());
              
		return context.trim();
	}

	public int getWindowSize() {
		return windowSize;
	}
        
   
      
        
       public  void  setTokSen_StartEnd(Token t){
            
            for(int i=0;i<sentenceOffsets.size();i++){
                  if(sentenceOffsets.get(i).getFirst()<=t.getStart() && sentenceOffsets.get(i).getSecond()>=t.getEnd()){
                       int start=sentenceOffsets.get(i).getFirst();
                       int end=sentenceOffsets.get(i).getSecond();
                       t.startSE=start;
                       t.endSE=end;
                       
                      break;
                    }
              }
       }
            
   public  void  setTokSen_StartEnd(Shingle t){
            
            for(int i=0;i<sentenceOffsets.size();i++){
                  if(sentenceOffsets.get(i).getFirst()<=t.getStart() && sentenceOffsets.get(i).getSecond()>=t.getEnd()){
                       int start=sentenceOffsets.get(i).getFirst();
                       int end=sentenceOffsets.get(i).getSecond();
                       t.startSE=start;
                       t.endSE=end;
                       
                      break;
                    }
              }
            
        }
	protected void init(String text) {
		int len = text.length();
		positions.add(0);
		for (int i = 0; i < len; i++) {
			if (text.charAt(i) == ' ') {
				positions.add(i++);
				while ((i < len)
						&& ((text.charAt(i) == ' ') || (text.charAt(i) == '.')
								|| (text.charAt(i) == ',') || (text.charAt(i) == ';')))
					i++;
			}
		}
		positions.add(text.length());
	}

        protected void initialize(String text) {
             PosTagger ps = PosTagger.getInstance();
             sentenceOffsets=ps.extractSentecnesOffset(text);
             textSize=sentenceOffsets.get(sentenceOffsets.size()-1).getSecond();
	}
        
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	private int termPos(int pos) {
		// return Collections.binarySearch(positions,pos);
		return closest(pos, positions);

	}
        
        
        

}
