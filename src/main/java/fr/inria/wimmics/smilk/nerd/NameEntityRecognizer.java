 
package fr.inria.wimmics.smilk.nerd;

import it.cnr.isti.hpc.dexter.*;
import it.cnr.isti.hpc.dexter.common.Document;
import it.cnr.isti.hpc.dexter.entity.EntityMatchList;
import it.cnr.isti.hpc.dexter.util.DexterLocalParams;
import it.cnr.isti.hpc.dexter.util.DexterParams;
import it.cnr.isti.hpc.text.Token;
import java.util.List;

/**
 *  fnrooala
 */
public interface NameEntityRecognizer {

	/**
	 * Takes a Document performs the Named entity recognition.  
	 * 
	 * @param document
	 *            - a document to annotate.
	 * @returns A list of entities detected in the document, an empty list if
	 *          the tagger does not annotate anything.
	 */
	public List<Token> nerd(Document document);

	public List<Token> getContext(); 

	/**
	 * Initializes the NERD with the global params.
	 * 
	 * @param dexterParams
	 *            the global params of the project.
	 * 
	 * @param defaultModuleParams
	 *            the module init params
	 */
	public void init(DexterParams dexterParams,
			DexterLocalParams defaultModuleParams);
}
