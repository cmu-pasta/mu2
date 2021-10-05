#!/bin/bash

#example usage:
#  ./getMutants.sh edu.berkeley.cs.jqf.examples.commons.PatriciaTrieTest testPrefixMap org.apache.commons.collections4.trie 10
#NOTE: use from higher-level folder! (Copy outside of Mu2)

getResults() {
    cd jqf/examples

    mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dout=tmpZest -Dengine=zest -Dtrials=15000
    mvn jqf:fuzz -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dout=tmpMu2 -Dengine=mutation -Dtrials=15000

    mvn jqf:mutate -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dinput=target/tmpZest/corpus > ../../zest-results.txt
    mvn jqf:mutate -Dclass=$1 -Dmethod=$2 -Dincludes=$3 -Dinput=target/tmpMu2/corpus > ../../mutate-results.txt

    cd ../..

    cat zest-results.txt | grep -v "SUCCESS" | grep "FAILURE" -B 1 | grep -v "FAILURE" | grep -v "\-\-" > filters/zest-filter$4.txt
    cat mutate-results.txt | grep -v "SUCCESS" | grep "FAILURE" -B 1 | grep -v "FAILURE" | grep -v "\-\-" > filters/mutate-filter$4.txt
}

mkdir filters

for i in $(seq 1 1 $4)
do
    getResults $1 $2 $3 $i
done

python3 venn.py $4

#rm zest-results.txt
#rm mutate-results.txt
#rm -r filters
