package cmu.pasta.mu2.examples.sort;

import static cmu.pasta.mu2.examples.sort.SortUtils.*;

/**
 * Implementation of gnome sort
 *
 * @author Podshivalov Nikita (https://github.com/nikitap492)
 * @since 2018-04-10
 */
public class GnomeSort implements SortAlgorithm {

  @Override
  public <T extends Comparable<T>> T[] sort(T[] arr) {
    int i = 1;
    int j = 2;
    while (i < arr.length) {
      if (less(arr[i - 1], arr[i])) i = j++;
      else {
        swap(arr, i - 1, i);
        if (--i == 0) {
          i = j++;
        }
      }
    }

    return arr;
  }
}
