/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * A curve containing the (estimated) price index at different maturities.
 * Simple price index curve described by a DoublesCurve.
 * The maturities can be (slightly) negative as the price index are known only with a certain lag.
 * The price index for a given month is on the first of the month point.
 */
public class PriceIndexCurveSimple implements PriceIndexCurve {

  /**
   * The price index curve.
   */
  private final DoublesCurve _curve;

  /**
   * A small amount of time
   */
  private static final double SMALL_TIME = 1.0E-6;

  /**
   * Constructor from a curve object.
   * @param curve The curve.
   */
  public PriceIndexCurveSimple(final DoublesCurve curve) {
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
  public static PriceIndexCurveSimple fromStartOfMonth(final double[] nodeTimeKnown, final double[] indexKnown, final double[] nodeTimeOther, final double[] rate) {
    final int nbNode = nodeTimeKnown.length + nodeTimeOther.length;
    final double[] nodeTime = new double[nbNode];
    System.arraycopy(nodeTimeKnown, 0, nodeTime, 0, nodeTimeKnown.length);
    System.arraycopy(nodeTimeOther, 0, nodeTime, nodeTimeKnown.length, nodeTimeOther.length);
    final double[] indexValue = new double[nbNode];
    System.arraycopy(indexKnown, 0, indexValue, 0, nodeTimeKnown.length);
    final double[] nbYear = new double[nodeTimeOther.length];
    for (int loopperiod = 0; loopperiod < nodeTimeOther.length; loopperiod++) {
      nbYear[loopperiod] = Math.round(nodeTimeOther[loopperiod] - nodeTimeKnown[0]);
      indexValue[nodeTimeKnown.length + loopperiod] = indexKnown[0] * Math.pow(1 + rate[loopperiod], nbYear[loopperiod]);
    }
    final InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(nodeTime, indexValue, new LinearInterpolator1D());
    return new PriceIndexCurveSimple(curve);
  }

  /**
   * Gets the underlying curve object.
   * @return The curve.
   */
  public DoublesCurve getCurve() {
    return _curve;
  }

  /**
   * Returns the curve name.
   * @return The name.
   */
  @Override
  public String getName() {
    return _curve.getName();
  }

  /**
   * Returns the estimated price index for a given time to index.
   * @param timeToIndex The time
   * @return The price index.
   */
  @Override
  public double getPriceIndex(final Double timeToIndex) {
    return _curve.getYValue(timeToIndex);
  }

  /**
   * Returns the estimated inflation rate between two given time .
   * @param firstTime The time
   * @param secondTime The time
   * @return The price index.
   */
  @Override
  public double getInflationRate(final Double firstTime, final Double secondTime) {
    ArgumentChecker.isTrue(firstTime < secondTime, "firstTime should be before secondTime");
    return _curve.getYValue(secondTime) / _curve.getYValue(firstTime) - 1.0;
  }

  /**
   * Gets the number of parameters in a curve.
   * @return The number of parameters
   */
  @Override
  public int getNumberOfParameters() {
    return _curve.size();
  }

  /**
   * Return the number of intrinsic parameters for the definition of the curve. 
   * Which is the total number of parameters minus the parameters of the curves in curvesNames (If they are in curves).
   *  @param curvesNames The list of curves names.
   *  @return The number of parameters.
   */
  @Override
  public int getNumberOfIntrinsicParameters(final Set<String> curvesNames) {
    return _curve.size();
  }

  /**
   * The list of underlying curves (up to one level).
   * @return The list.
   */
  @Override
  public List<String> getUnderlyingCurvesNames() {
    return new ArrayList<>();
  }

  /**
   * Gets the sensitivities of the price index to the curve parameters for a time.
   * @param time The time
   * @return The sensitivities. If the time is less than 1e<sup>-6</sup>, the rate is
   * ill-defined and zero is returned.
   */
  @Override
  public double[] getPriceIndexParameterSensitivity(final double time) {
    final Double[] curveSensitivity = _curve.getYValueParameterSensitivity(time);
    final double[] priceIndexZeroSensitivity = new double[curveSensitivity.length];
    // Implementation note: if time = 0, the rate is ill-defined: return 0 sensitivity
    if (Math.abs(time) < SMALL_TIME) {
      return priceIndexZeroSensitivity;
    }
    for (int loopp = 0; loopp < curveSensitivity.length; loopp++) {
      priceIndexZeroSensitivity[loopp] = curveSensitivity[loopp];
    }
    return priceIndexZeroSensitivity;
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
    final PriceIndexCurveSimple other = (PriceIndexCurveSimple) obj;
    return ObjectUtils.equals(_curve, other._curve);
  }

}
