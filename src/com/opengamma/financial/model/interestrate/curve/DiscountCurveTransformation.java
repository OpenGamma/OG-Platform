/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.math.interpolation.InterpolationException;

/**
 * A set of methods that perform transformations of DiscountCurves. They do not
 * alter the input curves. This class can be used to obtain parallel-shifted
 * curves, curves with a single point shifted (e.g. to calculate rho) and curves
 * with multiple points shifted (e.g. to produce a skewed curve).
 * 
 * @author emcleod
 */

// TODO I don't like this name.
public class DiscountCurveTransformation {

  /**
   * Shifts all interest rates that form the original curve by a constant value.
   * 
   * @param original
   *          The original discount curve.
   * @param shift
   *          The amount as a decimal by which to shift the curve.
   * @return A parallel-shifted discount curve.
   */
  public static DiscountCurve getParallelShiftedCurve(DiscountCurve original, double shift) {
    if (shift == 0)
      return new DiscountCurve(original.getData(), original.getInterpolator());
    final Map<Double, Double> data = new HashMap<Double, Double>();
    for (final Map.Entry<Double, Double> entry : original.getData().entrySet()) {
      data.put(entry.getKey(), entry.getValue() + shift);
    }
    return new DiscountCurve(data, original.getInterpolator());
  }

  /**
   * Shifts the data point at an index. For example, if the data points for the
   * original curve are {{1.5, 0.03}, {2.5, 0.05}, {3.5, 0.035}}, the shift
   * index is 1 and the shift amount is 0.005, the new discount curve will be
   * formed with the data points {{1.5, 0.03}, {2.5, 0.055}, {3.5, 0.035}}. Note
   * that this is zero-valued: to shift the first point, the index must be zero.
   * 
   * @param original
   *          The original discount curve.
   * @param dataIndex
   *          The index of the point to be shifted.
   * @param shift
   *          The amount as a decimal by which to shift the data.
   * @return A discount curve with one point shifted.
   * @throws IllegalArgumentException
   *           If the shift index is negative or greater than the size of the
   *           data map of the original curve.
   */
  public static DiscountCurve getSingleShiftedDataPointCurve(DiscountCurve original, int dataIndex, double shift) {
    if (dataIndex < 0)
      throw new IllegalArgumentException("Shift point must be positive");
    if (dataIndex >= original.getData().size())
      throw new IllegalArgumentException("Could not shift point " + dataIndex + "; number of data points in DiscountCurve is " + original.getData().size());
    if (shift == 0)
      return new DiscountCurve(original.getData(), original.getInterpolator());
    final SortedMap<Double, Double> data = new TreeMap<Double, Double>(original.getData());
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      if (i++ == dataIndex) {
        data.put(entry.getKey(), entry.getValue() + shift);
        break;
      }
    }
    return new DiscountCurve(data, original.getInterpolator());
  }

  /**
   * Shifts the curve at a particular time to maturity.
   * 
   * @param original
   *          The original discount curve.
   * @param dataIndex
   *          The time to maturity of the point being shifted.
   * @param shift
   *          The amount as a decimal by which to shift the point.
   * @return A discount curve with one point shifted.
   * @throws IllegalArgumentException
   *           If the shift time to maturity is negative or is greater than the
   *           longest time to maturity of the original curve.
   */
  public static DiscountCurve getSingleShiftedPointCurve(DiscountCurve original, double shiftTime, double shift) {
    if (shiftTime < 0)
      throw new IllegalArgumentException("Shift time must be positive");
    final SortedMap<Double, Double> data = new TreeMap<Double, Double>(original.getData());
    if (shiftTime >= data.lastKey())
      throw new IllegalArgumentException("Could not shift at time " + shiftTime + "; last time in DiscountCurve is " + data.lastKey());
    if (shift == 0)
      return new DiscountCurve(original.getData(), original.getInterpolator());
    if (data.containsKey(shiftTime)) {
      data.put(shiftTime, data.get(shiftTime) + shift);
    } else {
      try {
        final double interpolatedPoint = original.getInterestRate(shiftTime);
        data.put(shiftTime, interpolatedPoint + shift);
      } catch (final InterpolationException e) {
        // TODO logging
        return null;
      }
    }
    return new DiscountCurve(data, original.getInterpolator());
  }

  /**
   * Shifts the data point at various indices. For example, if the data points
   * for the original curve are {{1.5, 0.03}, {2.5, 0.05}, {3.5, 0.035}}, and
   * the shifts are {{2, 0.005}, {3, -0.015}}, the new discount curve will be
   * formed with the data points {{1.5, 0.03}, {2.5, 0.055}, {3.5, 0.02}}. Note
   * that this is zero-valued: to shift the first point, the index must be zero.
   * 
   * @param original
   *          The original discount curve.
   * @param shifts
   *          A map containing the index of the data points to shift and shifts.
   *          If this map is null or empty then a copy of the original is
   *          returned. If this map contains any points that do not coincide
   *          with data points in the original curve, it is ignored.
   * @return A discount curve with shifted points.
   */
  public static DiscountCurve getMultipleShiftedDataPointCurve(DiscountCurve original, Map<Integer, Double> shifts) {
    if (shifts == null || shifts.isEmpty())
      return new DiscountCurve(original.getData(), original.getInterpolator());
    final SortedMap<Double, Double> data = new TreeMap<Double, Double>();
    data.putAll(original.getData());
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      if (shifts.containsKey(i)) {
        data.put(entry.getKey(), entry.getValue() + shifts.get(i));
      }
      i++;
    }
    return new DiscountCurve(data, original.getInterpolator());
  }

  /**
   * Shifts the curve at a various times to maturity.
   * 
   * @param original
   *          The original discount curve.
   * @param shifts
   *          A map containing the times to maturity and the value of the
   *          shifts. If this map is null or empty then a copy of the original
   *          curve is returned.
   * @return A discount curve with various points shifted.
   */

  public static DiscountCurve getMultipleShiftedPointCurve(DiscountCurve original, Map<Double, Double> shifts) {
    if (shifts == null || shifts.isEmpty())
      return new DiscountCurve(original.getData(), original.getInterpolator());
    final Map<Double, Double> data = new HashMap<Double, Double>(original.getData());
    for (final Map.Entry<Double, Double> entry : shifts.entrySet()) {
      if (data.containsKey(entry.getKey())) {
        data.put(entry.getKey(), entry.getValue() + data.get(entry.getKey()));
      } else {
        try {
          final Double interpolatedPoint = original.getInterestRate(entry.getKey());
          data.put(entry.getKey(), interpolatedPoint + entry.getValue());
        } catch (final InterpolationException e) {
          // TODO logging
          return null;
        }
      }
    }
    return new DiscountCurve(data, original.getInterpolator());
  }
}
