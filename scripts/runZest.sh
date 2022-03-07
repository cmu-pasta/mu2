#!/bin/bash

#Runs Zest sessions between $MIN and $MAX (inclusive). Will output fuzz-results to $DIR/target/$TARGETNAME-fuzz-results/zest
#example usage:
#  ./runZest.sh diff.DiffTest fuzzTimSort TimSort 2 3 180m $HOME/Work/sort-benchmarks/

fuzzZest() {
    echo mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dout=$3-fuzz-results/zest/exp_$4 -Dengine=zest -DrandomSeed=$4 -Dtime=$TIME
    mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dout=$3-fuzz-results/zest/exp_$4 -Dengine=zest -DrandomSeed=$4 -Dtime=$TIME
}

if [ $# -lt 8 ]; then
	echo "Usage: $0 CONFIG FUZZMETHOD TARGETNAME MIN MAX TIME DIR" > /dev/stderr
	exit 1
fi

CONFIG=$1
CLASS=$2
FUZZMETHOD=$3
TARGETNAME=$4
MIN=$5
MAX=$6
TIME=$7
DIR=$8

CURDIR=$(pwd)

cd $DIR
for i in $(seq $MIN 1 $MAX)
do
    fuzzZest $CLASS $FUZZMETHOD $TARGETNAME $i $TIME
done
wait
