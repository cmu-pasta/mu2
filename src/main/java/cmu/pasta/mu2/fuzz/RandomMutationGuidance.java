package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.util.ArraySet;
import cmu.pasta.mu2.diff.guidance.OptimizedMutationGuidance;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

public class RandomMutationGuidance extends MutationGuidance implements OptimizedMutationGuidance{
    
    private int k;

    public RandomMutationGuidance(String testName, MutationClassLoaders mutationClassLoaders,
    Duration duration, Long trials, File outputDirectory, File seedInputDir, Random rand, int k) throws IOException {
        super(testName, mutationClassLoaders, duration, trials, outputDirectory, seedInputDir, rand);
        this.k = k;
    }

    @Override
    public ArraySet filterMutants() {
        
        ArrayList<Integer> mutantList = new ArrayList<Integer>();
        int numMutants = 0;
        int size = runMutants.size();
        int n = 0;

        // add all mutants in runMutants to mutantList
        while(numMutants < size){
            if (runMutants.contains(n)){
                numMutants++;
                mutantList.add(n);
            }
            n++;
        }

        // shuffle mutantList to randomize first k elements
        Collections.shuffle(mutantList);

        // add first k mutants in mutantList to filtered ArraySet
        ArraySet filtered = new ArraySet();
        for(int i = 0; i < k  && i < mutantList.size(); i++){
            filtered.add(mutantList.get(i));
        }

        return filtered;

    }
    
}

