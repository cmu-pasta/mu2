package cmu.pasta.mu2.fuzz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.mutators.Mutator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class KRandomFilterTest{

    List<MutationInstance> toFilter;

    @Before
    public void initMutators()  {
        Mutator.initializeMutators();
        toFilter = new ArrayList<>(Arrays.asList(
                //add to sub
                new MutationInstance("ExampleClass", Mutator.allMutators.get(0), 1, 1, "ExampleClass.java"),
                //mul to div
                new MutationInstance("ExampleClass", Mutator.allMutators.get(2), 2, 2, "ExampleClass.java"),
                //div to mul
                new MutationInstance("ExampleClass2", Mutator.allMutators.get(3), 3, 2, "ExampleClass2.java"),
                //sub to add
                new MutationInstance("ExampleClass2", Mutator.allMutators.get(1), 4, 42, "ExampleClass2.java"),
                //sub to add
                new MutationInstance("ExampleClass3", Mutator.allMutators.get(1), 5, 42, "ExampleClass3.java")
        ));
    }

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
