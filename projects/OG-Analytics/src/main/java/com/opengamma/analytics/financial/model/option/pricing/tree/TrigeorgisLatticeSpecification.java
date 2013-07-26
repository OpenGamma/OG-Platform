/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public class TrigeorgisLatticeSpecification extends LatticeSpecification {

  @Override
  public double[] getParameters(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    final double mudt = (interestRate - 0.5 * volatility * volatility) * dt;
    final double dx = Math.sqrt(volatility * volatility * dt + mudt * mudt);
    final double upFactor = Math.exp(dx);
    final double downFactor = Math.exp(-dx);
    final double upProbability = 0.5 + 0.5 * mudt / dx;

    return new double[] {upFactor, downFactor, upProbability, 1. - upProbability };
  }
}
