#!/bin/bash

#Runs Mu2 sessions between $MIN and $MAX (inclusive). Will output fuzz-results to $DIR/target/$TARGETNAME-fuzz-results/mu2/
#example usage:
#  ./runMu2Time.sh mu2 diff.DiffTest testTimSort sort.TimSort diff.DiffTest,sort \
#     TimSort 180m 2 3 ../../sort-benchmarks

fuzzMu2Time() {
    echo mvn mu2:diff -Dclass=$2 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dout=$6-fuzz-results/$1/exp_$7 -DrandomSeed=$7 -Dtime=$8 -DoptLevel=EXECUTION
    mvn mu2:diff -Dclass=$2 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dout=$6-fuzz-results/$1/exp_$7 -DrandomSeed=$7 -Dtime=$8 -DoptLevel=EXECUTION
}

if [ $# -lt 9 ]; then
	echo "Usage: $0 CONFIG DIFFMETHOD INCLUDES TARGETINCLUDES TARGETNAME MIN MAX DIR" > /dev/stderr
	exit 1
fi

CONFIG=$1
CLASS=$2
DIFFMETHOD=$3
INCLUDES=$4
TARGETINCLUDES=$5
TARGETNAME=$6
TIME=$7
MIN=$8
MAX=$9
DIR=${10}

CURDIR=$(pwd)

cd $DIR

for i in $(seq $MIN 1 $MAX)
do
    fuzzMu2Time $CONFIG $CLASS $DIFFMETHOD $INCLUDES $TARGETINCLUDES $TARGETNAME $i $TIME
done
wait
