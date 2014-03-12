/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.Triple;

/**
 * A cube that is defined by a set of nodal points (i.e. <i>(x, y, z)</i> data). Any attempt to find a <i>z</i> value 
 * for which there is no <i>(x, y)</i> nodal point will result in failure.
 */
public class NodalDoublesCube extends DoublesCube {

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData An array of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @return A nodal cube with automatically-generated name
   */
  public static NodalDoublesCube from(final double[] xData, final double[] yData, final double[] zData, final double[] vData) {
    return new NodalDoublesCube(xData, yData, zData, vData);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData An array of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @return A nodal cube with automatically-generated name
   */
  public static NodalDoublesCube from(final Double[] xData, final Double[] yData, final Double[] zData, final Double[] vData) {
    return new NodalDoublesCube(xData, yData, zData, vData);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData A list of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @return A nodal cube with automatically-generated name
   */
  public static NodalDoublesCube from(final List<Double> xData, final List<Double> yData, final List<Double> zData, final List<Double> vData) {
    return new NodalDoublesCube(xData, yData, zData, vData);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData A list of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the cube
   * @return A nodal cube with automatically-generated name
   */
  public static NodalDoublesCube from(final double[] xData, final double[] yData, final double[] zData, final double[] vData, final String name) {
    return new NodalDoublesCube(xData, yData, zData, vData, name);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData A list of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the cube
   * @return A nodal cube with automatically-generated name
   */
  public static NodalDoublesCube from(final Double[] xData, final Double[] yData, final Double[] zData, final Double[] vData, final String name) {
    return new NodalDoublesCube(xData, yData, zData, vData, name);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData A list of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the cube
   * @return A nodal cube with automatically-generated name
   */
  public static NodalDoublesCube from(final List<Double> xData, final List<Double> yData, final List<Double> zData, final List<Double> vData, final String name) {
    return new NodalDoublesCube(xData, yData, zData, vData, name);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData A list of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   */
  public NodalDoublesCube(final double[] xData, final double[] yData, final double[] zData, final double[] vData) {
    super(xData, yData, zData, vData);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData A list of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   */
  public NodalDoublesCube(final Double[] xData, final Double[] yData, final Double[] zData, final Double[] vData) {
    super(xData, yData, zData, vData);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData A list of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   */
  public NodalDoublesCube(final List<Double> xData, final List<Double> yData, final List<Double> zData, final List<Double> vData) {
    super(xData, yData, zData, vData);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData A list of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the cube
   */
  public NodalDoublesCube(final double[] xData, final double[] yData, final double[] zData, final double[] vData, final String name) {
    super(xData, yData, zData, vData, name);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData A list of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the cube
   */
  public NodalDoublesCube(final Double[] xData, final Double[] yData, final Double[] zData, final Double[] vData, final String name) {
    super(xData, yData, zData, vData, name);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData A list of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the cube
   */
  public NodalDoublesCube(final List<Double> xData,
      final List<Double> yData,
      final List<Double> zData,
      final List<Double> vData,
      final String name) {
    super(xData, yData, zData, vData, name);
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the <i>(x, y, z)</i> value is not a nodal point
   */
  @Override
  public Double getValue(final Double x, final Double y, final Double z) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    Validate.notNull(y, "z");
    final double[] xArray = getXDataAsPrimitive();
    final double[] yArray = getYDataAsPrimitive();
    final double[] zArray = getZDataAsPrimitive();
    final int n = size();
    for (int i = 0; i < n; i++) {
      if (Double.doubleToLongBits(xArray[i]) == Double.doubleToLongBits(x) && Double.doubleToLongBits(yArray[i]) == Double.doubleToLongBits(y) &&
          Double.doubleToLongBits(zArray[i]) == Double.doubleToLongBits(z)) {
        return getValuesAsPrimitive()[i];
      }
    }
    throw new IllegalArgumentException("No x-y-z data in cube for (" + x + ", " + y + ", " + z + ")");
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the <i>(x, y, z)</i> value is not a nodal point
   */
  @Override
  public Double getValue(final Triple<Double, Double, Double> xyz) {
    Validate.notNull(xyz, "x-y-z triple");
    return getValue(xyz.getFirst(), xyz.getSecond(), xyz.getThird());
  }

}
