/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * YieldAndDiscountCurve created by adding the zero-coupon continuously compounded rate of other curves.
 */
public class YieldAndDiscountAddZeroSpreadCurve extends YieldAndDiscountCurve {

  /**
   * The array of underlying curves.
   */
  private final YieldAndDiscountCurve[] _curves;
  /**
   * If -1 the rate of all curves, except the first one, will be subtracted from the first one. If +1, all the rates are added.
   */
  private final double _sign;

  /**
   * Constructor from an array of curves. 
   * The new curve interest rate (zero-coupon continuously compounded) will be the sum (or the difference) of the different underlying curves.
   * @param name The curve name.
   * @param substract If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   * @param curves The array of underlying curves.
   */
  public YieldAndDiscountAddZeroSpreadCurve(final String name, final boolean substract, final YieldAndDiscountCurve... curves) {
    super(name);
    ArgumentChecker.notNull(curves, "Curves");
    _sign = substract ? -1.0 : 1.0;
    _curves = curves;
  }

  @Override
  public double getInterestRate(Double t) {
    double rate = _curves[0].getInterestRate(t);
    for (int loopcurve = 1; loopcurve < _curves.length; loopcurve++) {
      rate += _sign * _curves[loopcurve].getInterestRate(t);
    }
    return rate;
  }

  @Override
  public double[] getInterestRateParameterSensitivity(double time) {
    final List<Double> result = new ArrayList<Double>();
    double[] temp;
    temp = _curves[0].getInterestRateParameterSensitivity(time);
    for (int loops = 0; loops < temp.length; loops++) {
      result.add(temp[loops]);
    }
    for (int loopcurve = 1; loopcurve < _curves.length; loopcurve++) {
      temp = _curves[loopcurve].getInterestRateParameterSensitivity(time);
      for (int loops = 0; loops < temp.length; loops++) {
        result.add(temp[loops]);
      }
    }
    return ArrayUtils.toPrimitive(result.toArray(new Double[0]));
  }

  @Override
  public int getNumberOfParameters() {
    int result = 0;
    for (int loopcurve = 0; loopcurve < _curves.length; loopcurve++) {
      result += _curves[loopcurve].getNumberOfParameters();
    }
    return result;
  }

  public YieldAndDiscountCurve[] getCurves() {
    return _curves;
  }

}
