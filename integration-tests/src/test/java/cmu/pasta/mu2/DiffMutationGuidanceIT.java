package cmu.pasta.mu2;

import cmu.pasta.mu2.fuzz.MutationGuidance;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.instrument.OptLevel;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DiffMutationGuidanceIT extends AbstractMutationTest {
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
        String testClassName = "cmu.pasta.mu2.examples.sort.DiffTest";
        String testMethod = "testBubbleSort";
        String targetInst = "cmu.pasta.mu2.examples.sort.BubbleSort";
        long trials = 100;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, "cmu.pasta.mu2.examples.sort", OptLevel.NONE);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

        // Fuzz
        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);

        Assert.assertEquals(9, mu2.corpusCount());
    }

    @Test
    public void noncompareBubbleSort() throws Exception {
        // Set up test params
        String testClassName = "cmu.pasta.mu2.examples.sort.DiffTest";
        String testMethod = "testBubbleSortNonCompare";
        String targetInst = "cmu.pasta.mu2.examples.sort.BubbleSort";
        long trials = 100;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, "cmu.pasta.mu2.examples.sort", OptLevel.NONE);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

        // Fuzz
        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);

        Assert.assertEquals(8, mu2.corpusCount());
    }

    @Test
    public void fuzzBubbleSort() throws Exception {
        // Set up test params
        String testClassName = "cmu.pasta.mu2.examples.sort.DiffTest";
        String testMethod = "fuzzBubbleSort";
        String targetInst = "cmu.pasta.mu2.examples.sort.BubbleSort";
        long trials = 100;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, "cmu.pasta.mu2.examples.sort", OptLevel.NONE);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

        // Fuzz
        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);

        Assert.assertEquals(8, mu2.corpusCount());
    }

    @Test
    public void compareTimSort() throws Exception {
        // Set up test params
        String testClassName = "cmu.pasta.mu2.examples.sort.DiffTest";
        String testMethod = "testTimSort";
        String targetInst = "cmu.pasta.mu2.examples.sort.TimSort";
        long trials = 100;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, "cmu.pasta.mu2.examples.sort", OptLevel.NONE);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

        // Fuzz
        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);

        Assert.assertEquals(35, mu2.corpusCount());
    }

    @Test
    public void noncompareTimSort() throws Exception {
        // Set up test params
        String testClassName = "cmu.pasta.mu2.examples.sort.DiffTest";
        String testMethod = "testTimSortNonCompare";
        String targetInst = "cmu.pasta.mu2.examples.sort.TimSort";
        long trials = 100;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, "cmu.pasta.mu2.examples.sort", OptLevel.NONE);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

        // Fuzz
        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);

        // With a dummy compare method, more inputs are saved since later candidates 
        // kill mutants that would not survive with the proper compare method.
        Assert.assertEquals(37, mu2.corpusCount());
    }

    @Test
    public void fuzzTimSort() throws Exception {
        // Set up test params
        String testClassName = "cmu.pasta.mu2.examples.sort.DiffTest";
        String testMethod = "fuzzTimSort";
        String targetInst = "cmu.pasta.mu2.examples.sort.TimSort";
        long trials = 100;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, "cmu.pasta.mu2.examples.sort", OptLevel.NONE);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

        // Fuzz
        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);

        // Since this fuzz driver has no return object, this should act the same as a nonCompare method.
        Assert.assertEquals(37, mu2.corpusCount());
    }

}
