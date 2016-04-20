
#!/usr/bin/env bash


source scripts/config.sh
mkdir -p $ANNOTATION_FOLDER


$JAVA fr.inria.wimmics.smilk.cli.AnnotationCLI   --output $ANNOTATION_FOLDER

