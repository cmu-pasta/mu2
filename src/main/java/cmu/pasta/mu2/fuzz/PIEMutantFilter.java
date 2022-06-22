package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.instrument.MutationSnoop;
import cmu.pasta.mu2.instrument.OptLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PIEMutantFilter implements MutantFilter {

    private Object infectedValue;
    private boolean infectedValueStored;
    private MutationGuidance mutationGuidance;

    PIEMutantFilter(MutationGuidance mutationGuidance, OptLevel optLevel) {
        this.mutationGuidance = mutationGuidance;
        MutationSnoop.setMutantExecutionCallback(m -> mutationGuidance.mutantsToRun.add(m.id));
        if(optLevel == OptLevel.INFECTION){
            BiConsumer<MutationInstance, Object> infectionCallback = (m, value) -> {
                if (!infectedValueStored) {
                    infectedValue = value;
                    infectedValueStored = true;
                } else {
                    if (infectedValue == null) {
                        if (value != null) {
                            mutationGuidance.mutantsToRun.add(m.id);
                        }
                    } else if (!infectedValue.equals(value)) {
                        mutationGuidance.mutantsToRun.add(m.id);
                    }
                    infectedValueStored = false;
                }
            };
            MutationSnoop.setMutantInfectionCallback(infectionCallback);
        }
    }
    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {
        List<MutationInstance> runMuts = new ArrayList<>();
        for(MutationInstance m : toFilter){
            if(mutationGuidance.mutantsToRun.contains(m.id)){
                runMuts.add(m);
            }
        }
       return runMuts;
    }
}
