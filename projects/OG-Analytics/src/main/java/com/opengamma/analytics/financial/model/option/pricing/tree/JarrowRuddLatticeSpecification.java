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

  @Override
  public double[] getParametersTrinomial(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    final double lambda = Math.sqrt(1.5);
    final double rootT = Math.sqrt(dt);
    final double halfVolSq = 0.5 * volatility * volatility;
    final double nu = interestRate - halfVolSq;

    final double u = Math.exp(lambda * volatility * rootT);
    final double d = 1 / u;
    final double logK = halfVolSq * dt;
    final double k = Math.exp(logK);
    final double kQuar = Math.exp(4. * logK);

    final double upProbability = (kQuar - (d + 1.) * k + d) / (u - d) / (u - 1.);
    final double downProbability = (kQuar - (u + 1.) * k + u) / (u - d) / (1. - d);

    final double middleFactor = Math.exp(nu * dt);
    final double upFactor = middleFactor * u;
    final double downFactor = middleFactor / u;

    return new double[] {upFactor, middleFactor, downFactor, upProbability, 1. - upProbability - downProbability, downProbability };
  }
}
