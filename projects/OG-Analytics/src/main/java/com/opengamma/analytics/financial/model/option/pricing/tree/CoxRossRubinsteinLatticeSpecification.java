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
  public double getTheta(final double spot, final double volatility, final double interestRate, final double dividend, final double dt, final double[] greeksTmp) {
    return 0.5 * (greeksTmp[3] - greeksTmp[0]) / dt;
  }

  @Override
  public double[] getParametersTrinomial(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    final double lambda = Math.sqrt(1.5);
    final double rootT = Math.sqrt(dt);
    final double lambdaSq = 1.5;
    final double nu = interestRate - 0.5 * volatility * volatility;
    final double dx = lambda * volatility * rootT;
    final double upFactor = Math.exp(dx);
    final double middleFactor = 1.;
    final double downFactor = Math.exp(-dx);
    final double upProbability = 0.5 / lambdaSq + 0.5 * nu * rootT / lambda / volatility;
    final double middleProbability = 1. - 1. / lambdaSq;
    final double downProbability = 0.5 / lambdaSq - 0.5 * nu * rootT / lambda / volatility;

    return new double[] {upFactor, middleFactor, downFactor, upProbability, middleProbability, downProbability };
  }

  /**
   * Parameters for two-dimensional extension of CRR trinomial model, here lambda = 1. is used
   * This choice results in negative probabilities, but is numerically stable
   * @param volatility Volatility
   * @param interestRate The interest rate
   * @param dt Time step
   * @return  {up factor, middle factor, down Factor, up Probability, middle probability, down Probability}
   */
  public double[] getParametersTrinomial(final double volatility, final double interestRate, final double dt) {
    final double lambda = Math.sqrt(1.);
    final double rootT = Math.sqrt(dt);
    final double lambdaSq = 1.;
    final double nu = interestRate - 0.5 * volatility * volatility;
    final double dx = lambda * volatility * rootT;
    final double upFactor = Math.exp(dx);
    final double middleFactor = 1.;
    final double downFactor = Math.exp(-dx);
    final double upProbability = 0.5 / lambdaSq + 0.5 * nu * rootT / lambda / volatility;
    final double middleProbability = 1. - 1. / lambdaSq;
    final double downProbability = 0.5 / lambdaSq - 0.5 * nu * rootT / lambda / volatility;

    return new double[] {upFactor, middleFactor, downFactor, upProbability, middleProbability, downProbability };
  }
}
