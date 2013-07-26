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

}
