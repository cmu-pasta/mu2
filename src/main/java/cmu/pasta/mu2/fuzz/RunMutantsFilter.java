package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;

import java.util.ArrayList;
import java.util.List;

public class RunMutantsFilter implements MutantFilter {
    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {
        List<MutationInstance> runMuts = new ArrayList<>();
        for(MutationInstance m : toFilter){
            if(MutationGuidance.runMutants.contains(m.id)){
                runMuts.add(m);
            }
        }
        return runMuts;
    }
}
