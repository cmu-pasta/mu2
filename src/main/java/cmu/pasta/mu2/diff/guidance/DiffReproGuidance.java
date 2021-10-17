package cmu.pasta.mu2.diff.guidance;

import cmu.pasta.mu2.diff.DiffException;
import cmu.pasta.mu2.diff.junit.DiffTrialRunner;
import edu.berkeley.cs.jqf.fuzz.repro.ReproGuidance;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DiffReproGuidance extends ReproGuidance implements DiffGuidance {
    private Method compare;
    private List<Object> cmpTo, results;
    private boolean comparing;

    public DiffReproGuidance(File inputFile, File traceDir) throws IOException {
        super(inputFile, traceDir);
        cmpTo = null;
        comparing = false;
        results = new ArrayList<>();
        try {
            compare = Object.class.getMethod("equals", Object.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public DiffReproGuidance(File inputFile, File traceDir, List<Object> cT) throws IOException {
        this(inputFile, traceDir);
        results = new ArrayList<>();
        cmpTo = cT;
        comparing = true;
    }

    @Override
    public void setCompare(Method m) {
        compare = m;
    }

    public List<Object> getResults() {
        return results;
    }

    @Override
    public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
        DiffTrialRunner dtr = new DiffTrialRunner(testClass.getJavaClass(), method, args);
        dtr.run();
        results.add(dtr.getResult());
        if(!comparing) return;
        Object o = compare.invoke(null, cmpTo.get(results.size() - 1), results.get(results.size() - 1));
        if (!Boolean.TRUE.equals(o)) {
            throw new DiffException("diff!");
        }
    }
}
