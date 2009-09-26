/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.financial.model.volatility.VolatilityModel;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.plot.RenderVisitor;
import com.opengamma.plot.Renderable;
import com.opengamma.util.FirstThenSecondPairComparator;
import com.opengamma.util.Pair;

/**
 * 
 * A volatility surface contains volatilities for pairs of values (x, y) (e.g.
 * time and strike).
 * 
 * @author emcleod
 */

public class VolatilitySurface implements VolatilityModel<Double, Double>, Renderable {
  private final SortedMap<Pair<Double, Double>, Double> _data;
  private final Interpolator2D _interpolator;

  /**
   * 
   * @param data
   *          A map containing pairs of (x, y) values and volatilities as
   *          decimals (i.e. 20% = 0.2).
   * @param interpolator
   *          An interpolator to get volatilities for an (x, y) pair that falls
   *          in between nodes. This can be null.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty.
   */
  public VolatilitySurface(Map<Pair<Double, Double>, Double> data, Interpolator2D interpolator) {
    if (data == null)
      throw new IllegalArgumentException("Data map was null");
    if (data.isEmpty())
      throw new IllegalArgumentException("Data map was empty");
    SortedMap<Pair<Double, Double>, Double> sorted = new TreeMap<Pair<Double, Double>, Double>(new FirstThenSecondPairComparator<Double, Double>());
    sorted.putAll(data);
    _data = Collections.<Pair<Double, Double>, Double> unmodifiableSortedMap(sorted);
    _interpolator = interpolator;
  }

  /**
   * 
   * @return The data sorted by (x, y) pair. The ordering is first x, then y (
   *         {@link #FirstThenSecondPairComparator
   *         FirstThenSecondPairComparator}).
   */
  public SortedMap<Pair<Double, Double>, Double> getData() {
    return _data;
  }

  /**
   * 
   * @return The interpolator for this surface.
   */
  public Interpolator2D getInterpolator() {
    return _interpolator;
  }

  /**
   * 
   * @return The volatility for (x, y).
   */
  @Override
  public double getVolatility(Double x, Double y) {
    return _interpolator.interpolate(_data, new Pair<Double, Double>(x, y)).getResult();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_data == null) ? 0 : _data.hashCode());
    result = prime * result + ((_interpolator == null) ? 0 : _interpolator.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    VolatilitySurface other = (VolatilitySurface) obj;
    if (_data == null) {
      if (other._data != null)
        return false;
    } else if (!_data.equals(other._data))
      return false;
    if (_interpolator == null) {
      if (other._interpolator != null)
        return false;
    } else if (!_interpolator.equals(other._interpolator))
      return false;
    return true;
  }

  @Override
  public <T> T accept(RenderVisitor<T> visitor) {
    return visitor.visitVolatilitySurface(this);
  }
}
