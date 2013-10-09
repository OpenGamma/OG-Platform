/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.util.ArgumentChecker;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

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
  public double getInterestRate(final Double t) {
    double rate = _curves[0].getInterestRate(t);
    for (int loopcurve = 1; loopcurve < _curves.length; loopcurve++) {
      rate += _sign * _curves[loopcurve].getInterestRate(t);
    }
    return rate;
  }

  @Override
  public double getForwardRate(final double t) {
    double f = _curves[0].getForwardRate(t);
    for (int loopcurve = 1; loopcurve < _curves.length; loopcurve++) {
      f += _sign * _curves[loopcurve].getForwardRate(t);
    }
    return f;
  }

  @Override
  public double[] getInterestRateParameterSensitivity(final double time) {
    final DoubleArrayList result = new DoubleArrayList();
    double[] temp;
    temp = _curves[0].getInterestRateParameterSensitivity(time);
    for (final double element : temp) {
      result.add(element);
    }
    for (int loopcurve = 1; loopcurve < _curves.length; loopcurve++) {
      temp = _curves[loopcurve].getInterestRateParameterSensitivity(time);
      for (final double element : temp) {
        result.add(element);
      }
    }
    return result.toDoubleArray();
  }

  @Override
  public int getNumberOfParameters() {
    int result = 0;
    for (final YieldAndDiscountCurve curve : _curves) {
      result += curve.getNumberOfParameters();
    }
    return result;
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    final List<String> names = new ArrayList<>();
    for (final YieldAndDiscountCurve curve : _curves) {
      names.add(curve.getName());
    }
    return names;
  }

  public YieldAndDiscountCurve[] getCurves() {
    return _curves;
  }

  public double getSign() {
    return _sign;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_curves);
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
    final YieldAndDiscountAddZeroSpreadCurve other = (YieldAndDiscountAddZeroSpreadCurve) obj;
    if (!Arrays.equals(_curves, other._curves)) {
      return false;
    }
    if (Double.doubleToLongBits(_sign) != Double.doubleToLongBits(other._sign)) {
      return false;
    }
    return true;
  }

}
