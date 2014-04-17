/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public class FlexibleLatticeSpecification extends LatticeSpecification {

  @Override
  public double[] getParameters(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    final double rootDt = Math.sqrt(dt);
    final double sigRootDt = volatility * rootDt;

    final double ref = 0.5 * Math.log(strike / spot) / sigRootDt + 0.5 * nSteps;
    int position = (int) ref;
    position = Math.abs(ref - position - 1.) < 1.e-12 ? position + 1 : position;
    final double cf = Math.exp(sigRootDt);
    final double mod = Math.pow(strike / spot * Math.pow(cf, nSteps - 2 * position), 1. / nSteps);

    final double upFactor = mod * cf;
    final double downFactor = mod / cf;
    final double upProbability = (Math.exp(interestRate * dt) - downFactor) / (upFactor - downFactor);

    return new double[] {upFactor, downFactor, upProbability, 1. - upProbability };
  }
}
