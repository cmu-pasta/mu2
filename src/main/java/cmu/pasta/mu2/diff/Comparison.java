package cmu.pasta.mu2.diff;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link cmu.pasta.mu2.diff.Comparison} annotation marks a method as a comparison
 * function for regression-based fuzz testing.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Comparison {
}
