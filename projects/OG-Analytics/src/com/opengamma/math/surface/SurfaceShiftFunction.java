/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

/**
 * Given a surface and some form of shift, return a shifted surface (where the shift can be defined in several ways).
 * @param <T> The type of the surface
 */
public interface SurfaceShiftFunction<T extends Surface<Double, Double, Double>> {

  /**
   * Returns a new surface shifted by a constant <i>z</i> amount (i.e. a parallel shift). The name of the new surface will be automatically generated.
   * @param surface The surface, not null
   * @param shift The amount to shift
   * @return The shifted surface
   */
  T evaluate(T surface, double shift);

  /**
   * Returns a new surface shifted by a constant <i>z</i> amount (i.e. a parallel shift). The name of the new surface is supplied.
   * @param surface The surface, not null
   * @param shift The amount to shift
   * @param newName The name for the shifted surface
   * @return The shifted surface
   */
  T evaluate(T surface, double shift, String newName);

  /**
   * Returns a new surface shifted by an amount <i>z</i> at the point <i>(x, y)</i>. The name of the new surface will be automatically generated.
   * @param surface The surface, not null
   * @param x The <i>x</i> location of the shift
   * @param y The <i>y</i> location of the shift
   * @param shift The amount to shift
   * @return The shifted surface
   */
  T evaluate(T surface, double x, double y, double shift);

  /**
   * Returns a new surface shifted by an amount <i>z</i> at the point <i>(x, y)</i>. The name of the new surface is supplied.
   * @param surface The surface, not null
   * @param x The <i>x</i> location of the shift
   * @param y The <i>y</i> location of the shift
   * @param shift The amount to shift
   * @param newName The name for the shifted surface
   * @return The shifted surface
   */
  T evaluate(T surface, double x, double y, double shift, String newName);

  /**
   * Returns a new surface shifted by amounts <i>z</i> at points <i>(x, y)</i>. The name of the new surface will
   * be automatically generated.
   * @param surface The surface, not null
   * @param xShift An array of <i>x</i> values of the points to be shifted, not null. If this array is empty a new surface identical to the original 
   * will be returned
   * @param yShift An array of <i>x</i> values of the points to be shifted, not null. Must contain the same number of points as the array of <i>x</i> values
   * @param shift The amounts to shift the surface at each <i>(x, y)</i> value, not null, must contain the same number of points as the array of <i>x</i> values
   * @return A shifted surface
   */
  T evaluate(T surface, double[] xShift, double[] yShift, double[] shift);

  /**
   * Returns a new surface shifted by amounts <i>z</i> at points <i>(x, y)</i>. The name of the new surface is supplied.
   * @param surface The surface, not null
   * @param xShift An array of <i>x</i> values of the points to be shifted, not null. If this array is empty a new surface identical to the original 
   * will be returned
   * @param yShift An array of <i>x</i> values of the points to be shifted, not null. Must contain the same number of points as the array of <i>x</i> values
   * @param shift The amounts to shift the surface at each <i>(x, y)</i> value, not null, must contain the same number of points as the array of <i>x</i> values
   * @param newName The name of the shifted surface
   * @return A shifted surface
   */
  T evaluate(T surface, double[] xShift, double[] yShift, double[] shift, String newName);
}
