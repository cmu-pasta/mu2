package cmu.pasta.mu2.diff.guidance;

import cmu.pasta.mu2.diff.junit.DiffTrialRunner;
import edu.berkeley.cs.jqf.fuzz.random.NoGuidance;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.PrintStream;
import java.lang.reflect.Method;

public class DiffNoGuidance extends NoGuidance implements DiffGuidance {
    public DiffNoGuidance(long maxTrials, PrintStream out) {
        super(maxTrials, out);
    }

    @Override
    public void setCompare(Method m) {

    }

    @Override
    public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
        new DiffTrialRunner(testClass.getJavaClass(), method, args).run();
    }
}
