#!/bin/bash
#runs each benchmark, repros the results, and graphs/plots them with the other scripts in the directory.
#ARGS: $1 - time per run

MY_PATH=$(dirname "$0")
echo "$MY_PATH"
echo "$0"
echo "$BASH_SOURCE"

mvn mu2:diff  -Dclass=sort.TimSortTest -Dmethod=fuzzTimSort -Dincludes=sort.TimSort -Dguidance=$2 -Dout=fuzz-results-timsort -Dtime=$1
python $MYPATH/plot_mutant_data.py target/fuzz-results-timsort/plot_data timsortPlot
python $MYPATH/graph_log.py target/fuzz-results-timsort/fuzz.log --chartout timsortLogChart.png

mvn mu2:diff -Dclass=diff.GSonTest -Dmethod=testJSONParse -Dincludes=com.google.gson.stream,com.google.gson.Gson,com.google.gson.Json -Dguidance=$2 -Dout fuzz-results-gson -Dtime=$1

python $MYPATH/plot_mutant_data.py target/fuzz-results-gson/plot_data gsonPlot
python $MYPATH/graph_log.py target/fuzz-results-gson/fuzz.log --chartout gsonLogChart.png

mvn mu2:diff -Dclass=diff.JacksonDatabindTest -Dmethod=testJsonReadValue -Dincludes=com.fasterxml.jackson.core.json,com.fasterxml.jackson.databind.json -Dguidance=$2 -Dout=fuzz-results-jackson -Dtime=$1

python $MYPATH/plot_mutant_data.py target/fuzz-results-jackson/plot_data jacksonPlot
python $MYPATH/graph_log.py target/fuzz-results-jackson/fuzz.log --chartout jacksonLogChart.png

mvn mu2:diff -Dclass=diff.ApacheTomcatTest -Dmethod=testWithGenerator -Dincludes=org.apache.tomcat.util.descriptor,org.apache.tomcat.util.digester -Dguidance=$2 -Dout=fuzz-results-tomcat -Dtime=$1

python $MYPATH/plot_mutant_data.py target/fuzz-results-tomcat/plot_data tomcatPlot
python $MYPATH/graph_log.py target/fuzz-results-tomcat/fuzz.log --chartout tomcatLogChart.png

mvn mu2:diff -Dclass=diff.ClosureTest -Dmethod=testWithGenerator -Dincludes=com.google.javascript.jscomp.Compiler -Dguidance=$2 -Dout=fuzz-results-closure -Dtime=$1

python $MYPATH/plot_mutant_data.py target/fuzz-results-closure/plot_data closurePlot
python $MYPATH/graph_log.py target/fuzz-results-closure/fuzz.log --chartout closureLogChart.png
