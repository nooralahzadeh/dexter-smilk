package fr.inria.wimmics.smilk;

import it.cnr.isti.hpc.benchmark.Stopwatch;
import it.cnr.isti.hpc.dexter.StandardTagger;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 * Given a file containing plain text prints on the stdout the entities detected
 * by the {@link StandardTagger} tagger.
 *
 */
public class SmilkCall {

    public static Stopwatch stopwatch = new Stopwatch();

    // static final String DATASET = "/user/fnoorala/home/NetBeansProjects/dexter/dexter-core/annotation";
    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        FTPClient client = new FTPClient();
//        client.connect("ftp1.viseo.com");
//        client.login("guest_anr-smilk", "V3Zz5Rug75");
//        client.enterLocalPassiveMode();
//        client.setFileType(FTP.BINARY_FILE_TYPE);
//        InputStream fileIn= client.retrieveFileStream("/home/SMILK/EntityLinking/fr.data/lucene");
//        

        String input = "Jennifer Connelly et Shiseido, Marie-Ange Casta et Vichy, Natasha Poly pour L'Oréal Paris (notrephoto), Michelle Yeoh chez Guerlain, Olivia Palermo pour Rochas, ou encore Naomi Watts pour Astalift, la marque de soin de Fujifilm... Pas une semaine ne passe sans que ne tombe l'annonce d'une nouvelle collaboration entre une marque de beauté et une célébrité. Le phénomène des égéries, observé depuis trois ans par CosmétiqueMaget le cabinet d'études LexisNexis, ne cesse de s'amplifier, s'étendant à tous les segments, au-delà du parfum, et à tous les circuits de distribution, y compris la pharmacie. Le dernier classement de la visibilité médiatique des égéries, publié dans CosmétiqueMag daté d'avril, consacre la chanteuse Lady Gaga, reine absolue des citations dans la presse internationale (près de 23 000 articles recensés dans les 250 titres généralistes analysés, loin devant les 10 242 de sa dauphine Beyoncé). Mais aussi de la présence sur les réseaux sociaux, donnée relativement récente qui bouscule la relation entre les stars, leur public et les marques. Lady Gaga informe elle-même ses fans de son actualité à travers ses comptes Facebook, Twitter, Google +, Myspace, sans oublier son site officiel et les pages Youtube. Selon LexisNexis, la diva totalise près de 2 milliards de vues sur Youtube, 47 millions de \"likers\" sur Facebook, 18 millions de \"followers\" sur Twitter et 35 millions de Google blogs. Une caisse de résonance sur laquelle a pu s'appuyer la Fondation Mac contre le sida (groupe Estée Lauder) qui s'est attaché le soutien de l'artiste. Autre exemple, la chanteuse Cheryl Cole, ambassadrice L'Oréal Paris au Royaume-Uni et troisième du classement (8 556 citations), échange volontiers avec ses 'followers\" sur Twitter (1,5 million au total), mais essentiellement sur son actualité personnelle. En dehors de partenariats d'ordre caritatif, on peut penser qu'une communication digitale trop ouvertement commerciale pourrait être mal perçue par les fans.";
        PipelineService pipelineService = new PipelineService("dexter-conf.xml", "fr", "smilk", "namedEntity", "renco", "linear");

        pipelineService.annotate(input);
    }

}
