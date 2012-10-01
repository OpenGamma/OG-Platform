/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import com.opengamma.util.ArgumentChecker;

/**
 * Class for constructing and querying a survival curve from a user-input set of tenors and hazard rates for these tenors
 */
public class SurvivalCurve {

  // TODO : Check the getSurvivalProbability routine more carefully (the counter and the calculation itself)
  // TODO : Need to include the interpolator and extrapolators
  // TODO : Should really have these functions in a seperate 'HazardRateCurve' class
  // TODO : This class needs revisiting - badly structured

  // --------------------------------------------------------------------------------------

  private final int _numberOfTenors;

  private final double[] _tenors;

  private final double[] _hazardRates;

  // --------------------------------------------------------------------------------------

  public SurvivalCurve(double[] tenorsAsDoubles, double[] hazardRates) {

    ArgumentChecker.notNull(tenorsAsDoubles, "Tenors as doubles field");
    ArgumentChecker.notNull(hazardRates, "Hazard rates field");
    //ArgumentChecker.isTrue(tenorsAsDoubles.length == hazardRates.length, "Tenor and hazard rate vectors are not the same length");

    _numberOfTenors = tenorsAsDoubles.length;

    _tenors = tenorsAsDoubles;

    _hazardRates = hazardRates;

  }

  // --------------------------------------------------------------------------------------

  public double getSurvivalProbability(double t) {

    ArgumentChecker.notNegative(t, "time");

    int counter = 0;

    while (t > this.getTenors()[counter] && counter < this.getNumberOfTenors() - 1) {
      counter++;
    }

    if (counter > this.getNumberOfTenors()) {
      counter = this.getNumberOfTenors() - 1;
    }

    final double hazardRate = this.getHazardRates()[counter];

    return Math.exp(-hazardRate * t);

  }

  // --------------------------------------------------------------------------------------

  public int getNumberOfTenors() {
    return _numberOfTenors;
  }

  public double[] getTenors() {
    return _tenors;
  }

  public double[] getHazardRates() {
    return _hazardRates;
  }

  // --------------------------------------------------------------------------------------

  public SurvivalCurve bootstrapHelperSurvivalCurve(double[] tenorsAsDoubles, double[] hazardRates) {

    ArgumentChecker.notNull(tenorsAsDoubles, "Tenors as doubles field");
    ArgumentChecker.notNull(hazardRates, "Hazard rates field");
    //ArgumentChecker.isTrue(tenorsAsDoubles.length == hazardRates.length, "Tenor and hazard rate vectors are not the same length");

    SurvivalCurve modifiedSurvivalCurve = new SurvivalCurve(tenorsAsDoubles, hazardRates);

    return modifiedSurvivalCurve;
  }

  // --------------------------------------------------------------------------------------
}
