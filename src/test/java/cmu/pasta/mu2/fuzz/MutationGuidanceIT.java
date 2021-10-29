package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.instrument.AbstractMutationTest;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.instrument.OptLevel;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test for {@link MutationGuidance}.
 */
public class MutationGuidanceIT extends AbstractMutationTest {

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
  public void fuzzTimSortO1() throws Exception {
    // Set up test params
    String testClassName = "sort.TimSortTest";
    String testMethod = "fuzzTimSort";
    String targetInst = "sort.TimSort";
    long trials = 100;
    Random rnd = new Random(42);


    // Create guidance
    MutationClassLoaders mcls = initClassLoaders(targetInst, OptLevel.EXECUTION);
    ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

    // Fuzz
    GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);


    Assert.assertEquals(22, mu2.corpusCount());


  }
}
