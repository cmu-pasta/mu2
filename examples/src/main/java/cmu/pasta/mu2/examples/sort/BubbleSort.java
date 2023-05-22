package cmu.pasta.mu2.examples.sort;

import static cmu.pasta.mu2.examples.sort.SortUtils.*;

/**
 * @author Varun Upadhyay (https://github.com/varunu28)
 * @author Podshivalov Nikita (https://github.com/nikitap492)
 * @see SortAlgorithm
 */
public class BubbleSort implements SortAlgorithm {

  /**
   * This method implements the Generic Bubble Sort
   *
   * @param array The array to be sorted Sorts the array in ascending order
   */
  @Override
  public <T extends Comparable<T>> T[] sort(T[] array) {
    for (int i = 0, size = array.length; i < size - 1; ++i) {
      boolean swapped = false;
      for (int j = 0; j < size - 1 - i; ++j) {
        if (greater(array[j], array[j + 1])) {
          swap(array, j, j + 1);
          swapped = true;
        }
      }
      if (!swapped) {
        break;
      }
    }
    return array;
  }
}
