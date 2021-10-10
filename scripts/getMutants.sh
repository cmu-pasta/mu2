#!/bin/bash

#Leaves a venn diagram of Zest's mutant finding vs. Mu2's mutant finding in the the directory at $6.
#example usage:
#  ./getMutants.sh edu.berkeley.cs.jqf.examples.commons.PatriciaTrieTest testPrefixMap org.apache.commons.collections4.trie 3 1000 ../../jqf/examples

getResults() {
    mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dout=tmpZest -Dengine=zest -Dtrials=$5
    mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dout=tmpMu2 -Dengine=mutation -Dtrials=$5

    mvn jqf:mutate -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dinput=target/tmpZest/corpus > zest-results.txt
    mvn jqf:mutate -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dinput=target/tmpMu2/corpus > mutate-results.txt

    cat zest-results.txt | grep -v "SUCCESS" | grep "FAILURE" -B 1 | grep -v "FAILURE" | grep -v "\-\-" > filters/zest-filter$4.txt
    cat mutate-results.txt | grep -v "SUCCESS" | grep "FAILURE" -B 1 | grep -v "FAILURE" | grep -v "\-\-" > filters/mutate-filter$4.txt
}

CURDIR=$(pwd)
cd $6
mkdir filters

for i in $(seq 1 1 $4)
do
    getResults $1 $2 $3 $i $5
done

python3 $CURDIR/venn.py $4

#comment the below lines to not remove the created files
rm zest-results.txt
rm mutate-results.txt
rm -r filters
rm -r target/tmpZest
rm -r target/tmpMu2

cd $CURDIR