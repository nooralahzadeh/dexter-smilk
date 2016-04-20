
#!/usr/bin/env bash
 
#create the jar file with all dependency by mvn assembly:assembly
#mvn -Dmaven.test.skip=true assembly:assembly

source ./scripts/smilk-scripts/config.sh

LOG=DEBUG


xmlconfige=dexter-conf.xml

#Preparing dataset to test : annotation directory
ANNOTATION_FOLDER=$DATA_DIR/KORE50_AIDA

#Annotation result folder
ANNOTATION_RESULT=/home/fnoorala/NetBeansProjects/neleval/TAK14


fromPath=$ANNOTATION_RESULT/eval/
toPath=$ANNOTATION_FOLDER/eval

result=$ANNOTATION_RESULT/eval/system.evaluation

endpoint="http://dbpedia.org/sparql"

xmlstarlet ed -L -u "/config/models/model[name='en']/sparqlendpoint" -v $endpoint  $xmlconfige

## declare an array variable
declare -a spotter=("oketokens")
declare -a nerd=("stanfordwithoutcoref")
declare -a disambiguator=("pagerank")
declare -a relatedness=("discoveryHub" "discovery-milnewitten")
declare -a lablesimilarity=("levenshtein")
declare -a contextsimilarity=("tfidf-similarity-filter")


#Robust disambiguation of Named Entity linking in text (Max-Plank) 
declare -a alpha=(0.43)
declare -a beta=(0.47)
if [ "$s" == "namedEntity-dbpedia" ]
then
	index=1
else
	index=3
fi

## now loop through the above array
for s in "${spotter[@]}"
do	
echo "$s"
xmlstarlet ed -L -u "/config/taggers/tagger[name='smilk']/spotter" -v $s  $xmlconfige
if [ "$s" == "namedEntity" ]
then 
xmlstarlet ed -L -u "config/spotters/spotter[name='$s']/params/param[name='tools']/value" -v $ner  $xmlconfige
# relatedness
echo "$rel"
xmlstarlet ed -L -u "/config/taggers/tagger[name='smilk']/relatedness" -v $rel  $xmlconfige
# lablesimilarity
echo "$label"
xmlstarlet ed -L -u "/config/similarities/similarity/labelsimilarity" -v $label  $xmlconfige
# contextsimilarity
echo "$cntx"
xmlstarlet ed -L -u "/config/spotters/spotter[name='$s']/filters/filter[$index]/name" -v $cntx  $xmlconfige
# disambiguator					
echo "$dis"
xmlstarlet ed -L -u "/config/taggers/tagger[name='smilk']/disambiguator" -v $dis  $xmlconfige	
echo $alpha				
xmlstarlet ed -L -u "/config/disambiguators/disambiguator[name='$dis']/params/param[name='alpha']/value" -v $palph  $xmlconfige
# beta 
echo "$beta"
xmlstarlet ed -L -u "/config/disambiguators/disambiguator[name='$dis']/params/param[name='beta']/value" -v $beta  $xmlconfige
# iteration 
it= 100
echo "$it"
xmlstarlet ed -L -u "/config/disambiguators/disambiguator[name='$dis']/params/param[name='max-iteration']/value" -v $it  $xmlconfige						
echo "spoting and linking "
$JAVAP   fr.inria.wimmics.smilk.cli.SmilkKOREAnnotationTAK14CLI   $ANNOTATION_FOLDER/$1    $ANNOTATION_RESULT   $2
echo "evaluation"
cd ../../neleval
sh ./scripts/run_tac14_evaluation.sh  $ANNOTATION_RESULT/gold.xml $ANNOTATION_RESULT/gold.tab $ANNOTATION_RESULT/system/  $ANNOTATION_RESULT/eval/  4
temp=$toPath/'temp'.$s.$ner.$rel.$label.$cntx.$dis.$palph.$beta.$it.txt
cp  $result  $temp	
#cd "$fromPath" && xargs mv -t "$toPath" < "$result"
echo -e "${s}\t${ner}\t${rel}\t${label}\t${cntx}\t${dis}\t${palph}\t${beta}\t${it}" | cat - $temp > t && mv t $temp
#sed -i -e "1i \${s} \${ner} " "$temp"
rm   $ANNOTATION_RESULT/system/system.tab
cd ../dexter/dexter-core
									

