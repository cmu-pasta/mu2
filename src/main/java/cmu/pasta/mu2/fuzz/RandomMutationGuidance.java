package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.diff.DiffException;
import cmu.pasta.mu2.diff.Outcome;
import cmu.pasta.mu2.util.Serializer;
import cmu.pasta.mu2.diff.guidance.DiffGuidance;
import cmu.pasta.mu2.diff.junit.DiffTrialRunner;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.instrument.MutationSnoop;
import cmu.pasta.mu2.instrument.OptLevel;
import cmu.pasta.mu2.util.ArraySet;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.util.MovingAverage;
import edu.berkeley.cs.jqf.instrument.InstrumentationException;
import cmu.pasta.mu2.diff.guidance.OptimizedMutationGuidance;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Objects;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import java.util.Collections;

public class RandomMutationGuidance extends MutationGuidance implements OptimizedMutationGuidance{
    
    private int k;

    public RandomMutationGuidance(String testName, MutationClassLoaders mutationClassLoaders,
    Duration duration, Long trials, File outputDirectory, File seedInputDir, Random rand, int k) throws IOException {
        super(testName, mutationClassLoaders, duration, trials, outputDirectory, seedInputDir, rand);
        this.k = k;
    }

    @Override
    public ArraySet filterMutants() {

        ArrayList<Integer> mutantList = new ArrayList<Integer>();
        int numMutants = 0;
        int size = runMutants.size();
        int n = 0;

        while(numMutants < size){
            if (runMutants.contains(n)){
                numMutants++;
                mutantList.add(n);
            }
            n++;
        }

        Collections.shuffle(mutantList);

        ArraySet filtered = new ArraySet();
        
        for(int i = 0; i < k  && i < mutantList.size(); i++){
            filtered.add(mutantList.get(i));
        }

        return filtered;

    }

    @Override
    public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
        numRuns++;
        runMutants.reset();
        MutationSnoop.setMutantCallback(m -> runMutants.add(m.id));
        mutantExceptionList.clear();

        long startTime = System.currentTimeMillis();

        //run with CCL
        Outcome cclOutcome = getOutcome(testClass.getJavaClass(), method, args);

        // set up info
        long trialTime = System.currentTimeMillis() - startTime;
        byte[] argBytes = Serializer.serialize(args);
        int run = 1;

        runMutants = filterMutants();

        for (MutationInstance mutationInstance : getMutationInstances()) {
        if (deadMutants.contains(mutationInstance.id)) {
            continue;
        }
        if (optLevel != OptLevel.NONE  &&
            !runMutants.contains(mutationInstance.id)) {
            continue;
        }

        // update info
        run += 1;
        mutationInstance.resetTimer();

        MutationRunInfo mri = new MutationRunInfo(mutationClassLoaders, mutationInstance, testClass, argBytes, args, method);

        // run with MCL
        Outcome mclOutcome;
        try {
            DiffTrialRunner dtr = new DiffTrialRunner(mri.clazz, mri.method, mri.args);
            dtr.run();
            if(dtr.getOutput() == null) mclOutcome = new Outcome(null, null);
            else {
            mclOutcome = new Outcome(Serializer.translate(dtr.getOutput(),
                    mutationClassLoaders.getCartographyClassLoader()), null);
            }
        } catch (InstrumentationException e) {
            throw new GuidanceException(e);
        } catch (GuidanceException e) {
            throw e;
        } catch (Throwable e) {
            mclOutcome = new Outcome(null, e);
        }

        // MCL outcome and CCL outcome should be the same (either returned same value or threw same exception)
        // If this isn't the case, the mutant is killed.
        // This catches validity differences because an invalid input throws an AssumptionViolatedException,
        // which will be compared as the thrown value.
        if(!Outcome.same(cclOutcome, mclOutcome, compare)) {
            deadMutants.add(mutationInstance.id);
            Throwable t;
            if(cclOutcome.thrown == null && mclOutcome.thrown != null) {
            // CCL succeeded, MCL threw an exception
            t = mclOutcome.thrown;
            } else {
            t = new DiffException(cclOutcome, mclOutcome);
            }
            mutantExceptionList.add("(" + mutationInstance.toString() + ", " +  t.getClass().getName()+")");

            ((MutationCoverage) runCoverage).kill(mutationInstance);
        }

        // run
        ((MutationCoverage) runCoverage).see(mutationInstance);
        }

        //throw exception if an exception was found by the CCL
        if(cclOutcome.thrown != null) throw cclOutcome.thrown;

        long completeTime = System.currentTimeMillis() - startTime;

        recentRun.add(run);
        mappingTime += trialTime;
        testingTime += completeTime;
        numRuns += run;
    }
    
}

