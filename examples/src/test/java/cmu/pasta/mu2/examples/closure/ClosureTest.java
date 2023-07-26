package cmu.pasta.mu2.examples.closure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import edu.berkeley.cs.jqf.examples.js.JavaScriptCodeGenerator;
import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.difffuzz.Comparison;
import edu.berkeley.cs.jqf.fuzz.difffuzz.DiffFuzz;

import static org.junit.Assume.assumeTrue;

@RunWith(JQF.class)
public class ClosureTest {

    static {
        // Disable all logging by Closure passes
        LogManager.getLogManager().reset();
    }

    private Compiler compiler = new Compiler(new PrintStream(new ByteArrayOutputStream(), false));
    private CompilerOptions options = new CompilerOptions();
    private SourceFile externs = SourceFile.fromCode("externs", "");

    @Before
    public void initCompiler() {
        // Don't use threads
        compiler.disableThreads();
        // Don't print things
        options.setPrintConfig(false);
        // Enable all safe optimizations
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
    }

    private void doCompile(SourceFile input) {
        Result result = compiler.compile(externs, input, options);
        Assume.assumeTrue(result.success);
    }

    public void testWithString(@From(AsciiStringGenerator.class) String code) {
        SourceFile input = SourceFile.fromCode("input", code);
        doCompile(input);
    }

    @DiffFuzz
    public String testWithGenerator(@From(JavaScriptCodeGenerator.class) String code) {
        testWithString(code);
        return compiler.toSource();
    }

    @DiffFuzz(cmp = "noncompare")
    public String testWithGeneratorNoncompare(@From(JavaScriptCodeGenerator.class) String code) {
        testWithString(code);
        return compiler.toSource();
    }

    @Fuzz(repro="${repro}")
    public void fuzzWithGenerator(@From(JavaScriptCodeGenerator.class) String code) {
        testWithString(code);
        compiler.toSource();
    }

    @Comparison
    public static Boolean noncompare(String s1, String s2) {
        return true;
    }


}
