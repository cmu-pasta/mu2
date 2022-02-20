package cmu.pasta.mu2.diff.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import cmu.pasta.mu2.diff.guidance.DiffReproGuidance;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.repro.ReproGuidance;
import edu.berkeley.cs.jqf.instrument.InstrumentingClassLoader;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.junit.runner.Result;

/** version of repro for diffuzzing*/
@Mojo(name="repro",
        requiresDependencyResolution=ResolutionScope.TEST)
public class ReproDiffGoal extends AbstractMojo {

    @Parameter(defaultValue="${project}", required=true, readonly=true)
    MavenProject project;

    @Parameter(defaultValue="${project.build.directory}", readonly=true)
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
     * Input file or directory to reproduce test case(s).
     *
     * <p>These files will typically be taken from the test corpus
     * ("queue") directory or the failures ("crashes") directory
     * generated by JQF in a previous fuzzing run, for the same
     * test class and method.</p>
     *
     */
    @Parameter(property="input", required=true)
    private String input;

    /**
     * Output file to dump coverage info.
     *
     * <p>This is an optional parameter. If set, the value is the name
     * of a file where JQF will dump code coverage information for
     * the test inputs being replayed.</p>
     */
    @Parameter(property="logCoverage")
    private String logCoverage;

    /**
     * Comma-separated list of FQN prefixes to exclude from
     * coverage instrumentation.
     *
     * <p>This property is only useful if {@link #logCoverage} is
     * set. The semantics are similar to the similarly named
     * property in the goal <code>jqf:fuzz</code>.</p>
     */
    @Parameter(property="excludes")
    private String excludes;

    /**
     * Comma-separated list of FQN prefixes to forcibly include,
     * even if they match an exclude.
     *
     * <p>Typically, these will be a longer prefix than a prefix
     * in the excludes clauses.</p>
     *
     * <p>This property is only useful if {@link #logCoverage} is
     * set. The semantics are similar to the similarly named
     * property in the goal <code>jqf:fuzz</code>.</p>
     */
    @Parameter(property="includes")
    private String includes;

    /**
     * Whether to print the args to each test case.
     *
     * <p>The input file being repro'd is usually a sequence of bytes
     * that is decoded by the junit-quickcheck generators corresponding
     * to the parameters declared in the test method. Unless the test method
     * contains just one arg of type InputStream, the input file itself
     * does not directly correspond to the args sent to the test method.</p>
     *
     * <p>If this flag is set, then the args decoded from a repro'd input
     * file are first printed to standard output before invoking the test
     * method.</p>
     */
    @Parameter(property="printArgs")
    private boolean printArgs;

    /**
     * Whether to dump the args to each test case to file(s).
     *
     * <p>The input file being repro'd is usually a sequence of bytes
     * that is decoded by the junit-quickcheck generators corresponding
     * to the parameters declared in the test method. Unless the test method
     * contains just one arg of type InputStream, the input file itself
     * does not directly correspond to the args sent to the test method.</p>
     *
     * <p>If provided, then the args decoded from a repro'd input
     * file are dumped to corresponding files
     * in this directory before invoking the test method.</p>
     */
    @Parameter(property="dumpArgsDir")
    private String dumpArgsDir;

    @Parameter(property="noSerialization", defaultValue = "false")
    private boolean notSerializing;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ClassLoader loader;
        DiffReproGuidance guidance;
        Log log = getLog();
        PrintStream out = System.out; // TODO: Re-route to logger from super.getLog()
        Result result;

        // Configure classes to instrument
        if (excludes != null) {
            System.setProperty("janala.excludes", excludes);
        }
        if (includes != null) {
            System.setProperty("janala.includes", includes);
        }

        try {
            List<String> classpathElements = project.getTestClasspathElements();

            loader = new InstrumentingClassLoader(
                    classpathElements.toArray(new String[0]),
                    getClass().getClassLoader());
        } catch (DependencyResolutionRequiredException|MalformedURLException e) {
            throw new MojoExecutionException("Could not get project classpath", e);
        }

        // If a coverage dump file was provided, enable logging via system property
        if (logCoverage != null) {
            System.setProperty("jqf.repro.logUniqueBranches", "true");
        }

        // If args should be printed, set system property
        if (printArgs) {
            System.setProperty("jqf.repro.printArgs", "true");
        }

        // If args should be dumped, set system property
        if (dumpArgsDir != null) {
            System.setProperty("jqf.repro.dumpArgsDir", dumpArgsDir);
        }

        File inputFile = new File(input);
        if (!inputFile.exists() || !inputFile.canRead()) {
            throw new MojoExecutionException("Cannot find or open file " + input);
        }

        try {
            guidance = new DiffReproGuidance(inputFile, null, !notSerializing);
            result = GuidedFuzzing.run(testClassName, testMethod, loader, guidance, out);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Could not load test class", e);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Bad request", e);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("File not found", e);
        } catch (IOException e) {
            throw new MojoExecutionException("I/O error", e);
        } catch (RuntimeException e) {
            throw new MojoExecutionException("Internal error", e);
        }

        // If a coverage dump file was provided, then dump coverage
        if (logCoverage != null) {
            Set<String> coverageSet = guidance.getBranchesCovered();
            assert (coverageSet != null); // Should not happen if we set the system property above
            SortedSet<String> sortedCoverage = new TreeSet<>(coverageSet);
            try (PrintWriter covOut = new PrintWriter(new File(logCoverage))) {
                for (String b : sortedCoverage) {
                    covOut.println(b);
                }
            } catch (IOException e) {
                log.error("Could not dump coverage info.", e);
            }
        }

        if (!result.wasSuccessful()) {
            throw new MojoFailureException("Test case produces a failure.");
        }
    }
}
