package cmu.pasta.mu2.diff;

import cmu.pasta.mu2.fuzz.MutationGuidance;
import cmu.pasta.mu2.instrument.AbstractMutationTest;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.instrument.OptLevel;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
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
    public void compareBubbleSort() throws Exception {
        // Set up test params
        String testClassName = "diff.DiffTest";
        String testMethod = "testBubbleSort";
        String targetInst = "sort.BubbleSort";
        long trials = 100;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, "sort,diff", OptLevel.NONE);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

        // Fuzz
        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);

        Assert.assertEquals(9, mu2.corpusCount());
    }

    @Test
    public void noncompareBubbleSort() throws Exception {
        // Set up test params
        String testClassName = "diff.DiffTest";
        String testMethod = "otherBubbleSort";
        String targetInst = "sort.BubbleSort";
        long trials = 100;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, "sort,diff", OptLevel.NONE);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

        // Fuzz
        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);

        Assert.assertEquals(8, mu2.corpusCount());
    }

    @Test
    public void compareTimSort() throws Exception {
        // Set up test params
        String testClassName = "diff.DiffTest";
        String testMethod = "testTimSort";
        String targetInst = "sort.TimSort";
        long trials = 100;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, "sort,diff", OptLevel.NONE);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

        // Fuzz
        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);

        Assert.assertEquals(36, mu2.corpusCount());
    }

    @Test
    public void noncompareTimSort() throws Exception {
        // Set up test params
        String testClassName = "diff.DiffTest";
        String testMethod = "otherTimSort";
        String targetInst = "sort.TimSort";
        long trials = 100;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, "sort,diff", OptLevel.NONE);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

        // Fuzz
        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);

        Assert.assertEquals(36, mu2.corpusCount());
    }

    //@Test
    public void fuzzTimSort() throws Exception {
        // Set up test params
        String testClassName = "diff.DiffTest";
        String testMethod = "fuzzTimSort";
        String targetInst = "sort.TimSort";
        long trials = 100;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, "sort,diff", OptLevel.NONE);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

        // Fuzz
        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);

        Assert.assertEquals(36, mu2.corpusCount());
    }

}
