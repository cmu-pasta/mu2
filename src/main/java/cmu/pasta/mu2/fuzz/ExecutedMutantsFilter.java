package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.instrument.MutationSnoop;
import cmu.pasta.mu2.util.ArraySet;

import java.util.ArrayList;
import java.util.List;

public class ExecutedMutantsFilter implements MutantFilter {

    private ArraySet executedMutants = new ArraySet();
    ExecutedMutantsFilter(){
        MutationSnoop.setMutantExecutionCallback(m -> executedMutants.add(m.id));
    }
    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {
        List<MutationInstance> runMuts = new ArrayList<>();
        for(MutationInstance m : toFilter){
            if(executedMutants.contains(m.id)){
                runMuts.add(m);
            }
        }
        return runMuts;
    }

    @Override
    public void prepTrial() {
        executedMutants.reset();
    }
}
