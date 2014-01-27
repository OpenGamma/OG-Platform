/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.cube;

import com.opengamma.analytics.math.cube.Cube;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.util.tuple.Triple;

/**
 * Defines a general cube <i>(x, y, z, value)</i> class. The data data be of any type. The cubes are named; if a name is not provided then a unique ID will
 * be generated.
 * @param <S> The type of the x data
 * @param <T> The type of the y data
 * @param <U> The type of the z data
 */
public class StaticDoublesCube<S, T, U> extends Cube<S, T, U, Double> {

  private VolatilityCubeData<S, T, U> _cubeData;

  public StaticDoublesCube(VolatilityCubeData cubeData) {
    _cubeData = cubeData;
  }

  @Override
  public S[] getXData() {
    return _cubeData.getXs();
  }

  @Override
  public T[] getYData() {
    return _cubeData.getYs();
  }

  @Override
  public U[] getZData() {
    return _cubeData.getZs();
  }

  @Override
  public Double[] getValues() {
    return _cubeData.getVs();
  }

  @Override
  public int size() {
    return _cubeData.size();
  }

  @Override
  public Double getValue(S x, T y, U z) {
    return _cubeData.getVolatility(x, y, z);
  }

  @Override
  public Double getValue(Triple<S, T, U> xyz) {
    return _cubeData.getVolatility(xyz.getFirst(), xyz.getSecond(), xyz.getThird());
  }
}
