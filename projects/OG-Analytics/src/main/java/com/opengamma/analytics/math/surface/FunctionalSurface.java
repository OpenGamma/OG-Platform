/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * A surface backed by a {@link Function2D}. This could be regarded as a generalisation of a {@link FunctionalDoublesSurface}
 * as there is no restriction on the types of the ordinates. 
 * @param <U> Type of the arguments (i.e. x & y)
 * @param <V> Type of the surface (z)
 */
public class FunctionalSurface<U, V> extends Surface<U, U, V> {

  private final Function2D<U, V> _function;

  /**
   * Create a FunctionalSurface
   * @param function a a {@link Function2D} that describes the surface.
   */
  public FunctionalSurface(final Function2D<U, V> function) {
    ArgumentChecker.notNull(function, "function");
    _function = function;
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public U[] getXData() {
    throw new UnsupportedOperationException("Cannot get x data - this surface is defined by a function");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public U[] getYData() {
    throw new UnsupportedOperationException("Cannot get y data - this surface is defined by a function");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public V[] getZData() {
    throw new UnsupportedOperationException("Cannot get z data - this surface is defined by a function");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public int size() {
    throw new UnsupportedOperationException("Cannot get size - this surface is defined by a function");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public V getZValue(final U x, final U y) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    return _function.evaluate(x, y);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public V getZValue(final Pair<U, U> xy) {
    Validate.notNull(xy, "x-y pair");
    return _function.evaluate(xy.getFirst(), xy.getSecond());
  }

}
