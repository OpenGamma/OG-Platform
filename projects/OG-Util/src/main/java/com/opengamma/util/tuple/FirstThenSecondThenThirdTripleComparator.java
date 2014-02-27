/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import java.util.Comparator;

/**
 * A comparator for two triple instances capturing the generic types ensuring they are comparable.
 * <p>
 * A Triple <i>(x<sub>1</sub>, y<sub>1</sub>, z<sub>1</sub>)</i> is less than another triple
 * <i>(x<sub>2</sub>, y<sub>2</sub>, z<sub>2</sub>)</i> if one of these is true:<br />
 * <i>x<sub>1</sub> < x<sub>2</sub></i><br>
 * <i>x<sub>1</sub> = x<sub>2</sub></i> and <i>y<sub>1</sub> < y<sub>2</sub></i><br>
 * <i>x<sub>1</sub> = x<sub>2</sub></i> and <i>y<sub>1</sub> = y<sub>2</sub></i><br> and <i>z<sub>1</sub> < z<sub>2</sub></i><br> 
 * <p>
 * This comparator does not support null elements in the triple.
 * <p>
 * This class is immutable and thread-safe.
 *
 * @param <A> the first element type
 * @param <B> the second element type
 * @param <C> the third element type
 */
public final class FirstThenSecondThenThirdTripleComparator<A extends Comparable<A>, B extends Comparable<B>, C extends Comparable<C>> implements Comparator<Triple<A, B, C>> {

  /**
   * Singleton instance that relies on all elements in the triple being {@link Comparable}.
   */
  @SuppressWarnings({"unchecked", "rawtypes" })
  public static final Comparator<Triple<?, ?, ?>> INSTANCE = new FirstThenSecondThenThirdTripleComparator();

  /**
   * Singleton instance that is based on doubles.
   */
  public static final FirstThenSecondThenThirdTripleComparator<Double, Double, Double> INSTANCE_DOUBLES = new FirstThenSecondThenThirdTripleComparator<>();

  @Override
  public int compare(final Triple<A, B, C> p1, final Triple<A, B, C> p2) {
    if (p1.getFirst().equals(p2.getFirst())) {
      if (p1.getSecond().equals(p2.getSecond())) {
        return p1.getThird().compareTo(p2.getThird());
      }
      return p1.getSecond().compareTo(p2.getSecond());
    }
    return p1.getFirst().compareTo(p2.getFirst());
  }
}
