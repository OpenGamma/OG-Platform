/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public class CoxRossRubinsteinLatticeSpecification extends LatticeSpecification {

  @Override
  public double[] getParameters(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    final double upFactor = Math.exp(volatility * Math.sqrt(dt));
    final double downFactor = 1. / upFactor;
    final double upProbability = (Math.exp(interestRate * dt) - downFactor) / (upFactor - downFactor);

    return new double[] {upFactor, downFactor, upProbability, 1. - upProbability };
  }

  @Override
  public double getTheta(final double spot, final double volatility, final double interestRate, final double dt, final double[] greeksTmp) {
    return 0.5 * (greeksTmp[3] - greeksTmp[0]) / dt;
  }
}
