/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public abstract class LatticeSpecification {

  /**
   * Compute up factor, down factor, up probability, down probability
   * @param spot Spot price of underlying
   * @param strike Strike price
   * @param timeToExpiry Time to expiry of option
   * @param volatility Volatility
   * @param interestRate Interest rate
   * @param nSteps Number of Steps
   * @param dt Time step, that is, dt * N = tiemToExpiry
   * @return {up factor, down factor, up probability, down probability}
   */
  public abstract double[] getParameters(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt);

  /**
   * If (up factor)*(down factor)=1 is satisfied, e.g., CRR specification, simpler approximation can be used
   * @param spot Spot
   * @param volatility Volatility
   * @param interestRate Interest rate
   * @param dividend Continuous dividend
   * @param dt Time step
   * @param greeksTmp {price_{0,0}, delta_{0,0}, gamma_{0,0}, price_{2,1}}
   * @return Theta 
   */
  public double getTheta(final double spot, final double volatility, final double interestRate, final double dividend, final double dt, final double[] greeksTmp) {

    return interestRate * greeksTmp[0] - (interestRate - dividend) * spot * greeksTmp[1] - 0.5 * volatility * volatility * spot * spot * greeksTmp[2];
  }

  /**
   * Compute up factor, middle factor, down factor, up probability, middle probability, down probability
   * @param spot Spot price of underlying
   * @param strike Strike price
   * @param timeToExpiry Time to expiry of option
   * @param volatility Volatility
   * @param interestRate Interest rate
   * @param nSteps Number of Steps
   * @param dt Time step, that is, dt * N = tiemToExpiry
   * @return {up factor, middle factor, down Factor, up Probability, middle probability, down Probability}
   */
  public double[] getParametersTrinomial(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    throw new IllegalArgumentException("This lattice specification does not cover trinomial tree");
  }

}
