/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;

/**
 *  PriceIndexCurve created by adding the price index of two curves. One curve is fixed and there is no sensitivity to that curve.
 * In general the fixed curve represent a static adjustment like a seasonality adjustment.
 * The term "fixed" for the second curve means that no parameter is associated to that curve.
 */
public class PriceIndexCurveAddPriceIndexFixedCurve extends PriceIndexCurveSimple {

  /**
   * The main underlying curve.
   */
  private final PriceIndexCurveSimple _curve;
  /**
   * The fixed curve.
   */
  private final PriceIndexCurveSimple _curveFixed;
  /**
   * If 1 the rates are added, if -1, they are subtracted (curve - curveFixed).
   */
  private final double _sign;

  /**
   * Constructor from an array of curves.
   * The new price index curve  will be the sum (or the difference) of the different underlying curves.
   * @param name The curve name.
   * @param substract If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   * @param curve The main curve.
   * @param curveFixed The fixed curve (as a spread).
   */
  public PriceIndexCurveAddPriceIndexFixedCurve(final String name, final boolean substract, final PriceIndexCurveSimple curve, final PriceIndexCurveSimple curveFixed) {
    super(curve.getCurve());
    ArgumentChecker.notNull(curve, "Curve");
    ArgumentChecker.notNull(curveFixed, "Curve fixed");
    _sign = substract ? -1.0 : 1.0;
    _curve = curve;
    _curveFixed = curveFixed;
  }

  @Override
  public double getPriceIndex(final Double timeToIndex) {
    return _curve.getPriceIndex(timeToIndex) + _sign * _curveFixed.getPriceIndex(timeToIndex);
  }

  @Override
  public double getInflationRate(final Double firstTime, final Double secondTime) {
    ArgumentChecker.isTrue(firstTime < secondTime, "firstTime should be before secondTime");
    return this.getPriceIndex(secondTime) / this.getPriceIndex(firstTime) - 1.0;
  }

  @Override
  public double[] getPriceIndexParameterSensitivity(final double time) {
    return _curve.getPriceIndexParameterSensitivity(time);
  }

  @Override
  public int getNumberOfParameters() {
    return _curve.getNumberOfParameters();
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    final List<String> names = new ArrayList<>();
    names.add(_curve.getName());
    names.add(_curveFixed.getName());
    return names;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curve.hashCode();
    result = prime * result + _curveFixed.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_sign);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final PriceIndexCurveAddPriceIndexFixedCurve other = (PriceIndexCurveAddPriceIndexFixedCurve) obj;
    if (!ObjectUtils.equals(_curve, other._curve)) {
      return false;
    }
    if (!ObjectUtils.equals(_curveFixed, other._curveFixed)) {
      return false;
    }
    if (Double.doubleToLongBits(_sign) != Double.doubleToLongBits(other._sign)) {
      return false;
    }
    return true;
  }
}
