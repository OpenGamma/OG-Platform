/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * PriceIndexCurve created by multiplying a fixed curve to a price index curve. 
 * No parameter is associated to the fixed curve.
 * The fixed curve can be used for multiplicative seasonal adjustment.
 */
public class PriceIndexCurveMultiplyFixedCurve  implements PriceIndexCurve {

  /** The curve name. */
  private final String _name;
  /** The main underlying curve. */
  private final PriceIndexCurve _curve;
  /** The fixed curve. */
  private final DoublesCurve _fixedCurve;

  /**
   * Constructor from an array of curves.
   * The new price index curve will be the multiplication of the underlying curve and the seasonal curve.
   * @param name The curve name. Not null.
   * @param curve The main curve. Not null.
   * @param fixedCurve The fixed curve (as a multiplicative spread). Not null.
   */
  public PriceIndexCurveMultiplyFixedCurve(final String name, final PriceIndexCurve curve,
                                           final DoublesCurve fixedCurve) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(curve, "Curve");
    ArgumentChecker.notNull(fixedCurve, "Curve fixed");
    _name = name;
    _curve = curve;
    _fixedCurve = fixedCurve;
  }

  @Override
  public double getPriceIndex(final Double timeToIndex) {
    return _curve.getPriceIndex(timeToIndex) * _fixedCurve.getYValue(timeToIndex);
  }

  @Override
  public double getInflationRate(final Double firstTime, final Double secondTime) {
    ArgumentChecker.isTrue(firstTime < secondTime, "first time {} should be before second time {}", firstTime, secondTime);
    return this.getPriceIndex(secondTime) / this.getPriceIndex(firstTime) - 1.0;
  }

  @Override
  public double[] getPriceIndexParameterSensitivity(final double time) {
    double[] sensiUnderlying = _curve.getPriceIndexParameterSensitivity(time);
    double[] sensi = new double[sensiUnderlying.length];
    for (int i = 0; i < sensiUnderlying.length; i++) {
      sensi[i] = sensiUnderlying[i] * _fixedCurve.getYValue(time);
    }
    return sensi;
  }

  @Override
  public int getNumberOfParameters() {
    return _curve.getNumberOfParameters();
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    return new ArrayList<>();
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public int getNumberOfIntrinsicParameters(Set<String> curvesNames) {
    return _curve.getNumberOfIntrinsicParameters(curvesNames);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curve.hashCode();
    result = prime * result + _fixedCurve.hashCode();
    result = prime * result + _name.hashCode();
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
    final PriceIndexCurveMultiplyFixedCurve other = (PriceIndexCurveMultiplyFixedCurve) obj;
    if (!ObjectUtils.equals(_curve, other._curve)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixedCurve, other._fixedCurve)) {
      return false;
    }
    return true;
  }

}