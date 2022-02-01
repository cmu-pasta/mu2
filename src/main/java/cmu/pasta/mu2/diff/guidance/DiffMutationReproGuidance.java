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
import com.pholser.junit.quickcheck.Pair;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.instrument.InstrumentationException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * to avoid the problem of the generator type registry not updating for each ClassLoader
 */
public class DiffMutationReproGuidance extends DiffReproGuidance {
    public List<Outcome> cclOutcomes;

    /**
     *  mutation analysis results for each MutationInstance
     * paired with the index of the outcome that killed the mutant
     */
    public Map<MutationInstance, Pair<List<Outcome>, Integer>> mclOutcomes;

    private final MutationClassLoaders MCLs;
    private int ind;

    /**
     * The mutants killed so far
     */
    protected final ArraySet deadMutants = new ArraySet();

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
        for(MutationInstance mutationInstance : MCLs.getCartographyClassLoader().getMutationInstances()) {
            mclOutcomes.put(mutationInstance, new Pair<>(new ArrayList<>(), -1));
        }

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
                if (mclOutcomes.containsKey(mutationInstance) && mclOutcomes.get(mutationInstance).second < 0) {
                    mclOutcomes.get(mutationInstance).first.add(null);
                }
                continue;
            }

            MutationRunInfo mri = new MutationRunInfo(MCLs, mutationInstance, testClass, argBytes, args, method);

            // run with MCL
            try {
                super.run(new TestClass(mri.clazz), mri.method, mri.args);
            } catch (DiffException e) {
                deadMutants.add(mutationInstance.id);
                if (mclOutcomes.containsKey(mutationInstance) && mclOutcomes.get(mutationInstance).second < 0)
                    mclOutcomes.put(mutationInstance, new Pair<>(mclOutcomes.get(mutationInstance).first, ind));
                else if(!mclOutcomes.containsKey(mutationInstance)) {
                    List<Outcome> toAdd = new ArrayList<>();
                    for (int c = 0; c < ind; c++) {
                        toAdd.add(null);
                    }
                    toAdd.add(recentOutcomes.get(recentOutcomes.size() - 1));
                    mclOutcomes.put(mutationInstance, new Pair<>(toAdd, ind));
                }
            } catch(InstrumentationException e) {
                throw new GuidanceException(e);
            } catch (GuidanceException e) {
                throw e;
            } catch (Throwable e) {}

            // add to matching MCL list
            if (mclOutcomes.containsKey(mutationInstance))
                mclOutcomes.get(mutationInstance).first.add(recentOutcomes.get(recentOutcomes.size() - 1));
            else {
                List<Outcome> toAdd = new ArrayList<>();
                for (int c = 0; c < ind; c++) {
                    toAdd.add(null);
                }
                toAdd.add(recentOutcomes.get(recentOutcomes.size() - 1));
                mclOutcomes.put(mutationInstance, new Pair<>(toAdd, -1));
            }
            recentOutcomes.clear();
        }
        if(cclOutcomes.get(cclOutcomes.size() - 1).thrown != null) throw cclOutcomes.get(cclOutcomes.size() - 1).thrown;
    }

}
