/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * A surface that is defined by a set of nodal points (i.e. <i>(x, y, z)</i> data). Any attempt to find a <i>z</i> value
 * for which there is no <i>(x, y)</i> nodal point will result in failure.
 * @param <T> The type of the x-axis data
 * @param <U> The type of the y-axis data
 * @param <V> The type of the z-axis data
 */
public class NodalObjectsSurface<T, U, V> extends ObjectsSurface<T, U, V> {

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param <T> The type of the x-axis data
   * @param <U> The type of the y-axis data
   * @param <V> The type of the z-axis data
   * @return A nodal surface with automatically-generated name
   */
  public static <T, U, V> NodalObjectsSurface<T, U, V> from(final T[] xData, final U[] yData, final V[] zData) {
    return new NodalObjectsSurface<>(xData, yData, zData);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param <T> The type of the x-axis data
   * @param <U> The type of the y-axis data
   * @param <V> The type of the z-axis data
   * @param name The name of the surface
   * @return A nodal surface with automatically-generated name
   */
  public static <T, U, V> NodalObjectsSurface<T, U, V> from(final T[] xData, final U[] yData, final V[] zData, final String name) {
    return new NodalObjectsSurface<>(xData, yData, zData, name);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   */
  public NodalObjectsSurface(final T[] xData, final U[] yData, final V[] zData) {
    super(xData, yData, zData);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the surface
   */
  public NodalObjectsSurface(final T[] xData, final U[] yData, final V[] zData, final String name) {
    super(xData, yData, zData, name);
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the <i>(x, y)</i> value is not a nodal point
   */
  @Override
  public V getZValue(final T x, final U y) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.notNull(y, "y");
    final T[] xArray = getXData();
    final U[] yArray = getYData();
    //    final int index = Arrays.binarySearch(xArray, x);
    //    if (index < 0) {
    //      throw new IllegalArgumentException("No x-y-z data in surface for (" + x + ", " + y + ")");
    //    }
    //    final U[] yArray = getYData();
    //    if (yArray[index].equals(y)) {
    //      final V[] zArray = getZData();
    //      return zArray[index];
    //    }

    final int arrayLength = xArray.length;
    for (int i = 0; i < arrayLength; ++i) {
      if (x.equals(xArray[i]) && y.equals(yArray[i])) {
        return getZData()[i];
      }
    }

    throw new IllegalArgumentException("No x-y-z data in surface for (" + x + ", " + y + ")");
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the <i>(x, y)</i> value is not a nodal point
   */
  @Override
  public V getZValue(final Pair<T, U> xy) {
    ArgumentChecker.notNull(xy, "x-y pair");
    return getZValue(xy.getFirst(), xy.getSecond());
  }

}
