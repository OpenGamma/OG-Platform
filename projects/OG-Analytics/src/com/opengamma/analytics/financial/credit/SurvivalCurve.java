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
 * The input is a vector of tenors and market observed par CDS spread qoutes for those tenors
 * The output is a vector of tenors and the calibrated survival probabilities for those tenors
 */
public class SurvivalCurve {

  // ------------------------------------------------------------------------

  // TODO : Lots of work to do in here
  // TODO : Check that length of the tenor and parCDSSpreads vectors are the same
  // TODO : Check that the tenors are in ascending order
  // TODO : Add the interpolation and extrapolation methods

  // ------------------------------------------------------------------------

  // Private member variables

  // Flat hazard rate value (mostly used for testing purposes only)
  private final double _flatHazardRate;

  // ------------------------------------------------------------------------

  // Default SurvivalCurve constructor
  public SurvivalCurve() {
    _flatHazardRate = 0.0;
  }

  // ------------------------------------------------------------------------

  // SurvivalCurve constructor (very basic flat curve - mostly used for testing purposes)
  public SurvivalCurve(final double parSpread, final double curveRecoveryRate) {
    _flatHazardRate = (parSpread / 10000.0) / (1 - curveRecoveryRate);
  }

  // ------------------------------------------------------------------------

  // Main method for calibrating a CDS objects survival curve
  public double[][] calibrateSurvivalCurve(CreditDefaultSwapDefinition cds, ZonedDateTime[] tenors, double[] parCDSSpreads) {

    int numberOfTenors = tenors.length;
    int numberOfSpreads = parCDSSpreads.length;

    double[][] survivalCurve = new double[numberOfTenors][2];

    return survivalCurve;
  }

  // ------------------------------------------------------------------------

  // Member function to get the survival probability at time t from a calibrated survival curve
  public double getSurvivalProbability(double t) {

    double survivalProbability = 0.0;

    return survivalProbability;
  }

  // ------------------------------------------------------------------------

  // Overloaded member function to get a survival probability from a flat hazard rate curve
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

  // Public accessor method to return the flat hazard rate
  public double getFlatHazardRate() {
    return _flatHazardRate;
  }

  // ------------------------------------------------------------------------
}
