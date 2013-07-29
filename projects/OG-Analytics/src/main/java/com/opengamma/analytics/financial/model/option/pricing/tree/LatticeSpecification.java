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
   * Compute up Factor, down Factor, up Probability, down Probability
   * @param spot Spot price of underlying
   * @param strike Strike price
   * @param timeToExpiry Time to expiry of option
   * @param volatility Volatility
   * @param interestRate Interest rate
   * @param nSteps Number of Steps
   * @param dt Time step, that is, dt * N = tiemToExpiry
   * @return {up Factor, down Factor, up Probability, down Probability}
   */
  public abstract double[] getParameters(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt);

  /**
   * If (up factor)*(down factor)=1 is satisfied, e.g., CRR and Tian specifications, simpler approximation can be used
   * @param spot Spot
   * @param volatility Volatility
   * @param interestRate Interest rate
   * @param dt Time step
   * @param greeksTmp {price_{0,0}, delta_{0,0}, gamma_{0,0}, price_{2,1}}
   * @return Theta 
   */
  public double getTheta(final double spot, final double volatility, final double interestRate, final double dt, final double[] greeksTmp) {

    return interestRate * greeksTmp[0] - interestRate * spot * greeksTmp[1] - 0.5 * volatility * volatility * spot * spot * greeksTmp[2];
  }
}
