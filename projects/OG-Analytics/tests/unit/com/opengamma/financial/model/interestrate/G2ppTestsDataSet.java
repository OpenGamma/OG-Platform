/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import com.opengamma.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;

/**
 * Data used for the G2++ model tests.
 */
public class G2ppTestsDataSet {

  private static final double[] MEAN_REVERSION = new double[] {0.01, 0.30};
  private static final double[][] VOLATILITY = new double[][] { {0.01, 0.011, 0.012, 0.013, 0.014}, {0.01, 0.009, 0.008, 0.007, 0.006}};
  private static final double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0};
  private static final double CORRELATION = -0.50;
  private static final G2ppPiecewiseConstantParameters MODEL_PARAMETERS = new G2ppPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME, CORRELATION);

  /**
   * Create a set of G2++ parameters for testing.
   * @return The parameters.
   */
  public static G2ppPiecewiseConstantParameters createG2ppParameters() {
    return MODEL_PARAMETERS;
  }

}
