package cmu.pasta.mu2.diff.junit;

import cmu.pasta.mu2.diff.Diff;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.junit.TrialRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

public class DiffTrialRunner extends TrialRunner {
    private Object output;

    public DiffTrialRunner(Class<?> testClass, FrameworkMethod method, Object[] args) throws InitializationError {
        super(testClass, method, args);
    }

    @Override protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> diffMethods = getTestClass().getAnnotatedMethods(Diff.class);
        List<FrameworkMethod> fuzzMethods = getTestClass().getAnnotatedMethods(Fuzz.class);
        List<FrameworkMethod> testMethods = new ArrayList<>();
        if(diffMethods.size() > 0) testMethods.addAll(diffMethods);
        if(fuzzMethods.size() > 0) testMethods.addAll(fuzzMethods);
        return testMethods;
    }

    @Override protected Statement methodInvoker(
            FrameworkMethod frameworkMethod,
            Object test) {
        return new Statement() {
            @Override public void evaluate() throws Throwable {
                output = frameworkMethod.invokeExplosively(test, args);
            }
        };
    }

    public Object getOutput() {
        return output;
    }
}
