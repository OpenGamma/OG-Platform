/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import javax.time.calendar.ZonedDateTime;

/**
 * Class for constructing and querying a survival curve from a user-input set of tenors and hazard rates for these tenors
 */
public class SurvivalCurve {

  // TODO : Check the validity of the arguments in the ctor e.g. not null, equal number of tenors/spreads
  // TODO : Is there a better way to overload the ctor (problem is the compiler complains about unassigned member variables)
  // TODO : Do we even need the ZonedDateTime version of the ctor? Survival curve can function perfectly well with tenors as doubles
  // TODO : Check the getSurvivalProbability routine more carefully (the counter and the calculation itself)
  // TODO : Need to include the interpolator and extrapolators

  // --------------------------------------------------------------------------------------

  private final int _numberOfTenors;

  private final ZonedDateTime[] _tenors;
  private final double[] _tenorsAsDoubles;

  private final double[] _hazardRates;

  // --------------------------------------------------------------------------------------

  public SurvivalCurve(ZonedDateTime[] tenors, double[] hazardRates) {

    _numberOfTenors = tenors.length;

    _tenors = tenors;
    _tenorsAsDoubles = null;

    _hazardRates = hazardRates;

  }

  // --------------------------------------------------------------------------------------

  public SurvivalCurve(double[] tenors, double[] hazardRates) {

    _numberOfTenors = tenors.length;

    _tenors = null;
    _tenorsAsDoubles = tenors;

    _hazardRates = hazardRates;

  }

  // --------------------------------------------------------------------------------------  

  public double getSurvivalProbability(double t) {

    int counter = 0;

    while (t > this.getTenorsAsDoubles()[counter] && counter < this.getNumberOfTenors() - 1) {
      counter++;
    }

    /*
    // Do we need this?
    if (counter > this.getNumberOfTenors()) {
      counter = this.getNumberOfTenors() - 1;
    }
    */

    double hazardRate = this.getHazardRates()[counter];

    double survivalProbability = Math.exp(-hazardRate * t);

    return survivalProbability;

  }

  // --------------------------------------------------------------------------------------

  public int getNumberOfTenors() {
    return _numberOfTenors;
  }

  public ZonedDateTime[] getTenors() {
    return _tenors;
  }

  public double[] getHazardRates() {
    return _hazardRates;
  }

  public double[] getTenorsAsDoubles() {
    return _tenorsAsDoubles;
  }

  // --------------------------------------------------------------------------------------

  public SurvivalCurve bootstrapHelperSurvivalCurve(ZonedDateTime[] tenors, double[] hazardRates) {

    SurvivalCurve modifiedSurvivalCurve = new SurvivalCurve(tenors, hazardRates);

    return modifiedSurvivalCurve;
  }

  // --------------------------------------------------------------------------------------

  public SurvivalCurve bootstrapHelperSurvivalCurve(double[] tenors, double[] hazardRates) {

    SurvivalCurve modifiedSurvivalCurve = new SurvivalCurve(tenors, hazardRates);

    return modifiedSurvivalCurve;
  }

  // --------------------------------------------------------------------------------------
}
