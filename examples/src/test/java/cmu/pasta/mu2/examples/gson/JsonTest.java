package cmu.pasta.mu2.examples.gson;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.difffuzz.Comparison;
import edu.berkeley.cs.jqf.fuzz.difffuzz.DiffFuzz;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import org.junit.Assume;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
public class JsonTest {

    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.setLenient().create();

    @DiffFuzz
    public Object testJSONParser(@From(AsciiStringGenerator.class) String input) {
        Object out = null;
        try {
            out = gson.fromJson(input, Object.class);
        } catch (JsonSyntaxException e) {
            Assume.assumeNoException(e);
        } catch (JsonIOException e) {
            Assume.assumeNoException(e);
        }
        return out;
    }

    @Fuzz(repro="${repro}")
    public void fuzzJSONParser(@From(AsciiStringGenerator.class) String input) {
        Object out = null;
        try {
            out = gson.fromJson(input, Object.class);
        } catch (JsonSyntaxException e) {
            Assume.assumeNoException(e);
        } catch (JsonIOException e) {
            Assume.assumeNoException(e);
        }
    }

    @DiffFuzz(cmp = "noncompare")
    public Object testJSONParserNoncompare(@From(AsciiStringGenerator.class) String input) {
        Object out = null;
        try {
            out = gson.fromJson(input, Object.class);
        } catch (JsonSyntaxException e) {
            Assume.assumeNoException(e);
        } catch (JsonIOException e) {
            Assume.assumeNoException(e);
        }
        return out;
    }

    @Comparison
    public static Boolean noncompare(Object o1, Object o2) {
        return true;
    }

}
