package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.diff.Outcome;
import cmu.pasta.mu2.diff.guidance.DiffMutationReproGuidance;
import cmu.pasta.mu2.diff.guidance.DiffReproGuidance;
import cmu.pasta.mu2.instrument.AbstractMutationTest;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.instrument.OptLevel;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiffFuzzIT extends AbstractMutationTest {
    // Temp directory to store fuzz results
    protected static File resultsDir;

    @Before
    public void initTempDir() throws IOException {
        resultsDir = Files.createTempDirectory("fuzz-results").toFile();
    }

    @After
    public void clearTempDir() {
        resultsDir.delete();
    }

    protected static class ProbedMutationGuidance extends MutationGuidance {
        public ProbedMutationGuidance(MutationClassLoaders mcls, long trials, Random rnd, File resultsDir) throws IOException {
            super(null, mcls,null, trials, resultsDir, null, rnd);
        }

        public int killedMutants() {
            return deadMutants.size();
        }

        int corpusCount() {
            return savedInputs.size();
        }
    }

    protected static class ProbedDMRGuidance extends DiffMutationReproGuidance {
        public ProbedDMRGuidance(File inputFile, File traceDir, MutationClassLoaders mcls) throws IOException {
            super(inputFile, traceDir, mcls);
        }

        public int killedMutants() {
            return deadMutants.size();
        }
    }

    protected int fuzzMutate(String testClassName, String testMethod, String targetInst, int expectCorpus, String targetClasses) throws Exception {
        // Set up test params
        long trials = 100;
        Random rnd = new Random(42);

        // Create guidance
        MutationClassLoaders mcls = initClassLoaders(targetInst, targetClasses, OptLevel.EXECUTION);
        ProbedMutationGuidance mu2 = new ProbedMutationGuidance(mcls, trials, rnd, resultsDir);

        // Fuzz
        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), mu2, System.out);

        Assert.assertEquals(expectCorpus, mu2.corpusCount());

        return mu2.killedMutants();
    }

    public void fuzzRepro(String testClassName, String testMethod, String targetInst, String targetClasses, String corpusSrc, Object expectedOut) throws Exception {
        MutationClassLoaders mcls = initClassLoaders(targetInst, targetClasses, OptLevel.EXECUTION);
        DiffReproGuidance drg = new DiffReproGuidance(new File(corpusSrc), null);

        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), drg, null);

        Assert.assertEquals(DiffReproGuidance.recentOutcomes.toString(), expectedOut.toString());
    }

    public void fuzzMutateRepro(String testClassName, String testMethod, int expectCorpus, String targetClasses, String targetInst, String corpusSrc) throws Exception { //same # mutants
        int fuzzPerf = fuzzMutate(testClassName, testMethod, targetInst, expectCorpus, targetClasses);

        MutationClassLoaders mcls = initClassLoaders(targetInst, targetClasses, OptLevel.EXECUTION);
        ProbedDMRGuidance pdmrg = new ProbedDMRGuidance(new File(corpusSrc), null, mcls);

        GuidedFuzzing.run(testClassName, testMethod, mcls.getCartographyClassLoader(), pdmrg, null);

        Assert.assertEquals(fuzzPerf, pdmrg.killedMutants());
    }

    //TimSort: basic fuzz

    @Test
    public void fuzzMutateTimSort() throws Exception {
        //mvn mu2:diff -Dclass=sort.TimSortTest -Dmethod=fuzzTimSort -Dincludes=sort.TimSort -DrandomSeed=42 -Dtrials=100
        String testClassName = "sort.TimSortTest";
        String testMethod = "fuzzTimSort";
        String targetInst = "sort.TimSort";

        fuzzMutate(testClassName, testMethod, targetInst, 22,"sort");
    }

    @Test
    public void fuzzReproTimSort() throws Exception {
        String testClassName = "sort.TimSortTest";
        String testMethod = "fuzzTimSort";
        String targetInst = "sort.TimSort";
        String targetClasses = "sort";
        String corpusSrc = "./test-comparisons/TimSort";

        List<Outcome> expectedOut = new ArrayList<>();
        for(int c = 0; c < 22; c++) expectedOut.add(new Outcome(null, null));

        fuzzRepro(testClassName, testMethod, targetInst, targetClasses, corpusSrc, expectedOut);
    }

    @Test
    public void fuzzMutateReproTimSort() throws Exception {
        String testClassName = "sort.TimSortTest";
        String testMethod = "fuzzTimSort";
        String targetInst = "sort.TimSort";
        String targetClasses = "sort";
        String corpusSrc = "./test-comparisons/TimSort";

        fuzzMutateRepro(testClassName, testMethod, 22, targetClasses, targetInst, corpusSrc);
    }

    //PatriciaTrie: basic diff

    @Test
    public void diffMutatePatriciaTrie() throws Exception {
        String testClassName = "diff.PatriciaTrieTest";
        String testMethod = "testPrefixMap";
        String targetInst = "org.apache.commons.collections4.trie";
        String targetClasses = "org.apache.commons.collections4.trie,diff";

        fuzzMutate(testClassName, testMethod, targetInst, 20, targetClasses);
    }

    @Test
    public void diffReproPatriciaTrie() throws Exception {
        String testClassName = "diff.PatriciaTrieTest";
        String testMethod = "testPrefixMap";
        String targetInst = "org.apache.commons.collections4.trie";
        String targetClasses = "org.apache.commons.collections4.trie,diff";
        String corpusSrc = "./test-comparisons/PatriciaTrie";

        File resultFile = new File("./test-comparisons/PatriciaTrie-repro.txt");
        String expectedOut;
        try(BufferedReader br = new BufferedReader(new FileReader(resultFile))) {
            expectedOut = br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        fuzzRepro(testClassName, testMethod, targetInst, targetClasses, corpusSrc, expectedOut);
    }

    @Test
    public void diffMutateReproPatriciaTrie() throws Exception {
        String testClassName = "diff.PatriciaTrieTest";
        String testMethod = "testPrefixMap";
        String targetInst = "org.apache.commons.collections4.trie";
        String targetClasses = "org.apache.commons.collections4.trie,diff";
        String corpusSrc = "./test-comparisons/PatriciaTrie";

        fuzzMutateRepro(testClassName, testMethod, 20, targetClasses, targetInst, corpusSrc);
    }

    /*@Test
    public void fuzzMutateCommonsCLI() throws Exception {

    }

    @Test
    public void fuzzReproCommonsCLI() throws Exception {

    }

    @Test
    public void fuzzMutateReproCommonsCLI() throws Exception {

    }

    @Test
    public void fuzzMutateJacksonDatabind() throws Exception {

    }

    @Test
    public void fuzzReproJacksonDatabind() throws Exception {

    }

    @Test
    public void fuzzMutateReproJacksonDatabind() throws Exception {

    }*/
}
