
package fr.inria.wimmics.smilk.cli;

import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;
import it.cnr.isti.hpc.io.reader.JsonRecordParser;
import it.cnr.isti.hpc.io.reader.RecordReader;
import it.cnr.isti.hpc.log.ProgressLogger;
import it.cnr.isti.hpc.wikipedia.article.Article;
import approxmatch.ApproximateMatcher;
import it.cnr.isti.hpc.dexter.util.DexterParams;

/**
 * Takes the JSON dump and produce a approximate match index file containing, a
 *
 * @author fnoorala
 */
public class IndexingForApproximateMatchCLI extends AbstractCommandLineInterface {

    private static String[] params = new String[]{INPUT};

    private static final String USAGE = "java -cp $jar "
            + IndexingForApproximateMatchCLI.class
            + " -input spot.csv";
  
      private static final DexterParams dexterParams = DexterParams.getInstance();
     

    public IndexingForApproximateMatchCLI(String[] args) {
        super(args, params, USAGE);
    }

    public static void main(String[] args) {
        
        String path=dexterParams.getDefaultModel().getPath();
        ApproximateMatcher apm = new ApproximateMatcher();
        IndexingForApproximateMatchCLI cli = new IndexingForApproximateMatchCLI(args);
        ProgressLogger pl = new ProgressLogger("dumped {} titles", 10000);
        cli.openInput();
       // RecordReader<Article> reader = new RecordReader<Article>(
              //  cli.getInput(), new JsonRecordParser<Article>(Article.class));     
        
        apm.setIndexFile(path+"/fmindex_fmi");
        String line;
        String currentSpot="";
        while ((line = cli.readLineFromInput()) != null) {

			String[] elems = line.split("\t");
			 
			if (!elems[0].equals(currentSpot)) {
				pl.up();
				currentSpot = elems[0];
				 apm.addString(currentSpot);
				
			}
			// }
		}
        
//        for (Article a : reader) {
//            pl.up();
//           
//            if (a.getType() == Article.Type.ARTICLE) {
//                 apm.addString(a.getTitle()); 
//
//            }
//        }
         apm.commit();
         cli.closeInput();
    }
}
