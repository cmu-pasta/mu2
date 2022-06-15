package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.util.ArraySet;

import java.util.ArrayList;
import java.util.List;

public class DeadMutantsFilter implements MutantFilter {
    private MutationGuidance mutationGuidance;
    DeadMutantsFilter(MutationGuidance mutationGuidance){
        this.mutationGuidance = mutationGuidance;
    }
    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {
        List<MutationInstance> aliveMuts = new ArrayList<>();
        for(MutationInstance m : toFilter){
            if(!mutationGuidance.deadMutants.contains(m.id)){
                aliveMuts.add(m);
            }
        }
        return aliveMuts;
    }
}
