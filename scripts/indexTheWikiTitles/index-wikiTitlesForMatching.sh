#!/usr/bin/env bash

source scripts/config.sh
in=/user/fnoorala/home/NetBeansProjects/dexter/dexter-core/en.data/spot/spots-doc-freq.tsv

echo "indexing the titles for matching "
$JAVA fr.inria.wimmics.smilk.cli.IndexingForApproximateMatchCLI --input $in
 echo "indexing DONE!"	
