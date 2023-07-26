package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.instrument.MutationInstance;
import cmu.pasta.mu2.instrument.MutationClassLoader;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import edu.berkeley.cs.jqf.fuzz.difffuzz.Serializer;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** separate functionality for loading with a particular MCL */
public class MutationRunInfo {
    public Class<?> clazz;
    public FrameworkMethod method;
    public Object[] args;

    public MutationRunInfo(MutationClassLoaders MCLs, MutationInstance mutationInstance, TestClass testClass, byte[] argBytes, Object[] oArgs, FrameworkMethod fm) throws ClassNotFoundException, NoSuchMethodException, IOException {
        // load class with MCL
        MutationClassLoader mcl = MCLs.getMutationClassLoader(mutationInstance);
        clazz = Class.forName(testClass.getName(), true, mcl);

        // load method with MCL
        List<Class<?>> paramTypes = new ArrayList<>();
        for (Class<?> clz : fm.getMethod().getParameterTypes()) {
            paramTypes.add(Class.forName(clz.getName(), true, mcl));
        }
        method = new FrameworkMethod(clazz.getMethod(fm.getName(),
                paramTypes.toArray(new Class<?>[]{})));

        // load args with MCL
        args = Serializer.deserialize(argBytes, mcl, oArgs);
    }
}
