package cmu.pasta.mu2.diff.guidance;

import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;

import java.lang.reflect.Method;

public interface DiffGuidance extends Guidance {
    void setCompare(Method m);
}
