/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.math.curve.Curve;

/**
 * YieldPeriodicCurve created by adding the periodic compounded rate of two curves. One curve is fixed and there is no sensitivity to that curve.
 * The term "fixed" for the second curve means that no parameter is associated to that curve.
 */
public class YieldPeriodicAddZeroFixedCurve extends YieldAndDiscountAddZeroFixedCurve {
  
  /**
   * The number of compounding periods per year of the base curve.
   */
  private final int _compoundingPeriodsPerYear;
  /**
   * The base curve.
   */
  private final Curve<Double, Double> _baseCurve;
  /**
   * The fixed curve.
   */
  private final Curve<Double, Double> _fixedCurve;
  
  /**
   * If 1 the rates are added, if -1, they are subtracted (curve - curveFixed).
   */
  private final double _sign;
    
  public YieldPeriodicAddZeroFixedCurve(String name, boolean subtract, YieldPeriodicCurve curve, YieldCurve curveFixed) {
    super(name, subtract, curve, curveFixed);
    _baseCurve = curve.getCurve();
    _fixedCurve = curveFixed.getCurve();
    _compoundingPeriodsPerYear = curve.getCompoundingPeriodsPerYear();
    _sign = subtract ? -1.0 : 1.0;
  }

  @Override
  public double getInterestRate(final Double time) {
    final double rate = _baseCurve.getYValue(time) + _sign * _fixedCurve.getYValue(time);
    return _compoundingPeriodsPerYear * Math.log(1 + rate / _compoundingPeriodsPerYear);
  }

  @Override
  public double getForwardRate(final double t) {
    return _baseCurve.getYValue(t) + _sign * _fixedCurve.getYValue(t);
  }

  @Override
  public double getDiscountFactor(final double t) {
    final double rate = _baseCurve.getYValue(t) + _sign * _fixedCurve.getYValue(t);
    return Math.pow(1 + rate / _compoundingPeriodsPerYear, -_compoundingPeriodsPerYear * t);
  }

  @Override
  public double getPeriodicInterestRate(final double t, final int compoundingPeriodsPerYear) {
    double baseIR = _baseCurve.getYValue(t) + _sign * _fixedCurve.getYValue(t);
    if (compoundingPeriodsPerYear == _compoundingPeriodsPerYear) {
      return baseIR;
    }
    final InterestRate rc = new PeriodicInterestRate(baseIR, _compoundingPeriodsPerYear);
    // Implementation note: rate in the composition of the storage.
    final InterestRate rq = rc.toPeriodic(compoundingPeriodsPerYear);
    return rq.getRate();
  }
}
