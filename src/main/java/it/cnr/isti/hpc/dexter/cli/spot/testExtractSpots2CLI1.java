/**
 *  Copyright 2011 Diego Ceccarelli
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
package it.cnr.isti.hpc.dexter.cli.spot;

import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;
import it.cnr.isti.hpc.dexter.analysis.SpotCleaner;
import it.cnr.isti.hpc.dexter.label.IdHelper;
import it.cnr.isti.hpc.dexter.label.IdHelperFactory;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.io.reader.JsonRecordParser;
import it.cnr.isti.hpc.io.reader.RecordReader;
import it.cnr.isti.hpc.log.ProgressLogger;
import it.cnr.isti.hpc.wikipedia.article.Article;
import it.cnr.isti.hpc.wikipedia.article.Link;
import it.cnr.isti.hpc.wikipedia.reader.filter.TypeFilter;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves all the titles and anchors from the Wikipedia articles, considers
 * only articles, redirects, templates and categories. The output file contains
 * the fields: <br>
 * <br>
 * {@code spot <tab> source id (id article containing the spot) <tab> target id (id of the target) article }
 * <br>
 * <br>
 * In case of a redirect or a title the source id is equal to the target id.
 * Each spot is processed using the {@link SpotManager#getStandardSpotManager()
 * standard spot manager}, which cleans, enriches and filters the text.
 * 
 */
public class testExtractSpots2CLI1 {
	

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException  {
            
		String in="/user/fnoorala/home/NetBeansProjects/SMILK_NERD/wikiDump/wikipedia-dump.json.gz";
                String out="/user/fnoorala/home/NetBeansProjects/dexter/dexter-core/data__/spot/spot-src-target.tsv";
                PrintWriter writer = new PrintWriter(out, "UTF-8");
		
		// Thread.sleep(20000);
		SpotCleaner spotManager = new SpotCleaner();
		IdHelper hp = IdHelperFactory.getStdIdHelper();
		RecordReader<Article> reader = new RecordReader<Article>(
				in, new JsonRecordParser<Article>(Article.class))
				.filter(TypeFilter.STD_FILTER);

		 

		Set<String> spots = new HashSet<String>();

		for (Article a : reader) {
			spots.clear();
			int target = 0;
			int source = a.getWikiId();
			if (a.isRedirect()) {
				target = hp.getId(a.getRedirectNoAnchor());
				spotManager.getAllSpots(a, spots);
                               	for (String spot : spots) {
					if (target == 0) {
						
						continue;
					}
					if (target > 0) {
						// if target > 0, then target is not a disambiguation
						// (disambiguations has id < 0)
						writer.println(spot + "\t" + target + "\t"
								+ target);
					}
				}
				spots.clear();
			} else {

				if (!a.isDisambiguation()) {
					spotManager.enrich(a.getTitle(), spots);
                                        
//                                        if(spots.size()>2){
//                                            System.out.println(new String(a.getWikiTitle().getBytes("UTF-8"),"iso-8859-1")+"\t"+a.getTitle());
//                                            System.out.println(spots);
//                                        }
					for (String spot : spots) {
						writer.println(spot + "\t" + source + "\t"
								+ source);
					}
					spots.clear();
				}
                                if(a.isDisambiguation() && a.getLinks().size()>2){
                                     System.out.println("article \t"+new String(a.getWikiTitle().getBytes("UTF-8"),"iso-8859-1")+"\t"+a.getTitle());
				for (Link l : a.getLinks()) {
					spots.clear();
					spotManager.enrich(l.getDescription(), spots);
					for (String spot : spots) {
						target = hp.getId(l.getCleanId());
						if (target == 0) {
							continue;
						}

						if (hp.isDisambiguation(target)) {	

							continue;
						}
						if (a.isDisambiguation()) {
							// if a is a disambiguation, the label of a is good
							// for all
							// the pointed articles
							Set<String> spots2 = new HashSet<String>();
                                                        
							spotManager.enrich(l.getDescription(), spots2);
                                                       
                                                        System.out.println("link:  "+l.getDescription());
                                                        System.out.println(spots2);
							for (String label : spots2) {
//                                                            int disambig_source= -source;homonymie
                                                            System.out.println(hp.getLabel(-source).replace("_(homonymie)", "") + "\t targetlabel " + l.getCleanId()
										+ "\t targetid " + hp.getId(l.getCleanId()));
						           writer.println(label + "\t" + source 
										+ "\t" + target);

							}
						}

						writer.println(spot + "\t" + source + "\t"
								+ target);

					}
				}
			}
                        }
		}
		writer.close();
	}

	
}
