package cmu.pasta.mu2.examples.sort;

import java.util.Arrays;
import java.util.List;

/**
 * The common interface of most sorting algorithms.
 *
 * At least one of the two sort methods must be overridden.
 *
 * @author Podshivalov Nikita (https://github.com/nikitap492)
 */
public interface SortAlgorithm {

  /**
   * Method to sort arrays
   *
   * @param unsorted - an array should be sorted
   * @return a sorted array
   */
  default <T extends Comparable<T>> T[] sort(T[] unsorted) {
    return sort(Arrays.asList(unsorted)).toArray(unsorted);
  }

  /**
   * Method for algorithms what wanted to work with lists from JCF
   *
   * @param unsorted - a list should be sorted
   * @return a sorted list
   */
  @SuppressWarnings("unchecked")
  default <T extends Comparable<T>> List<T> sort(List<T> unsorted) {
    return Arrays.asList(sort(unsorted.toArray((T[]) new Comparable[unsorted.size()])));
  }

}
