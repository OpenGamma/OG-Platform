/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public class TianLatticeSpecification extends LatticeSpecification {

  @Override
  public double[] getParameters(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    final double sigSqT = volatility * volatility * dt;
    final double rv = Math.exp(interestRate * dt + sigSqT);
    final double v = Math.exp(sigSqT);
    final double upFactor = 0.5 * rv * (v + 1. + Math.sqrt(v * v + 2. * v - 3.));
    final double downFactor = 0.5 * rv * (v + 1. - Math.sqrt(v * v + 2. * v - 3.));
    final double upProbability = (Math.exp(interestRate * dt) - downFactor) / (upFactor - downFactor);

    return new double[] {upFactor, downFactor, upProbability, 1 - upProbability };
  }

  @Override
  public double[] getParametersTrinomial(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    final double m = Math.exp(interestRate * dt);
    final double v = Math.exp(volatility * volatility * dt);
    final double k = m * (v + 3.) / 4.;
    final double middleFactor = 0.5 * m * (3. - v);
    final double part = Math.sqrt(k * k - middleFactor * middleFactor);
    final double upFactor = k + part;
    final double downFactor = k - part;

    return new double[] {upFactor, middleFactor, downFactor, 1. / 3., 1. / 3., 1. / 3. };
  }
}
