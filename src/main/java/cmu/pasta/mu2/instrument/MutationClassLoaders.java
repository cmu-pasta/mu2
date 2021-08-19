package cmu.pasta.mu2.instrument;

import cmu.pasta.mu2.MutationInstance;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A lazily instantiated collection of {@link MutationClassLoader}s, one for each instance, with the
 * same {@code URL} list of class paths and parent {@code ClassLoader}.
 */
public class MutationClassLoaders {

  private final URL[] paths;
  private final ClassLoader parentClassLoader;
  private final CartographyClassLoader cartographyClassLoader;
  private final Map<MutationInstance, MutationClassLoader> mutationClassLoaderMap;

  /**
   * Creates an {@code MutationClassLoaders}
   *
   * @param paths             The paths for the {@code MutationClassLoader}
   * @param include           Comma-separated list of include prefixes
   * @param exclude           Comma-separated list of exclude prefixes
   * @param parentClassLoader The parent for the {@code MutationClassLoader}
   */
  public MutationClassLoaders(URL[] paths, String include, String exclude,
      ClassLoader parentClassLoader) {
    this.paths = paths;
    this.parentClassLoader = parentClassLoader;
    this.cartographyClassLoader = new CartographyClassLoader(paths,
        include != null ? include.split(",") : new String[0],
        exclude != null ? exclude.split(",") : new String[0],
        parentClassLoader);
    this.mutationClassLoaderMap = new HashMap<>();
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
}
