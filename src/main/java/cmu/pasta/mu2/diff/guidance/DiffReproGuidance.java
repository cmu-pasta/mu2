package cmu.pasta.mu2.diff.guidance;

import cmu.pasta.mu2.diff.DiffException;
import cmu.pasta.mu2.diff.Outcome;
import cmu.pasta.mu2.diff.junit.DiffTrialRunner;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.repro.ReproGuidance;
import edu.berkeley.cs.jqf.instrument.InstrumentationException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DiffReproGuidance extends ReproGuidance implements DiffGuidance {
    private Method compare;
    private List<Outcome> cmpTo;
    public static final List<Outcome> recentOutcomes = new ArrayList<>();
    private boolean comparing;

    public DiffReproGuidance(File inputFile, File traceDir) throws IOException {
        super(inputFile, traceDir);
        cmpTo = null;
        comparing = false;
        recentOutcomes.clear();
        try {
            compare = Objects.class.getMethod("equals", Object.class, Object.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public DiffReproGuidance(File inputFile, File traceDir, List<Outcome> cmpRes) throws IOException {
        this(inputFile, traceDir);
        cmpTo = cmpRes;
        comparing = true;
    }

    @Override
    public void setCompare(Method m) {
        compare = m;
    }

    @Override
    public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
        DiffTrialRunner dtr = new DiffTrialRunner(testClass.getJavaClass(), method, args);
        Outcome out;
        try {
            dtr.run();
            out = new Outcome(dtr.getOutput(), null);
        } catch(InstrumentationException e) {
            throw new GuidanceException(e);
        } catch (GuidanceException e) {
            throw e;
        } catch(Throwable e) {
            out = new Outcome(null, e);
        }
        recentOutcomes.add(out);
        if(!comparing) {
            if(out.thrown != null) throw out.thrown;
            return;
        }
        if(!Outcome.same(cmpTo.get(recentOutcomes.size() - 1), out, compare)) {
            throw new DiffException(cmpTo.get(recentOutcomes.size() - 1), recentOutcomes.get(recentOutcomes.size() - 1));
        }
    }
}
