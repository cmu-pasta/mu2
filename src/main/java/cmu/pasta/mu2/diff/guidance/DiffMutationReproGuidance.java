package cmu.pasta.mu2.diff.guidance;

import cmu.pasta.mu2.ValidityDifferenceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import org.junit.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiffMutationReproGuidance extends DiffReproGuidance {
    private List<Result> cclResults;
    public static List<Result> recentResults = new ArrayList<>();
    private int ind;

    public DiffMutationReproGuidance(File inputFile, File traceDir, List<Result> cclResults) throws IOException {
        super(inputFile, traceDir);
        this.cclResults = cclResults;
        recentResults.clear();
        ind = -1;
    }

    public DiffMutationReproGuidance(File inputFile, File traceDir, List<Object> cT, List<Result> cclResults) throws IOException {
        super(inputFile, traceDir, cT);
        this.cclResults = cclResults;
        recentResults.clear();
        ind = -1;
    }

    @Override
    public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
        ind++;
        Result mclResult = Result.SUCCESS;
        try {
            super.run(testClass, method, args);
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
