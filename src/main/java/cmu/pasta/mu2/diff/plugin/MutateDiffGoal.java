package cmu.pasta.mu2.diff.plugin;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.MutationReproGuidance;
import cmu.pasta.mu2.diff.guidance.DiffMutationReproGuidance;
import cmu.pasta.mu2.diff.guidance.DiffReproGuidance;
import cmu.pasta.mu2.instrument.CartographyClassLoader;
import cmu.pasta.mu2.instrument.MutationClassLoader;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.instrument.OptLevel;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.util.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.junit.runner.Result;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader.stringsToUrls;

/**
 * Repro goal for mu2. Performs mutation testing to calculate
 * mutation score for a saved test input corpus.
 *
 * @author Bella Laybourn
 */
@Mojo(name="mutatediff",
        requiresDependencyResolution= ResolutionScope.TEST)
public class MutateDiffGoal extends AbstractMojo {
    @Parameter(defaultValue="${project}", required=true, readonly=true)
    MavenProject project;

    @Parameter(property="resultsDir", defaultValue="${project.build.directory}", readonly=true)
    private File resultsDir;

    /**
     * The corpus of inputs to repro
     */
    @Parameter(property="input", required=true)
    private File input;

    /**
     * Test class
     */
    @Parameter(property = "class", required=true)
    String testClassName;

    /**
     * Test method
     */
    @Parameter(property = "method", required=true)
    private String testMethod;

    /**
     * classes to be mutated
     */
    @Parameter(property = "includes")
    String includes;

    @Parameter(property="targetIncludes")
    private String targetIncludes;

    /**
     * Allows user to set optimization level for mutation-guided fuzzing.
     */
    @Parameter(property="optLevel", defaultValue = "none")
    private String optLevel;

    List<Object> reproResults;
    int ind = 0;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();

        OptLevel ol;
        try {
            ol = OptLevel.valueOf(optLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Invalid Mutation OptLevel!");
        }

        if(targetIncludes == null) {
            targetIncludes = "";
        }

        try {
            // Get project-specific classpath and output directory
            List<String> classpathElements = project.getTestClasspathElements();
            String[] classPath = classpathElements.toArray(new String[0]);
            IOUtils.createDirectory(resultsDir);

            // Create mu2 classloaders from the test classpath
            MutationClassLoaders mcls = new MutationClassLoaders(classPath, includes, targetIncludes, ol);
            CartographyClassLoader ccl = mcls.getCartographyClassLoader();

            // Run initial test to compute mutants dynamically
            System.out.println("Starting Initial Run:");
            Result initialResults = runMutRepro(ccl, null, false, null);
            List<edu.berkeley.cs.jqf.fuzz.guidance.Result> cclResults = new ArrayList<>(DiffMutationReproGuidance.recentResults);
            List<Object> cclOutputs = reproResults;
            System.out.println("cclResults: " + cclResults);
            if (!initialResults.wasSuccessful()) {
                throw new MojoFailureException("Initial test run fails",
                        initialResults.getFailures().get(0).getException());
            }

            // Retrieve dynamically collected mutation instances
            List<MutationInstance> mutationInstances = ccl.getMutationInstances();

            // Track which mutants get killed
            List<MutationInstance> killedMutants = new ArrayList<>();

            // Run a repro on all mutants
            for (MutationInstance mutationInstance : mutationInstances) {
                log.info("Running Mutant " + mutationInstance.toString());
                MutationClassLoader mcl = mcls.getMutationClassLoader(mutationInstance);
                Result res = runMutRepro(mcl, cclOutputs, true,  cclResults);
                if (!res.wasSuccessful()) {
                    killedMutants.add(mutationInstance);
                }
            }

            File mutantReport = new File(resultsDir, "mutant-report.csv");
            try (PrintWriter pw = new PrintWriter(mutantReport)) {
                for (MutationInstance mi : mutationInstances) {
                    pw.printf("%s,%s\n",
                            mi.toString(),
                            killedMutants.contains(mi) ? "Killed" : "Alive");
                }
            }

            String ls = String.format("Mutants Run: %d, Killed Mutants: %d",
                    mutationInstances.size(),
                    killedMutants.size());
            System.out.println(ls);
        } catch (AbstractMojoExecutionException e) {
            throw e; // Propagate as is
        } catch (Exception e) {
            throw new MojoExecutionException(e.toString(), e);
        }
    }

    // Executes a fresh repro with a given classloader
    private Result runRepro(ClassLoader classLoader, List<Object> cclReturn, boolean useCR) throws ClassNotFoundException, IOException {
        DiffReproGuidance repro;
        if(useCR) {
            repro = new DiffReproGuidance(input, null, cclReturn);
        } else {
            repro = new DiffReproGuidance(input, null);
        }
        repro.setStopOnFailure(true);
        Result toReturn = GuidedFuzzing.run(testClassName, testMethod, classLoader, repro, null);
        reproResults = repro.getResults();
        return toReturn;
    }

    private Result runMutRepro(ClassLoader classLoader, List<Object> cclReturn, boolean useCR, List<edu.berkeley.cs.jqf.fuzz.guidance.Result> cclResults) throws ClassNotFoundException, IOException {
        DiffMutationReproGuidance repro;
        if(useCR) {
            repro = new DiffMutationReproGuidance(input, null, cclReturn, cclResults);
        } else {
            repro = new DiffMutationReproGuidance(input, null, cclResults);
        }
        repro.setStopOnFailure(true);
        Result toReturn = DiffedFuzzing.run(testClassName, testMethod, classLoader, repro, null);
        reproResults = repro.getResults();
        return toReturn;
    }
}
