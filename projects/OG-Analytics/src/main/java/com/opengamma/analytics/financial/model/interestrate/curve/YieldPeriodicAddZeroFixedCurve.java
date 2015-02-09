/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
* YieldPeriodicCurve created by adding the periodic compounded rate of two curves. 
* One curve is fixed and there is no sensitivity to that curve.
* The term "fixed" for the second curve means that no parameter is associated to that curve.
*/
public class YieldPeriodicAddZeroFixedCurve extends YieldAndDiscountCurve {
  /**
  * The number of compounding periods per year of the base curve.
  */
  private final int _compoundingPeriodsPerYear;
  /**
  * The base curve.
  */
  private final DoublesCurve _baseCurve;
  /**
  * The fixed curve.
  */
  private final DoublesCurve _fixedCurve;
  /**
  * If 1 the rates are added, if -1, they are subtracted (curve - curveFixed).
  */
  private final double _sign;

  /**
  * Constructor for periodic yield curve that takes a base curve and a fixed curve. The new curve interest rate
  * (zero-coupon continuously compounded) will be the sum (or the difference) of the different underlying curves. There
  * will be sensitivity to the base curve only, not to the fixed curve.
  * @param name the curve name.
  * @param subtract if true, the rate of all curves, except the first one, will be subtracted from the first one. If
  * false, all the rates are added.
  * @param curve the main curve.
  * @param curveFixed the fixed curve (as a spread).
  */
  public YieldPeriodicAddZeroFixedCurve(String name,
      boolean subtract,
      YieldPeriodicCurve curve,
      DoublesCurve curveFixed) {
    super(name);
    ArgumentChecker.notNull(curve, "curve");
    ArgumentChecker.notNull(curveFixed, "curveFixed");
    _baseCurve = curve.getCurve();
    _fixedCurve = curveFixed;
    _compoundingPeriodsPerYear = curve.getCompoundingPeriodsPerYear();
    _sign = subtract ? -1.0 : 1.0;
  }

  @Override
  public double getInterestRate(Double time) {
    double discountFactor = getDiscountFactor(time);
    return -Math.log(discountFactor) / time;
  }

  @Override
  public double getForwardRate(double t) {
    throw new NotImplementedException("Instantaneous Forward rate not implemented for Periodic rate curves.");
  }

  @Override
  public double getDiscountFactor(double t) {
    final double rate = _baseCurve.getYValue(t) + _sign * _fixedCurve.getYValue(t);
    return Math.pow(1 + rate / _compoundingPeriodsPerYear, -_compoundingPeriodsPerYear * t);
  }

  @Override
  public double getPeriodicInterestRate(double t, int compoundingPeriodsPerYear) {
    double baseIR = _baseCurve.getYValue(t) + _sign * _fixedCurve.getYValue(t);
    if (compoundingPeriodsPerYear == _compoundingPeriodsPerYear) {
      return baseIR;
    }
    InterestRate baseRate = new PeriodicInterestRate(baseIR, _compoundingPeriodsPerYear);
    // rate in the composition of the storage.
    InterestRate periodicRate = baseRate.toPeriodic(compoundingPeriodsPerYear);
    return periodicRate.getRate();
  }

  @Override
  public double[] getInterestRateParameterSensitivity(double time) {
    Double[] drPdp = _baseCurve.getYValueParameterSensitivity(time); // d (r_Periodic) / d parameters
    int nbParam = drPdp.length;
    double rPS = _baseCurve.getYValue(time) + _sign * _fixedCurve.getYValue(time); // r_Periodic +/- spread
    double drCdrP = 1.0d / (1.0d + rPS / _compoundingPeriodsPerYear); // d (r_ContinouslyCompounded) / d (r_Periodic)
    double[] drCdp = new double[nbParam]; // d (r_ContinouslyCompounded) / d parameters
    for (int i = 0; i < nbParam; i++) {
      drCdp[i] = drCdrP * drPdp[i];
    }
    return drCdp;
  }

  @Override
  public int getNumberOfParameters() {
    return _baseCurve.size();
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    return new ArrayList<>();
  }
  
}
