/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public class JabbourKraminYoungLatticeSpecification extends LatticeSpecification {

  @Override
  public double[] getParameters(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    final double mudt = (interestRate - 0.5 * volatility * volatility) * dt;
    final double sigmaRootT = volatility * Math.sqrt(dt);
    final double upProbability = 0.5 * (1. - sigmaRootT / Math.sqrt(4. + volatility * volatility * dt));
    final double den = Math.sqrt(upProbability * (1. - upProbability));
    final double upFactor = Math.exp(mudt + (1. - upProbability) * sigmaRootT / den);
    final double downFactor = Math.exp(mudt - upProbability * sigmaRootT / den);

    return new double[] {upFactor, downFactor, upProbability, 1. - upProbability };
  }
}
