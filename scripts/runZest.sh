#!/bin/bash

#Runs Zest sessions between $MIN and $MAX (inclusive). Will output fuzz-results to $DIR/target/$TARGETNAME-fuzz-results/zest
#example usage:
#  ./runZest.sh diff.DiffTest fuzzTimSort TimSort 2 3 180m $HOME/Work/sort-benchmarks/

fuzzZest() {
    echo mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dout=$3-fuzz-results/zest/exp_$4 -Dengine=zest -DrandomSeed=$4 -Dtime=$TIME
    mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dout=$3-fuzz-results/zest/exp_$4 -Dengine=zest -DrandomSeed=$4 -Dtime=$TIME
}

if [ $# -lt 7 ]; then
	echo "Usage: $0 CONFIG PROCS REP FUZZMETHOD TARGETNAME TIME DIR" > /dev/stderr
	exit 1
fi

CONFIG=$1
PROCS=$2
REP=$3
CLASS=$4
FUZZMETHOD=$5
TARGETNAME=$6
TIME=$7
DIR=$8

MIN=$REP
MAX=$REP+$PROCS

CURDIR=$(pwd)

cd $DIR
N=$PROCS
for i in $(seq $MIN 1 $MAX)
do
    fuzzZest $CLASS $FUZZMETHOD $TARGETNAME $i $TIME &
    ((j=j%N)); ((j++==0)) && wait
done
wait
