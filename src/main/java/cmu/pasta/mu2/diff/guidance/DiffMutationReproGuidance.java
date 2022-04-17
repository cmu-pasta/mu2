package cmu.pasta.mu2.diff.guidance;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.diff.DiffException;
import cmu.pasta.mu2.diff.Outcome;
import cmu.pasta.mu2.diff.junit.DiffTrialRunner;
import cmu.pasta.mu2.fuzz.MutationRunInfo;
import cmu.pasta.mu2.instrument.MutationSnoop;
import cmu.pasta.mu2.instrument.MutationTimeoutException;
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
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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

    private File reportFile;

    /**
     * Timeout for each mutant (DEFAULT: 10 seconds).
     */
    private static int TIMEOUT = Integer.getInteger("mu2.TIMEOUT", 10);

    public static boolean RUN_MUTANTS_IN_PARALLEL = Boolean.getBoolean("mu2.PARALLEL");

    /**
     * Number of threads used to run mutants.
     */
    private static int BACKGROUND_THREADS = Integer.getInteger("mu2.BACKGROUND_THREADS", Runtime.getRuntime().availableProcessors());

    private final ExecutorService executor = Executors.newFixedThreadPool(BACKGROUND_THREADS);

    public DiffMutationReproGuidance(File inputFile, File traceDir, MutationClassLoaders mcls, File resultsDir) throws IOException {
        super(inputFile, traceDir);
        MCLs = mcls;
        ind = -1;

        reportFile = new File(resultsDir, "mutate-repro-out.txt");
        this.optLevel = MCLs.getCartographyClassLoader().getOptLevel();
    }

    public Outcome dispatchMutationInstance(MutationInstance mutationInstance, TestClass testClass, byte[] argBytes,
                                            Object[] args, FrameworkMethod method)
            throws IOException, ClassNotFoundException, NoSuchMethodException, ExecutionException, InterruptedException {
        if (deadMutants.contains(mutationInstance.id)) {
            return null;
        }
        if (optLevel != OptLevel.NONE  &&
                !runMutants.contains(mutationInstance.id)) {
            return null;
        }

        MutationRunInfo mri = new MutationRunInfo(MCLs, mutationInstance, testClass, argBytes, args, method);
        mutationInstance.resetTimer();

        // run with MCL
        FutureTask<Outcome> task = new FutureTask<>(() -> {
            try {
                return getOutcome((new TestClass(mri.clazz)).getJavaClass(), mri.method, mri.args);
            } catch(InstrumentationException e) {
                throw new GuidanceException(e);
            }
        });
        Outcome mclOutcome = null;

        if (RUN_MUTANTS_IN_PARALLEL) {

            Thread thread = new Thread(task);
            thread.start();

            ExecutorService service = Executors.newSingleThreadExecutor();
            Future<Outcome> outcome = service.submit(() -> task.get());

            try {
                mclOutcome = outcome.get(TIMEOUT, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                thread.stop();
                mclOutcome = new Outcome(null, new MutationTimeoutException(TIMEOUT));
            }

            service.shutdownNow();
        } else {
            task.run();
            mclOutcome = task.get();
        }

        return mclOutcome;
    }

    @Override
    public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
        runMutants.reset();
        MutationSnoop.setMutantCallback(m -> runMutants.add(m.id));
        ind++;
        Outcome cclOut;

        // run CCL
        try {
            cclOut = getOutcome(testClass.getJavaClass(), method, args);
        } catch(InstrumentationException e) {
            throw new GuidanceException(e);
        }

        System.out.println("CCL Outcome for input " + ind + ": " + cclOut);
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(reportFile, true))) {
            pw.printf("CCL Outcome for input %d: %s\n", ind, cclOut.toString());
        }

        // set up info
        byte[] argBytes = Serializer.serialize(args);
        List<Outcome> results = new ArrayList<>();

        if (RUN_MUTANTS_IN_PARALLEL) {
            List<Future<Outcome>> futures = MCLs.getCartographyClassLoader().getMutationInstances()
                    .stream()
                    .map(instance ->
                            executor.submit(() ->
                                    dispatchMutationInstance(instance, testClass, argBytes, args, method)))
                    .collect(Collectors.toList());
            // Use for loop to capture exceptions.
            for (Future<Outcome> future : futures) {
                results.add(future.get());
            }
        } else {
            for (MutationInstance instance: MCLs.getCartographyClassLoader().getMutationInstances()) {
                results.add(dispatchMutationInstance(instance, testClass, argBytes, args, method));
            }
        }

        ClassLoader cmpCL = compare.getDeclaringClass().getClassLoader();
        Outcome cmpSerial = new Outcome(Serializer.translate(cclOut.output, cmpCL), cclOut.thrown);

        for(int c = 0; c < results.size(); c++) {
            Outcome out = results.get(c);
            if(out == null) continue;
            System.out.println("Running Mutant " + MCLs.getCartographyClassLoader().getMutationInstances().get(c));
            try (PrintWriter pw = new PrintWriter(new FileOutputStream(reportFile, true))) {
                pw.printf("Running Mutant %s\n", MCLs.getCartographyClassLoader().getMutationInstances().get(c).toString());
            }
            Outcome outSerial = new Outcome(Serializer.translate(out.output, cmpCL), out.thrown);
            if (!Outcome.same(cmpSerial, outSerial, compare)) {
                Throwable e = new DiffException(cclOut, out);
                deadMutants.add(MCLs.getCartographyClassLoader().getMutationInstances().get(c).id);
                System.out.println("FAILURE: killed by input " + ind + ": " + e);
                try (PrintWriter pw = new PrintWriter(new FileOutputStream(reportFile, true))) {
                    pw.printf("FAILURE: killed by input %d: %s\n", ind, e.toString());
                }
            }
        }

        if(cclOut.thrown != null) throw cclOut.thrown;
    }

}
