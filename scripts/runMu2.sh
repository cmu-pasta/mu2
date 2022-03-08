#!/bin/bash

#Runs Mu2 sessions between $MIN and $MAX (inclusive). Will output fuzz-results to $DIR/target/$TARGETNAME-fuzz-results/mu2/
#Requires existing Zest fuzz-results to be saved in $DIR/target/$TARGETNAME-fuzz-results/zest in order to calculate trial limit
#example usage:
#  ./runMu2.sh mu2 diff.DiffTest testTimSort sort.TimSort diff.DiffTest,sort \
#     TimSort 2 3 ../../sort-benchmarks

fuzzMu2() {
    STR=$(tail -n1 target/$6-fuzz-results/zest/exp_$7/plot_data);
    arrSTR=(${STR//,/ });
    VALID=${arrSTR[11]};
    INVALID=${arrSTR[12]};
    NUM_TRIALS=$((VALID+INVALID))
    echo mvn mu2:diff -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -DtargetIncludes=$4 -Dout=$5-fuzz-results/$1/exp_$6 -DrandomSeed=$6 -Dtrials=$NUM_TRIALS -DoptLevel=EXECUTION -Dmu2.PARALLEL=true
    mvn mu2:diff -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -DtargetIncludes=$4 -Dout=$5-fuzz-results/$1/exp_$6 -DrandomSeed=$6 -Dtrials=$NUM_TRIALS -DoptLevel=EXECUTION -Dmu2.PARALLEL=true
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
MIN=$7
MAX=$8
DIR=$9

CURDIR=$(pwd)

cd $DIR

for i in $(seq $MIN 1 $MAX)
do
    fuzzMu2 $CONFIG $CLASS $DIFFMETHOD $INCLUDES $TARGETINCLUDES $TARGETNAME $i
done
wait
