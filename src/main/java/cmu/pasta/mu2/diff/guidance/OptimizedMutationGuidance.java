package cmu.pasta.mu2.diff.guidance;

import cmu.pasta.mu2.util.ArraySet;

public interface OptimizedMutationGuidance extends DiffGuidance {
    ArraySet filterMutants(ArraySet runMutants);
}
