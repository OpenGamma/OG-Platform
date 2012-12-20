/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;


/**
 * Given a curve and some form of shift, return a shifted curve (where the shift can be defined in several ways).
 * @param <T> The type of the curve to shift
 */
public interface CurveShiftFunction<T extends Curve<Double, Double>> {

  /**
   * Returns a new curve shifted by a constant <i>y</i> amount (i.e. a shift parallel to the <i>x</i> axis). The name of the new curve will
   * be automatically generated.
   * @param curve The curve, not null
   * @param shift The amount to shift
   * @return A shifted curve
   */
  T evaluate(final T curve, final double shift);

  /**
   * Returns a new curve shifted by a constant <i>y</i> amount (i.e. a shift parallel to the <i>x</i> axis). The name of the new curve is 
   * supplied.
   * @param curve The curve, not null
   * @param shift The amount to shift
   * @param newName The name for the shifted curve
   * @return A shifted curve
   */
  T evaluate(final T curve, final double shift, String newName);

  /**
   * Returns a new curve shifted by an amount <i>y</i> at value <i>x</i>. The name of the new curve will
   * be automatically generated.
   * @param curve The curve, not null
   * @param x The <i>x</i> value of the point to be shifted
   * @param shift The amount to shift
   * @return A shifted curve
   */
  T evaluate(T curve, double x, double shift);

  /**
   * Returns a new curve shifted by an amount <i>y</i> at value <i>x</i>. The name of the new curve is 
   * supplied.
   * @param curve The curve, not null
   * @param x The <i>x</i> value of the of the point to be shifted
   * @param shift The amount to shift
   * @param newName The name for the shifted curve
   * @return A shifted curve
   */
  T evaluate(T curve, double x, double shift, String newName);

  /**
   * Returns a new curve shifted by amounts <i>y</i> at values <i>x</i>. The name of the new curve will
   * be automatically generated.
   * @param curve The curve, not null
   * @param xShift An array of <i>x</i> values of the points to be shifted, not null. If this array is empty a new curve identical to the original 
   * will be returned
   * @param yShift The amounts to shift the curve at each <i>x</i> value, not null, must contain the same number of points as the array of <i>x</i> values 
   * @return A shifted curve
   */
  T evaluate(T curve, double[] xShift, double[] yShift);

  /**
   * Returns a new curve shifted by amounts <i>y</i> values <i>x</i>. The name of the new curve is 
   * supplied.
   * @param curve The curve, not null
   * @param xShift An array of <i>x</i> values of the points to be shifted, not null. If this array is empty a new curve identical to the original 
   * will be returned
   * @param yShift The amounts to shift the curve at each <i>x</i> value, not null, must contain the same number of points as the array of <i>x</i> values
   * @param newName The name for the shifted curve
   * @return A shifted curve
   */
  T evaluate(T curve, double[] xShift, double[] yShift, String newName);
}
