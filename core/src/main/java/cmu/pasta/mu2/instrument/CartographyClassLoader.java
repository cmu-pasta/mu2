package cmu.pasta.mu2.instrument;

import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import cmu.pasta.mu2.mutators.Mutator;
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
    Mutator.initializeMutators();
  }

  public List<MutationInstance> getMutationInstances() {
    return mutationInstances;
  }

  @Override
  public Class<?> findClass(String name) throws ClassNotFoundException, GuidanceException {
    try {
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

      // Check whether this class is mutable
      boolean mutable = false;

      for (String s : mutableClasses) {
        if (name.startsWith(s)) {
          mutable = true;
          break;
        }
      }

      // Make cartograph
      if (mutable) {
        Cartographer c = Cartographer.explore(bytes.clone(), this);
        for (List<MutationInstance> opportunities : c.getOpportunities().values()) {
          for (MutationInstance mi : opportunities) {
            mutationInstances.add(mi);
          }
        }

        bytes = c.toByteArray();
      }

      return defineClass(name, bytes, 0, bytes.length);
    } catch (OutOfMemoryError e) {
      throw new GuidanceException(e);
    }
  }

  public OptLevel getOptLevel() {
    return optLevel;
  }
}
