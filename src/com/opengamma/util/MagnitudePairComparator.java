package com.opengamma.util;

import java.util.Comparator;


/**
 * 
 * @author emcleod
 * 
 */

public class MagnitudePairComparator implements Comparator<Pair<Double, Double>> {
  private final Comparator<Pair<Double, Double>> _quadrantComparator = new QuadrantPairComparator();

  @Override
  public int compare(Pair<Double, Double> p1, Pair<Double, Double> p2) {
    double z1 = p1.getFirst() * p1.getFirst() + p1.getSecond() * p1.getSecond();
    double z2 = p2.getFirst() * p2.getFirst() + p2.getSecond() * p2.getSecond();
    if (z1 == z2)
      return _quadrantComparator.compare(p1, p2);
    return z1 < z2 ? -1 : 1;
  }
}
