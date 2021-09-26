package cmu.pasta.mu2.diff;

import cmu.pasta.mu2.diff.junit.DiffedFuzzing;
import cmu.pasta.mu2.fuzz.MutationGuidance;
import cmu.pasta.mu2.instrument.AbstractMutationTest;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.instrument.OptLevel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

//@RunWith(Mu2.class)
public class DiffIT extends AbstractMutationTest {
    // Temp directory to store fuzz results
    protected static File resultsDir;

    @Before
    public void initTempDir() throws IOException {
        resultsDir = Files.createTempDirectory("fuzz-results").toFile();
    }

    @After
    public void clearTempDir() {
        resultsDir.delete();
    }

    private static class ProbedMutationGuidance extends MutationGuidance {
        List<Integer> inputHashes = new ArrayList<>();

        ProbedMutationGuidance(MutationClassLoaders mcls, long trials, Random rnd) throws IOException {
            super(null, mcls,null, trials, resultsDir, null, rnd);
        }

        @Override
        public void observeGeneratedArgs(Object[] args) {
            this.inputHashes.add(Arrays.hashCode(args));
        }

        int hashInputHashes() {
            return inputHashes.hashCode();
        }

        int corpusCount() {
            return savedInputs.size();
        }

        int hashTotalCoverage() {
            return totalCoverage.hashCode();
        }

        int hashValidCoverage() {
            return validCoverage.hashCode();
        }
    }


    @Test
    public void fuzzBubbleSortO1() throws Exception {
        // Set up test params
        String testClassName = "diff.DiffTest";
        String testMethod = "testBubbleSort";
        String targetInst = "sort.BubbleSort";
        long trials = 1000;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, OptLevel.EXECUTION);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

        // Fuzz
        DiffedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);

        Assert.assertEquals(10, mu2.corpusCount());

    }
}
