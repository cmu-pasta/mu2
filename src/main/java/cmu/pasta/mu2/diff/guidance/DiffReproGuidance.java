package cmu.pasta.mu2.diff.guidance;

import cmu.pasta.mu2.diff.DiffException;
import cmu.pasta.mu2.diff.junit.DiffTrialRunner;
import edu.berkeley.cs.jqf.fuzz.repro.ReproGuidance;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public class DiffReproGuidance extends ReproGuidance implements DiffGuidance {
    private Method compare;
    private Object cmpTo, result;
    private final ClassLoader classLoader;

    public DiffReproGuidance(File inputFile, File traceDir, ClassLoader cl) throws IOException {
        super(inputFile, traceDir);
        classLoader = cl;
        cmpTo = null;
        try {
            compare = Object.class.getMethod("equals", Object.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public DiffReproGuidance(File inputFile, File traceDir, ClassLoader cl, Object cT) throws IOException {
        this(inputFile, traceDir, cl);
        cmpTo = cT;
    }

    @Override
    public void setCompare(Method m) {
        compare = m;
    }

    public Object getResult() {
        return result;
    }

    @Override
    public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
        System.out.print("args: ");
        for(Object a : args) {
            System.out.print(a + ", ");
        }
        System.out.println();
        DiffTrialRunner dtr = new DiffTrialRunner(testClass.getJavaClass(), method, args);
        dtr.run();
        result = dtr.getResult();
        if(cmpTo == null) return;
        Class<?> clazz = Class.forName(testClass.getName(),true, classLoader);
        Object o = compare.invoke(clazz.getConstructors()[0].newInstance(), cmpTo, result);
        if (!Boolean.TRUE.equals(o)) {
            throw new DiffException("diff!");
        }
    }
}
