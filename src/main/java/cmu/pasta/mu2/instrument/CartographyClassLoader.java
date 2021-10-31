package cmu.pasta.mu2.instrument;

import cmu.pasta.mu2.MutationInstance;
import edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader;
import janala.instrument.SnoopInstructionTransformer;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ClassLoader for initial run in mutation guidance Runs like InstrumentingClassLoader while also
 * prepping MutationInstances
 *
 * @author Bella Laybourn
 */
public class CartographyClassLoader extends URLClassLoader {

  /**
   * List of available MutationInstances
   */
  private final List<MutationInstance> mutationInstances;

  /**
   * List of prefixes of fully-qualified class names that are mutable
   */
  private final List<String> mutableClasses;

  /**
   * see {@link InstrumentingClassLoader}
   */
  private final ClassFileTransformer lineCoverageTransformer = new SnoopInstructionTransformer();

  /** The optimization level to be applied. */
  private final OptLevel optLevel;

  /**
   * Constructor
   */
  public CartographyClassLoader(URL[] paths, String[] mutableClasses, ClassLoader parent,
      OptLevel opt) {
    super(paths, parent);
    this.mutableClasses = Arrays.asList(mutableClasses);
    this.mutationInstances = new ArrayList<>();
    this.optLevel = opt;
  }

  public List<MutationInstance> getMutationInstances() {
    return mutationInstances;
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if(!mutable(name)) {
      Class<?> cl = super.loadClass(name, resolve);
      return cl;
    }
    synchronized (getClassLoadingLock(name)) {
      Class<?> c = findLoadedClass(name);
      if (c == null) {
        c = findClass(name);
      }
      if (resolve) {
        resolveClass(c);
      }
      return c;
    }
  }

  @Override
  public Class<?> findClass(String name) throws ClassNotFoundException {
    byte[] bytes;

    String internalName = name.replace('.', '/');
    String path = internalName.concat(".class");
    try (InputStream in = super.getResourceAsStream(path)) {
      if (in == null) {
        throw new ClassNotFoundException("Cannot find class " + name);
      }
      bytes = in.readAllBytes();
    } catch (IOException e) {
      throw new ClassNotFoundException("I/O exception while loading class.", e);
    }

    // Make cartograph
    if (mutable(name)) { // Check whether this class is mutable
      Cartographer c = Cartographer.explore(bytes, this);
      for (List<MutationInstance> opportunities : c.getOpportunities().values()) {
          for (MutationInstance mi : opportunities) {
              mutationInstances.add(mi);
          }
      }

      bytes = c.toByteArray();
    }

    // Instrument class to measure both line coverage and mutation coverage
    //
    try {
      byte[] instrumented;
      instrumented = lineCoverageTransformer
          .transform(this, internalName, null, null, bytes.clone());
        if (instrumented != null) {
            bytes = instrumented;
        }
    } catch (IllegalClassFormatException __) {
    }

    return defineClass(name, bytes, 0, bytes.length);
  }

  private boolean mutable(String name) {
    boolean mutable = false;
    for (String s : mutableClasses) {
      if (name.startsWith(s)) {
        mutable = true;
        break;
      }
    }
    return mutable;
  }

  public OptLevel getOptLevel() {
    return optLevel;
  }
}
