package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.diff.DiffException;
import cmu.pasta.mu2.diff.guidance.DiffGuidance;
import cmu.pasta.mu2.diff.junit.DiffTrialRunner;
import cmu.pasta.mu2.instrument.MutationClassLoader;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.instrument.MutationSnoop;
import cmu.pasta.mu2.instrument.OptLevel;
import cmu.pasta.mu2.util.ArraySet;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.junit.TrialRunner;
import edu.berkeley.cs.jqf.fuzz.util.MovingAverage;
import edu.berkeley.cs.jqf.instrument.InstrumentationException;

import java.io.*;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

/**
 * Guidance that performs mutation-guided fuzzing
 *
 * @author Bella Laybourn
 * @author Rafaello Sanna
 * @author Rohan Padhye
 */
public class MutationGuidance extends ZestGuidance implements DiffGuidance {

  /**
   * The classloaders for cartography and individual mutation instances
   */
  private MutationClassLoaders mutationClassLoaders;

  /**
   * The mutants killed so far
   */
  private ArraySet deadMutants = new ArraySet();

  /**
   * The number of actual runs of the test
   */
  private long numRuns = 0;

  /**
   * The number of runs done in the last interval
   */
  private long lastNumRuns = 0;

  /**
   * The total time spent in the cartography class loader
   */
  private long mappingTime = 0;

  /**
   * The total time spent running the tests
   */
  private long testingTime = 0;

  /**
   * The size of the moving averages
   */
  private static final int MOVING_AVERAGE_CAP = 10;

  /**
   * The number of mutants run in the most recent test runs
   */
  private MovingAverage recentRun = new MovingAverage(MOVING_AVERAGE_CAP);

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

  private Method compare;

  private final List<String> exceptions = new ArrayList<>();

  public MutationGuidance(String testName, MutationClassLoaders mutationClassLoaders,
      Duration duration, Long trials, File outputDirectory, File seedInputDir, Random rand)
      throws IOException {
    super(testName, duration, trials, outputDirectory, seedInputDir, rand);
    this.mutationClassLoaders = mutationClassLoaders;
    this.totalCoverage = new MutationCoverage();
    this.runCoverage = new MutationCoverage();
    this.validCoverage = new MutationCoverage();
    this.optLevel = mutationClassLoaders.getCartographyClassLoader().getOptLevel();
    try {
      compare = Objects.class.getMethod("equals", Object.class, Object.class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  // Retreive the latest list of mutation instances
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
      criteria.add(String.format("+%d mutants %s", newKilledMutants, exceptions.toString()));
    }

    // TODO: Add responsibilities for mutants killed

    return criteria;
  }

  @Override
  public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
    numRuns++;
    runMutants.reset();
    MutationSnoop.setMutantCallback(m -> runMutants.add(m.id));
    exceptions.clear();

    long startTime = System.currentTimeMillis();

    DiffTrialRunner dtr = new DiffTrialRunner(testClass.getJavaClass(), method, args);
    dtr.run(); // loaded by CartographyClassLoader
    Object cclResult = dtr.getResult();

    long trialTime = System.currentTimeMillis() - startTime;

    List<Throwable> fails = new ArrayList<>();
    List<Class<?>> expectedExceptions = Arrays.asList(method.getMethod().getExceptionTypes());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(out);
    for(Object arg : args) {
      oos.writeObject(arg);
    }
    oos.writeObject(cclResult);
    byte[] argBytes = out.toByteArray();

    int run = 1;
    for (MutationInstance mutationInstance : getMutationInstances()) {
      if (deadMutants.contains(mutationInstance.id)) {
        continue;
      }
      if (optLevel != OptLevel.NONE  &&
          !runMutants.contains(mutationInstance.id)) {
        continue;
      }

      run += 1;
      try {
        mutationInstance.resetTimer();
        MutationClassLoader mcl = mutationClassLoaders.getMutationClassLoader(mutationInstance);
        Class<?> clazz = Class.forName(testClass.getName(), true, mcl);
        List<Class<?>> paramTypes = new ArrayList<>();
        for(Class<?> clz : method.getMethod().getParameterTypes()) {
          paramTypes.add(Class.forName(clz.getName(), true, mcl));
        }
        List<Object> argsList = new ArrayList<>();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(argBytes)) {
          @Override
          public Class<?> resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException {
            try {
              return Class.forName(osc.getName(), true, mcl);
            } catch (Exception e) {
              System.out.println("in resolveClass");
              e.printStackTrace();
              return super.resolveClass(osc);
            }
          }
        };
        for(Object arg : args) {
          argsList.add(ois.readObject());
        }
        Object nCclResult = ois.readObject();
        ois.close();
        dtr = new DiffTrialRunner(clazz,
            new FrameworkMethod(clazz.getMethod(method.getName(),
                paramTypes.toArray(new Class<?>[]{}))),
                argsList.toArray());
        dtr.run();
        if(compare != null) {
          Method ncmp = Class.forName(compare.getDeclaringClass().getName(), true, mcl).getMethod(compare.getName(), compare.getParameterTypes());
          if(!Boolean.TRUE.equals(ncmp.invoke(null, nCclResult, dtr.getResult()))) {
            throw new DiffException(nCclResult + " vs " + dtr.getResult());
          }
        }
      } catch (InstrumentationException e) {
        throw new GuidanceException(e);
      } catch (GuidanceException e) {
        throw e;
      } catch (AssumptionViolatedException e) {
        // ignored
      } catch (Throwable e) {
        if (!isExceptionExpected(e.getClass(), expectedExceptions)) {
          // failed
          deadMutants.add(mutationInstance.id);
          exceptions.add(e.getClass().getName());

          ((MutationCoverage) runCoverage).kill(mutationInstance);
          fails.add(e);
        }
      }
      // run
      ((MutationCoverage) runCoverage).see(mutationInstance);
    }

    long completeTime = System.currentTimeMillis() - startTime;

    recentRun.add(run);
    mappingTime += trialTime;
    testingTime += completeTime;
    numRuns += run;
    oos.close();
  }

  private boolean isExceptionExpected(Class<? extends Throwable> e,
      List<Class<?>> expectedExceptions) {
    for (Class<?> expectedException : expectedExceptions) {
      if (expectedException.isAssignableFrom(e)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void displayStats(boolean force) {
    Date now = new Date();
    long intervalTime = now.getTime() - lastRefreshTime.getTime();
    long totalTime = now.getTime() - startTime.getTime();

    if (intervalTime < STATS_REFRESH_TIME_PERIOD) {
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
        "%d, %d, %d, %d, %d, %d, %.2f%%, %d, %d, %d, %.2f, %d, %d, %.2f%%, %d, %d, %d, %.2f, %d, %d",
        TimeUnit.MILLISECONDS.toSeconds(now.getTime()), cyclesCompleted, currentParentInputIdx,
        numSavedInputs, 0, 0, nonZeroFraction, uniqueFailures.size(), 0, 0, intervalTrialsPerSec,
        numValid, numTrials - numValid, nonZeroValidFraction,
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
