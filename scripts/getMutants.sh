#!/bin/bash

#Leaves a venn diagram of Zest's mutant finding vs. Mu2's mutant finding in the the directory at $6.
#example usage:
#  ./getMutants.sh edu.berkeley.cs.jqf.examples.commons.PatriciaTrieTest testPrefixMap org.apache.commons.collections4.trie 3 1000 ../../jqf/examples

getResults() {
    mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dout=tmpZest/exp_$4 -Dengine=zest -Dtrials=$5
    mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dout=tmpMu2/exp_$4 -Dengine=mutation -Dtrials=$5

    # Debug purposes - dump args to look at actual files
    mvn jqf:repro -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dinput=target/tmpZest/exp_$4/corpus -DdumpArgsDir=target/tmpZest/exp_$4/args_corpus/
    mvn jqf:repro -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dinput=target/tmpMu2/exp_$4/corpus -DdumpArgsDir=target/tmpMu2/exp_$4/args_corpus/

    mvn jqf:mutate -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dinput=target/tmpZest/exp_$4/corpus > results/zest-results-$4.txt
    mvn jqf:mutate -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dinput=target/tmpMu2/exp_$4/corpus > results/mutate-results-$4.txt

    cat results/zest-results-$4.txt | grep "Running Mutant\|FAILURE" > filters/zest-filter-$4.txt
    cat results/mutate-results-$4.txt | grep "Running Mutant\|FAILURE" > filters/mutate-filter-$4.txt
}

CURDIR=$(pwd)
cd $6
mkdir filters
mkdir results

for i in $(seq 1 1 $4)
do
    getResults $1 $2 $3 $i $5
done

cd $CURDIR
python venn.py --filters_dir $6/filters --num_experiments $4 --output_img venn.png

#comment the below lines to not remove the created files
# rm -r filters
# rm -r results
# rm -r target/tmpZest
# rm -r target/tmpMu2
