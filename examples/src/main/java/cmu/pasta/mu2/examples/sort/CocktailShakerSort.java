package cmu.pasta.mu2.examples.sort;

/**
 * @author Mateus Bizzo (https://github.com/MattBizzo)
 * @author Podshivalov Nikita (https://github.com/nikitap492)
 */
class CocktailShakerSort implements SortAlgorithm {

  /**
   * This method implements the Generic Cocktail Shaker Sort
   *
   * @param array The array to be sorted Sorts the array in increasing order
   */
  @Override
  public <T extends Comparable<T>> T[] sort(T[] array) {

    int length = array.length;
    int left = 0;
    int right = length - 1;
    int swappedLeft, swappedRight;
    while (left < right) {
      // front
      swappedRight = 0;
      for (int i = left; i < right; i++) {
        if (SortUtils.less(array[i + 1], array[i])) {
          SortUtils.swap(array, i, i + 1);
          swappedRight = i;
        }
      }
      // back
      right = swappedRight;
      swappedLeft = length - 1;
      for (int j = right; j > left; j--) {
        if (SortUtils.less(array[j], array[j - 1])) {
          SortUtils.swap(array, j - 1, j);
          swappedLeft = j;
        }
      }
      left = swappedLeft;
    }
    return array;
  }
}
