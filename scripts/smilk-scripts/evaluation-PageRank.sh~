
#!/usr/bin/env bash
 
#create the jar file with all dependency by mvn assembly:assembly
#mvn -Dmaven.test.skip=true assembly:assembly

source ./scripts/smilk-scripts/config.sh

LOG=DEBUG


xmlconfige=dexter-conf.xml

#Preparing dataset to test : annotation directory
ANNOTATION_FOLDER=$DATA_DIR/KORE50_AIDA

#Annotation result folder
ANNOTATION_RESULT=/user/fnoorala/home/NetBeansProjects/neleval/TAK14


fromPath=$ANNOTATION_RESULT/eval/
toPath=$ANNOTATION_FOLDER/eval

result=$ANNOTATION_RESULT/eval/system.evaluation
#http://dbpedia.org/sparql

#http://dbpedia.org/sparql
endpoint="http://dbpedia.org/sparql"
xmlstarlet ed -L -u "/config/models/model[name='en']/sparqlendpoint" -v $endpoint  $xmlconfige

## declare an array variable
declare -a spotter=("oketokens")
declare -a alpha=("0.4")
dis="pagerank"
label="levenshtein"

declare -a relatedness=("discoveryHub" "milnewitten")


#Robust disambiguation of Named Entity linking in text (Max-Plank) 

it=100

## now loop through the above array

for s in "${spotter[@]}"
do
for alph in "${alpha[@]}"
do
# relatedness
for rel in "${relatedness[@]}"
do
echo "$rel"
xmlstarlet ed -L -u "/config/taggers/tagger[name='smilk']/relatedness" -v $rel  $xmlconfige

echo "$s"
xmlstarlet ed -L -u "/config/taggers/tagger[name='smilk']/spotter" -v $s  $xmlconfige


# lablesimilarity
echo "$label"
xmlstarlet ed -L -u "/config/similarities/similarity/labelsimilarity" -v $label  $xmlconfige

# disambiguator					
echo "$dis"
xmlstarlet ed -L -u "/config/taggers/tagger[name='smilk']/disambiguator" -v $dis  $xmlconfige	

# alpha 
echo "$alph"				
xmlstarlet ed -L -u "/config/disambiguators/disambiguator[name='$dis']/params/param[name='alpha']/value" -v $alph  $xmlconfige

# iteration 

echo "$it"
xmlstarlet ed -L -u "/config/disambiguators/disambiguator[name='$dis']/params/param[name='max-iteration']/value" -v $it  $xmlconfige	
					
echo "spoting and linking "
$JAVAP   fr.inria.wimmics.smilk.cli.SmilkKOREAnnotationTAK14CLI   $ANNOTATION_FOLDER/$1    $ANNOTATION_RESULT   $2

echo "evaluation"
cd ../../neleval
sh ./scripts/run_tac14_evaluation.sh  $ANNOTATION_RESULT/gold.xml $ANNOTATION_RESULT/gold.tab $ANNOTATION_RESULT/system/  $ANNOTATION_RESULT/eval/  4
temp=$toPath/'KORE-ent2vec-tfidf'.$s.$rel.$label.$dis.$alpha.$beta.$it.txt
cp  $result  $temp	
#cd "$fromPath" && xargs mv -t "$toPath" < "$result"
echo -e "${s}\t${rel}\t${label}\t${dis}\t${alpha}\t${beta}\t${it}" | cat - $temp > t && mv t $temp
#sed -i -e "1i \${s} \${ner} " "$temp"
#rm   $ANNOTATION_RESULT/system/system.tab
cd ../dexter/dexter-core
done
done
done
									

