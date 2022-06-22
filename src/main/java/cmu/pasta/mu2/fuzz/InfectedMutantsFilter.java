package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.instrument.MutationSnoop;
import cmu.pasta.mu2.util.ArraySet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class InfectedMutantsFilter implements MutantFilter {

    private Object infectedValue;
    private boolean infectedValueStored;
    private ArraySet infectedMutants = new ArraySet();
    InfectedMutantsFilter() {
        //only ever called if opt level is set to infection
        BiConsumer<MutationInstance, Object> infectionCallback = (m, value) -> {
            if (!infectedValueStored) {
                infectedValue = value;
                infectedValueStored = true;
            } else {
                if (infectedValue == null) {
                    if (value != null) {
                        infectedMutants.add(m.id);
                    }
                } else if (!infectedValue.equals(value)) {
                    infectedMutants.add(m.id);
                }
                infectedValueStored = false;
            }
        };
        MutationSnoop.setMutantInfectionCallback(infectionCallback);
    }
    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {
        List<MutationInstance> runMuts = new ArrayList<>();
        for(MutationInstance m : toFilter){
            if(infectedMutants.contains(m.id)){
                runMuts.add(m);
            }
        }
        return runMuts;
    }

    @Override
    public void prepTrial() {
        infectedMutants.reset();
    }
}
