#!/bin/bash

#Leaves a venn diagram of Zest's mutant finding vs. Mu2's mutant finding in the output directory.
#example usage:
#  ./getMutants.sh diff.DiffTest fuzzTimSort testTimSort sort.TimSort diff.DiffTest,sort \
#     10 60m ../../sort-benchmarks timsort 5

fuzzZest() {
    echo mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dout=$3-fuzz-results/tmpZest/exp_$4 -Dengine=zest -DrandomSeed=$4 -Dtime=$TIME
    mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dout=$3-fuzz-results/tmpZest/exp_$4 -Dengine=zest -DrandomSeed=$4 -Dtime=$TIME
}

fuzzMu2() {
    STR=$(tail -n1 target/$5-fuzz-results/tmpZest/exp_$6/plot_data);
    arrSTR=(${STR//,/ });
    VALID=${arrSTR[11]};
    INVALID=${arrSTR[12]};
    NUM_TRIALS=$((VALID+INVALID))
    echo mvn mu2:diff -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -DtargetIncludes=$4 -Dout=$5-fuzz-results/tmpMu2/exp_$6 -DrandomSeed=$6 -Dtrials=$NUM_TRIALS -DoptLevel=EXECUTION
    mvn mu2:diff -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -DtargetIncludes=$4 -Dout=$5-fuzz-results/tmpMu2/exp_$6 -DrandomSeed=$6 -Dtrials=$NUM_TRIALS -DoptLevel=EXECUTION
    # echo mvn mu2:diff -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -DtargetIncludes=$4 -Dout=$5-fuzz-results/tmpMu2NoSave/exp_$6 -DrandomSeed=$6 -Dtrials=$NUM_TRIALS -DoptLevel=EXECUTION
    # mvn mu2:diff -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -DtargetIncludes=$4 -Dout=$5-fuzz-results/tmpMu2NoSave/exp_$6 -DrandomSeed=$6 -Dtrials=$NUM_TRIALS -DoptLevel=EXECUTION
}

getResults() {

    rm -r target/$7-fuzz-results/tmpZest/exp_$6/args_corpus/
    rm -r target/$7-fuzz-results/tmpMu2/exp_$6/args_corpus/

    # Debug purposes - dump args to look at actual files

    mvn jqf:repro -Dclass=$1 -Dmethod=$2 -Dinput=target/$7-fuzz-results/tmpZest/exp_$6/corpus -Djqf.repro.dumpArgsDir=target/$7-fuzz-results/tmpZest/exp_$6/args_corpus/
    mvn jqf:repro -Dclass=$1 -Dmethod=$2 -Dinput=target/$7-fuzz-results/tmpMu2/exp_$6/corpus -Djqf.repro.dumpArgsDir=target/$7-fuzz-results/tmpMu2NoSave/exp_$6/args_corpus/ 
    mvn jqf:repro -Dclass=$1 -Dmethod=$2 -Dinput=target/$7-fuzz-results/tmpMu2NoSave/exp_$6/corpus -Djqf.repro.dumpArgsDir=target/$7-fuzz-results/tmpMu2NoSave/exp_$6/args_corpus/ 

    # Remove previous mutate repro-out 
    rm -r $7-results/zest-results-$6
    rm -r $7-results/mutate-results-$6
    rm -r $7-results/mutate-results-no-save-$6
    rm -r $7-results/mutate-results-time-constrained-$6
    rm -r $7-results/mutate-results-no-save-time-constrained-$6

    # Zest mutate diff goal
    echo mvn mu2:mutate -Dclass=$1 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dinput=target/$7-fuzz-results/tmpZest/exp_$6/corpus -DresultsDir=$7-results/zest-results-$6 #> $7-results/zest-results-$6.txt
    mvn mu2:mutate -Dclass=$1 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dinput=target/$7-fuzz-results/tmpZest/exp_$6/corpus -DresultsDir=$7-results/zest-results-$6 #> $7-results/zest-results-$6.txt

    # Mu2 mutate diff goal
    echo mvn mu2:mutate -Dclass=$1 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dinput=target/$7-fuzz-results/tmpMu2/exp_$6/corpus -DresultsDir=$7-results/mutate-results-$6 #> $7-results/mutate-results-$6.txt
    mvn mu2:mutate -Dclass=$1 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dinput=target/$7-fuzz-results/tmpMu2/exp_$6/corpus -DresultsDir=$7-results/mutate-results-$6 #> $7-results/mutate-results-$6.txt

    # Mu2NoSave mutate diff goal
    echo mvn mu2:mutate -Dclass=$1 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dinput=target/$7-fuzz-results/tmpMu2NoSave/exp_$6/corpus -DresultsDir=$7-results/mutate-results-no-save-$6 #> $7-results/mutate-results-$6.txt
    mvn mu2:mutate -Dclass=$1 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dinput=target/$7-fuzz-results/tmpMu2NoSave/exp_$6/corpus -DresultsDir=$7-results/mutate-results-no-save-$6 #> $7-results/mutate-results-$6.txt

    # Mu2 time constrained mutate diff goal
    python $CURDIR/create_time_constrained_corpus.py --exp_dir target/$TARGETNAME-fuzz-results/tmpMu2/exp_$i
    echo mvn mu2:mutate -Dclass=$1 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dinput=target/$7-fuzz-results/tmpMu2/exp_$6/time_constrained_corpus -DresultsDir=$7-results/mutate-results-time-constrained-$6
    mvn mu2:mutate -Dclass=$1 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dinput=target/$7-fuzz-results/tmpMu2/exp_$6/time_constrained_corpus -DresultsDir=$7-results/mutate-results-time-constrained-$6

    # Mu2NoSave time constrained mutate diff goal
    python $CURDIR/create_time_constrained_corpus.py --exp_dir target/$TARGETNAME-fuzz-results/tmpMu2NoSave/exp_$i
    echo mvn mu2:mutate -Dclass=$1 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dinput=target/$7-fuzz-results/tmpMu2NoSave/exp_$6/time_constrained_corpus -DresultsDir=$7-results/mutate-no-save-results-time-constrained-$6
    mvn mu2:mutate -Dclass=$1 -Dmethod=$3 -Dincludes=$4 -DtargetIncludes=$5 -Dinput=target/$7-fuzz-results/tmpMu2NoSave/exp_$6/time_constrained_corpus -DresultsDir=$7-results/mutate-no-save-results-time-constrained-$6

    # Create filter files
    cat $7-results/zest-results-$6/mutate-repro-out.txt | grep -a "Running Mutant\|FAILURE" > $7-filters/zest-filter-$6.txt
    cat $7-results/mutate-results-$6/mutate-repro-out.txt | grep -a "Running Mutant\|FAILURE" > $7-filters/mutate-filter-$6.txt 
    cat $7-results/mutate-results-time-constrained-$6/mutate-repro-out.txt | grep -a "Running Mutant\|FAILURE" > $7-filters/mutate-filter-time-constrained-$6.txt 
    cat $7-results/mutate-results-no-save-$6/mutate-repro-out.txt | grep -a "Running Mutant\|FAILURE" > $7-filters/mutate-filter-no-save-$6.txt 
    cat $7-results/mutate-no-save-results-time-constrained-$6/mutate-repro-out.txt | grep -a "Running Mutant\|FAILURE" > $7-filters/mutate-filter-no-save-time-constrained-$6.txt 
}

CLASS=$1
FUZZMETHOD=$2
DIFFMETHOD=$3
INCLUDES=$4
TARGETINCLUDES=$5
REPS=$6
TIME=$7
DIR=$8
TARGETNAME=$9
MUPROCS=${10}

CURDIR=$(pwd)
cd $DIR
mkdir $TARGETNAME-filters
mkdir $TARGETNAME-results
mkdir $TARGETNAME-mutant-plots

N=10
for i in $(seq 1 1 $REPS)
do
    ((j=j%N)); ((j++==0)) && wait
    fuzzZest $CLASS $FUZZMETHOD $TARGETNAME $i $TRIALS &
done
wait

N=$MUPROCS
for i in $(seq 1 1 $REPS)
do
    ((j=j%N)); ((j++==0)) && wait
    fuzzMu2 $CLASS $DIFFMETHOD $INCLUDES $TARGETINCLUDES $TARGETNAME $i $TRIALS &
done
wait

N=$MUPROCS
for i in $(seq 1 1 $REPS)
do
    ((j=j%N)); ((j++==0)) && wait
    getResults $CLASS $FUZZMETHOD $DIFFMETHOD $INCLUDES $TARGETINCLUDES $i $TARGETNAME &
done
wait

for i in $(seq 1 1 $6)
do
    python plot_mutant_data.py $DIR/target/$TARGETNAME-fuzz-results/tmpMu2/exp_$i/plot_data $DIR/$TARGETNAME-mutant-plots/exp_$i.png
done


#comment the below lines to not remove the created files
# rm -r filters
# rm -r results
# rm -r target/tmpZest # rm -r target/tmpMu2
