package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.instrument.Mutator;
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

    static final List<MutationInstance> muts = new ArrayList<>(Arrays.asList(
            new MutationInstance("ExampleClass", Mutator.I_ADD_TO_SUB, 1, 1, "ExampleClass.java"),
            new MutationInstance("ExampleClass", Mutator.ARETURN_TO_NULL, 2, 2, "ExampleClass.java"),
            new MutationInstance("ExampleClass2", Mutator.I_DIV_TO_MUL, 3, 2, "ExampleClass2.java"),
            new MutationInstance("ExampleClass2", Mutator.D_SUB_TO_ADD, 4, 42, "ExampleClass2.java")
    ));

    @Before
    public void createFile() throws IOException {
        testFile = new File(fileName);
        if(!testFile.createNewFile()){
            throw new IOException("Could not create new file for file filter test");
        }
         writer = new FileWriter(fileName);
    }

    @After
    public void deleteFile(){
        testFile.delete();
    }


    @Test
    public void testFileNum() throws IOException {
        writer.write("ExampleClass2.java:42\nExampleClass.java:2\nClassThatDoesNotExist.java:1444");
        writer.close();
        FileMutantFilter filter =  new FileMutantFilter(fileName);
        List<MutationInstance> filteredMutants =  filter.filterMutants(muts);
        Assert.assertTrue(filteredMutants.contains(muts.get(3)));
        Assert.assertTrue(filteredMutants.contains(muts.get(1)));
    }
    @Test
    public void testUniqId() throws IOException {
        writer.write("ExampleClass2:D_SUB_TO_ADD:4\nExampleClass:ARETURN_TO_NULL:2\nClassThatDoesNotExist.java:1444");
        writer.close();
        FileMutantFilter filter =  new FileMutantFilter(fileName);
        List<MutationInstance> filteredMutants =  filter.filterMutants(muts);
        Assert.assertTrue(filteredMutants.contains(muts.get(1)));
        Assert.assertTrue(filteredMutants.contains(muts.get(3)));
    }
}
