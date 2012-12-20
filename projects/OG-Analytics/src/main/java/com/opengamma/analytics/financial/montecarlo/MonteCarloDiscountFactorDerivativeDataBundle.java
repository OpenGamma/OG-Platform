/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo;

/**
 * The data bundle with the Monte Carlo discount factors and the reference amounts.
 */
public class MonteCarloDiscountFactorDerivativeDataBundle extends MonteCarloDiscountFactorDataBundle {
  /**
   * The derivatives of the paths discount factors. The dimensions are path/step/cash-flow.
   */
  private Double[][][] _pathDiscountingFactorDerivative;
  /**
   * The derivatives of the reference amounts at the impact dates. The dimensions are step/cash-flow.
   */
  private double[][] _impactAmountDerivative;

  /**
   * Constructor.
   * @param pathDiscountingFactor The paths discount factors.
   * @param impactAmount The reference amounts at the impact dates.
   */
  public MonteCarloDiscountFactorDerivativeDataBundle(Double[][][] pathDiscountingFactor, double[][] impactAmount) {
    super(pathDiscountingFactor, impactAmount);
    _pathDiscountingFactorDerivative = new Double[0][0][0];
    _impactAmountDerivative = new double[0][0];
  }

  /**
   * Gets the derivatives of path discounting factors.
   * @return The derivatives of path discounting factors.
   */
  public Double[][][] getPathDiscountingFactorDerivative() {
    return _pathDiscountingFactorDerivative;
  }

  /**
   * Sets the derivatives of path discounting factors.
   * @param pathDiscountingFactorDerivative The derivatives of path discounting factors.
   */
  public void setPathDiscountingFactorDerivative(Double[][][] pathDiscountingFactorDerivative) {
    _pathDiscountingFactorDerivative = pathDiscountingFactorDerivative;
  }

  /**
   * Gets the derivatives of impact amounts.
   * @return The derivatives of impact amounts.
   */
  public double[][] getImpactAmountDerivative() {
    return _impactAmountDerivative;
  }

  /**
   * Sets the derivatives of impact amounts.
   * @param impactAmountDerivative The derivatives of impact amounts.
   */
  public void setImpactAmountDerivative(double[][] impactAmountDerivative) {
    _impactAmountDerivative = impactAmountDerivative;
  }

}
