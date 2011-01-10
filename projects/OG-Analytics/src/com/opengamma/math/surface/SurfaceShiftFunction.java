/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

/**
 * 
 * @param <T> The type of the curve
 */
public interface SurfaceShiftFunction<T extends Surface<Double, Double, Double>> {

  T evaluate(T surface, double shift);

  T evaluate(T surface, double shift, String newName);

  T evaluate(T surface, double x, double y, double shift);

  T evaluate(T surface, double x, double y, double shift, String newName);

  T evaluate(T surface, double[] xShift, double[] yShift, double[] shift);

  T evaluate(T surface, double[] xShift, double[] yShift, double[] shift, String newName);
}
