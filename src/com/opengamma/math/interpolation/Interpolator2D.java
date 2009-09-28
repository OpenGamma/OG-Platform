/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.util.Pair;

/**
 * A base class for two-dimensional interpolation.
 * 
 * @author emcleod
 */

public abstract class Interpolator2D implements Interpolator<Map<Pair<Double, Double>, Double>, Pair<Double, Double>, Double> {

  /**
   * @param data
   *          A map of (x, y) pairs to z values.
   * @param value
   *          The (x, y) value for which an interpolated value for z is to be
   *          found.
   * @returns An InterpolationResult containing the interpolated value of z and
   *          (if appropriate) the estimated error of this value.
   */
  @Override
  public abstract InterpolationResult<Double> interpolate(Map<Pair<Double, Double>, Double> data, Pair<Double, Double> value);

  /**
   * Checks that there is sufficient data and returns the data as a sorted map.
   * 
   * @param data
   * @param comparator
   * @return
   */
  protected TreeMap<Pair<Double, Double>, Double> initData(Map<Pair<Double, Double>, Double> data, Comparator<Pair<Double, Double>> comparator) {
    checkData(data, comparator);
    TreeMap<Pair<Double, Double>, Double> result = new TreeMap<Pair<Double, Double>, Double>(comparator);
    result.putAll(data);
    return result;
  }

  /**
   * Checks the data.
   * 
   * @param data
   * @param comparator
   * @throws IllegalArgumentException
   *           If the data map is null; if the comparator is null; or if the
   *           size of the data array is less than four (because finding the
   *           interpolated value of a function needs four surrounding points).
   */
  protected void checkData(Map<Pair<Double, Double>, Double> data, Comparator<Pair<Double, Double>> comparator) {
    if (data == null)
      throw new IllegalArgumentException("Data map was null");
    if (comparator == null)
      throw new IllegalArgumentException("The comparator was null");
    if (data.size() < 4)
      throw new IllegalArgumentException("Need at least four points to perform 2D interpolation");
  }

  // TODO this probably will need to be changed because non-grid methods will
  // behave differently from each other.

  protected List<Pair<Double, Double>> getSurroundingPoints(TreeSet<Pair<Double, Double>> sorted, Pair<Double, Double> value, boolean mustBeAGrid) {
    if (mustBeAGrid)
      return getSurroundingPointsFromGrid(sorted, value);
    return getSurroundingPoints(sorted, value);
  }

  /**
   * Finds the four data points surrounding an (x, y) pair.
   * 
   * @param sorted
   * @param value
   * @throws InterpolationException
   *           If the data do not form an x-y grid; if the data points are only
   *           one-dimensional; if the x-value of the point for which to find
   *           the interpolated value is greater(lower) than the
   *           greatest(lowest) x-point in the data grid; or if the y-value of
   *           the point for which to find the interpolated value is
   *           greater(lower) than the greatest(lowest) y-point in the data
   *           grid;
   * @return
   */
  protected List<Pair<Double, Double>> getSurroundingPointsFromGrid(TreeSet<Pair<Double, Double>> sorted, Pair<Double, Double> value) {
    TreeMap<Double, TreeSet<Double>> strips = new TreeMap<Double, TreeSet<Double>>();
    Double x = null;
    Double previousX = null;
    TreeSet<Double> strip = new TreeSet<Double>();
    for (Pair<Double, Double> pair : sorted) {
      x = pair.getFirst();
      if (!x.equals(previousX) && previousX != null) {
        if (strips.size() > 0 && strip.size() != strips.get(strips.firstKey()).size())
          throw new InterpolationException("The data were not on a grid of (x, y) points");
        strips.put(previousX, new TreeSet<Double>(strip));
        strip = new TreeSet<Double>();
      }
      strip.add(pair.getSecond());
      previousX = x;
    }
    strips.put(x, strip);
    if (strips.size() == 1)
      throw new InterpolationException("All of the data points are on a single line");
    if (value.getFirst() <= strips.firstKey())
      throw new InterpolationException("x-value of the point for which to get the interpolated value was lower than the lowest value of x in the grid");
    if (value.getFirst() >= strips.lastKey())
      throw new InterpolationException("x-value of the point for which to get the interpolated value was greater than the greatest value of x in the grid");
    if (value.getSecond() <= strips.get(strips.firstKey()).first())
      throw new InterpolationException("y-value of the point for which to get the interpolated value was lower than the lowest value of y in the grid");
    if (value.getSecond() >= strips.get(strips.lastKey()).last())
      throw new InterpolationException("y-value of the point for which to get the interpolated value was greater than the greatest value of y in the grid");
    Double xInterp = value.getFirst();
    Double yInterp = value.getSecond();
    Map.Entry<Double, TreeSet<Double>> stripBefore = strips.floorEntry(xInterp);
    Map.Entry<Double, TreeSet<Double>> stripAfter = strips.ceilingEntry(xInterp);
    List<Pair<Double, Double>> result = new ArrayList<Pair<Double, Double>>();
    result.add(new Pair<Double, Double>(stripBefore.getKey(), stripBefore.getValue().floor(yInterp)));
    result.add(new Pair<Double, Double>(stripBefore.getKey(), stripBefore.getValue().ceiling(yInterp)));
    result.add(new Pair<Double, Double>(stripAfter.getKey(), stripAfter.getValue().floor(yInterp)));
    result.add(new Pair<Double, Double>(stripAfter.getKey(), stripAfter.getValue().ceiling(yInterp)));
    return result;
  }

  protected List<Pair<Double, Double>> getSurroundingPoints(TreeSet<Pair<Double, Double>> sorted, Pair<Double, Double> value) {
    throw new NotImplementedException();
  }
}
