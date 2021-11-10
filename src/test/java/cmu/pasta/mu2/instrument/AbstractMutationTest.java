package cmu.pasta.mu2.instrument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;

/**
 * Abstract class containing common functionality for testing Mu2. This class provides methods
 * to set-up up the classpath for instrumentable classes before running tests and provides
 * utility methods to create mutation classloaders.
 */
public class AbstractMutationTest {

  /** Class path for target application classes. */
  protected String[] instrumentablesClassPath;

  @Before
  public void initClassPath() throws IOException {
    // Walk dependency tree to get all instrumentable JARs, e.g. jqf-examples.
    // Anything that's in this directory should not actually be on
    // the test classpath, so it will have to be loaded with the loader defined below
    this.instrumentablesClassPath = Files.walk(Paths.get("target/instrumentables"))
        .map(Path::toString).toArray(String[]::new);
  }

  /**
   * Creates mutation classloaders for this test.
   *
   * @param mutableClasses comma-separated include list for target app classes
   * @param opt the optimization level
   * @return the classloaders
   * @throws IOException if classpath is malformed
   */
  protected MutationClassLoaders initClassLoaders(String mutableClasses, String dependencyClasses, OptLevel opt)
      throws IOException {
    return new MutationClassLoaders(instrumentablesClassPath, mutableClasses, dependencyClasses, opt);
  }

}
