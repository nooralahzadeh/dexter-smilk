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
package it.cnr.isti.hpc.text;

import java.util.List;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 * Created on Sep 4, 2012
 */
public class Token {
	
	String text;
	int start;
	int end;
      
        String type;
        String[] types;
        List<String> posTags;
        List<Token> tokens;
        List<Token> NEtokens;
    
        
        
        
	public Token(int start, int end) {
		super();
		this.start = start;
		this.end = end;
	}
	
	public Token(String text, int start, int end) {
		super();
		this.text = text;
		this.start = start;
		this.end = end;
	}
        
        //<fnoorala>
        
       public int startSE;
       public int endSE;
       public String pos;
        
       //for Coref
        
       int tokenIndex;
       int sentIndx;
	 
    public Token(String text, int start, int end, String pos, int startSE, int endSE) {
		
		this.text = text;
		this.start = start;
		this.end = end;
                this.pos=pos;
                this.startSE=startSE;
                this.endSE=endSE;
	}
        
        
        public Token(String text, String type ,int start, int end){
		
		this.text = text;
		this.start = start;
		this.end = end;
                this.type=type;
               	}
                
        public Token(String text, int start, int end, String pos,String type) {
		 
		this.text = text;
		this.start = start;
		this.end = end;
                this.pos=pos;
                this.type=type;
	}
        
        
	//</fnoorala>
	public boolean isEmpty(){
		return text.isEmpty();
	}
	
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @param text the text to set
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
	 * @param start the start to set
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
	 * @param end the end to set
	 */
	public void setEnd(int end) {
		this.end = end;
	}
	
	public String toString(){
		return "Token["+text+"] POS: "+getPos()+ " TYPE: "+type+ " ("+start+","+end+")";
                        
	}
	

        
        /**
	 * @return the pos
	 */
	public String getPos() {
		return pos;
	}

        /**
	 * @param pos the start to set
	 */
	public void setPos(String pos) {
		this.pos = pos;
	}
        
        /**
	 * @return sentence start
	 */
	public int getStartSE() {
		return this.startSE;
	}

        /**
	 * @param sratr of sentence
	 */
	public void setStartSE(int startSE) {
		this.startSE =startSE;
	}
 
         /**
	 * @return sentence end
	 */
	public int getEndSE() {
		return this.endSE;
	}

        /**
	 * @param end of sentence
	 */
	public void setEndSE(int EndSE) {
		this.endSE =EndSE;
	}
        
        /**
	 * @param type of token
	 */
	public void setType(String type) {
		this.type =type;
	}
        
         /**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
         
        
        public void setTypes(String[] types) {
		this.types =types;
	}
        
          
	public String[] getTypes() {
		return types;
	}
        
        
        public void setTokenIndex(int tokenIndex) {
		this.tokenIndex = tokenIndex;
        }
	public int getTokenIndx() {
		return tokenIndex;
	}
        
        public void setSentIndx(int sentIndx) {
		this.sentIndx = sentIndx;
        }
	public int getSentIndx() {
		return sentIndx;
	}
        
         public void setPosTags(List<String> posTags) {
		this.posTags = posTags;
        }
	public List<String>  getPosTags() {
		return posTags;
	}
        
         public void setSubTokens(List<Token> tokens) {
		this.tokens = tokens;
        }
	public List<Token>  getSubTokens() {
		return tokens;
	}
        
         public void setNESubTokens(List<Token> tokens) {
		this.NEtokens = tokens;
        }
	public List<Token>  getNESubTokens() {
		return NEtokens;
	}
        
    
        @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Token other = (Token) obj;
		if (this.getStart() != other.getStart() && this.getEnd()!=other.getEnd())
			return false;
		return true;
	}
}
