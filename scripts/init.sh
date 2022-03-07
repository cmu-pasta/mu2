#!/bin/bash
# Initializes a remote machine with all the repos

set -e

sudo apt-get -y update > /dev/null
sudo apt-get -y install maven

cd $HOME

# JQF 
[ -d jqf ] || git clone -b vasu/jqf-1.9 https://github.com/rohanpadhye/JQF.git jqf
echo "Installing JQF..."
(cd jqf; mvn install > /dev/null && echo "Built: JQF")
(cd jqf/examples; mvn install jar:test-jar > /dev/null && 
  cp target/jqf-examples-1.9-tests.jar ~/.m2/repository/edu/berkeley/cs/jqf/jqf-examples/1.9/ &&
  echo "Built: JQF")

# Mu2
[ -d mu2 ] || git clone -b vasu/remote_scripts git@github.com:cmu-pasta/mu2.git
echo "Compiling Mu2 without tests..."
(cd mu2; git pull; mvn install -DskipTests > /dev/null && echo "Built: Mu2 (without tests)")

# sort-benchmarks
# TODO: merge branch vasu/common-cli to master
[ -d sort-benchmarks ] || git clone -b vasu/common-cli git@github.com:cmu-pasta/sort-benchmarks.git
echo "Compiling sort-benchmarks..."
(cd sort-benchmarks; git pull; mvn install > /dev/null && echo "Built: sort-benchmarks")

# Mu2 With Tests
echo "Compiling Mu2..."
(cd mu2; git pull; mvn install -DskipTests > /dev/null && echo "Built: Mu2 (with tests)")

# fuzz_chocopy
# TODO: Merge branch vasu/add_diff_chocopy to master
# [ -d fuzz_chocopy ] || git clone -b vasu/add_diff_chocopy git@github.com:cmu-pasta/fuzz_chocopy.git
# [ -d $HOME/fuzz_chocopy/src/main/java/chocopy/reference ] || mv $HOME/reference/ $HOME/zest_chocopy/src/main/java/chocopy/
# echo "Compiling fuzz_chocopy..."
# (cd fuzz_chocopy; git pull; ./install_jars.sh; mvn install > /dev/null && echo "Built: fuzz_chocopy")
