/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

/**
 * Given a cube and some form of shift, return a shifted cube (where the shift can be defined in several ways).
 * @param <T> The type of the cube
 */
public interface CubeShiftFunction<T extends Cube<Double, Double, Double, Double>> {

  /**
   * Returns a new cube shifted by a constant <i>v</i> amount (i.e. a parallel shift). The name of the new cube will be automatically generated.
   * @param cube The cube, not null
   * @param shift The amount to shift
   * @return The shifted cube
   */
  T evaluate(T cube, double shift);

  /**
   * Returns a new cube shifted by a constant <i>v</i> amount (i.e. a parallel shift). The name of the new cube is supplied.
   * @param cube The cube, not null
   * @param shift The amount to shift
   * @param newName The name for the shifted cube
   * @return The shifted cube
   */
  T evaluate(T cube, double shift, String newName);

  /**
   * Returns a new cube shifted by an amount <i>v</i> at the point <i>(x, y, z)</i>. The name of the new cube will be automatically generated.
   * @param cube The cube, not null
   * @param x The <i>x</i> location of the shift
   * @param y The <i>y</i> location of the shift
   * @param z The <i>y</i> location of the shift
   * @param shift The amount to shift
   * @return The shifted cube
   */
  T evaluate(T cube, double x, double y, double z, double shift);

  /**
   * Returns a new cube shifted by an amount <i>v</i> at the point <i>(x, y, z)</i>. The name of the new cube is supplied.
   * @param cube The cube, not null
   * @param x The <i>x</i> location of the shift
   * @param y The <i>y</i> location of the shift
   * @param z The <i>y</i> location of the shift
   * @param shift The amount to shift
   * @param newName The name for the shifted cube
   * @return The shifted cube
   */
  T evaluate(T cube, double x, double y, double z, double shift, String newName);

  /**
   * Returns a new cube shifted by amounts <i>v</i> at points <i>(x, y, z)</i>. The name of the new cube will
   * be automatically generated.
   * @param cube The cube, not null
   * @param xShift An array of <i>x</i> values of the points to be shifted, not null. If this array is empty a new cube identical to the original 
   * will be returned
   * @param yShift An array of <i>y</i> values of the points to be shifted, not null. Must contain the same number of points as the array of <i>x</i> values
   * @param zShift An array of <i>z</i> values of the points to be shifted, not null. Must contain the same number of points as the array of <i>x</i> values
   * @param shift The amounts to shift the cube at each <i>(x, y)</i> value, not null, must contain the same number of points as the array of <i>x</i> values
   * @return A shifted cube
   */
  T evaluate(T cube, double[] xShift, double[] yShift, double[] zShift, double[] shift);

  /**
   * Returns a new cube shifted by amounts <i>v</i> at points <i>(x, y, z)</i>. The name of the new cube is supplied.
   * @param cube The cube, not null
   * @param xShift An array of <i>x</i> values of the points to be shifted, not null. If this array is empty a new cube identical to the original 
   * will be returned
   * @param yShift An array of <i>y</i> values of the points to be shifted, not null. Must contain the same number of points as the array of <i>x</i> values
   * @param zShift An array of <i>z</i> values of the points to be shifted, not null. Must contain the same number of points as the array of <i>x</i> values
   * @param shift The amounts to shift the cube at each <i>(x, y)</i> value, not null, must contain the same number of points as the array of <i>x</i> values
   * @param newName The name of the shifted cube
   * @return A shifted cube
   */
  T evaluate(T cube, double[] xShift, double[] yShift, double[] zShift, double[] shift, String newName);
}
