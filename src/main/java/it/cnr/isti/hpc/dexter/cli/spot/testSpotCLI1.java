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

import it.cnr.isti.hpc.benchmark.Stopwatch;
import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.common.FlatDocument;
import it.cnr.isti.hpc.dexter.spot.SpotMatch;
import it.cnr.isti.hpc.dexter.spot.SpotMatchList;
import it.cnr.isti.hpc.dexter.spotter.DictionarySpotter;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs the spotting over a document using the {@link DictionarySpotter},
 * prints on the stout the list of the matched spots with their candidate
 * entities.
 * 
 */
public class testSpotCLI1  {
	 
	 

	public static void main(String[] args) {
		 
		String input ="Après une exclusivité de quelques mois chez Monoprix, Essie, la marque de vernis dans le giron de Garnier Gemey-Maybelline sera en vente à partir de mai dans près de 400 points de vente Marionnaud alors que son concurrent de taille OPi (groupe Coty) est distribué chez Sephora.\n" +
"La marque lancée en octobre 2011 dans 185 Monoprix est déjà numéro 2 du marché selon L'Oréal.\n" +
"En parallèle, rappelons que la division des Produits professionnels lance une version à destination des manucures, Essie Professio-nal, où OPI est aussi bien implanté.";
		Document doc = new FlatDocument(input);
		DexterParams dexterParams = DexterParams.getInstance();
		DictionarySpotter spotter = new DictionarySpotter();
		spotter.init(dexterParams, null);
		//Stopwatch stopwatch = new Stopwatch();
		//stopwatch.start("spot");

		SpotMatchList sml = spotter.match(null, doc);
		//stopwatch.stop("spot");
 
		for(SpotMatch smp:sml){
                    System.out.println(smp.getMention());
                }

	}

	 
}
