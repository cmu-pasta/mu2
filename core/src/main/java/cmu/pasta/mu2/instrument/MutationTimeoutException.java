package cmu.pasta.mu2.instrument;

/**
 * Class for instrumenting timeouts
 *
 * @author Bella Laybourn
 */
public class MutationTimeoutException extends Exception {

  public MutationTimeoutException(long ticks) {
    super(String.format("Timeout due to execution of %d ticks", ticks));
  }
}
