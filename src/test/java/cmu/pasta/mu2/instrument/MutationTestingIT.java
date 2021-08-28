package cmu.pasta.mu2.instrument;

import cmu.pasta.mu2.MutationInstance;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;

/**
 * Integration test for validating the basic mutation testing functionality
 */
public class MutationTestingIT extends AbstractMutationTest {

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
    MutationClassLoaders mcls = initClassLoaders(targetInst, opt);
    runTest(testClassName, testMethod, mcls.getCartographyClassLoader());

    // Retrieve dynamically collected mutation instances
    List<MutationInstance> mutants = mcls.getMutationInstances();
    Assert.assertEquals(expectedMutants, mutants.size());

    // Run mutants and compute mutation score
    int run = 0;
    int killed = 0;
    for (MutationInstance mutant : mutants) {
      // Skip if optimization is enabled
      if (opt != OptLevel.NONE && !seenMutants.contains(mutant)) {
        continue;
      }

      // Run select mutants
      run++;
      Result r = runTest(testClassName, testMethod, mcls.getMutationClassLoader(mutant));
      if (!r.wasSuccessful()) {
        killed++;
      }
    }
    Assert.assertEquals(expectedRun, run);
    Assert.assertEquals(expectedKilled, killed);

  }

  @Test
  public void mutateTimSortNoOpt() throws Exception {
    validateMutationScores("sort.TimSortTest", "testTimSort",
        "sort.TimSort",  OptLevel.NONE, 503, 503, 27);
  }


  @Test
  public void mutateTimSortO1() throws Exception {
    validateMutationScores("sort.TimSortTest", "testTimSort",
        "sort.TimSort",  OptLevel.EXECUTION, 503,  50,27);
  }
}
