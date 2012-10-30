/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * A surface that is defined by a set of nodal points (i.e. <i>(x, y, z)</i> data). Any attempt to find a <i>z</i> value 
 * for which there is no <i>(x, y)</i> nodal point will result in failure.
 */
public class NodalDoublesSurface extends DoublesSurface {

  /**
   * @param xData An array of <i>x</i> data points, not null 
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final double[] xData, final double[] yData, final double[] zData) {
    return new NodalDoublesSurface(xData, yData, zData);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null 
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final Double[] xData, final Double[] yData, final Double[] zData) {
    return new NodalDoublesSurface(xData, yData, zData);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final List<Double> xData, final List<Double> yData, final List<Double> zData) {
    return new NodalDoublesSurface(xData, yData, zData);
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null 
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final DoublesPair[] xyData, final Double[] zData) {
    return new NodalDoublesSurface(xyData, zData);
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final DoublesPair[] xyData, final double[] zData) {
    return new NodalDoublesSurface(xyData, zData);
  }

  /**
   * @param xyData A list of <i>x-y</i> data points, not null
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final List<DoublesPair> xyData, final List<Double> zData) {
    return new NodalDoublesSurface(xyData, zData);
  }

  /**
   * @param data A map of <i>x-y</i> data points to <i>z</i> data points, not null
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final Map<DoublesPair, Double> data) {
    return new NodalDoublesSurface(data);
  }

  /**
   * @param xyzData A list of <i>x-y-z</i> data points, not null
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final List<Triple<Double, Double, Double>> xyzData) {
    return new NodalDoublesSurface(xyzData);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null 
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the surface
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final double[] xData, final double[] yData, final double[] zData, final String name) {
    return new NodalDoublesSurface(xData, yData, zData, name);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null 
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the surface
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final Double[] xData, final Double[] yData, final Double[] zData, final String name) {
    return new NodalDoublesSurface(xData, yData, zData, name);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the surface
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final List<Double> xData, final List<Double> yData, final List<Double> zData, final String name) {
    return new NodalDoublesSurface(xData, yData, zData, name);
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null 
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @param name The name of the surface
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final DoublesPair[] xyData, final double[] zData, final String name) {
    return new NodalDoublesSurface(xyData, zData, name);
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @param name The name of the surface
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final DoublesPair[] xyData, final Double[] zData, final String name) {
    return new NodalDoublesSurface(xyData, zData, name);
  }

  /**
   * @param xyData A list of <i>x-y</i> data points, not null
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @param name The name of the surface
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final List<DoublesPair> xyData, final List<Double> zData, final String name) {
    return new NodalDoublesSurface(xyData, zData, name);
  }

  /**
   * @param xyzData A map of <i>x-y</i> data points to <i>z</i> data points, not null
   * @param name The name of the surface
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final Map<DoublesPair, Double> xyzData, final String name) {
    return new NodalDoublesSurface(xyzData, name);
  }

  /**
   * @param xyzData A list of <i>x-y-z</i> data points, not null
   * @param name The name of the surface
   * @return A nodal surface with automatically-generated name
   */
  public static NodalDoublesSurface from(final List<Triple<Double, Double, Double>> xyzData, final String name) {
    return new NodalDoublesSurface(xyzData, name);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null 
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   */
  public NodalDoublesSurface(final double[] xData, final double[] yData, final double[] zData) {
    super(xData, yData, zData);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null 
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   */
  public NodalDoublesSurface(final Double[] xData, final Double[] yData, final Double[] zData) {
    super(xData, yData, zData);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   */
  public NodalDoublesSurface(final List<Double> xData, final List<Double> yData, final List<Double> zData) {
    super(xData, yData, zData);
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null 
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   */
  public NodalDoublesSurface(final DoublesPair[] xyData, final double[] zData) {
    super(xyData, zData);
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   */
  public NodalDoublesSurface(final DoublesPair[] xyData, final Double[] zData) {
    super(xyData, zData);
  }

  /**
   * @param xyData A list of <i>x-y</i> data points, not null
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   */
  public NodalDoublesSurface(final List<DoublesPair> xyData, final List<Double> zData) {
    super(xyData, zData);
  }

  /**
   * @param xyzData A map of <i>x-y</i> data points to <i>z</i> data points, not null
   */
  public NodalDoublesSurface(final Map<DoublesPair, Double> xyzData) {
    super(xyzData);
  }

  /**
   * @param xyzData A list of <i>x-y-z</i> data points, not null
   */
  public NodalDoublesSurface(final List<Triple<Double, Double, Double>> xyzData) {
    super(xyzData);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null 
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the surface
   */
  public NodalDoublesSurface(final double[] xData, final double[] yData, final double[] zData, final String name) {
    super(xData, yData, zData, name);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null 
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the surface
   */
  public NodalDoublesSurface(final Double[] xData, final Double[] yData, final Double[] zData, final String name) {
    super(xData, yData, zData, name);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the surface
   */
  public NodalDoublesSurface(final List<Double> xData, final List<Double> yData, final List<Double> zData, final String name) {
    super(xData, yData, zData, name);
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null 
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @param name The name of the surface
   */
  public NodalDoublesSurface(final DoublesPair[] xyData, final double[] zData, final String name) {
    super(xyData, zData, name);
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @param name The name of the surface
   */
  public NodalDoublesSurface(final DoublesPair[] xyData, final Double[] zData, final String name) {
    super(xyData, zData, name);
  }

  /**
   * @param xyData A list of <i>x-y</i> data points, not null
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @param name The name of the surface
   */
  public NodalDoublesSurface(final List<DoublesPair> xyData, final List<Double> zData, final String name) {
    super(xyData, zData, name);
  }

  /**
   * @param xyzData A map of <i>x-y</i> data points to <i>z</i> data points, not null
   * @param name The name of the surface
   */
  public NodalDoublesSurface(final Map<DoublesPair, Double> xyzData, final String name) {
    super(xyzData, name);
  }

  /**
   * @param xyzData A list of <i>x-y-z</i> data points, not null
   * @param name The name of the surface
   */
  public NodalDoublesSurface(final List<Triple<Double, Double, Double>> xyzData, final String name) {
    super(xyzData, name);
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the <i>(x, y)</i> value is not a nodal point 
   */
  @Override
  public Double getZValue(final Double x, final Double y) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    final double[] xArray = getXDataAsPrimitive();
    final double[] yArray = getYDataAsPrimitive();
    final int n = size();
    for (int i = 0; i < n; i++) {
      if (Double.doubleToLongBits(xArray[i]) == Double.doubleToLongBits(x)) {
        if (Double.doubleToLongBits(yArray[i]) == Double.doubleToLongBits(y)) {
          return getZDataAsPrimitive()[i];
        }
      }
    }
    throw new IllegalArgumentException("No x-y data in surface for (" + x + ", " + y + ")");
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the <i>(x, y)</i> value is not a nodal point 
   */
  @Override
  public Double getZValue(final Pair<Double, Double> xy) {
    Validate.notNull(xy, "x-y pair");
    return getZValue(xy.getFirst(), xy.getSecond());
  }

}
