package com.opengamma.util;

import java.util.Comparator;

/**
 * 
 * @author emcleod
 * 
 */

public class FirstThenSecondPairComparator<S extends Comparable<S>, T extends Comparable<T>> implements Comparator<Pair<S, T>> {

  @Override
  public int compare(Pair<S, T> p1, Pair<S, T> p2) {
    if (p1.getFirst().equals(p2.getFirst())) {
      return p1.getSecond().compareTo(p2.getSecond());
    }
    return p1.getFirst().compareTo(p2.getFirst());
  }
}
