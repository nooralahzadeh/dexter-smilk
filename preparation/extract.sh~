#!/usr/bin/env bash

source $(pwd)/config.sh


#Generates the mapping Title <-> Id
echo "Mapping Title <-> Id"
echo "export titles and redirects from the dump ($WIKI_JSON)"
$DEXTER_JAVA it.cnr.isti.hpc.dexter.cli.label.ExportArticlesIdCLI -input $WIKI_JSON -output $TITLE_FILE
echo "sort by id"
sort -t"	" -nk3 $TITLE_FILE > $TMP
mv  $TMP  $TITLE_FILE
echo "indexing id -> label"
$DEXTER_JAVA  it.cnr.isti.hpc.dexter.cli.label.IndexIdT   oLabelCLI -input $TITLE_FILE
echo "sort by title"
sort -k1 $TITLE_FILE > $TMP
mv  $TMP $TITLE_FILE

echo "adding redirect"
$DEXTER_JAVA   it.cnr.isti.hpc.dexter.cli.label.AddRedirectIdCLI -input $TITLE_FILE -output $TMP
mv  $TMP  $TITLE_FILE
echo "sort title and redirect"
sort -k1 $TITLE_FILE > $TMP
mv  $TMP  $TITLE_FILE
echo "indexing label -> id"
$DEXTER_JAVA  it.cnr.isti.hpc.dexter.cli.label.IndexLabelToIdCLI -input $TITLE_FILE
rm $TITLE_FILE	
########################################################################################################################
echo "indexing dump ($WIKI_JSON)"
$DEXTER_JAVA  $CLI.index.IndexWikipediaOnLuceneCLI -input $WIKI_JSON 

#########################################################################################################################
echo "extracting spots "
$DEXTER_JAVA it.cnr.isti.hpc.dexter.cli.spot.ExtractSpotsCLI --input $WIKI_JSON --output $TMP
echo "sorting spots by text and target entity"
cat $TMP | sort -t'	' -k1,1 -k3,3n  | uniq > $SPOT

###################################################################################
echo "Generates spots frequencies in $SPOT_DOC_FREQ"

# produce a file in $SPOT_DOC_FREQ containing 
# <spot> \t <df(spot)>
# where df(spot) is the document frequency of the text in wikipedia collection 
$DEXTER_JAVA $CLI.spot.GenerateSpotDocumentFrequencyCLI -input $SPOT -output $SPOT_DOC_FREQ

########################################################################################################################
echo "merging $SPOT and $SPOT_DOC_FREQ in $SPOT_FILE"
$DEXTER_JAVA $CLI.spot.WriteOneSpotPerLineCLI -input $SPOT -freq $SPOT_DOC_FREQ -output $SPOT_FILE

echo "results in $SPOT_FILE"
########################################################################################################################
mkdir -p "$SPOT_FOLDER/ram"
$DEXTER_JAVA it.cnr.isti.hpc.dexter.cli.spot.ram.GenerateSpotsMinimalPerfectHashCLI  -output $SPOT_HASHES

echo "uncompressing spot file $SPOT_FILE "
zcat ${SPOT_FILE/.gz/} > $TTMP

echo "uncompressing hash file $SPOT_HASHES"
gunzip $SPOT_HASHES


SPOT_HASHES=${SPOT_HASHES/.gz/} 

echo "paste the spot file with the hashes"
paste $SPOT_HASHES  $TTMP > $TMP
echo "sorting the file by hash (output in $TTMP)"
sort -nk1 $TMP > $TTMP
cut -f 2,3,4,5,6 $TTMP > $TMP
echo "index spot file and generate offsets"
$DEXTER_JAVA it.cnr.isti.hpc.dexter.cli.spot.ram.IndexSpotFileAndGenerateOffsetsCLI -input $TMP
echo "index offsets using eliasfano"
$DEXTER_JAVA it.cnr.isti.hpc.dexter.cli.spot.ram.IndexOffsetsUsingEliasFanoCLI

echo "delete tmp files"
rm $TMP $TTMP 
rm $SPOT_HASHES
########################################################################################################################

echo "generates incoming and outcoming edges for each node (in $OUT_EDGES and $IN_EDGES) requires $SPOT"
echo "filtering edges"
cat $SPOT | awk -F'	' '{if ($2 > 0 && $3 > 0) print $2"	"$3}'  | sort -nk2,2 -nk1,1 | uniq > $TMP
echo "generating incoming link file in $IN_EDGES"
awk -F'	' 'BEGIN{current=$2; incoming=""} {if ($1 != $2) if ($2 == current) {incoming=incoming$1" "} else { print current"\t"incoming; current=$2; incoming=$1" "} } END {print current"\t"incoming}' $TMP  > $TTMP

#first line is empty, i need to remove it 
sed 1d $TTMP > $TTTMP

echo "compressing"
gzip $TTTMP
mv $TTTMP.gz $IN_EDGES

echo "sorting by sorce"
sort -nk1,1 -nk2,2 $TMP > $TTMP
echo "generating outcoming link file in $OUT_EDGES"
awk -F'	' 'BEGIN{current=$1; incoming=""} {if ($1 != $2) if ($1 == current) {incoming=incoming$2" "} else { print current"\t"incoming; current=$1; incoming=$2" "} } END {print current"\t"incoming}' $TTMP  > $TMP

sed 1d $TMP > $TTMP
gzip $TTMP 
echo "compressing"
mv $TTMP.gz $OUT_EDGES
rm $TMP
########################################################################################################################

echo "index incoming and outcoming edges (in $OUT_EDGES and $IN_EDGES)"

echo "outcoming: "
$DEXTER_JAVA $CLI.graph.IndexOutcomingNodesCLI -input $OUT_EDGES

echo "incoming"
$DEXTER_JAVA $CLI.graph.IndexIncomingNodesCLI -input $IN_EDGES

########################################################################################################################
rm $WIKI_JSON
 
