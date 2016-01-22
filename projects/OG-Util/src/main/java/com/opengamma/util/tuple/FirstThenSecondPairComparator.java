/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A comparator for two pair instances capturing the generic types ensuring they are comparable.
 * <p>
 * A Pair <i>(x<sub>1</sub>, y<sub>1</sub>)</i> is less than another pair
 * <i>(x<sub>2</sub>, y<sub>2</sub>)</i> if one of these is true:<br />
 * <i>x<sub>1</sub> < x<sub>2</sub></i><br>
 * <i>x<sub>1</sub> = x<sub>2</sub></i> and <i>y<sub>1</sub> < y<sub>2</sub></i><br>
 * <p>
 * This comparator does not support null elements in the pair.
 * <p>
 * This class is immutable and thread-safe.
 *
 * @param <A> the first element type
 * @param <B> the second element type
 */
public final class FirstThenSecondPairComparator<A extends Comparable<A>, B extends Comparable<B>> implements Comparator<Pair<A, B>>, Serializable {

  /**
   * Singleton instance that relies on both elements in the pair being {@link Comparable}.
   */
  @SuppressWarnings({"unchecked", "rawtypes" })
  public static final Comparator<Pair<?, ?>> INSTANCE = new FirstThenSecondPairComparator();

  /**
   * Singleton instance that is based on doubles.
   */
  public static final FirstThenSecondPairComparator<Double, Double> INSTANCE_DOUBLES = new FirstThenSecondPairComparator<Double, Double>();

  @Override
  public int compare(final Pair<A, B> p1, final Pair<A, B> p2) {
    if (p1.getFirst().equals(p2.getFirst())) {
      return p1.getSecond().compareTo(p2.getSecond());
    }
    return p1.getFirst().compareTo(p2.getFirst());
  }

}
