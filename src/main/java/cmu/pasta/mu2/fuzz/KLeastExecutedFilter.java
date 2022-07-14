package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * MutantFilter that filters a list of MutationInstances
 * by choosing the k (or k%) least-executed mutants.
 */
public class KLeastExecutedFilter implements MutantFilter {
    /**
     * The number of MutationInstances the filtered list should contain,
     * as set in the constructor.
     */
    private int k;
    private boolean percent;
    private MutationGuidance guidance;
    /**
     * A map of MutationInstances to the number of times they have been executed.
     */
    HashMap<MutationInstance, Integer> executionCounts;
    
    /**
    * Constructor for KLeastExecutedFilter (sets percent to False by default)
    * @param k the number of MutationInstances the filtered list should contain
    */
    public KLeastExecutedFilter(int k) {
        this.k = k;
        this.executionCounts = new HashMap<MutationInstance, Integer>();
        this.percent = false;
        this.guidance = null;
    }

    /**
    * Another constructor for KLeastExecutedFilter
    * @param k the number/percentage of MutationInstances the filtered list should contain
    * @param percent True if k is percentage, False if k is number
    */
    public KLeastExecutedFilter(int k, boolean percent, MutationGuidance guidance) {

        this.k = k;
        this.executionCounts = new HashMap<MutationInstance, Integer>();
        this.percent = percent;
        this.guidance = guidance;
    }
    
    /**
    * Filter method that takes in a list of MutationInstances to be filtered
    * and returns a filter list of the k least-executed MutationInstances.
    * @param toFilter the list of MutationInstances to be filtered
    * @return         the filtered list of k least-executed MutationInstances
    */
    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {
 
        // determine number of mutants to run
        int n = this.percent ? (guidance.getSeenMutants() * k / 100) : k;
        
        // initialize filtered list to be returned
        ArrayList<MutationInstance> filteredList = new ArrayList<MutationInstance>();
        ArrayList<MutationInstance> executedMutants = new ArrayList<MutationInstance>();

        // add (up to n) mutants in toFilter that have not been executed before to filteredList
        int numMutants = 0;
        for (MutationInstance mutant : toFilter){
            if (numMutants < n && !executionCounts.containsKey(mutant)){
                filteredList.add(mutant);
                numMutants++;
            }
            // add all mutants that have already been executed before to a list
            else if (executionCounts.containsKey(mutant)){
                executedMutants.add(mutant);
            }  
        }

        // if numMutants < n mutants have never been executed, add the next (n - numMutants) least executed mutants to filtered list
        if (numMutants < n){

            // sort list of already executed MutationInstances by execution count
            Collections.sort(executedMutants, (e1, e2) -> executionCounts.get(e1).compareTo(executionCounts.get(e2)));

            // add least executed to filteredList until |filteredList| = n
            int size = executedMutants.size();
            for(int i = 0; i < size && numMutants < n; i++){
                MutationInstance mutant = executedMutants.get(i);
                filteredList.add(mutant);
                numMutants++;
            }
        }

        // increment execution count for each mutant in filteredList
        for (MutationInstance mutant: filteredList) {
            if (!executionCounts.containsKey(mutant)){
                executionCounts.put(mutant, 1); 
            } else {
                executionCounts.put(mutant, executionCounts.get(mutant)+1);
            }
        }

        return filteredList;
    }
    
}
