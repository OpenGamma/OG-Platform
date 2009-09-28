/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.financial.model.interestrate.InterestRateModel;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.plot.RenderVisitor;
import com.opengamma.plot.Renderable;

/**
 * A DiscountCurve contains discount factors <i>e<sup>-r(t)t</sup></i> (where
 * <i>t</i> is the maturity in years and <i>r(t)</i> is the interest rate at
 * maturity <i>t</i>).
 * 
 * @author emcleod
 */

public class DiscountCurve implements InterestRateModel<Double>, Renderable {
  private final SortedMap<Double, Double> _data;
  private final Interpolator1D _interpolator;

  /**
   * 
   * @param data
   *          A map containing pairs of maturities in years and interest rates
   *          as decimals (i.e. 3% = 0.03).
   * @param interpolator
   *          An interpolator to get interest rates / discount factors for
   *          maturities that fall in between nodes. This can be null.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty, or if it contains a
   *           negative time to maturity.
   */
  public DiscountCurve(final Map<Double, Double> data, final Interpolator1D interpolator) {
    if (data == null)
      throw new IllegalArgumentException("Data map was null");
    if (data.isEmpty())
      throw new IllegalArgumentException("Data map was empty");
    final SortedMap<Double, Double> sorted = new TreeMap<Double, Double>(data);
    if (sorted.firstKey() < 0)
      throw new IllegalArgumentException("Cannot have negative time in a discount curve");
    _data = Collections.<Double, Double> unmodifiableSortedMap(sorted);
    _interpolator = interpolator;
  }

  /**
   * 
   * @return The data sorted by maturity.
   */
  public SortedMap<Double, Double> getData() {
    return _data;
  }

  /**
   * 
   * @return The interpolator for this curve.
   */
  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  /**
   * @return The interest rate for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  @Override
  public double getInterestRate(final Double t) {
    if (t < 0)
      throw new IllegalArgumentException("Cannot have a negative time in a DiscountCurve: provided " + t);
    return _interpolator.interpolate(_data, t).getResult();
  }

  /**
   * @return The discount factor for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  public double getDiscountFactor(final Double t) {
    if (t < 0)
      throw new IllegalArgumentException("Cannot have a negative time in a DiscountCurve: provided " + t);
    return Math.exp(-getInterestRate(t) * t);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_data == null ? 0 : _data.hashCode());
    result = prime * result + (_interpolator == null ? 0 : _interpolator.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final DiscountCurve other = (DiscountCurve) obj;
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
  
  public <T> T accept(RenderVisitor<T> visitor) {
    return visitor.visitDiscountCurve(this);
  }
}
