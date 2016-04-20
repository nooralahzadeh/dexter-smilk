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
package fr.inria.wimmics.smilk.cli.spot;

import fr.inria.wimmics.smilk.util.ListFilesUtil;
import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;

import it.cnr.isti.hpc.dexter.cli.spot.WriteOneSpotPerLineCLI;
import it.cnr.isti.hpc.dexter.label.IdHelper;
import it.cnr.isti.hpc.dexter.label.IdHelperFactory;
import it.cnr.isti.hpc.dexter.spot.Spot;
import it.cnr.isti.hpc.dexter.spot.SpotReader;
import it.cnr.isti.hpc.dexter.spot.SpotReader.SpotSrcTarget;
import it.cnr.isti.hpc.dexter.spot.SpotReader.SpotSrcTargetParser;
import it.cnr.isti.hpc.dexter.spot.clean.SpotManager;
import it.cnr.isti.hpc.dexter.spot.cleanpipe.filter.CommonnessFilter;
import it.cnr.isti.hpc.io.reader.RecordParser;
import it.cnr.isti.hpc.io.reader.RecordReader;
import it.cnr.isti.hpc.log.ProgressLogger;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets in input a wikilink files containing :
 *
 * (the output of {@link GenerateSpotDocumentFrequencyCLI}) where df(spot) is
 * the number of documents containing the text of the spot (as anchor or simple
 * text). <br/>
 * <br/>
 * Produces in output a file containing: <br/>
 * <br/> null {@code <spot> <tab> <target entities>
 * <tab> <link(spot)> <tab> <df(spot)>} <br/>
 * <br/>
 *
 * where <code>link(spot)</code> is the number of documents that contains the
 * spot as an anchor.
 *
 */
/**
 *
 * @author fnoorala
 */
public class GenerateSpotsWikilinks extends AbstractCommandLineInterface {

    /**
     * @param args the command line arguments
     */
    /**
     * Logger for this class
     */
//private static final Logger logger = LoggerFactory
    //.getLogger(GenerateSpotsWikilinks.class);
    private static String[] params = new String[]{INPUT, OUTPUT};

    private static final String USAGE = "java -cp $jar "
            + GenerateSpotsWikilinks.class
            + " -input wikilinksFolder spot-src-target";
    static SpotSrcTarget currentSST;

    static Iterator<SpotSrcTarget> spotSrcTargetIterator;

    private static IdHelper helper = IdHelperFactory.getStdIdHelper();

    static int i = 0;

    static SpotManager spotManager = SpotManager.getStandardSpotManager();

    public static void main(String[] args) {

        // TODO code application logic here
        ListFilesUtil listfiles = new ListFilesUtil();

        GenerateSpotsWikilinks cli = new GenerateSpotsWikilinks(args);

        String folder = cli.getInput();
        ProgressLogger progress = new ProgressLogger(
                "processed {} distinct spots", 100000);

        listfiles.listFiles(folder);

        for (String file : listfiles.files) {

            String input = folder + "/" + file;

            RecordReader<SpotSrcTarget> reader = new RecordReader<SpotSrcTarget>(
                    input, new SpotSrcTargetParser());

            cli.openOutput();

            spotSrcTargetIterator = reader.iterator();
            StringBuilder sb = new StringBuilder();

            while (spotSrcTargetIterator.hasNext()) {

                sb.setLength(0);
                progress.up();
                currentSST = spotSrcTargetIterator.next();

                if (currentSST != null) {

                    sb.append(currentSST.getSpot() + "\t" + currentSST.getSrc() + "\t" + currentSST.getTarget());
                    sb.append("\n");
                    // System.out.println(sb.toString());
                    cli.writeInOutput(sb.toString());
                }

            }
            cli.closeOutput();
            //logger.info("done");

        }
    }

    public GenerateSpotsWikilinks(String[] args) {
        super(args, params, USAGE);
    }

    public static class SpotSrcTargetParser implements
            RecordParser<SpotSrcTarget> {

        @Override
        public SpotSrcTarget decode(String record) {
            SpotSrcTarget rec = new SpotSrcTarget();
            Scanner scanner = new Scanner(record).useDelimiter("\t");
            String label = scanner.next();

            if (label.equalsIgnoreCase("URL")) {
                i++;
            }
            if (label.equalsIgnoreCase("MENTION")) {
                rec.setSpot(spotManager.clean(scanner.next()));
                rec.setSrc(i);
                //escape the offset
                scanner.next();
                String wikiname = scanner.next().replace("http://en.wikipedia.org/wiki/", "");
                
                //in case of redirects 
             
                if (wikiname.contains("#")) {
                    
                    int index = wikiname.indexOf("#");
                    wikiname = wikiname.substring(0, index);

                }
                
                try {
                    //System.out.println(wikiname);
                    wikiname = URLDecoder.decode(wikiname, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    java.util.logging.Logger.getLogger(GenerateSpotsWikilinks.class.getName()).log(Level.SEVERE, null, ex);
                }
                rec.setTarget(helper.getId(wikiname));

                return rec;

            } else {
                return null;
            }

        }

        @Override
        public String encode(SpotSrcTarget r) {
            return r.getSpot() + "\t" + r.getSrc() + "\t" + r.getTarget();
        }

    }
}
