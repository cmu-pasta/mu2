package cmu.pasta.mu2.diff.plugin;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.diff.guidance.DiffMutationReproGuidance;
import cmu.pasta.mu2.instrument.CartographyClassLoader;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Repro goal for mu2. Performs mutation testing to calculate
 * mutation score for a saved test input corpus.
 *
 * @author Bella Laybourn
 */
@Mojo(name="mutate",
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
    @Parameter(property="optLevel", defaultValue = "execution")
    private String optLevel;

    @Parameter(property="serializing", defaultValue = "true")
    private boolean serializing;

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
            DiffMutationReproGuidance dmrg = new DiffMutationReproGuidance(input, null, mcls, serializing);
            dmrg.setStopOnFailure(true);
            Result result = GuidedFuzzing.run(testClassName, testMethod, ccl, dmrg, null);
            if (!result.wasSuccessful()) {
                throw new MojoFailureException("Test run failed",
                        result.getFailures().get(0).getException());
            }
            System.out.println("cclOutcomes: " + dmrg.cclOutcomes);
            List<MutationInstance> mutationInstances = mcls.getMutationInstances();
            List<MutationInstance> killedMutants = new ArrayList<>();
            for(MutationInstance mi : mcls.getMutationInstances()) {
                if(dmrg.mclOutcomes.get(mi) == null) {
                    continue;
                }
                System.out.println("Running Mutant " + mi);
                if(dmrg.mclOutcomes.get(mi).second >= 0) {
                    killedMutants.add(mi);
                    for(int c = 1; c < dmrg.mclOutcomes.get(mi).second; c++) {
                        System.out.println("Input " + c + " ::= " + dmrg.mclOutcomes.get(mi).first.get(c));
                    }
                    System.out.println("Input " + dmrg.mclOutcomes.get(mi).second + " ::= FAILURE");
                } else {
                    for(int c = 0; c < dmrg.mclOutcomes.get(mi).first.size(); c++) {
                        System.out.println("Input " + c + " ::= " + dmrg.mclOutcomes.get(mi).first.get(c));
                    }
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
}
