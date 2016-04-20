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
package it.cnr.isti.hpc.dexter.shingle;

import it.cnr.isti.hpc.text.Token;
import java.util.ArrayList;

import java.util.List;
import java.util.Set;

/**
 * A Shingle represents a fragment of text in a document to annotate.
 * 
 * 
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Sep 4, 2012
 */
public class Shingle {

	/** cleaned text */
	public String text;
	/** start position in the original text */
	public int start;
	/** end position in the original text */
	public int end;
        
        /** Set of Part of Speech for each*/
         //<fnoorala>
        public int startSE;
        public int endSE;
        public List<String> pos=new ArrayList<String>();
        public List<Token> subTokens=new ArrayList<Token>();
         //</fnoorala>
        public String type="";
        

	public Shingle(List<Token> tokens) {
		StringBuilder sb = new StringBuilder();
		start = tokens.get(0).getStart();
		end = tokens.get(tokens.size() - 1).getEnd();
                // FIX ME, when the gram is just one letter
		for (Token t : tokens)
			 
                            sb.append(t.getText()).append(" ");
                           
                        
		// sb.deleteCharAt(sb.length()-1);
		text = sb.substring(0, sb.length() - 1);
                subTokens=tokens;
	}

        //<fnoorala>
        public Shingle(List<Token> tokens,Boolean a) {
		StringBuilder sb = new StringBuilder();
                
		start = tokens.get(0).getStart();
		end = tokens.get(tokens.size() - 1).getEnd();
		for (Token t : tokens){
			 
			// if(t.getText().length()>1)
                     if(!t.getText().equalsIgnoreCase("l")) {// for l in expemption
                             sb.append(t.getText()).append(" ");
                         }else{
                             
                              sb.append(t.getText()).append("");
                         
                     }
                        pos.add(t.getPos());
                }
		// sb.deleteCharAt(sb.length()-1);
		text = sb.substring(0, sb.length() - 1);
                startSE = tokens.get(0).getStartSE();
		endSE = tokens.get(tokens.size() - 1).getEndSE();
                this.type=type;
	}
        
        
       
        
        //</fnoorala>
        
        public Shingle(String text, int start, int end, int startSE, int endSE) {
		super();
		this.text = text;
		this.start = start;
		this.end = end;
                this.startSE=startSE;
                this.endSE=endSE;
	}
        
	public Shingle(String text, int start, int end) {
		super();
		this.text = text;
		this.start = start;
		this.end = end;
	}

	public Shingle(String text) {
		this(text, -1, -1);
	}

	public boolean isEmpty() {
		return text.isEmpty();
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public void setEnd(int end) {
		this.end = end;
	}

	@Override
	public String toString() {
		return "<" + text + "> [" + start + "," + end +"], " + pos+ "";
	}

	public String originalShingle(String originalText) {
		return originalText.substring(start, end);
	}
        
        
        //<fnoorala>
        /**
	 * @return the start
	 */
	public List<String> getPos() {
		return pos;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setPos(List<String> pos) {
		this.pos = pos;
	}
        
         
	public int getStartSE() {
		return startSE;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStartSE(int startSE) {
		this.startSE = startSE;
	}

	/**
	 * @return the end
	 */
	public int getEndSE() {
		return endSE;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public void setEndSE(int endSE) {
		this.endSE = endSE;
	}
        //<fnoorala>
        
        public List<String> getTypes(){
            List<String> types=new ArrayList<String>();
            for(Token t:subTokens){
                types.add(t.getType());
            }
            return types;
        }
        
         public List<String> getPosTags(){
            List<String> posTags=new ArrayList<String>();
            for(Token t:subTokens){
                posTags.add(t.getPos());
            }
            return posTags;
        }
             
         
           public List<Token> getSubTokens(){
            
            return subTokens;
        }

}
