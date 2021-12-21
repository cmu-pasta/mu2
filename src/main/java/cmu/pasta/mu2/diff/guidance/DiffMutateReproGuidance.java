package cmu.pasta.mu2.diff.guidance;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.diff.DiffException;
import cmu.pasta.mu2.diff.Outcome;
import cmu.pasta.mu2.diff.Serializer;
import cmu.pasta.mu2.instrument.MutationClassLoader;
import cmu.pasta.mu2.instrument.MutationClassLoaders;
import com.pholser.junit.quickcheck.Pair;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.instrument.InstrumentationException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * to avoid the problem of the args having the wrong classloader to begin with
 */
public class DiffMutateReproGuidance extends DiffReproGuidance {
    public List<Outcome> cclOutcomes;
    public Map<MutationInstance, Pair<List<Outcome>, Integer>> mclOutcomes;
    private final MutationClassLoaders MCLs;
    private int ind;

    public DiffMutateReproGuidance(File inputFile, File traceDir, MutationClassLoaders mcls) throws IOException {
        super(inputFile, traceDir);
        cclOutcomes = new ArrayList<>();
        mclOutcomes = new HashMap<>();
        MCLs = mcls;
        ind = -1;
        for(MutationInstance mutationInstance : MCLs.getCartographyClassLoader().getMutationInstances()) {
            mclOutcomes.put(mutationInstance, new Pair<>(new ArrayList<>(), -1));
        }
    }

    @Override
    public void run(TestClass testClass, FrameworkMethod method, Object[] args) throws Throwable {
        recentOutcomes.clear();
        ind++;
        cmpTo = null;
        try {
            super.run(testClass, method, args); //CCL
        } catch(InstrumentationException e) {
            throw new GuidanceException(e);
        } catch (GuidanceException e) {
            throw e;
        } catch (Throwable e) {}
        cmpTo = new ArrayList<>(recentOutcomes);
        cclOutcomes.add(cmpTo.get(0));
        byte[] argBytes = Serializer.serialize(args);
        recentOutcomes.clear();
        for (MutationInstance mutationInstance : MCLs.getCartographyClassLoader().getMutationInstances()) {
            MutationClassLoader mcl = MCLs.getMutationClassLoader(mutationInstance);
            Class<?> clazz = Class.forName(testClass.getName(), true, mcl);
            try {
                TestClass tc = new TestClass(clazz);
                List<Class<?>> paramTypes = new ArrayList<>();
                for (Class<?> clz : method.getMethod().getParameterTypes()) {
                    paramTypes.add(Class.forName(clz.getName(), true, mcl));
                }
                List<Object> argsList = Serializer.deserialize(argBytes, mcl, args);
                FrameworkMethod fm = new FrameworkMethod(clazz.getMethod(method.getName(), paramTypes.toArray(new Class<?>[]{})));
                super.run(tc, fm, argsList.toArray());
            } catch (DiffException e) {
                if (mclOutcomes.containsKey(mutationInstance) && mclOutcomes.get(mutationInstance).second < 0)
                    mclOutcomes.put(mutationInstance, new Pair<>(mclOutcomes.get(mutationInstance).first, ind));
                else if(!mclOutcomes.containsKey(mutationInstance)) {
                    List<Outcome> toAdd = new ArrayList<>();
                    for (int c = 0; c < ind; c++) {
                        toAdd.add(null);
                    }
                    toAdd.add(recentOutcomes.get(recentOutcomes.size() - 1));
                    mclOutcomes.put(mutationInstance, new Pair<>(toAdd, ind));
                }
            } catch(InstrumentationException e) {
                throw new GuidanceException(e);
            } catch (GuidanceException e) {
                throw e;
            } catch (Throwable e) {}
            if (mclOutcomes.containsKey(mutationInstance))
                mclOutcomes.get(mutationInstance).first.add(recentOutcomes.get(recentOutcomes.size() - 1));
            else {
                List<Outcome> toAdd = new ArrayList<>();
                for (int c = 0; c < ind; c++) {
                    toAdd.add(null);
                }
                toAdd.add(recentOutcomes.get(recentOutcomes.size() - 1));
                mclOutcomes.put(mutationInstance, new Pair<>(toAdd, -1));
            }
            recentOutcomes.clear();
        }
        if(cclOutcomes.get(cclOutcomes.size() - 1).thrown != null) throw cclOutcomes.get(cclOutcomes.size() - 1).thrown;
    }

}
