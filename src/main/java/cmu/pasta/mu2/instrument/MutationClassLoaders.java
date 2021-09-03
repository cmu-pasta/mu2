package cmu.pasta.mu2.instrument;

import cmu.pasta.mu2.MutationInstance;
import edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A lazily instantiated collection of {@link MutationClassLoader}s, one for each instance, with the
 * same {@code URL} list of class paths and parent {@code ClassLoader}.
 */
public class MutationClassLoaders {

  private final URL[] paths;
  private final ClassLoader parentClassLoader;
  private final OptLevel optLevel;
  private final CartographyClassLoader cartographyClassLoader;
  private final Map<MutationInstance, MutationClassLoader> mutationClassLoaderMap;

  /**
   * @param paths             The class path
   * @param mutableClasses    Comma-separated list of prefixes of classes to instrument
   * @param optLevel          The optimization level
   */
  public MutationClassLoaders(URL[] paths, String mutableClasses, OptLevel optLevel,
      ClassLoader parent) {
    this.paths = paths;
    this.optLevel = optLevel;
    this.parentClassLoader = parent;
    this.cartographyClassLoader = new CartographyClassLoader(paths, mutableClasses.split(","),
        parent, optLevel);
    this.mutationClassLoaderMap = new HashMap<>();
    MutationInstance.resetMutationInstances();
  }

  /**
   * @param paths             The class path
   * @param mutableClasses    Comma-separated list of prefixes of classes to instrument
   * @param optLevel          The optimization level
   */
  public MutationClassLoaders(String[] paths, String mutableClasses, OptLevel optLevel)
      throws IOException {
    this(InstrumentingClassLoader.stringsToUrls(paths), mutableClasses, optLevel,
        MutationClassLoaders.class.getClassLoader());
  }

  /**
   * Returns the class loader which is used for initial test execution in order to compute mutants
   * and other probes for optimization.
   *
   * @return the class loader
   */
  public CartographyClassLoader getCartographyClassLoader() {
    return cartographyClassLoader;
  }

  /**
   * Retrieves a {@link MutationClassLoader} for a given {@link MutationInstance}, creating a new
   * classloader if such a mapping does not yet exist.
   *
   * @param mi The {@link MutationInstance} to be used
   * @return A {@link MutationClassLoader} which loads the given instance
   */
  public MutationClassLoader getMutationClassLoader(MutationInstance mi) {
    MutationClassLoader mcl = mutationClassLoaderMap.get(mi);
    if (mcl == null) {
      mcl = new MutationClassLoader(mi, paths, parentClassLoader);
      mutationClassLoaderMap.put(mi, mcl);
    }
    return mcl;
  }

  /**
   * Returns the configured optimization level
   * @return the optimization level
   */
  public OptLevel getOpt() {
    return this.optLevel;
  }

  /** Returns the current list of mutation instances. */
  public List<MutationInstance> getMutationInstances() {
    return cartographyClassLoader.getMutationInstances();
  }
}
