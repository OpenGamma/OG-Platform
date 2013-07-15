/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;

/**
 * Data used for the G2++ model tests.
 */
public class TestsDataSetG2pp {

  private static final double[] MEAN_REVERSION = new double[] {0.01, 0.30};
  private static final double[][] VOLATILITY_1 = new double[][] { {0.01, 0.011, 0.012, 0.013, 0.014}, {0.01, 0.009, 0.008, 0.007, 0.006}};
  private static final double[][] VOLATILITY_2 = new double[][] { {0.015, 0.013, 0.012, 0.010, 0.009}, {0.012, 0.009, 0.008, 0.007, 0.006}};
  private static final double[][] VOLATILITY_3 = new double[][] { {0.010, 0.011, 0.010, 0.010, 0.009}, {0.008, 0.009, 0.008, 0.007, 0.006}};
  private static final double[][] VOLATILITY_CST = new double[][] { {0.01}, {0.005}};
  private static final double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0};
  private static final double CORRELATION = -0.50;
  private static final G2ppPiecewiseConstantParameters MODEL_PARAMETERS_1 = new G2ppPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY_1, VOLATILITY_TIME, CORRELATION);
  private static final G2ppPiecewiseConstantParameters MODEL_PARAMETERS_2 = new G2ppPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY_2, VOLATILITY_TIME, CORRELATION);
  private static final G2ppPiecewiseConstantParameters MODEL_PARAMETERS_3 = new G2ppPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY_3, VOLATILITY_TIME, CORRELATION);
  private static final G2ppPiecewiseConstantParameters MODEL_PARAMETERS_CST = new G2ppPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY_CST, new double[0], CORRELATION);

  /**
   * Create a set of G2++ parameters for testing.
   * @return The parameters.
   */
  public static G2ppPiecewiseConstantParameters createG2ppParameters1() {
    return MODEL_PARAMETERS_1;
  }

  /**
  * Create a set of G2++ parameters for testing.
  * @return The parameters.
  */
  public static G2ppPiecewiseConstantParameters createG2ppParameters2() {
    return MODEL_PARAMETERS_2;
  }

  /**
  * Create a set of G2++ parameters for testing.
  * @return The parameters.
  */
  public static G2ppPiecewiseConstantParameters createG2ppParameters3() {
    return MODEL_PARAMETERS_3;
  }

  /**
   * Create a set of time-constant G2++ parameters for testing.
   * @return The parameters.
   */
  public static G2ppPiecewiseConstantParameters createG2ppCstParameters() {
    return MODEL_PARAMETERS_CST;
  }

}
