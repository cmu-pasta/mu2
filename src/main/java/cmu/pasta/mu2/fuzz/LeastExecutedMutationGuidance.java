package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.util.ArraySet;
import cmu.pasta.mu2.diff.guidance.OptimizedMutationGuidance;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;


public class LeastExecutedMutationGuidance extends MutationGuidance implements OptimizedMutationGuidance {

    private int k;
    private Hashtable<Integer, Integer> execution_count;

    public LeastExecutedMutationGuidance(String testName, MutationClassLoaders mutationClassLoaders, Duration duration,
            Long trials, File outputDirectory, File seedInputDir, Random rand, int k) throws IOException {
        super(testName, mutationClassLoaders, duration, trials, outputDirectory, seedInputDir, rand);
        this.k = k;
        this.execution_count = new Hashtable<Integer, Integer>();
    }

    @Override
    public ArraySet filterMutants() {
        ArraySet filtered = new ArraySet();
        ArrayList<Integer> filteredList = new ArrayList<Integer>();
        
        // iterate over all mutants in runMutants, add first k that have not been executed to filteredList
        int num = 0;
        int numMutants = 0;
        int size = runMutants.size();
        int n = 0;
        while(numMutants < size){
            if (runMutants.contains(n)){
                numMutants++;
                if (num < k && !execution_count.containsKey(n)){
                    filteredList.add(n);
                    num++;
                }
            }
            n++;
        }

        // if num < k mutants have never been executed, add the next k - num least executed to filtered list
        if (num < k){

            //get list of mutant ids sorted by execution count
            List<Map.Entry<Integer, Integer>> sorted_mutants = new ArrayList<Map.Entry<Integer, Integer>>(execution_count.entrySet());
            Collections.sort(sorted_mutants, new Comparator<Map.Entry<Integer, Integer>>(){
                public int compare(Map.Entry<Integer, Integer> entry1, Map.Entry<Integer, Integer> entry2) {
                    return entry1.getValue().compareTo( entry2.getValue() );
                }
            });

            //add least executed to sorted_mutants until |filteredList| = k
            for(int i = 0; i < sorted_mutants.size() && num < k; i++){
                filteredList.add(sorted_mutants.get(i).getKey());
                num++;
            }
        }

        //loop through filtered list, add to filtered ArraySet and increment execution count for each mutant to be executed
        for (int mutant : filteredList){
            filtered.add(mutant);
            // increment execution count for each mutant in filtered (insert if not in hashtable, increment count)
            execution_count.putIfAbsent(mutant, 0);
            execution_count.put(mutant, execution_count.get(mutant)+1);

        }

        return filtered;

        // TODO: maybe change to heap: create heap O(n) by count (tuples of count, id), pop minimum k(klogn) ids into new ArraySet
    }
    
}
