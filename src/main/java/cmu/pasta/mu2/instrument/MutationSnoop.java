package cmu.pasta.mu2.instrument;

import cmu.pasta.mu2.MutationInstance;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This is a class with static methods that are invoked from instrumentation in mutated test
 * methods.
 * <p>
 * {@see {@link edu.berkeley.cs.jqf.instrument.tracing.SingleSnoop}}
 */
public class MutationSnoop {

  public static final long TIMEOUT_TICKS = Integer
      .getInteger("jqf.mutation.TIMEOUT_TICKS", 100_0000);

  /**
   * Validates whether or not a timeout has occurred.
   * <p>
   * This method is invoked periodically from mutated test classes, in order to keep track of time.
   * Each invocation of this method increments a counter.
   *
   * @param id the unique identifier of the mutation instance
   * @throws MutationTimeoutException
   */
  public static void checkTimeout(int id) throws MutationTimeoutException {
    if (MutationInstance.getInstance(id).incrementTimeoutCounter() > TIMEOUT_TICKS) {
      throw new MutationTimeoutException(MutationInstance.getInstance(id).getTimeoutCounter());
    }
  }

  /**
   * The callback which must be run upon invoking a mutant
   */
  private static Consumer<MutationInstance> callback = x -> {
  };
  private static BiConsumer<MutationInstance, Object> infectionCallback = (x, id) -> {
  };

  /**
   * Called when a mutant is run in the intial run
   *
   * @param id The id of the {@link MutationInstance}
   * @see Cartographer
   */
  public static void logMutant(int id) {
    callback.accept(MutationInstance.getInstance(id));
  }

  public static void logValue(int value, int id) {
    infectionCallback.accept(MutationInstance.getInstance(id), value);
  }

  public static void logValue(float value, int id) {
    infectionCallback.accept(MutationInstance.getInstance(id), value);
  }

  public static void logValue(boolean value, int id) {
    infectionCallback.accept(MutationInstance.getInstance(id), value);
  }

  /**
   * Set the callback which will be run each time a mutant is run in the initial run of the tested
   * class
   *
   * @param cb The new callback
   */

  public static void setMutantCallback(Consumer<MutationInstance> cb) {
    callback = cb;
  }

  public static void setMutantInfectionCallback(BiConsumer<MutationInstance, Object> cb) {
    infectionCallback = cb;
  }
}
