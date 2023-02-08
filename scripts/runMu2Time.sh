#!/bin/bash

#Runs Mu2 sessions between $MIN and $MAX (inclusive). Will output fuzz-results to $DIR/target/$TARGETNAME-fuzz-results/mu2/
#example usage:
#  ./runMu2Time.sh mu2 diff.DiffTest testTimSort sort.TimSort diff.DiffTest,sort \
#     TimSort 180m 2 3 ../../sort-benchmarks

fuzzMu2Time() {
    echo mvn mu2:diff -Dclass="$2" -Dmethod="$3" -Dincludes="$4" -Dtarget="/dev/shm/" -Dout="$6-fuzz-results/$1/exp_$7" -DrandomSeed="$7" -Dtime="$8" -DoptLevel=INFECTION -Djqf.mutation.TIMEOUT_TICKS=10000000 $9
    mvn mu2:diff -Dclass="$2" -Dmethod="$3" -Dincludes="$4" -Dtarget="/dev/shm/" -Dout="$6-fuzz-results/$1/exp_$7" -DrandomSeed="$7" -Dtime="$8" -DoptLevel=INFECTION -Djqf.mutation.TIMEOUT_TICKS=10000000 $9
}

# if [ $# -lt 10 ]; then
# 	echo "Usage: $0 CONFIG PROCS REP CLASS DIFFMETHOD INCLUDES TARGETINCLUDES TARGETNAME TIME DIR" > /dev/stderr
# 	exit 1
# fi

CONFIG="$1"
PROCS="$2"
REP="$3"
CLASS="$4"
DIFFMETHOD="$5"
INCLUDES="$6"
TARGETINCLUDES="$7"
TARGETNAME="$8"
TIME="$9"
DIR="${10}"
EXTRA="${11}"

MIN=$REP
MAX=$(($REP+$PROCS-1))

CURDIR=$(pwd)

cd $DIR
N=$PROCS
for i in $(seq $MIN 1 $MAX)
do
    ((j=j%N)); ((j++==0)) && wait
    fuzzMu2Time $CONFIG $CLASS $DIFFMETHOD $INCLUDES $TARGETINCLUDES $TARGETNAME $i $TIME $EXTRA &
done
wait
