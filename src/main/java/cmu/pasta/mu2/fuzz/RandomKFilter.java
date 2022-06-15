package cmu.pasta.mu2.fuzz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cmu.pasta.mu2.MutationInstance;

public class RandomKFilter implements MutantFilter {
    
    private int k;

    private RandomKFilter (int k){
        this.k = k;
    }

    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {
        // shuffle list of mutants to randomize first k elements
        Collections.shuffle(toFilter);

        // add first k mutants in list to filtered list
        List<MutationInstance> filtered = new ArrayList<>();
        for(int i = 0; i < k; i++){
            filtered.add(toFilter.get(i));
        }

        return filtered;
    }

    
    
}
