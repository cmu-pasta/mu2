package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.instrument.MutationInstance;
import cmu.pasta.mu2.mutators.Mutator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileMutantFilterTest {

    static final String fileName = "testFileForFilter.txt";
    static File testFile;
    static FileWriter writer;

    List<MutationInstance> muts;
    @Before
    public void createFile() throws IOException {
        Mutator.initializeMutators();
         muts = new ArrayList<>(Arrays.asList(
                  //add to sub
                  new MutationInstance("ExampleClass", Mutator.allMutators.get(0), 1, 1, "ExampleClass.java"),
                  //mul to div
                  new MutationInstance("ExampleClass", Mutator.allMutators.get(2), 2, 2, "ExampleClass.java"),
                  //div to mul
                  new MutationInstance("ExampleClass2",Mutator.allMutators.get(3), 3, 2, "ExampleClass2.java"),
                  //sub to add
                  new MutationInstance("ExampleClass2",Mutator.allMutators.get(1), 4, 42, "ExampleClass2.java")
        ));
        testFile = File.createTempFile("test",null);
        writer = new FileWriter(testFile.getAbsolutePath());
    }

    @After
    public void deleteFile(){
        testFile.delete();
    }


    @Test
    public void testFileNum() throws IOException {
        writer.write("ExampleClass2.java:42\nExampleClass.java:2\nClassThatDoesNotExist.java:1444");
        writer.close();
        FileMutantFilter filter =  new FileMutantFilter(testFile.getAbsolutePath());
        List<MutationInstance> filteredMutants =  filter.filterMutants(muts);
        Assert.assertTrue(filteredMutants.contains(muts.get(3)));
        Assert.assertTrue(filteredMutants.contains(muts.get(1)));
    }
    @Test
    public void testUniqId() throws IOException {
        writer.write("ExampleClass2:I_SUB_TO_ADD:4\nExampleClass:I_MUL_TO_DIV:2\nClassThatDoesNotExist.java:1444");
        writer.close();
        FileMutantFilter filter =  new FileMutantFilter(testFile.getAbsolutePath());
        List<MutationInstance> filteredMutants =  filter.filterMutants(muts);
        System.out.println(filteredMutants);
        Assert.assertTrue(filteredMutants.contains(muts.get(1)));
        Assert.assertTrue(filteredMutants.contains(muts.get(3)));
    }
}
