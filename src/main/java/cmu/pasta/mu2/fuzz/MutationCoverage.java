package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;

import java.util.*;

/**
 * Utility class to collect mutation coverage
 *
 * @author Bella Laybourn
 */
public class MutationCoverage extends Coverage {

  private Set<MutationInstance> caughtMutants = new HashSet<>();
  private Set<MutationInstance> seenMutants = new HashSet<>();


  public void see(MutationInstance mi) {
    seenMutants.add(mi);
  }

  public void kill(MutationInstance mi) {
    caughtMutants.add(mi);
  }

  @Override
  public void clear() {
    super.clear();
    clearMutants();
  }

  public void clearMutants() {
    caughtMutants = new HashSet<>();
  }

  public int numCaughtMutants() {
    return caughtMutants.size();
  }

  public int updateMutants(MutationCoverage that) {
    int prevSize = caughtMutants.size();
    caughtMutants.addAll(that.caughtMutants);
    seenMutants.addAll(that.seenMutants);
    return caughtMutants.size() - prevSize;
  }

  public int numSeenMutants() {
    return seenMutants.size();
  }

  public Set<MutationInstance> getMutants() {
    return new HashSet<>(caughtMutants);
  }

  public Set<MutationInstance> getSeenMutants() {
    return new HashSet<>(seenMutants);
  }

  public Map<Integer, Integer> getNonZeroCounts() {
    Map<Integer, Integer> nonZeroCounts = new HashMap();
    Collection<Integer> indices = getCovered();
    Collection<Integer> counts = getCoveredCounts();
    assert(indices.size() == counts.size());
    Iterator it1 = indices.iterator();
    Iterator it2 = counts.iterator();
    while (it1.hasNext() && it2.hasNext()) {
      nonZeroCounts.put((Integer) it1.next(), (Integer) it2.next());
    }
    return nonZeroCounts;
  }
}
