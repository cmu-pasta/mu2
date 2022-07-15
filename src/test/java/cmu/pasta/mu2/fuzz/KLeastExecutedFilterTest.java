package cmu.pasta.mu2.fuzz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.mutators.Mutator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class KLeastExecutedFilterTest {

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

    @Test
    public void leastExecutedLogicWorks(){

        //TODO
        Set<MutationInstance> newMutants = new HashSet<>(Arrays.asList(
            //add to sub
            new MutationInstance("New1", Mutator.allMutators.get(0), 6, 1, "New1.java"),
            //sub to add
            new MutationInstance("New2", Mutator.allMutators.get(1), 7, 1, "New2.java"),
            //div to mul
            new MutationInstance("New3", Mutator.allMutators.get(3), 8, 1, "New3.java")
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

    /**
     * 
     */
    @Test
    public void filteringDoesNotAddNewMutants(){

        
        Set<MutationInstance> newMutants = new HashSet<>(Arrays.asList(
            //add to sub
            new MutationInstance("New1", Mutator.allMutators.get(0), 6, 1, "New1.java"),
            //sub to add
            new MutationInstance("New2", Mutator.allMutators.get(1), 7, 1, "New2.java"),
            //div to mul
            new MutationInstance("New3", Mutator.allMutators.get(3), 8, 1, "New3.java")
        ));

        KLeastExecutedFilter filter = new KLeastExecutedFilter(5);

        // Filter original mutants twice so that all are executed twice
        List<MutationInstance> originalMutants = filter.filterMutants(filter.filterMutants(toFilter));
        // Check that execution counts are correctly incremented
        for (MutationInstance mutant : originalMutants){
            assertEquals(2, filter.executionCounts.get(mutant).intValue());
        }
        // Filter newMutants so all are executed once (new 1, old 2, if filter old, should not contain any new)
        List<MutationInstance> newMutantsList = new ArrayList<>(newMutants);
        newMutantsList = filter.filterMutants(newMutantsList);
        // Check that execution counts are correctly incremented
        for (MutationInstance mutant : newMutantsList) {
            assertEquals(1, filter.executionCounts.get(mutant).intValue());
        }

        // Filter original mutants with k = 5
        originalMutants = filter.filterMutants(originalMutants);
        // Check that all original mutants and no new mutants are in filtered list
        for (MutationInstance mutant : originalMutants){
            assertEquals(3, filter.executionCounts.get(mutant).intValue());
        }
        for (MutationInstance mutant : newMutantsList){
            assertEquals(1, filter.executionCounts.get(mutant).intValue());
        }

        // Filter original mutants
        originalMutants.remove(4);
        originalMutants.remove(3);
        List<MutationInstance> filteredOriginalMutants = filter.filterMutants(originalMutants);
        // Check that the filtered list is of correct size
        assertEquals(3, filteredOriginalMutants.size());
        // Check that all original mutants and no new mutants are in filtered list
        for (MutationInstance mutant : filteredOriginalMutants){
            assertEquals(4, filter.executionCounts.get(mutant).intValue());
        }
        for (MutationInstance mutant : newMutantsList){
            assertEquals(1, filter.executionCounts.get(mutant).intValue());
        }
    }
}

