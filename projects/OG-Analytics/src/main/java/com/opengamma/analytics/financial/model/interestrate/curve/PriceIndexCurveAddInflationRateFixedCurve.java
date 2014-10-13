/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
 *  PriceIndexCurve created by adding a curve of double (the inflation rates curve which is represent  market quotes) to a PriceIndexCurve.
 *   One curve is fixed and there is no sensitivity to that curve.
 * In general the fixed curve represent a static adjustment like a seasonality adjustment.
 * The term "fixed" for the second curve means that no parameter is associated to that curve.
 */
public class PriceIndexCurveAddInflationRateFixedCurve extends PriceIndexCurveSimple {

  /**
   * The main underlying curve.
   */
  private final PriceIndexCurveSimple _curve;
  /**
   * The fixed curve.
   */
  private final DoublesCurve _curveFixed;
  /**
   * If 1 the rates are added, if -1, they are subtracted (curve - curveFixed).
   */
  private final double _sign;

  /**
   * The reference price index value, use to calculate the rate of all the curve.
   */
  private final double _referencePriceIndex;

  /**
   * Constructor from an array of curves.
   * The new price index curve  will be the sum (or the difference) of the different underlying curves.
   * @param name The curve name.
   * @param substract If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   * @param curve The main curve.
   * @param curveFixed The fixed curve (as a spread).
   * @param referencePriceIndex The reference price index value, use to calculate the rate of all the curve.
   */
  public PriceIndexCurveAddInflationRateFixedCurve(final String name, final PriceIndexCurveSimple curve, final DoublesCurve curveFixed, final boolean substract, final double referencePriceIndex) {
    super(curve.getCurve());
    ArgumentChecker.notNull(curve, "Curve");
    ArgumentChecker.notNull(curveFixed, "Curve fixed");
    _curve = curve;
    _curveFixed = curveFixed;
    _sign = substract ? -1.0 : 1.0;
    _referencePriceIndex = referencePriceIndex;
  }

  @Override
  public double getPriceIndex(final Double timeToIndex) {
    return _curve.getPriceIndex(timeToIndex) + _sign * _referencePriceIndex * Math.pow(1 + _curveFixed.getYValue(timeToIndex), timeToIndex);
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
    int result = super.hashCode();
    result = prime * result + ((_curve == null) ? 0 : _curve.hashCode());
    result = prime * result + ((_curveFixed == null) ? 0 : _curveFixed.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_referencePriceIndex);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_sign);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PriceIndexCurveAddInflationRateFixedCurve other = (PriceIndexCurveAddInflationRateFixedCurve) obj;
    if (_curve == null) {
      if (other._curve != null) {
        return false;
      }
    } else if (!_curve.equals(other._curve)) {
      return false;
    }
    if (_curveFixed == null) {
      if (other._curveFixed != null) {
        return false;
      }
    } else if (!_curveFixed.equals(other._curveFixed)) {
      return false;
    }
    if (Double.doubleToLongBits(_referencePriceIndex) != Double.doubleToLongBits(other._referencePriceIndex)) {
      return false;
    }
    if (Double.doubleToLongBits(_sign) != Double.doubleToLongBits(other._sign)) {
      return false;
    }
    return true;
  }

}
