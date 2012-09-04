/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;

/**
 * Class for the construction of survival curves calibrated to market observed par CDS spread term structure
 */
public class SurvivalCurve {

  // ------------------------------------------------------------------------

  // TODO : Lots of work to do in here
  // TODO : Check that length of the tenor and parCDSSpreads vectors are the same
  // TODO : Check that the tenors are in ascending order
  // TODO : Add the interpolation and extrapolation methods

  // ------------------------------------------------------------------------

  // Main method for calibrating a CDS objects survival curve
  public double[][] calibrateSurvivalCurve(CreditDefaultSwapDefinition cds, ZonedDateTime[] tenors, double[] parCDSSpreads) {

    int numberOfTenors = tenors.length;

    double[][] survivalCurve = new double[numberOfTenors][2];

    return survivalCurve;
  }

  // ------------------------------------------------------------------------

  public double getSurvivalProbability(SurvivalCurve survivalCurve, double t) {

    double survivalProbability = 0.0;

    return survivalProbability;
  }

  // ------------------------------------------------------------------------

  public double getSurvivalProbability(double hazardRate, double t) {

    double survivalProbability = Math.exp(-hazardRate * t);

    return survivalProbability;
  }

  // ------------------------------------------------------------------------

  public double getHazardRate(double t) {

    double hazardRate = 0.0;

    return hazardRate;
  }

  // ------------------------------------------------------------------------

  double[] convertDatesToDoubles(ZonedDateTime[] tenors) {

    int numberOfTenors = tenors.length;

    double[] tenorsAsDoubles = new double[numberOfTenors];

    return tenorsAsDoubles;
  }

  // ------------------------------------------------------------------------
}
