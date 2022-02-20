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
import edu.berkeley.cs.jqf.instrument.InstrumentationException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

/**
 * to avoid the problem of the generator type registry not updating for each ClassLoader
 */
public class DiffMutationReproGuidance extends DiffReproGuidance {
    public List<Outcome> cclOutcomes;

    /**
     *  mutation analysis results for each MutationInstance
     * paired with the index of the outcome that killed the mutant
     */

    private final MutationClassLoaders MCLs;
    private int ind;

    /**
     * The mutants killed so far
     */
    public final ArraySet deadMutants = new ArraySet();

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

    private boolean serializeIn;
    private boolean serializeOut;

    private File reportFile;

    public DiffMutationReproGuidance(File inputFile, File traceDir, MutationClassLoaders mcls, boolean serialIn, boolean serialOut, File resultsDir) throws IOException {
        super(inputFile, traceDir, true);
        cclOutcomes = new ArrayList<>();
        MCLs = mcls;
        ind = -1;
        serializeIn = serialIn;
        serializeOut = serialOut;
        reportFile = new File(resultsDir, "mutate-repro-out.txt");
        this.optLevel = MCLs.getCartographyClassLoader().getOptLevel();
    }

    @Override
    public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
        runMutants.reset();
        MutationSnoop.setMutantCallback(m -> runMutants.add(m.id));

        recentOutcomes.clear();
        cmpTo = null;
        ind++;

        // run CCL
        try {
            super.run(testClass, method, args);
        } catch(InstrumentationException e) {
            throw new GuidanceException(e);
        } catch (GuidanceException e) {
            throw e;
        } catch (Throwable e) {}

        System.out.println("CCL Outcome for input " + ind + ": " + recentOutcomes.get(0));
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(reportFile, true))) {
            pw.printf("CCL Outcome for input %d: %s\n", ind, recentOutcomes.get(0).toString());
        }

        // set up info
        cmpTo = new ArrayList<>(recentOutcomes);
        cclOutcomes.add(cmpTo.get(0));
        byte[] argBytes = null;
        if(serializeIn) argBytes = Serializer.serialize(args);
        recentOutcomes.clear();

        for (MutationInstance mutationInstance : MCLs.getCartographyClassLoader().getMutationInstances()) {
            if (deadMutants.contains(mutationInstance.id)) {
                continue;
            }
            if (optLevel != OptLevel.NONE  &&
                    !runMutants.contains(mutationInstance.id)) {
                continue;
            }

            MutationRunInfo mri = new MutationRunInfo(MCLs, mutationInstance, testClass, args, method);
            if(serializeIn) mri = new MutationRunInfo(MCLs, mutationInstance, testClass, argBytes, args, method);

            // run with MCL
            System.out.println("Running Mutant " + mutationInstance);
            try (PrintWriter pw = new PrintWriter(new FileOutputStream(reportFile, true))) {
                pw.printf("Running Mutant %s\n", mutationInstance.toString());
            }

            try {
                super.run(new TestClass(mri.clazz), mri.method, mri.args);
            } catch (DiffException e) {
                deadMutants.add(mutationInstance.id);
                System.out.println("FAILURE: killed by input " + ind + ": " + e);
                try (PrintWriter pw = new PrintWriter(new FileOutputStream(reportFile, true))) {
                    pw.printf("FAILURE: killed by input %d: %s\n", ind, e.toString());
                }
            } catch(InstrumentationException e) {
                throw new GuidanceException(e);
            } catch (GuidanceException e) {
                throw e;
            } catch (Throwable e) {}

            recentOutcomes.clear();
        }
        if(cclOutcomes.get(cclOutcomes.size() - 1).thrown != null) throw cclOutcomes.get(cclOutcomes.size() - 1).thrown;
    }

}
