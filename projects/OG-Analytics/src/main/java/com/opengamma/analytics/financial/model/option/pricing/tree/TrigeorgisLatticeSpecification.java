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

  @Override
  public double getTheta(final double spot, final double volatility, final double interestRate, final double dividend, final double dt, final double[] greeksTmp) {
    return 0.5 * (greeksTmp[3] - greeksTmp[0]) / dt;
  }

  @Override
  public double[] getParametersTrinomial(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    final double volSq = volatility * volatility;
    final double mu = interestRate - 0.5 * volSq;
    final double mudt = mu * dt;
    final double mudtSq = mudt * mudt;
    final double dx = volatility * Math.sqrt(3. * dt);
    final double upFactor = Math.exp(dx);
    final double downFactor = Math.exp(-dx);

    final double part = (volSq * dt + mudtSq) / dx / dx;
    final double upProbability = 0.5 * (part + mudt / dx);
    final double middleProbability = 1. - part;
    final double downProbability = 0.5 * (part - mudt / dx);

    return new double[] {upFactor, 1., downFactor, upProbability, middleProbability, downProbability };
  }
}
