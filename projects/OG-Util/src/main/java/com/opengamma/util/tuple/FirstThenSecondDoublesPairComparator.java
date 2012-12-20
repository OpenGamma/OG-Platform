/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import java.util.Comparator;

/**
 * A comparator for two {@code DoublesPair} instances.
 * <p>
 * A Pair <i>(x<sub>1</sub>, y<sub>1</sub>)</i> is less than another pair
 * <i>(x<sub>2</sub>, y<sub>2</sub>)</i> if one of these is true:<br />
 * <i>x<sub>1</sub> < x<sub>2</sub></i><br>
 * <i>x<sub>1</sub> = x<sub>2</sub></i> and <i>y<sub>1</sub> < y<sub>2</sub></i><br>
 * <p>
 * This class is immutable and thread-safe.
 */
public final class FirstThenSecondDoublesPairComparator implements Comparator<DoublesPair> {

  /**
   * Singleton instance that relies on both elements in the pair being {@link Comparable}.
   */
  public static final Comparator<DoublesPair> INSTANCE = new FirstThenSecondDoublesPairComparator();

  @Override
  public int compare(final DoublesPair p1, final DoublesPair p2) {
    if (Double.compare(p1.first, p2.first) == 0) {
      return Double.compare(p1.second, p2.second);
    }
    return Double.compare(p1.first, p2.first);
  }

}
