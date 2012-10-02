/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import com.opengamma.util.ArgumentChecker;

/**
 * Class for constructing and querying a survival curve object from a supplied set of tenors and hazard rates for these tenors
 */
public class SurvivalCurve {

  // TODO : Check the getSurvivalProbability routine more carefully (the counter and the calculation itself)
  // TODO : Need to include the interpolator and extrapolators
  // TODO : Add the arg checker to verify the input vectors are the same size
  // TODO : This class needs revisiting - badly structured

  // --------------------------------------------------------------------------------------

  private final int _numberOfTenors;

  private final double[] _tenors;

  private final double[] _hazardRates;

  // --------------------------------------------------------------------------------------

  public SurvivalCurve(double[] tenorsAsDoubles, double[] hazardRates) {

    ArgumentChecker.notNull(tenorsAsDoubles, "Tenors as doubles field");
    ArgumentChecker.notNull(hazardRates, "Hazard rates field");

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

  // Builder method to build a new SurvivalCurve object given the tenor and hazard rate inputs

  public SurvivalCurve bootstrapHelperSurvivalCurve(double[] tenorsAsDoubles, double[] hazardRates) {

    ArgumentChecker.notNull(tenorsAsDoubles, "Tenors as doubles field");
    ArgumentChecker.notNull(hazardRates, "Hazard rates field");

    SurvivalCurve modifiedSurvivalCurve = new SurvivalCurve(tenorsAsDoubles, hazardRates);

    return modifiedSurvivalCurve;
  }

  // --------------------------------------------------------------------------------------
}
