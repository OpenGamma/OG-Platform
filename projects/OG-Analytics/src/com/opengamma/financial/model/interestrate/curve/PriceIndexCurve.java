/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.curve.Curve;

/**
 * A curve containing the (estimated) price index at different maturities. The maturities can be (slightly) negative as the price index are known only with a certain lag.
 * The price index for a given month is on the first of the month point.
 */
public class PriceIndexCurve {

  /**
   * The price index curve.
   */
  private final Curve<Double, Double> _curve;

  /**
   * Constructor from a curve object.
   * @param curve The curve.
   */
  public PriceIndexCurve(final Curve<Double, Double> curve) {
    Validate.notNull(curve, "curve");
    _curve = curve;
  }

  /**
   * Gets the underlying curve object.
   * @return The curve.
   */
  public Curve<Double, Double> getCurve() {
    return _curve;
  }

  /**
   * Returns the estimated price index for a given time to index.
   * @param timeToIndex The time 
   * @return The price index.
   */
  public double getPriceIndex(final Double timeToIndex) {
    return _curve.getYValue(timeToIndex);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curve.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PriceIndexCurve other = (PriceIndexCurve) obj;
    return ObjectUtils.equals(_curve, other._curve);
  }

}
