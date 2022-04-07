package cmu.pasta.mu2.util;

import java.util.Arrays;

/**
 * ArrayMap - an <int, Object> map backed by an array
 */
public class ArrayMap {

  private static final int INITIAL_ARRAYSET_SIZE = 10;

  boolean[] bs = new boolean[INITIAL_ARRAYSET_SIZE];
  Object[] os = new Object[INITIAL_ARRAYSET_SIZE];

  public void reset() {
    for (int i = 0; i < bs.length; i++) {
      bs[i] = false;
    }
  }

  public void put(int i, Object val) {
    ensureHas(i);
    bs[i] = true;
    os[i] = val;
  }

  public boolean contains(int i) {
    return i < bs.length && bs[i];
  }

  public Object get(int i) {
    assert contains(i);
    return os[i];
  }

  public void remove(int i) {
    bs[i] = false;
  }

  private void ensureHas(int i) {
    if (bs.length > i) {
      return;
    }

    int len = bs.length;

    while (len <= i) {
      len *= 2;
    }

    if (len != bs.length) {
      bs = Arrays.copyOf(bs, len);
      os= Arrays.copyOf(os, len);
    }
  }

  public int size() {
    int s = 0;
    for (int i = 0; i < bs.length; i++) {
      if (bs[i]) {
        s++;
      }
    }
    return s;
  }

}
