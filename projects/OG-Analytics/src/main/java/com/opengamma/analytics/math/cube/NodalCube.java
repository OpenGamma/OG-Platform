/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.Quadruple;
import com.opengamma.util.tuple.Triple;

/**
 * A cube that is defined by a set of nodal points (i.e. <i>(x, y, z)</i> data). Any attempt to find a <i>z</i> value 
 * for which there is no <i>(x, y)</i> nodal point will result in failure.
 * @param <X> type of <i>x</i> data points
 * @param <Y> type of <i>y</i> data points
 * @param <Z> type of <i>z</i> data points
 * @param <V> type of <i>v</i> data points
 */
public class NodalCube<X, Y, Z, V> extends Cube<X, Y, Z, V> {

  private final X[] _xData;
  private final Y[] _yData;
  private final Z[] _zData;
  private final V[] _vData;
  private final String _name;

  /**
   * @param points A collection of data points, not null
   * @param name A cube name   
   */
  public NodalCube(Collection<Quadruple<X, Y, Z, V>> points, String name) {
    List<X> xData = newArrayList();
    List<Y> yData = newArrayList();
    List<Z> zData = newArrayList();
    List<V> vData = newArrayList();

    for (Quadruple<X, Y, Z, V> point : points) {
      xData.add(point.getFirst());
      yData.add(point.getSecond());
      zData.add(point.getThird());
      vData.add(point.getFourth());
    }
    _xData = (X[]) xData.toArray();
    _yData = (Y[]) yData.toArray();
    _zData = (Z[]) zData.toArray();
    _vData = (V[]) vData.toArray();
    _name = name;
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData An array of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @param <X> type of <i>x</i> data points
   * @param <Y> type of <i>y</i> data points
   * @param <Z> type of <i>z</i> data points
   * @param <V> type of <i>v</i> data points
   * @return A nodal cube with automatically-generated name
   */
  public static <X, Y, Z, V> NodalCube from(final X[] xData, final Y[] yData, final Z[] zData, final V[] vData) {
    return new NodalCube(xData, yData, zData, vData);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData An array of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @param <X> type of <i>x</i> data points
   * @param <Y> type of <i>y</i> data points
   * @param <Z> type of <i>z</i> data points
   * @param <V> type of <i>v</i> data points
   * @return A nodal cube with automatically-generated name
   */
  public static <X, Y, Z, V> NodalCube from(final List<X> xData, final List<Y> yData, final List<Z> zData, final List<V> vData) {
    return new NodalCube(xData, yData, zData, vData);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData An array of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @param <X> type of <i>x</i> data points
   * @param <Y> type of <i>y</i> data points
   * @param <Z> type of <i>z</i> data points
   * @param <V> type of <i>v</i> data points
   * @param name The name of the cube
   * @return A nodal cube with automatically-generated name
   */
  public static <X, Y, Z, V> NodalCube from(final X[] xData, final Y[] yData, final Z[] zData, final V[] vData, final String name) {
    return new NodalCube(xData, yData, zData, vData, name);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData An array of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @param <X> type of <i>x</i> data points
   * @param <Y> type of <i>y</i> data points
   * @param <Z> type of <i>z</i> data points
   * @param <V> type of <i>v</i> data points
   * @param name The name of the cube
   * @return A nodal cube with automatically-generated name
   */
  public static <X, Y, Z, V> NodalCube from(final List<X> xData, final List<Y> yData, final List<Z> zData, final List<V> vData, final String name) {
    return new NodalCube(xData, yData, zData, vData, name);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData An array of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   */
  public NodalCube(final X[] xData, final Y[] yData, final Z[] zData, final V[] vData) {
    this(xData, yData, zData, vData, null);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData An array of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   */
  public NodalCube(final List<X> xData, final List<Y> yData, final List<Z> zData, final List<V> vData) {
    this(xData, yData, zData, vData, null);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData An array of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the cube
   */
  public NodalCube(final X[] xData, final Y[] yData, final Z[] zData, final V[] vData,
      final String name) {
    _xData = xData;
    _yData = yData;
    _zData = zData;
    _vData = vData;
    _name = name;
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param vData An array of <i>v</i> data points, not null, contains same number of entries as <i>x</i>
   * @param name The name of the cube
   */
  public NodalCube(final List<X> xData, final List<Y> yData, final List<Z> zData, final List<V> vData,
      final String name) {
    _xData = (X[]) xData.toArray();
    _yData = (Y[]) yData.toArray();
    _zData = (Z[]) zData.toArray();
    _vData = (V[]) vData.toArray();
    _name = name;
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the <i>(x, y, z)</i> value is not a nodal point
   */
  @Override
  public V getValue(final X x, final Y y, final Z z) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    Validate.notNull(y, "z");
    final X[] xArray = getXData();
    final Y[] yArray = getYData();
    final Z[] zArray = getZData();
    final int n = size();
    for (int i = 0; i < n; i++) {
      if (xArray[i].equals(x) && yArray[i].equals(y) && zArray[i].equals(z)) {
        return getValues()[i];
      }
    }
    throw new IllegalArgumentException("No x-y-z data in cube for (" + x + ", " + y + ", " + z + ")");
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the <i>(x, y, z)</i> value is not a nodal point
   */
  @Override
  public V getValue(final Triple<X, Y, Z> xyz) {
    Validate.notNull(xyz, "x-y-z triple");
    return getValue(xyz.getFirst(), xyz.getSecond(), xyz.getThird());
  }

  @Override
  public X[] getXData() {
    return _xData;
  }

  @Override
  public Y[] getYData() {
    return _yData;
  }

  @Override
  public Z[] getZData() {
    return _zData;
  }

  @Override
  public V[] getValues() {
    return _vData;
  }

  @Override
  public int size() {
    return _vData.length;
  }

  public String getName() {
    return _name;
  }
}
