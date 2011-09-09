/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.montecarlo;

/**
 * The data bundle with the Monte Carlo discount factors and the reference amounts.
 */
public class MonteCarloDiscountFactorDataBundle {

  /**
   * The paths discount factors. The dimensions are path/step/cash-flow.
   */
  private final Double[][][] _pathDiscountingFactors;
  /**
   * The reference amounts at the impact dates. The dimensions are step/cash-flow.
   */
  private final double[][] _impactAmount;

  /**
   * Constructor.
   * @param pathDiscountingFactors The paths discount factors.
   * @param impactAmount The reference amounts at the impact dates.
   */
  public MonteCarloDiscountFactorDataBundle(Double[][][] pathDiscountingFactors, double[][] impactAmount) {
    super();
    _pathDiscountingFactors = pathDiscountingFactors;
    _impactAmount = impactAmount;
  }

  /**
   * Gets the _pathDiscountingFactors field.
   * @return the _pathDiscountingFactors
   */
  public Double[][][] getPathDiscountingFactors() {
    return _pathDiscountingFactors;
  }

  /**
   * Gets the _impactAmount field.
   * @return the _impactAmount
   */
  public double[][] getImpactAmount() {
    return _impactAmount;
  }

}
