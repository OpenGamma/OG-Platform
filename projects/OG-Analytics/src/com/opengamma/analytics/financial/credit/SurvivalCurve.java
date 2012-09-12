/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import javax.time.calendar.ZonedDateTime;

/**
 * Class for constructing a survival curve from a user-input set of tenors and hazard rates for these tenors
 */
public class SurvivalCurve {

  // TODO : Check the validity of the arguments in the ctor

  // --------------------------------------------------------------------------------------

  private final int _numberOfTenors;

  private final ZonedDateTime[] _tenors;

  private final double[] _hazardRates;

  // --------------------------------------------------------------------------------------

  public SurvivalCurve(ZonedDateTime[] tenors, double[] hazardRates) {

    _numberOfTenors = tenors.length;

    _tenors = tenors;

    _hazardRates = hazardRates;

  }

  // --------------------------------------------------------------------------------------  

  public double getSurvivalProbability(double t) {

    double runningTotal = 0.0;
    double survivalProbability = 0.0;

    int counter = 0;

    /*
    while (t <= this.getTenors[counter]) {

      runningTotal += hazardRates[counter] * tenors[counter];
      counter++;
    }
    */

    survivalProbability = Math.exp(-runningTotal);

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

  // --------------------------------------------------------------------------------------

  public SurvivalCurve bootstrapHelperSurvivalCurve(ZonedDateTime[] tenors, double[] hazardRates) {

    SurvivalCurve modifiedSurvivalCurve = new SurvivalCurve(tenors, hazardRates);

    return modifiedSurvivalCurve;
  }

  // --------------------------------------------------------------------------------------
}
