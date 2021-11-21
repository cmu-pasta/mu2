package cmu.pasta.mu2;

import cmu.pasta.mu2.diff.junit.DiffTrialRunner;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.junit.TrialRunner;
import edu.berkeley.cs.jqf.fuzz.repro.ReproGuidance;
import edu.berkeley.cs.jqf.fuzz.util.IOUtils;
import janala.logger.inst.ATHROW;
import org.junit.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MutationReproGuidance extends ReproGuidance {
    private List<Result> cclResults;
    public static List<Result> recentResults = new ArrayList<>();
    private int ind;

    public MutationReproGuidance(File[] inputFiles, File traceDir, List<Result> cclResults) throws IOException {
        super(inputFiles, traceDir);
        this.cclResults = cclResults;
        recentResults.clear();
        ind = -1;
    }

    public MutationReproGuidance(File inputFile, File traceDir, List<Result> cclResults) throws IOException {
        this(IOUtils.resolveInputFileOrDirectory(inputFile), traceDir, cclResults);
    }

    @Override
    public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
        ind++;
        Result mclResult = Result.SUCCESS;
        try {
            new TrialRunner(testClass.getJavaClass(), method, args).run();
        } catch (AssumptionViolatedException e) {
            if(cclResults == null) throw e;
            mclResult = Result.INVALID;
        }
        if(cclResults != null && mclResult != cclResults.get(ind)) {
            throw new ValidityDifferenceException("ccl and mcl had different validity results");
        }
    }

    @Override
    public void handleResult(Result result, Throwable error) {
        recentResults.add(result);
        super.handleResult(result, error);
    }
}
