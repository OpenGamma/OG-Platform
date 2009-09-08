package com.opengamma.util;

/**
 * @author jim This class is supposed to hold an immutable 2-tuple, and is
 *         distinct from KeyValuePair<K, V> in that no relationship between the
 *         values is implied.
 * @param <F>
 *          first value
 * @param <S>
 *          second value
 */
public class Pair<F, S> {
  private F _first;
  private S _second;

  public Pair(F first, S second) {
    _first = first;
    _second = second;
  }

  public F getFirst() {
    return _first;
  }

  public S getSecond() {
    return _second;
  }

  @Override
  public boolean equals(Object o) {
    if(o instanceof Pair) {
      Pair<?, ?> pair = (Pair<?, ?>)o;
      return CompareUtils.equalsWithNull(getFirst(), pair.getFirst()) && CompareUtils.equalsWithNull(getSecond(), pair.getSecond());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return getFirst().hashCode() ^ getSecond().hashCode();
  }
}
