package cmu.pasta.mu2.fuzz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

import cmu.pasta.mu2.MutationInstance;

/**
 * MutantFilter that filters a list of MutationInstances
 * by choosing a random subset of size k.
 */
public class KRandomFilter implements MutantFilter {
    private int k;
    private boolean percent;

    /**
    * Constructor for KRandomFilter (sets percent to False by default)
    * @param k the number of MutationInstances the filtered list should contain
    */
    public KRandomFilter (int k){
        this.k = k;
        this.percent = false;
    }

    /**
    * Another constructor for KRandomFilter 
    * @param k the number of MutationInstances the filtered list should contain
    * @param percent True if k is percentage, False if k is number
    */
    public KRandomFilter (int k, boolean percent) {
        this.k = k;
        this.percent = percent;
    }

    /**
    * Filter method that takes in a list of MutationInstances to be filtered
    * and returns a filter list of k random MutationInstances.
    * @param toFilter the list of MutationInstances to be filtered
    * @return         the filtered list of k random MutationInstances
    */
    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {

        // determine number of mutants to run
        int n = this.percent ? (toFilter.size() * k / 100) : k;

        // shuffle list of mutants to randomize first n elements
        Collections.shuffle(toFilter);

        // add first k mutants in list to filtered list
        List<MutationInstance> filtered = new ArrayList<>();
        for(int i = 0; i < n && i < toFilter.size(); i++){
            filtered.add(toFilter.get(i));
        }

        return filtered;
    }

    
    
}
