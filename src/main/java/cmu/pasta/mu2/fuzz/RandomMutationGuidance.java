package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.diff.DiffException;
import cmu.pasta.mu2.diff.Outcome;
import cmu.pasta.mu2.util.Serializer;
import cmu.pasta.mu2.diff.guidance.DiffGuidance;
import cmu.pasta.mu2.diff.junit.DiffTrialRunner;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import cmu.pasta.mu2.instrument.MutationSnoop;
import cmu.pasta.mu2.instrument.OptLevel;
import cmu.pasta.mu2.util.ArraySet;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.util.MovingAverage;
import edu.berkeley.cs.jqf.instrument.InstrumentationException;
import cmu.pasta.mu2.diff.guidance.OptimizedMutationGuidance;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Objects;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
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

        while(numMutants < size){
            if (runMutants.contains(n)){
                numMutants++;
                mutantList.add(n);
            }
            n++;
        }

        Collections.shuffle(mutantList);

        ArraySet filtered = new ArraySet();
        
        for(int i = 0; i < k  && i < mutantList.size(); i++){
            filtered.add(mutantList.get(i));
        }

        return filtered;

    }
    
}

