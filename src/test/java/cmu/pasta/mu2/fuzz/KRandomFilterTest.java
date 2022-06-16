package cmu.pasta.mu2.fuzz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.instrument.Mutator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class KRandomFilterTest {

   static final List<MutationInstance> toFilter = new ArrayList<>(Arrays.asList(
            new MutationInstance("ExampleClass", Mutator.I_ADD_TO_SUB, 1, 1, "ExampleClass.java"),
            new MutationInstance("ExampleClass", Mutator.ARETURN_TO_NULL, 2, 2, "ExampleClass.java"),
            new MutationInstance("ExampleClass2", Mutator.I_DIV_TO_MUL, 3, 2, "ExampleClass2.java"),
            new MutationInstance("ExampleClass2", Mutator.D_SUB_TO_ADD, 4, 42, "ExampleClass2.java"),
            new MutationInstance("ExampleClass3", Mutator.D_SUB_TO_ADD, 5, 42, "ExampleClass3.java")
    ));
    
    @Test
    public void filteredListSizeIsCorrect1(){
        int k = 3;
        // instantiate random filter with parameter k
        KRandomFilter filter = new KRandomFilter(k);
        // call filter method to obtain filtered list
        List<MutationInstance> filteredList = filter.filterMutants(toFilter);
        // filtered list should be of size k
        assertEquals(k, filteredList.size());
    }

    @Test
    public void filteredListSizeIsCorrect2(){
        int k = 5;
        // instantiate random filter with parameter k
        KRandomFilter filter = new KRandomFilter(k);
        // call filter method to obtain filtered list
        List<MutationInstance> filteredList = filter.filterMutants(toFilter);
        // filtered list should be of size k
        assertEquals(k, filteredList.size());
    }

    @Test
    public void filteredListSizeIsCorrect3(){
        int k = 7;
        // instantiate random filter with parameter k
        KRandomFilter filter = new KRandomFilter(k);
        // call filter method to obtain filtered list
        List<MutationInstance> filteredList = filter.filterMutants(toFilter);
        // filtered list should be of same size as toFilter
        assertEquals(toFilter.size(), filteredList.size());
    }

}
