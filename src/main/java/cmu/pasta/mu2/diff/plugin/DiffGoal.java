package cmu.pasta.mu2.diff.plugin;

import cmu.pasta.mu2.diff.junit.DiffedFuzzing;
import cmu.pasta.mu2.fuzz.MutationGuidance;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.instrument.OptLevel;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.junit.runner.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import static edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader.stringsToUrls;

@Mojo(name="diff",
        requiresDependencyResolution= ResolutionScope.TEST,
        defaultPhase= LifecyclePhase.VERIFY)
public class DiffGoal extends AbstractMojo {
    @Parameter(defaultValue="${project}", required=true, readonly=true)
    MavenProject project;

    @Parameter(property="target", defaultValue="${project.build.directory}", readonly=true)
    private File target;

    /**
     * The fully-qualified name of the test class containing methods
     * to fuzz.
     *
     * <p>This class will be loaded using the Maven project's test
     * classpath. It must be annotated with {@code @RunWith(JQF.class)}</p>
     */
    @Parameter(property="class", required=true)
    private String testClassName;

    /**
     * The name of the method to fuzz.
     *
     * <p>This method must be annotated with {@code @Fuzz}, and take
     * one or more arguments (with optional junit-quickcheck
     * annotations) whose values will be fuzzed by JQF.</p>
     *
     * <p>If more than one method of this name exists in the
     * test class or if the method is not declared
     * {@code public void}, then the fuzzer will not launch.</p>
     */
    @Parameter(property="method", required=true)
    private String testMethod;

    /**
     * Comma-separated list of FQN prefixes to forcibly include,
     * even if they match an exclude.
     *
     * <p>Typically, these will be a longer prefix than a prefix
     * in the excludes clauses.</p>
     */
    @Parameter(property="includes")
    private String includes;


    /**
     * The number of trials for which to run fuzzing.
     *
     * <p>
     * If neither this property nor {@code time} are provided, the fuzzing
     * session is run for an unlimited time until the process is terminated by the
     * user (e.g. via kill or CTRL+C).
     * </p>
     */
    @Parameter(property="trials")
    private Long trials;

    /**
     * A number to seed the source of randomness in the fuzzing algorithm.
     *
     * <p>
     * Setting this to any value will make the result of running the same fuzzer
     * with on the same input the same. This is useful for testing the fuzzer, but
     * shouldn't be used on code attempting to find real bugs. By default, the
     * seed is chosen randomly based on system state.
     * </p>
     */
    @Parameter(property="randomSeed")
    private Long randomSeed;

    /**
     * The name of the output directory where fuzzing results will
     * be stored.
     *
     * <p>The directory will be created inside the standard Maven
     * project build directory.</p>
     *
     * <p>If not provided, defaults to
     * <em>jqf-fuzz/${testClassName}/${$testMethod}</em>.</p>
     */
    @Parameter(property="out")
    private String outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ClassLoader loader;
        MutationGuidance guidance;
        Log log = getLog();
        PrintStream out = log.isDebugEnabled() ? System.out : null;
        Result result;

        if (outputDirectory == null || outputDirectory.isEmpty()) {
            outputDirectory = "fuzz-results" + File.separator + testClassName + File.separator + testMethod;
        }

        try {
            List<String> classpathElements = project.getTestClasspathElements();
            URL[] classPath = stringsToUrls(classpathElements.toArray(new String[0]));
            ClassLoader baseClassLoader = getClass().getClassLoader();
            String targetName = testClassName + "#" + testMethod;
            Random rnd = randomSeed != null ? new Random(randomSeed) : new Random();
            File resultsDir = new File(target, outputDirectory);

            //assume MutationGuidance for the moment
            if (includes == null) {
                throw new MojoExecutionException("Mutation-based fuzzing requires `-Dincludes`");
            }
            MutationClassLoaders mcl = new MutationClassLoaders(classPath, includes, OptLevel.EXECUTION,
                    baseClassLoader); // TODO: Should opt level be configurable?
            loader = mcl.getCartographyClassLoader();
            guidance = new MutationGuidance(targetName, mcl, null, trials, resultsDir, null, rnd);
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            throw new MojoExecutionException("Could not get project classpath", e);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("File not found", e);
        } catch (IOException e) {
            throw new MojoExecutionException("I/O error", e);
        }

        try {
            result = DiffedFuzzing.run(testClassName, testMethod, loader, guidance, out);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Could not load test class", e);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Bad request", e);
        } catch (RuntimeException e) {
            throw new MojoExecutionException("Internal error", e);
        }

        if (!result.wasSuccessful()) {
            Throwable e = result.getFailures().get(0).getException();
            e.printStackTrace();
            if (result.getFailureCount() == 1) {
                if (e instanceof GuidanceException) {
                    throw new MojoExecutionException("Internal error", e);
                }
            }
            throw new MojoFailureException(String.format("Fuzzing resulted in the test failing on " +
                            "%d input(s). Possible bugs found. " +
                            "Use mvn jqf:repro to reproduce failing test cases. ",
                    result.getFailureCount()) +
                    "Sample exception included with this message.", e);
        }
    }
}
