package cmu.pasta.mu2.instrument;

import cmu.pasta.mu2.fuzz.*;
import cmu.pasta.mu2.MutationInstance;

import java.util.Random;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.After;
import org.junit.Before;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;

/**
 * Integration test for validating the basic mutation testing functionality
 */
public class MutationTestingIT extends AbstractMutationTest {

  protected static File resultsDir;

  @Before
  public void initTempDir() throws IOException {
    resultsDir = Files.createTempDirectory("fuzz-results").toFile();
  }

  @After
  public void clearTempDir() {
    resultsDir.delete();
  }

  protected JUnitCore junit = new JUnitCore();

  protected Result runTest(String testClassName, String testMethod, ClassLoader loader) throws ClassNotFoundException {
    Class<?> testClass = Class.forName(testClassName, true, loader);
    Runner testRunner = Request.method(testClass, testMethod).getRunner();
    return junit.run(testRunner);
  }

  protected void validateMutationScores(String testClassName, String testMethod,
      String targetInst, OptLevel opt, int expectedMutants, int expectedRun, int expectedKilled) throws Exception  {

    final Set<MutationInstance> seenMutants = new HashSet<>();
    MutationSnoop.setMutantCallback(m -> seenMutants.add(m));

    // Create the JUnit test runner
    MutationClassLoaders mcls = initClassLoaders(targetInst, "sort", opt);
    runTest(testClassName, testMethod, mcls.getCartographyClassLoader());

    // // Retrieve dynamically collected mutation instances
    // List<MutationInstance> mutants = mcls.getMutationInstances();
    // Assert.assertEquals(expectedMutants, mutants.size());

    // // Run mutants and compute mutation score
    // int run = 0;
    // int killed = 0;
    // for (MutationInstance mutant : mutants) {
    //   // Skip if optimization is enabled
    //   if (opt != OptLevel.NONE && !seenMutants.contains(mutant)) {
    //     continue;
    //   }

    //   // Run select mutants
    //   run++;
    //   Result r = runTest(testClassName, testMethod, mcls.getMutationClassLoader(mutant));
    //   if (!r.wasSuccessful()) {
    //     killed++;
    //   }
    // }
    // Assert.assertEquals(expectedRun, run);
    // Assert.assertEquals(expectedKilled, killed);

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


  // @Test
  // public void mutateTimSortNoOpt() throws Exception {
  //   validateMutationScores("sort.TimSortTest", "testTimSort",
  //       "sort.TimSort",  OptLevel.NONE, 503, 503, 27);
  // }


  @Test
  public void mutateTimSortO1() throws Exception {
    validateMutationScores("sort.TimSortTest", "testTimSort",
        "sort.TimSort",  OptLevel.EXECUTION, 503,  50,27);
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
    MutationClassLoaders mcls = initClassLoaders(targetInst, "sort", OptLevel.EXECUTION);
    ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd);

    // Fuzz
    GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, null);


    Assert.assertEquals(22, mu2.corpusCount());
  }
}
