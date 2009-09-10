package com.opengamma.util;

import java.util.Comparator;

/**
 * 
 * @author emcleod
 * 
 */

public class QuadrantPairComparator implements Comparator<Pair<Double, Double>> {
  private final Comparator<Pair<Double, Double>> _firstThenSecond = new FirstThenSecondPairComparator<Double, Double>();

  @Override
  public int compare(Pair<Double, Double> p1, Pair<Double, Double> p2) {
    if (p1.equals(p2))
      return 0;
    int firstQuadrant = getQuadrant(p1);
    int secondQuadrant = getQuadrant(p2);
    if (firstQuadrant == secondQuadrant) {
      return _firstThenSecond.compare(p1, p2);
    }
    return firstQuadrant < secondQuadrant ? -1 : 1;
  }

  private int getQuadrant(Pair<Double, Double> pair) {
    double x = pair.getFirst();
    double y = pair.getSecond();
    if (x >= 0) {
      if (y >= 0)
        return 1;
      return 4;
    }
    if (y > 0)
      return 2;
    return 3;
  }
}
