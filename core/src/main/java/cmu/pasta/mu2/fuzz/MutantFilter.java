package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.instrument.MutationInstance;

import java.util.List;

public interface MutantFilter {
    List<MutationInstance> filterMutants(List<MutationInstance> toFilter);
}
