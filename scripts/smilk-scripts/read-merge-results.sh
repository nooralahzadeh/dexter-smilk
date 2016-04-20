


#!/usr/bin/env bash

rsfolder=/user/fnoorala/home/NetBeansProjects/dexter/en.data/OKE/eval/*.txt
for rs in $rsfolder; do
	#echo $rs
	line=$(head -n 1 $rs)
	echo $line
	while IFS=$'\t' read ptp	fp	rtp	fn	precis	recall	fscore	measure; do
		if [ "$measure" == "strong_mention_match" ] || [ "$measure" == "strong_typed_mention_match" ]  || [ "$measure" == "strong_link_match" ];
	
		then
		echo $ptp	$fp	$rtp	$fn	$precis	$recall	$fscore	$measure
		fi
		
	done < $rs
done
