/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * Class for the construction of flat survival curves (mostly used for testing purposes)
 */
public class FlatSurvivalCurve {

  // ------------------------------------------------------------------------

  // Private (final) member variables

  // Flat (across the term structure) hazard rate value
  private final double _flatHazardRate;

  // ------------------------------------------------------------------------

  // Default FlatSurvivalCurve constructor (not very useful)
  public FlatSurvivalCurve() {
    _flatHazardRate = 0.0;
  }

  // ------------------------------------------------------------------------

  // FlatSurvivalCurve constructor
  public FlatSurvivalCurve(final double parSpread, final double curveRecoveryRate) {
    _flatHazardRate = (parSpread / 10000.0) / (1 - curveRecoveryRate);
  }

  // ------------------------------------------------------------------------

  // Public member function to get a survival probability from a flat hazard rate curve
  public double getSurvivalProbability(double hazardRate, double t) {

    return Math.exp(-hazardRate * t);
  }

  // ------------------------------------------------------------------------

  // Public accessor method to return the flat hazard rate
  public double getFlatHazardRate() {
    return _flatHazardRate;
  }

  // ------------------------------------------------------------------------
}
