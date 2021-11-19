package cmu.pasta.mu2.instrument;

import cmu.pasta.mu2.MutationInstance;
import edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
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

  public static String[] dependencyStarts;

  /**
   * @param paths             The class path
   * @param mutableClasses    Comma-separated list of prefixes of classes to instrument
   * @param optLevel          The optimization level
   * @param dependencyClasses Comma-separated list of prefixes of mutable classes
   *                          as well as classes depending on mutable classes -
   *                          if empty, assumes all classes are mutable or dependent on mutable classes
   */
  public MutationClassLoaders(URL[] paths, String mutableClasses, String dependencyClasses, OptLevel optLevel,
      ClassLoader parent) {
    this.paths = paths;
    this.optLevel = optLevel;
    dependencyStarts = dependencyClasses.split(",");
    this.parentClassLoader = new URLClassLoader(paths, parent) {
      @Override
      public Class<?> findClass(String name) throws ClassNotFoundException {
        for (String s : dependencyStarts) {
          if (name.startsWith(s)) {
            throw new ClassNotFoundException(name + " is mutable or depends on mutable class");
          }
        }
        return super.findClass(name);
      }
    };
    this.cartographyClassLoader = new CartographyClassLoader(paths, mutableClasses.split(","),
        parentClassLoader, optLevel);
    this.mutationClassLoaderMap = new HashMap<>();
  }

  /**
   * @param paths             The class path
   * @param mutableClasses    Comma-separated list of prefixes of classes to instrument
   * @param optLevel          The optimization level
   */
  public MutationClassLoaders(String[] paths, String mutableClasses, String dependencyClasses, OptLevel optLevel)
      throws IOException {
    this(InstrumentingClassLoader.stringsToUrls(paths), mutableClasses, dependencyClasses, optLevel,
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
