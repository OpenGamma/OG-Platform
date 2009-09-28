/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.opengamma.util.FirstThenSecondPairComparator;
import com.opengamma.util.Pair;

/**
 * A two-dimensional linear interpolator. The data points <em>must</em> be
 * arranged in a grid.
 * 
 * @author emcleod
 */
public class LinearInterpolator2D extends Interpolator2D {

  /**
   * 
   * @param data
   *          A Map containing the (x, y, z) data points.
   * @param value
   *          The (x, y) point for which an interpolated value z is required.
   * @returns An InterpolationResult containing the value of the interpolated
   *          point and an interpolation error of zero.
   * @throws IllegalArgumentException
   *           If the (x, y) point to be interpolated is null or if either of
   *           the values x or y is null.
   */
  @Override
  public InterpolationResult<Double> interpolate(Map<Pair<Double, Double>, Double> data, Pair<Double, Double> value) {
    if (value == null)
      throw new IllegalArgumentException("Pair of values to be interpolated was null");
    if (value.getFirst() == null)
      throw new IllegalArgumentException("x-value to be interpolated was null");
    if (value.getSecond() == null)
      throw new IllegalArgumentException("y-value to be interpolated was null");
    Comparator<Pair<Double, Double>> comparator = new FirstThenSecondPairComparator<Double, Double>();
    checkData(data, comparator);
    TreeSet<Pair<Double, Double>> xySet = new TreeSet<Pair<Double, Double>>(comparator);
    xySet.addAll(data.keySet());
    List<Pair<Double, Double>> surroundingPoints = getSurroundingPoints(xySet, value, true);
    Pair<Double, Double> p1 = surroundingPoints.get(0);
    Pair<Double, Double> p2 = surroundingPoints.get(1);
    Pair<Double, Double> p3 = surroundingPoints.get(2);
    Pair<Double, Double> p4 = surroundingPoints.get(3);
    double x1 = p1.getFirst();
    double x2 = p2.getFirst();
    double y1 = p1.getSecond();
    double y2 = p2.getSecond();
    double z1 = data.get(p1);
    double z2 = data.get(p2);
    double z3 = data.get(p3);
    double z4 = data.get(p4);
    double x = value.getFirst();
    double y = value.getSecond();
    System.out.println(p1 + " " + p2);
    return new InterpolationResult<Double>((x2 - x) * ((y2 - y) * z1 + (y - y1) * z2) + (x - x1) * ((y2 - y) * z3 + (y - y1) * z4) / ((x2 - x1) * (y2 - y1)));
  }
}
