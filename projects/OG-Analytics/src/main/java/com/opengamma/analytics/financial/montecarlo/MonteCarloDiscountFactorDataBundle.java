/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo;

/**
 * The data bundle with the Monte Carlo discount factors and the reference amounts.
 */
public class MonteCarloDiscountFactorDataBundle {

  /**
   * The paths discount factors. The dimensions are path/step/cash-flow.
   */
  private final Double[][][] _pathDiscountingFactor;
  /**
   * The reference amounts at the impact dates. The dimensions are step/cash-flow.
   */
  private final double[][] _impactAmount;

  /**
   * Constructor.
   * @param pathDiscountingFactor The paths discount factors.
   * @param impactAmount The reference amounts at the impact dates.
   */
  public MonteCarloDiscountFactorDataBundle(Double[][][] pathDiscountingFactor, double[][] impactAmount) {
    super();
    _pathDiscountingFactor = pathDiscountingFactor;
    _impactAmount = impactAmount;
  }

  /**
   * Gets the path discounting factors.
   * @return The path discounting factors.
   */
  public Double[][][] getPathDiscountingFactor() {
    return _pathDiscountingFactor;
  }

  /**
   * Gets the impact amounts.
   * @return The impact amounts.
   */
  public double[][] getImpactAmount() {
    return _impactAmount;
  }

}
