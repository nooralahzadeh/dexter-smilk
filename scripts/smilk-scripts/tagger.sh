
#!/usr/bin/env bash


#create the jar file with all dependency by mvn assembly:assembly
mvn -Dmaven.test.skip=true assembly:assembly

source /user/fnoorala/home/NetBeansProjects/dexter/dexter-core/scripts/smilk-scripts/config.sh

mkdir -p $PREDICTION


echo "spoting and linking "
$JAVAPREDICTION fr.inria.wimmics.smilk.cli.SmilkCLI   --input $GOLDENTRUTH  --output $PREDICTION


NEWLINE=$'\n'

#all goldentruch files in one file
GOLDEN=$GOLDENTRUTH/GOLDENS.json
rm -f $GOLDEN

#check if the file exist in prediction folder
(for i in $GOLDENTRUTH/*.json ;do
	filename=$(basename "$i" .json)
	if [ -e "$PREDICTION/$filename.json" ];then
         cat $i ; echo $NEWLINE ; 
      fi
done )> $TMP

head -c-1 $TMP > $GOLDEN
 

#all prediction files in one file
PREDICTIONS=$PREDICTION/PREDICTIONS.json
rm -f $PREDICTIONS
( for i in $PREDICTION/*.json ; do cat $i ; echo $NEWLINE ; done ) >  $TMP
head -c-1 $TMP > $PREDICTIONS

