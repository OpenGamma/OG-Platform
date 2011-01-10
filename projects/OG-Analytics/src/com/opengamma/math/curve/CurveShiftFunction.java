/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

/**
 *
 * @param <T> The type of the curve
 */
public interface CurveShiftFunction<T extends Curve<Double, Double>> {

  T evaluate(final T curve, final double shift);

  T evaluate(final T curve, final double shift, String newName);

  T evaluate(T curve, double x, double shift);

  T evaluate(T curve, double x, double shift, String newName);

  T evaluate(T curve, double[] xShift, double[] yShift);

  T evaluate(T curve, double[] xShift, double[] yShift, String newName);
}
