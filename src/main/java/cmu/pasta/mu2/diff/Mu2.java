package cmu.pasta.mu2.diff;

import cmu.pasta.mu2.diff.guidance.DiffGuidance;
import cmu.pasta.mu2.diff.guidance.DiffNoGuidance;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.junit.quickcheck.FuzzStatement;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mu2 extends JQF {
    Map<String, FrameworkMethod> cmpNames;

    @SuppressWarnings("unused") // Invoked reflectively by JUnit
    public Mu2(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> methods = super.computeTestMethods();
        methods.addAll(getTestClass().getAnnotatedMethods(Diff.class));
        return methods;
    }

    @Override protected void validateTestMethods(List<Throwable> errors) {
        super.validateTestMethods(errors);
        cmpNames = new HashMap<>();
        validateComparisonMethods(errors);
        validateRegressionMethods(errors);
    }

    private void validateComparisonMethods(List<Throwable> errors) {
        for(FrameworkMethod method : getTestClass().getAnnotatedMethods(Comparison.class)) {
            if(method.getReturnType() != Boolean.class) {
                errors.add(new Exception("Method " + method.getName() + " must return Boolean"));
            }
            Class<?>[] parameters = method.getMethod().getParameterTypes();
            if(parameters.length != 2 || parameters[0] != parameters[1]) {
                errors.add(new Exception("Method " + method.getName()
                        + " must have exactly two parameters, and they must be of the same type"));
            }
            cmpNames.put(method.getName(), method);
        }
    }

    private void validateRegressionMethods(List<Throwable> errors) {
        for(FrameworkMethod method : getTestClass().getAnnotatedMethods(Diff.class)) {
            if(method.getReturnType() == null) {
                errors.add(new Exception("Method " + method.getName() + " cannot be void"));
            }
            if(!method.isPublic()) {
                errors.add(new Exception("Method " + method.getName() + " must be public"));
            }
            if(method.isStatic()) {
                errors.add(new Exception("Method " + method.getName() + " must not be static"));
            }

            String cmp = method.getAnnotation(Diff.class).cmp();
            if(!cmp.equals("") && !cmpNames.containsKey(cmp)) {
                errors.add(new Exception("cmp() in Diff annotation of method " + method.getName()
                        + " must be the name of a function in this class marked with the @Comparison annotation"));
            } else if(!cmp.equals("") && cmpNames.get(cmp).getMethod().getParameterTypes()[0] != method.getReturnType()) {
                errors.add(new Exception("The function referred to by cmp() in Regression annotation of method "
                        + method + " must accept arguments matching the return type of the method"));
            }
        }
    }

    @Override
    public Statement methodBlock(FrameworkMethod method) {
        if (method.getAnnotation(Diff.class) == null) {
            return super.methodBlock(method);
        }

        // Get currently set fuzzing guidance
        Guidance guidance = GuidedFuzzing.getCurrentGuidance();
        DiffGuidance diffGuidance;


        if(guidance == null) {
            guidance = new DiffNoGuidance(GuidedFuzzing.DEFAULT_MAX_TRIALS, System.err);
        }

        assert (guidance instanceof DiffGuidance);
        diffGuidance = (DiffGuidance) guidance;

        if(!method.getAnnotation(Diff.class).cmp().equals("")) {
            diffGuidance.setCompare(cmpNames.get(method.getAnnotation(Diff.class).cmp()).getMethod());
        }

        FuzzStatement fs = new FuzzStatement(method, getTestClass(), generatorRepository, diffGuidance);
        return fs;
    }
}
