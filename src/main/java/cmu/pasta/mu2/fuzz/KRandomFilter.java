package cmu.pasta.mu2.fuzz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cmu.pasta.mu2.MutationInstance;

/**
 * MutantFilter that filters a list of MutationInstances
 * by choosing a random subset of size k.
 */
public class KRandomFilter implements MutantFilter {
    private int k;

    /**
    * Constructor for KRandomFilter
    * @param k the number of MutationInstances the filtered list should contain
    */
    KRandomFilter (int k){
        this.k = k;
    }

    /**
    * Filter method that takes in a list of MutationInstances to be filtered
    * and returns a filter list of k random MutationInstances.
    * @param toFilter the list of MutationInstances to be filtered
    * @return         the filtered list of k random MutationInstances
    */
    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {
        // shuffle list of mutants to randomize first k elements
        Collections.shuffle(toFilter);

        // add first k mutants in list to filtered list
        List<MutationInstance> filtered = new ArrayList<>();
        for(int i = 0; i < k && i < toFilter.size(); i++){
            filtered.add(toFilter.get(i));
        }

        return filtered;
    }
}
