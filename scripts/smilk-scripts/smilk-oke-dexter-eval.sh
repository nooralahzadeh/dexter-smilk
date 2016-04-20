#!/usr/bin/env bash


#create the jar file with all dependency by mvn assembly:assembly
#mvn -Dmaven.test.skip=true assembly:assembly

source /user/fnoorala/home/NetBeansProjects/dexter/dexter-core/scripts/smilk-scripts/config.sh

#Preparing dataset to test : annotation directory
ANNOTATION_FOLDER=$DATA_DIR/OKE

#Annotation result folder
ANNOTATION_RESULT=$DATA_DIR/TAK14


echo "spoting and linking "
$JAVAPREDICTION fr.inria.wimmics.smilk.cli.SmilkOkeAnnotationTAK14CLI   --input $TRAINING"/"$1  --tmp $TRAINING"/"$2  --output $TRAINING"/"$3 --type $4




METRICS=/user/fnoorala/home/NetBeansProjects/dexter-eval/measure
DEBUG=False
 

 
 

OUTPUT=console

echo "evaluate prediction $PREDICTIONS against $GOLDEN, using comparator $1"
$JAVAEVAL -Ddebug=$DEBUG -Dmetrics=$METRICS/$6  $CLI.EvaluatorCLI -input $TRAINING"/"$3 -gt $TRAINING"/"$2 -cmp $5 -output $OUTPUT 


