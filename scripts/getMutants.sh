#!/bin/bash

#Leaves a venn diagram of Zest's mutant finding vs. Mu2's mutant finding in the output directory.
#example usage:
#  ./getMutants.sh diff.DiffTest fuzzTimSort testTimSort sort.TimSort diff.DiffTest,sort \
#     3 1000 ../../sort-benchmarks timsort

fuzzZest() {
    echo mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dout=$3-fuzz-results/tmpZest/exp_$4 -Dengine=zest -DrandomSeed=$4 -Dtrials=$5
    mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dout=$3-fuzz-results/tmpZest/exp_$4 -Dengine=zest -DrandomSeed=$4 -Dtrials=$5
}

fuzzMu2() {
    echo mvn mu2:diff -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -DtargetIncludes=$4 -Dout=$5-fuzz-results/tmpMu2/exp_$6 -DrandomSeed=$6 -Dtrials=$7
    mvn mu2:diff -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -DtargetIncludes=$4 -Dout=$5-fuzz-results/tmpMu2/exp_$6 -DrandomSeed=$6 -Dtrials=$7
}

getResults() {

    # Debug purposes - dump args to look at actual files
    mvn jqf:repro -Dclass=$1 -Dmethod=$2 -Dinput=target/$7-fuzz-results/tmpZest/exp_$6/corpus -Djqf.repro.dumpArgsDir=target/$7-fuzz-results/tmpZest/exp_$6/args_corpus/
    mvn jqf:repro -Dclass=$1 -Dmethod=$2 -Dinput=target/$7-fuzz-results/tmpMu2/exp_$6/corpus -Djqf.repro.dumpArgsDir=target/$7-fuzz-results/tmpMu2/exp_$6/args_corpus/

    mvn mu2:mutate -Dclass=$1 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dinput=target/$7-fuzz-results/tmpZest/exp_$6/corpus > $7-results/zest-results-$6.txt
    mvn mu2:mutate -Dclass=$1 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dinput=target/$7-fuzz-results/tmpMu2/exp_$6/corpus > $7-results/mutate-results-$6.txt

    cat $7-results/zest-results-$6.txt | grep -a "Running Mutant\|FAILURE" > $7-filters/zest-filter-$6.txt
    cat $7-results/mutate-results-$6.txt | grep -a "Running Mutant\|FAILURE" > $7-filters/mutate-filter-$6.txt

}

CLASS=$1
FUZZMETHOD=$2
DIFFMETHOD=$3
INCLUDES=$4
TARGETINCLUDES=$5
REPS=$6
TRIALS=$7
DIR=$8
TARGETNAME=$9

CURDIR=$(pwd)
cd $DIR
mkdir $TARGETNAME-filters
mkdir $TARGETNAME-results

for i in $(seq 1 1 $REPS)
do
    fuzzZest $CLASS $FUZZMETHOD $TARGETNAME $i $TRIALS
    fuzzMu2 $CLASS $DIFFMETHOD $INCLUDES $TARGETINCLUDES $TARGETNAME $i $TRIALS
done

for i in $(seq 1 1 $6)
do
    getResults $CLASS $FUZZMETHOD $DIFFMETHOD $INCLUDES $TARGETINCLUDES $i $TARGETNAME
done

cd $CURDIR
python3 venn.py --filters_dir $DIR/$TARGETNAME-filters --num_experiments $REPS --output_img $DIR/$TARGETNAME-venn.png

#comment the below lines to not remove the created files
# rm -r filters
# rm -r results
# rm -r target/tmpZest
# rm -r target/tmpMu2
