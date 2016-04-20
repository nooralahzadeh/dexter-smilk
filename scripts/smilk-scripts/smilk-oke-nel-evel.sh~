#!/usr/bin/env bash


source /user/fnoorala/home/NetBeansProjects/dexter/dexter-core/scripts/smilk-scripts/config.sh

#Preparing dataset to test : annotation directory
ANNOTATION_FOLDER=$DATA_DIR/OKE

#Annotation result folder
ANNOTATION_RESULT=/user/fnoorala/home/NetBeansProjects/neleval/TAK14



echo "spoting and linking "
$JAVAP   fr.inria.wimmics.smilk.cli.SmilkOkeAnnotationTAK14CLI   $ANNOTATION_FOLDER/$1    $ANNOTATION_RESULT   $2


echo "evaluation"
cd ../../neleval

sh ./scripts/run_tac14_evaluation.sh  $ANNOTATION_RESULT/gold.xml $ANNOTATION_RESULT/gold.tab $ANNOTATION_RESULT/system/  $ANNOTATION_RESULT/eval/  4


