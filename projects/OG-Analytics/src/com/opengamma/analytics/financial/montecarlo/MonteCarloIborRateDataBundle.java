/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo;

/**
 * The data bundle with the Monte Carlo Ibor rates and the reference amounts.
 */
public class MonteCarloIborRateDataBundle {

  /**
   * The paths Ibor rates. The dimensions are jump/Ibor/path.
   */
  private final double[][][] _pathIborRate;
  /**
   * The Libor accrual factors.
   */
  private final double[] _delta;
  /**
   * The reference amounts at the impact dates. The dimensions are step/cash-flow.
   */
  private final double[][] _impactAmount;
  /**
   * The Ibor index of each cash flow.  The dimensions are step/cash-flow.
   */
  private final int[][] _impactIndex;

  /**
   * Constructor.
   * @param pathIborRate The paths Ibor rates. Size: nbJump x nbPeriodLMM x nbPath
   * @param delta The Libor accrual factors.
   * @param impactAmount The reference amounts at the impact dates.
   * @param impactIndex The Ibor index of each cash flow.
   */
  public MonteCarloIborRateDataBundle(double[][][] pathIborRate, double[] delta, double[][] impactAmount, int[][] impactIndex) {
    _pathIborRate = pathIborRate;
    _delta = delta;
    _impactAmount = impactAmount;
    _impactIndex = impactIndex;
  }

  /**
   * Gets the path Ibor rates. Size: nbJump x nbPeriodLMM x nbPath
   * @return The rates.
   */
  public double[][][] getPathIborRate() {
    return _pathIborRate;
  }

  /**
   * Gets the _impactAmount field.
   * @return the _impactAmount
   */
  public double[][] getImpactAmount() {
    return _impactAmount;
  }

  /**
   * Gets the Ibor index of each impact date.
   * @return The Ibor index of each impact date.
   */
  public int[][] getImpactIndex() {
    return _impactIndex;
  }

  /**
   * Gets the Ibor accrual factors.
   * @return The Ibor accrual factors.
   */
  public double[] getDelta() {
    return _delta;
  }

}
