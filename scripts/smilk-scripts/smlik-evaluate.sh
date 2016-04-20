
#!/usr/bin/env bash
source /user/fnoorala/home/NetBeansProjects/dexter/dexter-core/scripts/smilk-scripts/config.sh

#update the config file by following values




#copy the jar file to the proper directory


EXPECTED_ARGS=2

METRICS=/user/fnoorala/home/NetBeansProjects/dexter-eval/measure
DEBUG=False
#DEBUG=True

#source /user/fnoorala/home/NetBeansProjects/dexter/dexter-core/scripts/smilk-scripts/tagger.sh

GOLDEN=$GOLDENTRUTH/GOLDENS.json

PREDICTIONS=$PREDICTION/PREDICTIONS.json
if [ $# -ne $EXPECTED_ARGS  ] && [ $# -ne 3 ]
then
  echo "Usage: `basename $0` predictions[.json,.tsv] goldentruth[.json,.tsv] comparator metrics.txt [output.html]"
  exit $E_BADARGS
fi

OUTPUT=console
if [ $# -ne 2 ]
	then
	echo "output $3"
	OUTPUT=$3
fi


echo "evaluate prediction $PREDICTIONS against $GOLDEN, using comparator $1"
$JAVAEVAL -Ddebug=$DEBUG -Dmetrics=$METRICS/$2 $CLI.EvaluatorCLI -input $PREDICTIONS -gt $GOLDEN -cmp $1 -output $OUTPUT


