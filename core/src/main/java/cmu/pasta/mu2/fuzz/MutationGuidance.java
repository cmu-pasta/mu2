package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.instrument.MutationInstance;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.instrument.OptLevel;
import cmu.pasta.mu2.util.ArraySet;
import edu.berkeley.cs.jqf.fuzz.difffuzz.DiffException;
import edu.berkeley.cs.jqf.fuzz.difffuzz.DiffFuzzGuidance;
import edu.berkeley.cs.jqf.fuzz.difffuzz.Outcome;
import edu.berkeley.cs.jqf.fuzz.difffuzz.Serializer;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.junit.TrialRunner;
import edu.berkeley.cs.jqf.fuzz.util.MovingAverage;
import edu.berkeley.cs.jqf.instrument.InstrumentationException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

/**
 * Guidance that performs mutation-guided fuzzing
 *
 * @author Bella Laybourn
 * @author Rafaello Sanna
 * @author Rohan Padhye
 */
public class MutationGuidance extends ZestGuidance implements DiffFuzzGuidance {

  /**
   * The classloaders for cartography and individual mutation instances
   */
  protected MutationClassLoaders mutationClassLoaders;

  /**
   * The mutants killed so far
   */
  protected ArraySet deadMutants = new ArraySet();

  /**
   * The number of actual runs of the test
   */
  protected long numRuns = 0;

  /**
   * The number of runs done in the last interval
   */
  protected long lastNumRuns = 0;

  /**
   * The total time spent in the cartography class loader
   */
  protected long mappingTime = 0;

  /**
   * The total time spent running the tests
   */
  protected long testingTime = 0;

  /**
   * The size of the moving averages
   */
  protected static final int MOVING_AVERAGE_CAP = 10;

  /**
   * The number of mutants run in the most recent test runs
   */
  protected MovingAverage recentRun = new MovingAverage(MOVING_AVERAGE_CAP);

  /**
   * Current optimization level
   */
  protected final OptLevel optLevel;

  /**
   * The set of mutants to execute for a given trial.
   *
   * This is used when the optLevel is set to something higher than NONE,
   * in order to selectively choose which mutants are interesting for a given
   * input. This may include already killed mutants; those are skipped separately.
   *
   * This set must be reset/cleared before execution of every new input.
   */

  protected Method compare;

  protected final List<String> mutantExceptionList = new ArrayList<>();

  protected final List<MutantFilter> filters = new ArrayList<>();

  protected ArraySet mutantsToRun = new ArraySet();

  public MutationGuidance(String testName, MutationClassLoaders mutationClassLoaders,
      Duration duration, Long trials, File outputDirectory, File seedInputDir, Random rand)
      throws IOException {
    super(testName, duration, trials, outputDirectory, seedInputDir, rand);
    this.mutationClassLoaders = mutationClassLoaders;
    this.totalCoverage = new MutationCoverage();
    this.runCoverage = new MutationCoverage();
    this.validCoverage = new MutationCoverage();
    this.optLevel = mutationClassLoaders.getCartographyClassLoader().getOptLevel();

    filters.add(new DeadMutantsFilter(this));
    if(optLevel != OptLevel.NONE){
      filters.add(new PIEMutantFilter(this,optLevel));
    }
    try {
      compare = Objects.class.getMethod("equals", Object.class, Object.class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  /** Add filters to be used (in addition to PIE and DeadMutants) */
  public void addFilters (List<MutantFilter> additionalFilters){
    filters.addAll(additionalFilters);
  }

  /** Get number of mutants seen so far */
  public int getSeenMutants() {
    return ((MutationCoverage) totalCoverage).numSeenMutants();
  }

  /** Retreive the latest list of mutation instances */
  protected List<MutationInstance> getMutationInstances() {
    // The latest list is available in the cartography class loader, which runs the initial test execution
    return mutationClassLoaders.getCartographyClassLoader().getMutationInstances();
  }

  public void setCompare(Method m) {
    compare = m;
  }

  /**
   * The names to be written to the top of the stats file
   */
  @Override
  protected String getStatNames() {
    return super.getStatNames()
        + ", found_muts, dead_muts, seen_muts, run_muts, total_time, map_time";
  }

  @Override
  protected List<String> checkSavingCriteriaSatisfied(Result result) {
    List<String> criteria = super.checkSavingCriteriaSatisfied(result);
    int newKilledMutants = ((MutationCoverage) totalCoverage).updateMutants(((MutationCoverage) runCoverage));
    if (newKilledMutants > 0) {
      criteria.add(String.format("+%d mutants %s", newKilledMutants, mutantExceptionList.toString()));
      currentInput.setFavored();
    }

    // TODO: Add responsibilities for mutants killed

    return criteria;
  }

  @Override
  public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
    numRuns++;
    mutantExceptionList.clear();
    mutantsToRun.reset();

    long startTime = System.currentTimeMillis();

    //run with CCL
    Outcome cclOutcome = getOutcome(testClass.getJavaClass(), method, args);

    // set up info
    long trialTime = System.currentTimeMillis() - startTime;
    byte[] argBytes = Serializer.serialize(args);
    int run = 0;

    List<MutationInstance> mutationInstances = getMutationInstances();
    for(MutantFilter filter : filters){
      mutationInstances = filter.filterMutants(mutationInstances);
    }

    for (MutationInstance mutationInstance : mutationInstances) {
      // update info
      run += 1;
      mutationInstance.resetTimer();

      MutationRunInfo mri = new MutationRunInfo(mutationClassLoaders, mutationInstance, testClass, argBytes, args, method);

      // run with MCL
      Outcome mclOutcome;
      try {
        TrialRunner dtr = new TrialRunner(mri.clazz, mri.method, mri.args);
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

  @Override
  protected void displayStats(boolean force) {
    Date now = new Date();
    long intervalTime = now.getTime() - lastRefreshTime.getTime();
    long totalTime = now.getTime() - startTime.getTime();

    if (intervalTime < STATS_REFRESH_TIME_PERIOD && !force) {
      return;
    }

    double trialsPerSec = numTrials * 1000L / totalTime;
    long interlvalTrials = numTrials - lastNumTrials;
    double intervalTrialsPerSec = interlvalTrials * 1000.0 / intervalTime;

    double runsPerSec = numRuns * 1000L / totalTime;
    long intervalRuns = numRuns - lastNumRuns;
    double intervalRunsPerSec = intervalRuns * 1000.0 / intervalTime;

    lastRefreshTime = now;
    lastNumTrials = numTrials;
    lastNumRuns = numRuns;

    String currentParentInputDesc;
    if (seedInputs.size() > 0 || savedInputs.isEmpty()) {
      currentParentInputDesc = "<seed>";
    } else {
      Input currentParentInput = savedInputs.get(currentParentInputIdx);
      currentParentInputDesc = currentParentInputIdx + " ";
      currentParentInputDesc += currentParentInput.isFavored() ? "(favored)" : "(not favored)";
      currentParentInputDesc += " {" + numChildrenGeneratedForCurrentParentInput +
          "/" + getTargetChildrenForParent(currentParentInput) + " mutations}";
    }

    int nonZeroCount = totalCoverage.getNonZeroCount();
    double nonZeroFraction = nonZeroCount * 100.0 / totalCoverage.size();
    int nonZeroValidCount = validCoverage.getNonZeroCount();
    double nonZeroValidFraction = nonZeroValidCount * 100.0 / validCoverage.size();
    int totalFound = getMutationInstances().size();

    if (console != null) {

      if (LIBFUZZER_COMPAT_OUTPUT) {
        console.printf("#%,d\tNEW\tcov: %,d exec/s: %,d L: %,d\n", numTrials, nonZeroValidCount,
            (long) intervalTrialsPerSec, currentInput.size());
      } else if (!QUIET_MODE) {
        console.printf("\033[2J");
        console.printf("\033[H");
        console.printf(this.getTitle() + "\n");
        if (this.testName != null) {
          console.printf("Test name:            %s\n", this.testName);
        }
        console.printf("Results directory:    %s\n", this.outputDirectory.getAbsolutePath());
        console.printf("Elapsed time:         %s (%s)\n", millisToDuration(totalTime),
            maxDurationMillis == Long.MAX_VALUE ? "no time limit"
                : ("max " + millisToDuration(maxDurationMillis)));
        console.printf("Number of trials:     %,d\n", numTrials);
        console.printf("Number of executions: %,d\n", numRuns);
        console
            .printf("Valid inputs:         %,d (%.2f%%)\n", numValid, numValid * 100.0 / numTrials);
        console.printf("Cycles completed:     %d\n", cyclesCompleted);
        console.printf("Unique failures:      %,d\n", uniqueFailures.size());
        console.printf("Queue size:           %,d (%,d favored last cycle)\n", savedInputs.size(),
            numFavoredLastCycle);
        console.printf("Current parent input: %s\n", currentParentInputDesc);
        console.printf("Fuzzing Throughput:   %,d/sec now | %,d/sec overall\n",
            (long) intervalTrialsPerSec, (long) trialsPerSec);
        console.printf("Execution Speed:      %,d/sec now | %,d/sec overall\n",
            (long) intervalRunsPerSec, (long) runsPerSec);
        console.printf("Testing Time:         %s\n", millisToDuration(totalTime));
        console
            .printf("Mapping Time:         %s (%.2f%% of total)\n", millisToDuration(mappingTime),
                (double) mappingTime * 100.0 / (double) totalTime);
        console.printf("Found Mutants:        %d\n", totalFound);
        console.printf("Recent Run Mutants:   %.2f (%.2f%% of total)\n", recentRun.get(),
            recentRun.get() * 100.0 / totalFound);
        console.printf("Total coverage:       %,d branches (%.2f%% of map)\n", nonZeroCount,
            nonZeroFraction);
        console.printf("Valid coverage:       %,d branches (%.2f%% of map)\n", nonZeroValidCount,
            nonZeroValidFraction);
        console.printf("Total coverage:       %,d mutants\n",
            ((MutationCoverage) totalCoverage).numCaughtMutants());
        console.printf("Available to Cover:   %,d mutants\n",
            ((MutationCoverage) totalCoverage).numSeenMutants());
      }
    }

    String plotData = String.format(
        "%d, %d, %d, %d, %d, %d, %.2f%%, %d, %d, %d, %.2f, %d, %d, %.2f%%, %d, %d, %d, %d, %d, %.2f, %d, %d",
        TimeUnit.MILLISECONDS.toSeconds(now.getTime()), cyclesCompleted, currentParentInputIdx,
        numSavedInputs, 0, 0, nonZeroFraction, uniqueFailures.size(), 0, 0, intervalTrialsPerSec,
        numValid, numTrials - numValid, nonZeroValidFraction, nonZeroCount, nonZeroValidCount,
        totalFound, deadMutants.size(), ((MutationCoverage) totalCoverage).numSeenMutants(),
        recentRun.get(), testingTime, mappingTime);
    appendLineToFile(statsFile, plotData);
  }

  @Override
  protected String getTitle() {
    if (blind) {
      return "Generator-based random fuzzing (no guidance)\n" +
          "--------------------------------------------\n";
    } else {
      return "Mutation-Guided Fuzzing\n" +
          "--------------------------\n";
    }
  }
}
