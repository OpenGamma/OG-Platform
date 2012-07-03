/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

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

  public YieldAndDiscountAddZeroSpreadCurve(final boolean substract, final YieldAndDiscountCurve... curves) {
    super("Spread" + curves[0].getName());
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

  public YieldAndDiscountCurve[] getCurves() {
    return _curves;
  }

}
