/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

/**
 *  PriceIndexCurve created by adding the price index of other curves.
 */
public class PriceIndexCurveAddPriceIndexSpreadCurve implements PriceIndexCurve {

  /** The curve name. */
  private final String _name;
  /**
   * The array of underlying curves.
   */
  private final PriceIndexCurve[] _curves;

  /**
   * If -1 the rate of all curves, except the first one, will be subtracted from the first one. If +1, all the rates are added.
   */
  private final double _sign;

  /**
   * Constructor from an array of curves.
   * The new price index curve  will be the sum (or the difference) of the different underlying curves.
   * @param name The curve name.
   * @param substract If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   * @param curves  The array of underlying curves.
   */
  public PriceIndexCurveAddPriceIndexSpreadCurve(final String name, final boolean substract, final PriceIndexCurve... curves) {
    ArgumentChecker.notNull(curves, "Curves");
    _name = name;
    _sign = substract ? -1.0 : 1.0;
    _curves = curves;
  }

  @Override
  public double getPriceIndex(final Double timeToIndex) {
    double priceIndex = _curves[0].getPriceIndex(timeToIndex);
    for (int loopcurve = 1; loopcurve < _curves.length; loopcurve++) {
      priceIndex += _sign * _curves[loopcurve].getPriceIndex(timeToIndex);
    }
    return priceIndex;
  }

  @Override
  public double getInflationRate(final Double firstTime, final Double secondTime) {
    ArgumentChecker.isTrue(firstTime < secondTime, "firstTime should be before secondTime");
    return this.getPriceIndex(secondTime) / this.getPriceIndex(firstTime) - 1.0;
  }

  @Override
  public double[] getPriceIndexParameterSensitivity(final double time) {
    final DoubleArrayList result = new DoubleArrayList();
    double[] temp;
    temp = _curves[0].getPriceIndexParameterSensitivity(time);
    for (final double element : temp) {
      result.add(element);
    }
    for (int loopcurve = 1; loopcurve < _curves.length; loopcurve++) {
      temp = _curves[loopcurve].getPriceIndexParameterSensitivity(time);
      for (final double element : temp) {
        result.add(element);
      }
    }
    return result.toDoubleArray();
  }

  @Override
  public int getNumberOfParameters() {
    int result = 0;
    for (final PriceIndexCurve curve : _curves) {
      result += curve.getNumberOfParameters();
    }
    return result;
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    final List<String> names = new ArrayList<>();
    for (final PriceIndexCurve curve : _curves) {
      names.add(curve.getName());
    }
    return names;
  }

  public PriceIndexCurve[] getCurves() {
    return _curves;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public int getNumberOfIntrinsicParameters(Set<String> curvesNames) {
    int nb = 0;
    for (int loopcurve = 0; loopcurve < _curves.length; loopcurve++) {
      nb += _curves[loopcurve].getNumberOfIntrinsicParameters(curvesNames);
    }
    return nb;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curves.hashCode();
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
    final PriceIndexCurveAddPriceIndexSpreadCurve other = (PriceIndexCurveAddPriceIndexSpreadCurve) obj;
    if (!ObjectUtils.equals(_curves, other._curves)) {
      return false;
    }
    if (Double.doubleToLongBits(_sign) != Double.doubleToLongBits(other._sign)) {
      return false;
    }
    return true;
  }

}