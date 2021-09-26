package cmu.pasta.mu2.diff.junit;

import cmu.pasta.mu2.diff.Diff;
import edu.berkeley.cs.jqf.fuzz.junit.TrialRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;

public class DiffTrialRunner extends TrialRunner {
    private Object result;

    public DiffTrialRunner(Class<?> testClass, FrameworkMethod method, Object[] args) throws InitializationError {
        super(testClass, method, args);
    }

    @Override protected List<FrameworkMethod> computeTestMethods() {
        return getTestClass().getAnnotatedMethods(Diff.class);
    }

    @Override protected Statement methodInvoker(
            FrameworkMethod frameworkMethod,
            Object test) {
        return new Statement() {
            @Override public void evaluate() throws Throwable {
                result = frameworkMethod.invokeExplosively(test, args);
            }
        };
    }

    public Object getResult() {
        return result;
    }
}
