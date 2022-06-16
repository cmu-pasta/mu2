package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;

import java.util.ArrayList;
import java.util.List;

public class RunMutantsFilter implements MutantFilter {
    private MutationGuidance mutationGuidance;
    RunMutantsFilter(MutationGuidance mutationGuidance){
       this.mutationGuidance = mutationGuidance;
    }
    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {
        List<MutationInstance> runMuts = new ArrayList<>();
        for(MutationInstance m : toFilter){
            if(mutationGuidance.runMutants.contains(m.id)){
                runMuts.add(m);
            }
        }
        return runMuts;
    }
}
