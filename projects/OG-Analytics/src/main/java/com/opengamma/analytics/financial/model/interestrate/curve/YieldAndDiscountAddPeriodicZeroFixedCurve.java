/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * A curve is created from the base curve (YieldAndDiscountCurve) and fixed curve (YieldCurve) 
 * where a spread of the fixed curve is added to periodic compounded rates of the base curve. 
 * The term "fixed" for the second curve means that no parameter is associated to that curve, i.e., there is no sensitivity to the fixed curve. 
 */
public class YieldAndDiscountAddPeriodicZeroFixedCurve extends YieldAndDiscountCurve {

  /**
   * The main underlying curve.
   */
  private final YieldAndDiscountCurve _curve;
  /**
   * The fixed curve.
   */
  private final YieldCurve _curveFixed;
  /**
   * If 1 the rates are added, if -1, they are subtracted (curve - curveFixed).
   */
  private final double _sign;
  /**
   * The number of periods per year, greater than 0.
   */
  private final int _nPeriodsPerYear;

  /**
   * Constructor
   * @param name The curve name.
   * @param substract If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   * @param curve The main curve.
   * @param curveFixed The fixed curve (as a spread).
   * @param nPeriodsPerYear The number of periods per year.
   */
  public YieldAndDiscountAddPeriodicZeroFixedCurve(String name, boolean substract, YieldAndDiscountCurve curve,
      YieldCurve curveFixed, int nPeriodsPerYear) {
    super(name);
    ArgumentChecker.notNull(curve, "Curve");
    ArgumentChecker.notNull(curveFixed, "Curve fixed");
    ArgumentChecker.isTrue(nPeriodsPerYear > 0, "nPeriodsParYear should be greater than 0");
    _sign = substract ? -1.0 : 1.0;
    _curve = curve;
    _curveFixed = curveFixed;
    _nPeriodsPerYear = nPeriodsPerYear;
  }

  @Override
  public double getInterestRate(final Double t) {
    double df = getDiscountFactor(t);
    return -Math.log(df) / t;
  }

  @Override
  public double getForwardRate(final double t) {
    double original = Math.pow(_curve.getDiscountFactor(t), -1.0 / _nPeriodsPerYear / t);
    double shifted = original + _sign * _curveFixed.getInterestRate(t) / _nPeriodsPerYear;
    double forward = _nPeriodsPerYear * Math.log(shifted) -
        original * (_curve.getForwardRate(t) - _curve.getInterestRate(t)) / shifted;
    return forward;
  }

  @Override
  public double getDiscountFactor(double t) {
    double ratePeriodicAnnualPlusOne = Math.pow(_curve.getDiscountFactor(t), -1.0 / _nPeriodsPerYear / t) + _sign *
        _curveFixed.getInterestRate(t) / _nPeriodsPerYear;
    double df = Math.pow(ratePeriodicAnnualPlusOne, -_nPeriodsPerYear * t);
    return df;
  }

  @Override
  public double[] getInterestRateParameterSensitivity(final double time) {
    return _curve.getInterestRateParameterSensitivity(time);
  }

  @Override
  public int getNumberOfParameters() {
    return _curve.getNumberOfParameters();
  }

  @Override
  public int getNumberOfIntrinsicParameters(final Set<String> curvesNames) {
    int result = 0;
    if (!curvesNames.contains(_curve.getName())) {
      result += _curve.getNumberOfParameters();
    }
    return result;
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    final List<String> names = new ArrayList<>();
    names.add(_curve.getName());
    names.add(_curveFixed.getName());
    return names;
  }

  /**
   * Gets of the curve.
   * @return The curves
   */
  public YieldAndDiscountCurve getCurve() {
    return _curve;
  }

  /**
   * Gets of the fixed curve.
   * @return The curves
   */
  public YieldAndDiscountCurve getCurveFixed() {
    return _curveFixed;
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
    final YieldAndDiscountAddPeriodicZeroFixedCurve other = (YieldAndDiscountAddPeriodicZeroFixedCurve) obj;
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
