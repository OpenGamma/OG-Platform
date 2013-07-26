/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public class JarrowRuddLatticeSpecification extends LatticeSpecification {

  @Override
  public double[] getParameters(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    final double mudt = (interestRate - 0.5 * volatility * volatility) * dt;
    final double sigmaRootT = volatility * Math.sqrt(dt);
    final double upFactor = Math.exp(mudt + sigmaRootT);
    final double downFactor = Math.exp(mudt - sigmaRootT);

    return new double[] {upFactor, downFactor, 0.5, 0.5 };
  }
}
