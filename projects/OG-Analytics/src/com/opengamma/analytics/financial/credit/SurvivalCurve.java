/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

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

  public SurvivalCurve() {

  }

  // --------------------------------------------------------------------------------------  

  public double getSurvivalProbability(double t) {

    return 0.0;

  }

  // --------------------------------------------------------------------------------------
}
