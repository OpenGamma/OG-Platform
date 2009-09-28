/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.Comparator;

/**
 * 
 * Compares pairs of numbers. A Pair <i>(x<sub>1</sub>, y<sub>1</sub>)</i> is
 * less than another pair <i>(x<sub>2</sub>, y<sub>2</sub>)</i> if:
 * 
 * <i>x<sub>1</sub> < x<sub>2</sub></i> and <i>y<sub>1</sub> < y<sub>2</sub></i><br>
 * 
 * <i>x<sub>1</sub> = x<sub>2</sub></i> and <i>y<sub>1</sub> < y<sub>2</sub></i><br>
 * 
 * @author emcleod
 */

public class FirstThenSecondPairComparator<S extends Comparable<S>, T extends Comparable<T>> implements Comparator<Pair<S, T>> {

  @Override
  public int compare(final Pair<S, T> p1, final Pair<S, T> p2) {
    if (p1.getFirst().equals(p2.getFirst()))
      return p1.getSecond().compareTo(p2.getSecond());
    return p1.getFirst().compareTo(p2.getFirst());
  }
}
