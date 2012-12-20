/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import java.util.Comparator;

/**
 * A comparator for a pair of doubles that evaluates the magnitude of the pairs.
 * <p>
 * This comparator first calculates the magnitude by adding the square of the first
 * element in the pair to the square of the second element in the pair.
 * If the magnitudes differ then the result is returned based on the magnitude.
 * If the magnitude is equal then the {@link QuadrantPairComparator} is used.
 * <p>
 * This comparator does not support null elements in the pair.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class MagnitudePairComparator implements Comparator<Pair<Double, Double>> {

  /**
   * Singleton instance.
   */
  public static final MagnitudePairComparator INSTANCE = new MagnitudePairComparator();

  /**
   * Restrictive constructor.
   */
  private MagnitudePairComparator() {
  }

  @Override
  public int compare(Pair<Double, Double> p1, Pair<Double, Double> p2) {
    double x1 = p1.getFirst();
    double y1 = p1.getSecond();
    double x2 = p2.getFirst();
    double y2 = p2.getSecond();
    double z1 = x1 * x1 + y1 * y1;
    double z2 = x2 * x2 + y2 * y2;
    if (z1 == z2) {
      return QuadrantPairComparator.INSTANCE.compare(p1, p2);
    }
    return z1 < z2 ? -1 : 1;
  }

}
