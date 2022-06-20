package cmu.pasta.mu2.fuzz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.instrument.Mutator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class KLeastExecutedFilterTest {

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
        KLeastExecutedFilter filter = new KLeastExecutedFilter(k);
        // call filter method to obtain filtered list
        List<MutationInstance> filteredList = filter.filterMutants(toFilter);
        // filtered list should be of size k
        assertEquals(k, filteredList.size());
    }

    @Test
    public void filteredListSizeIsCorrect2(){
        int k = 5;
        // instantiate random filter with parameter k
        KLeastExecutedFilter filter = new KLeastExecutedFilter(k);
        // call filter method to obtain filtered list
        List<MutationInstance> filteredList = filter.filterMutants(toFilter);
        // filtered list should be of size k
        assertEquals(k, filteredList.size());
    }

    @Test
    public void filteredListSizeIsCorrect3(){
        int k = 7;
        // instantiate random filter with parameter k
        KLeastExecutedFilter filter = new KLeastExecutedFilter(k);
        // call filter method to obtain filtered list
        List<MutationInstance> filteredList = filter.filterMutants(toFilter);
        // filtered list should be of same size as toFilter
        assertEquals(toFilter.size(), filteredList.size());
    }

    /*
     * 
     */
    @Test
    public void leastExecutedLogicWorks(){

        Set<MutationInstance> newMutants = new HashSet<>(Arrays.asList(
            new MutationInstance("New1", Mutator.I_ADD_TO_SUB, 6, 1, "New1.java"),
            new MutationInstance("New2", Mutator.ARETURN_TO_NULL, 7, 1, "New2.java"),
            new MutationInstance("New3", Mutator.I_DIV_TO_MUL, 8, 1, "New3.java")
        ));

        KLeastExecutedFilter filter = new KLeastExecutedFilter(5);

        // Filter original mutants so that all are executed once
        List<MutationInstance> originalMutants = filter.filterMutants(toFilter);
        // All mutants should be contained in filtered list
        assertEquals(5, originalMutants.size());
        // Add new mutants to combined list
        List<MutationInstance> allMutants = new ArrayList<>(toFilter);
        allMutants.addAll(newMutants);

        // Filter list again
        List<MutationInstance> filtered1 = filter.filterMutants(allMutants);
        // New (never-executed) mutants should all be in filtered list to be executed
        assertTrue(filtered1.containsAll(newMutants));
        // Count number of original mutants in filtered list, add to set of mutants that have been executed twice
        int count = 0;
        Set<MutationInstance> executedTwice = new HashSet<>();
        for (MutationInstance mutant: filtered1){
            if (originalMutants.contains(mutant)) {
                count += 1;
                executedTwice.add(mutant);
            }
        }
        // Filtered list should contain 5-3=2 original mutants
        assertEquals(2, count);

        // Filter list again
        List<MutationInstance> filtered2 = filter.filterMutants(allMutants);
        // Filtered list should not contain any of the 2 mutants that have been executed twice
        for (MutationInstance mutant : executedTwice) {
            assertFalse(filtered2.contains(mutant));
        }
        // Add all recently executed mutants to executedTwice
        for (MutationInstance mutant : filtered2) {
            executedTwice.add(mutant);
        }
        // All mutants except 1 should now have been executed twice
        assertEquals(7,executedTwice.size());

        // Filter list again
        List<MutationInstance> filtered3 = filter.filterMutants(allMutants);
        // The 1 remaining once-executed should be contained (the rest have all been executed twice)
        boolean found = false;
        for (MutationInstance mutant : filtered3) {
            if (!executedTwice.contains(mutant)){
                found = true;
                break;
            }
        }
        assertTrue(found);

    }
}

