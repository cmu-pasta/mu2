#!/bin/bash
#runs each benchmark,  graphs/plots them with the other scripts in the directory.
#ARGS: $1 - time per run
#      $2 - location of results
#      $3 - filters

MYPATH=$(dirname "$0")

mkdir -p $2

mvn mu2:diff -Dclass=sort.TimSortTest -Dmethod=fuzzTimSort -Dincludes=sort.TimSort -Dout=fuzz-results-timsort -Dtime=$1 -Dfilters=$3

python $MYPATH/plot_mutant_data.py target/fuzz-results-timsort/plot_data $2/timsortPlot
python $MYPATH/graph_log.py target/fuzz-results-timsort/fuzz.log --chartout $2/timsortLogChart.png

mvn mu2:diff -Dclass=diff.GsonTest -Dmethod=testJSONParser -Dincludes=com.google.gson.stream,com.google.gson.Gson,com.google.gson.Json -Dout=fuzz-results-gson -Dtime=$1 -Dfilters=$3

python $MYPATH/plot_mutant_data.py target/fuzz-results-gson/plot_data $2/gsonPlot
python $MYPATH/graph_log.py target/fuzz-results-gson/fuzz.log --chartout $2/gsonLogChart.png

mvn mu2:diff -Dclass=diff.JacksonDatabindTest -Dmethod=testJsonReadValue -Dincludes=com.fasterxml.jackson.core.json,com.fasterxml.jackson.databind.json -Dout=fuzz-results-jackson -Dtime=$1 -Dfilters=$3

python $MYPATH/plot_mutant_data.py target/fuzz-results-jackson/plot_data $2/jacksonPlot
python $MYPATH/graph_log.py target/fuzz-results-jackson/fuzz.log --chartout $2/jacksonLogChart.png

mvn mu2:diff -Dclass=diff.ApacheTomcatTest -Dmethod=testWithGenerator -Dincludes=org.apache.tomcat.util.descriptor,org.apache.tomcat.util.digester -Dout=fuzz-results-tomcat -Dtime=$1 -Dfilters=$3

python $MYPATH/plot_mutant_data.py target/fuzz-results-tomcat/plot_data $2/tomcatPlot
python $MYPATH/graph_log.py target/fuzz-results-tomcat/fuzz.log --chartout $2/tomcatLogChart.png

mvn mu2:diff -Dclass=diff.ClosureTest -Dmethod=testWithGenerator -Dincludes=com.google.javascript.jscomp.Compiler -Dout=fuzz-results-closure -Dtime=$1 -Dfilters=$3

python $MYPATH/plot_mutant_data.py target/fuzz-results-closure/plot_data $2/closurePlot
python $MYPATH/graph_log.py target/fuzz-results-closure/fuzz.log --chartout $2/closureLogChart.png
