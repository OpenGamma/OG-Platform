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
 *  PriceIndexCurve created by adding a seasonal curve to a price index curve. The seasonal curve is fixed and there is no sensitivity to that curve.
 * No parameter is associated to the seasonal curve.
 */
public class PriceIndexCurveAddSeasonalCurve extends PriceIndexCurve {

  /**
   * The main underlying curve.
   */
  private final PriceIndexCurve _curve;
  /**
   * The fixed curve.
   */
  private final SeasonalCurve _seasonalCurve;

  /**
   * Constructor from an array of curves.
   * The new price index curve  will be the sum (or the difference) of the different underlying curves.
   * @param name The curve name.
   * @param curve The main curve.
   * @param seasonalCurve The fixed curve (as a spread).
   */
  public PriceIndexCurveAddSeasonalCurve(final String name, final PriceIndexCurve curve, final SeasonalCurve seasonalCurve) {
    super(curve.getCurve());
    ArgumentChecker.notNull(curve, "Curve");
    ArgumentChecker.notNull(seasonalCurve, "Curve fixed");
    _curve = curve;
    _seasonalCurve = seasonalCurve;
  }

  @Override
  public double getPriceIndex(final Double timeToIndex) {
    return _curve.getPriceIndex(timeToIndex) + _seasonalCurve.getFunction().evaluate(timeToIndex);
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
    return new ArrayList<>();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curve.hashCode();
    result = prime * result + _seasonalCurve.hashCode();
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
    final PriceIndexCurveAddSeasonalCurve other = (PriceIndexCurveAddSeasonalCurve) obj;
    if (!ObjectUtils.equals(_curve, other._curve)) {
      return false;
    }
    if (!ObjectUtils.equals(_seasonalCurve, other._seasonalCurve)) {
      return false;
    }
    return true;
  }

}
