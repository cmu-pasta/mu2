package cmu.pasta.mu2.diff.guidance;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.diff.DiffException;
import cmu.pasta.mu2.diff.Outcome;
import cmu.pasta.mu2.fuzz.MutationRunInfo;
import cmu.pasta.mu2.instrument.MutationSnoop;
import cmu.pasta.mu2.instrument.OptLevel;
import cmu.pasta.mu2.util.ArraySet;
import cmu.pasta.mu2.util.Serializer;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import edu.berkeley.cs.jqf.instrument.InstrumentationException;
import org.junit.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * to avoid the problem of the generator type registry not updating for each ClassLoader
 */
public class DiffMutationReproGuidance extends DiffReproGuidance {
    public List<Outcome> cclOutcomes;

    /**
     *  mutation analysis results for each MutationInstance
     * paired with the index of the outcome that killed the mutant
     */
    public Map<MutationInstance, List<Result>> mclOutcomes;

    private final MutationClassLoaders MCLs;
    private int ind;

    /**
     * The mutants killed so far
     */
    private ArraySet deadMutants = new ArraySet();

    /**
     * Current optimization level
     */
    private final OptLevel optLevel;

    /**
     * The set of mutants to execute for a given trial.
     *
     * This is used when the optLevel is set to something higher than NONE,
     * in order to selectively choose which mutants are interesting for a given
     * input. This may include already killed mutants; those are skipped separately.
     *
     * This set must be reset/cleared before execution of every new input.
     */
    private static ArraySet runMutants = new ArraySet();

    public DiffMutationReproGuidance(File inputFile, File traceDir, MutationClassLoaders mcls) throws IOException {
        super(inputFile, traceDir);
        cclOutcomes = new ArrayList<>();
        mclOutcomes = new HashMap<>();
        MCLs = mcls;
        ind = -1;

        this.optLevel = MCLs.getCartographyClassLoader().getOptLevel();
    }

    @Override
    public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
        runMutants.reset();
        MutationSnoop.setMutantCallback(m -> runMutants.add(m.id));

        recentOutcomes.clear();
        ind++;
        cmpTo = null;

        // run CCL
        try {
            super.run(testClass, method, args);
        } catch(InstrumentationException e) {
            throw new GuidanceException(e);
        } catch (GuidanceException e) {
            throw e;
        } catch (Throwable e) {}

        // set up info
        cmpTo = new ArrayList<>(recentOutcomes);
        cclOutcomes.add(cmpTo.get(0));
        byte[] argBytes = Serializer.serialize(args);
        recentOutcomes.clear();

        for (MutationInstance mutationInstance : MCLs.getCartographyClassLoader().getMutationInstances()) {
            if (deadMutants.contains(mutationInstance.id)) {
                continue;
            }
            if (optLevel != OptLevel.NONE  &&
                    !runMutants.contains(mutationInstance.id)) {
                if (mclOutcomes.containsKey(mutationInstance)
                        && mclOutcomes.get(mutationInstance).get(mclOutcomes.get(mutationInstance).size() - 1) != Result.FAILURE)
                    mclOutcomes.get(mutationInstance).add(null);
                else if(!mclOutcomes.containsKey(mutationInstance))
                    mclOutcomes.put(mutationInstance, new ArrayList<>(Collections.singletonList(null)));
                continue;
            }

            MutationRunInfo mri = new MutationRunInfo(MCLs, mutationInstance, testClass, argBytes, args, method);

            // run with MCL
            try {
                super.run(new TestClass(mri.clazz), mri.method, mri.args);
            } catch (DiffException e) {
                deadMutants.add(mutationInstance.id);
                if (mclOutcomes.containsKey(mutationInstance)
                        && mclOutcomes.get(mutationInstance).get(mclOutcomes.get(mutationInstance).size() - 1) != Result.FAILURE)
                    mclOutcomes.get(mutationInstance).add(Result.FAILURE);
                else if(!mclOutcomes.containsKey(mutationInstance)) {
                    List<Result> toAdd = new ArrayList<>();
                    for (int c = 0; c < ind; c++) {
                        toAdd.add(null);
                    }
                    toAdd.add(Result.FAILURE);
                    mclOutcomes.put(mutationInstance, toAdd);
                }
            } catch(InstrumentationException e) {
                throw new GuidanceException(e);
            } catch (GuidanceException e) {
                throw e;
            } catch (Throwable e) {}

            Result result = Result.SUCCESS;
            Outcome oc = recentOutcomes.get(recentOutcomes.size() - 1);
            if(oc.thrown instanceof AssumptionViolatedException) result = Result.INVALID;
            else if(oc.thrown instanceof TimeoutException) result = Result.TIMEOUT;

            // add to matching MCL list
            if (mclOutcomes.containsKey(mutationInstance))
                mclOutcomes.get(mutationInstance).add(result);
            else {
                List<Result> toAdd = new ArrayList<>();
                for (int c = 0; c < ind; c++) {
                    toAdd.add(null);
                }
                toAdd.add(result);
                mclOutcomes.put(mutationInstance, toAdd);
            }
            recentOutcomes.clear();
        }
        if(cclOutcomes.get(cclOutcomes.size() - 1).thrown != null) throw cclOutcomes.get(cclOutcomes.size() - 1).thrown;
    }

}
