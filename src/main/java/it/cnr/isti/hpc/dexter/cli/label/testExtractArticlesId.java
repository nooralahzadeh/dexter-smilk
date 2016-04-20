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
package it.cnr.isti.hpc.dexter.cli.label;

import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;
import it.cnr.isti.hpc.dexter.util.TitleRedirectId;
import it.cnr.isti.hpc.io.reader.JsonRecordParser;
import it.cnr.isti.hpc.io.reader.RecordParser;
import it.cnr.isti.hpc.io.reader.RecordReader;
import it.cnr.isti.hpc.wikipedia.article.Article;
import it.cnr.isti.hpc.wikipedia.reader.filter.TypeFilter;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import static net.didion.jwnl.data.PointerType.USAGE;

/**
 *
 * @author fnoorala
 */
public class testExtractArticlesId  {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        // TODO code application logic here
        String input="/user/fnoorala/home/NetBeansProjects/dexter/dexter-core/en.data/enwikipedia-dump.json.gz";
        //String out="/user/fnoorala/home/NetBeansProjects/dexter/dexter-core/data/title-redirect-id.tsv";
       // PrintWriter writer = new PrintWriter(out, "UTF-8");

	
        RecordReader<Article> reader = new RecordReader<Article>(input,
				new JsonRecordParser<Article>(Article.class))
				.filter(TypeFilter.STD_FILTER);
        RecordParser<TitleRedirectId> encoder = new TitleRedirectId.Parser();
		for (Article a : reader) {
			// System.out.println(encoder.encode(new TitleRedirectId(a)));
                         //writer.println(encoder.encode(new TitleRedirectId(a)));
		}

//writer.close();
    }

   
    
}
