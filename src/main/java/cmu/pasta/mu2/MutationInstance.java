package cmu.pasta.mu2;

import cmu.pasta.mu2.mutators.Mutator;

import java.util.ArrayList;

/**
 * A mutation instance represents a single point mutation.
 *
 * <p>A mutation instance is uniquely represented by the class being mutated,
 * the type of mutator, and the index of that mutation operation within the class.</p>
 *
 * <p>Each mutation instance is also assigned a globally unique identifier, which allows
 * other classes to store information about mutation instances without having a reference
 * to the object itself. This is useful for referencing a mutation instance from instrumented
 * code, which typically only makes static method calls.</p>
 */
public class MutationInstance {

  /**
   * Globally unique identifier for this mutation instance
   */
  public final int id;

  /**
   * Static list of all registered mutation instances.
   */
  private static final ArrayList<MutationInstance> mutationInstances = new ArrayList<>();

  /**
   * The type of mutation represented by a mutator
   */
  public final Mutator mutator;

  /**
   * Name of the class to mutate
   */
  public final String className;

  /**
   * Numbered instance of the opportunity for mutation this classloader uses
   */
  public final long sequenceIdx;

  /**
   * Counter that is incremented during execution of this mutation instance to catch infinite
   * loops.
   */
  private long timeoutCounter = 0;

  private final int lineNum;
  private final String fileName;

  // TODO potential for more information:
  // line number
  // who's seen it
  // whether this mutation is likely to be killed by a particular input

  /**
   * Creates a new mutation instance.
   *
   * @param className   the fully-qualified name of the class being mutated
   * @param mutator     the mutator being applied
   * @param sequenceIdx the index of the mutator being applied on this class
   */
  public MutationInstance(String className, Mutator mutator, long sequenceIdx, int lineNum, String fileName) {
    this.id = mutationInstances.size();
    this.className = className;
    this.mutator = mutator;
    this.sequenceIdx = sequenceIdx;
    this.lineNum = lineNum;
    this.fileName = fileName;

    // Register mutation instance
    mutationInstances.add(this);
  }

  public void resetTimer() {
    this.timeoutCounter = 0;
  }

  public long getTimeoutCounter() {
    return this.timeoutCounter;
  }

  public long incrementTimeoutCounter() {
    return ++this.timeoutCounter;
  }

  @Override
  public String toString() {
    return String.format("%s::%s::%d (%s:L%d)", className, mutator, sequenceIdx, fileName, lineNum);
  }

  public static MutationInstance getInstance(int id) {
    return mutationInstances.get(id);
  }

  public static int getNumInstances() {
    return mutationInstances.size();
  }

  public int getLineNum() {
    return lineNum;
  }

  @Override
  public boolean equals(Object that) {
    // Mutation instances are globally unique
    return this == that;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
