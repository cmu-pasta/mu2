package cmu.pasta.mu2.examples.jackson;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.difffuzz.Comparison;
import edu.berkeley.cs.jqf.fuzz.difffuzz.DiffFuzz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;

import java.io.IOException;

import org.junit.Assume;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
public class JsonTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @DiffFuzz
    public Object testJsonReadValue(@From(AsciiStringGenerator.class) String input) {
        Object output = null;
        try {
            output = objectMapper.readValue(input, Object.class);
        } catch (JsonProcessingException e) {
           Assume.assumeNoException(e);
        } catch (IOException e) {
           Assume.assumeNoException(e);
        }
        return output;
    }

    @Fuzz(repro="${repro}")
    public void fuzzJsonReadValue(@From(AsciiStringGenerator.class) String input) {
        Object output = null;
        try {
            output = objectMapper.readValue(input, Object.class);
        } catch (JsonProcessingException e) {
           Assume.assumeNoException(e);
        } catch (IOException e) {
           Assume.assumeNoException(e);
        }
    }

    @DiffFuzz(cmp = "noncompare")
    public Object testJsonReadValueNoncompare(@From(AsciiStringGenerator.class) String input) {
        Object output = null;
        try {
            output = objectMapper.readValue(input, Object.class);
        } catch (JsonProcessingException e) {
           Assume.assumeNoException(e);
        } catch (IOException e) {
           Assume.assumeNoException(e);
        }
        return output;
    }

    @Comparison
    public static Boolean noncompare(Object o1, Object o2) {
        return true;
    }
}
