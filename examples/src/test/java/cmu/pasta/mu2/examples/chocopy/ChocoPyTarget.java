package cmu.pasta.mu2.examples.chocopy;

import chocopy.ChocoPy;
import chocopy.common.astnodes.Program;
import chocopy.common.astnodes.Node;
import chocopy.reference.RefAnalysis;
import chocopy.reference.RefParser;
import com.pholser.junit.quickcheck.From;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.difffuzz.Comparison;
import edu.berkeley.cs.jqf.fuzz.difffuzz.DiffFuzz;

import edu.berkeley.cs.jqf.examples.chocopy.ChocoPySemanticGenerator;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.junit.Assume.assumeNoException;

@RunWith(JQF.class)
public class ChocoPyTarget {

    @Fuzz(repro="${repro}")
    public void fuzzSemanticAnalysis(@From(ChocoPySemanticGenerator.class) String code) {
        Program program = RefParser.process(code, false);
        assumeTrue(!program.hasErrors());
        Program refTypedProgram = RefAnalysis.process(program);
        try {
            Node.readTree(refTypedProgram.toJSON());
        } catch (IOException e) {
            assumeNoException(e);
        }
    }

    @DiffFuzz
    public JsonNode testSemanticAnalysis(@From(ChocoPySemanticGenerator.class) String code) {
        Program program = RefParser.process(code, false);
        assumeTrue(!program.hasErrors());
        Program refTypedProgram = RefAnalysis.process(program);
        JsonNode outputNode = null;
        try {
            outputNode = Node.readTree(refTypedProgram.toJSON());
        } catch (IOException e) {
            assumeNoException(e);
        }
        return outputNode;
    }

    @DiffFuzz(cmp = "noncompare")
    public JsonNode testSemanticAnalysisNoncompare(@From(ChocoPySemanticGenerator.class) String code) {
        Program program = RefParser.process(code, false);
        assumeTrue(!program.hasErrors());
        Program refTypedProgram = RefAnalysis.process(program);
        JsonNode outputNode = null;
        try {
            outputNode = Node.readTree(refTypedProgram.toJSON());
        } catch (IOException e) {
            assumeNoException(e);
        }
        return outputNode;
    }

    @Comparison
    public static Boolean noncompare(JsonNode j1, JsonNode j2) {
        return true;
    }
}
