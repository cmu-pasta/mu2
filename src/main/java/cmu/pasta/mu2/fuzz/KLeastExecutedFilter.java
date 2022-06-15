package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class KLeastExecutedFilter implements MutantFilter {
    private int k;
    private Hashtable<Integer, Integer> execution_count;
    
    public KLeastExecutedFilter(int k) {
        this.k = k;
        this.execution_count = new Hashtable<Integer, Integer>();
    }

    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {

        ArrayList<MutationInstance> filteredList = new ArrayList<MutationInstance>();

        // add (up to k) mutants in toFilter that have not been executed before to filteredList
        int numMutants = 0;
        int id;
        for (MutationInstance mutant : toFilter){
            id = mutant.id;
            if (numMutants < k && !execution_count.containsKey(id)){
                filteredList.add(MutationInstance.getInstance(id));
                execution_count.put(id, 1); // set execution count of mutant to 1
                numMutants++;
            }
        }

        // if num < k mutants have never been executed, add the next k - num least executed to filtered list
        if (numMutants < k){
            //get list of mutant ids sorted by execution count
            List<Map.Entry<Integer, Integer>> sorted_mutants = new ArrayList<Map.Entry<Integer, Integer>>(execution_count.entrySet());
            Collections.sort(sorted_mutants, new Comparator<Map.Entry<Integer, Integer>>(){
                public int compare(Map.Entry<Integer, Integer> entry1, Map.Entry<Integer, Integer> entry2) {
                    return entry1.getValue().compareTo( entry2.getValue() );
                }
            });

            //add least executed to sorted_mutants until |filteredList| = k
            int size = sorted_mutants.size();
            for(int i = 0; i < size && numMutants < k; i++){
                id = sorted_mutants.get(i).getKey();
                filteredList.add(MutationInstance.getInstance(id));
                execution_count.put(id, execution_count.get(id)+1); // increment execution count of mutant by 1
                numMutants++;
            }
        }

        return filteredList;
    }
    
}
