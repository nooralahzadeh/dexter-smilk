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
package it.cnr.isti.hpc.dexter.cli.label;

import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;
import it.cnr.isti.hpc.dexter.util.TitleRedirectId;
import it.cnr.isti.hpc.io.reader.RecordReader;
import it.cnr.isti.hpc.log.ProgressLogger;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add redirects takes a file containing a list 
 * of serialized TitleRedirectId sorted by title, 
 * and create a file containing only the mapping 
 * between the redirect text (in the first column, instead 
 * of the second) 
 * and id of the target article. In this way the redirect 
 * text is indexed in order to point to the id of the 
 * target entity. 
 */
public class testAddRedirectIdCLI1  {
	


	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
            
         int key=  Integer.parseInt("-3632887");
        String in="/user/fnoorala/home/NetBeansProjects/dexter/dexter-core/data/title-redirect-id.tsv";
        String out="/user/fnoorala/home/NetBeansProjects/dexter/dexter-core/data/tmp/tmp1";

        PrintWriter writer = new PrintWriter(out, "UTF-8");
		
		
		RecordReader<TitleRedirectId> reader = new RecordReader<TitleRedirectId>(
				in, new TitleRedirectId.Parser());
		
		String currentTitle = "";
		Integer currentId = -1;
		Iterator<TitleRedirectId> iterator = reader.iterator();
		TitleRedirectId article = null;
		while (iterator.hasNext()) {
			
			article = iterator.next();

			if (!article.isRedirect()) {
				// real article
				currentTitle = article.getTitle();
				currentId = Integer.parseInt(article.getId());
				writer.println(article.getTitle() + "\t\t" + currentId);
			} else {
				// redirect
				if (currentId == -1)
					continue;
				if (article.getTitle().equals(currentTitle)) {
					writer.println(article.getRedirect() + "\t\t"
							+ currentId);
				}

			}
		}
		writer.close();
	}

	

}
