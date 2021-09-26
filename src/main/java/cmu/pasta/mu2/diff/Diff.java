package cmu.pasta.mu2.diff;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link cmu.pasta.mu2.diff.Diff} annotation marks a method as an entry-point for
 * regression-based fuzz testing.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Diff {
    String cmp();
}
