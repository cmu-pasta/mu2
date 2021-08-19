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
import org.objectweb.asm.ClassReader;

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
   * if nonempty, class must be here to be mutable
   */
  private final List<String> includeClasses;

  /**
   * class must not be here to be mutable
   */
  private final List<String> excludeClasses;

  /**
   * see {@link InstrumentingClassLoader}
   */
  private final ClassFileTransformer lineCoverageTransformer = new SnoopInstructionTransformer();

  /**
   * Constructor
   */
  public CartographyClassLoader(URL[] paths, String[] mutables, String[] immutables,
      ClassLoader parent) {
    super(paths, parent);
    includeClasses = new ArrayList<>(Arrays.asList(mutables));
    excludeClasses = new ArrayList<>(Arrays.asList(immutables));
    mutationInstances = new ArrayList<>();
  }

  public List<MutationInstance> getMutationInstances() {
    return mutationInstances;
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

    // Check includes + excludes
    boolean mutable = true;

    for (String s : excludeClasses) {
      if (name.startsWith(s)) {
        mutable = false;
        break;
      }
    }

    for (String s : includeClasses) {
      if (name.startsWith(s)) {
        mutable = true;
        break;
      }
    }

    // Make cartograph
    if (mutable) {
      Cartographer c = Cartographer.explore(new ClassReader(bytes), this);

        for (List<MutationInstance> opportunities : c.getOpportunities().values()) {
            for (MutationInstance chance : opportunities) {
                mutationInstances.add(chance);
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
}
