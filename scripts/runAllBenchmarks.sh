#!/bin/bash

./getMutants.sh diff.GsonTest fuzzJSONParser testJSONParser com.google.gson.stream,com.google.gson.Gson,com.google.gson.Json diff.GsonTest,com.google.gson,edu.berkeley.cs.jqf.examples 20 180m ../../sort-benchmarks/ gson 10
./getMutants.sh chocopy.diff.ChocoPyTarget fuzzSemanticAnalysis testSemanticAnalysis chocopy.reference.semantic diff.ChocoPyTarget,chocopy 20 180m ../../fuzz_chocopy/ ChocoPy 5
./getMutants.sh diff.JacksonDatabindTest fuzzJsonReadValue testJsonReadValue com.fasterxml.jackson.core.json,com.fasterxml.jackson.databind.json com.fasterxml.jackson.core,com.fasterxml.jackson.databind,diff.JacksonDatabindTest,edu.berkeley.cs.jqf.examples 20 180m ../../sort-benchmarks/ jacksonjson 10
./getMutants.sh diff.CommonsCSVTest fuzzCSVParser testCSVParser org.apache.commons.csv org.apache.commons.csv,diff.CommonsCSVTest,edu.berkeley.cs.jqf.examples 20 180m ../../sort-benchmarks/ commonscsv 5
./getMutants.sh diff.AntTest fuzzWithGenerator testWithGenerator org.apache.tools.ant.helper,org.apache.tools.ant.Project diff.AntTest,org.apache.tools.ant,edu.berkeley.cs.jqf.examples 20 180m ../../sort-benchmarks/ ant 2
./getMutants.sh diff.RhinoTest fuzzWithGenerator testWithGenerator org.mozilla.javascript diff.RhinoTest,org.mozilla.javascript,org.mozilla.classfile,edu.berkeley.cs.jqf.examples 20 180m ../../sort-benchmarks/ rhino 4
./getMutants.sh diff.ClosureTest fuzzWithGenerator testWithGenerator com.google.javascript.jscomp.Compil,com.google.javascript.jscomp.parsing, diff,com.google.javascript.jscomp,com.google.javascript.rhino,edu.berkeley.cs.jqf.examples 20 180m ../../sort-benchmarks/ closure 1
./getMutants.sh diff.ApacheTomcatTest fuzzWithGenerator testWithGenerator org.apache.tomcat.util.descriptor,org.apache.tomcat.util.digester, diff,org.apache.tomcat,edu.berkeley.cs.jqf.examples 20 180m ../../sort-benchmarks/ apachetomcat 5

