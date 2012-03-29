/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;

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
   * Build a simple price index curve from known index and annual zero-coupon swap rates. The inflation coupon reference are exact month are not interpolated. 
   * No seasonality is used. The price index are interpolated linearly.
   * @param nodeTimeKnown The time to the known price index. Those time will typically be negative (the price index are published after month end).
   * @param indexKnown The value of the known index. The first one in the list is the one used in the swaps used for curve construction.
   * @param nodeTimeOther The time to the price index reference for the swaps.
   * @param rate The zero-coupon swaps rates.
   * @return The price index curve.
   */
  public static PriceIndexCurve fromStartOfMonth(final double[] nodeTimeKnown, final double[] indexKnown, final double[] nodeTimeOther, final double[] rate) {
    int nbNode = nodeTimeKnown.length + nodeTimeOther.length;
    double[] nodeTime = new double[nbNode];
    System.arraycopy(nodeTimeKnown, 0, nodeTime, 0, nodeTimeKnown.length);
    System.arraycopy(nodeTimeOther, 0, nodeTime, nodeTimeKnown.length, nodeTimeOther.length);
    double[] indexValue = new double[nbNode];
    System.arraycopy(indexKnown, 0, indexValue, 0, nodeTimeKnown.length);
    double[] nbYear = new double[nodeTimeOther.length];
    for (int loopperiod = 0; loopperiod < nodeTimeOther.length; loopperiod++) {
      nbYear[loopperiod] = Math.round(nodeTimeOther[loopperiod] - nodeTimeKnown[0]);
      indexValue[nodeTimeKnown.length + loopperiod] = indexKnown[0] * Math.pow(1 + rate[loopperiod], nbYear[loopperiod]);
    }
    final InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(nodeTime, indexValue, new LinearInterpolator1D());
    return new PriceIndexCurve(curve);
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
