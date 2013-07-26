/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.apache.commons.lang.Validate;

/**
 * 
 * The formula is valid for odd number of steps. 
 * The even case is NOT implemented (Since formula for even number of steps has the convergence of order -1, there is no advantage to use Leisen-Reimer.) 
 */
public class LeisenReimerLatticeSpecification extends LatticeSpecification {

  @Override
  public double[] getParameters(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    Validate.isTrue((nSteps % 2 == 1), "The number of steps should be odd");
    final double sigmaRootT = volatility * Math.sqrt(timeToExpiry);
    final double d1 = (Math.log(spot / strike) + interestRate * timeToExpiry) / sigmaRootT + 0.5 * sigmaRootT;
    final double d2 = d1 - sigmaRootT;
    final double sig1 = d1 >= 0. ? 1. : -1.;
    final double sig2 = d2 >= 0. ? 1. : -1.;
    final double coef1 = d1 / (nSteps + 1. / 3. + 0.1 / (nSteps + 1.));
    final double coef2 = d2 / (nSteps + 1. / 3. + 0.1 / (nSteps + 1.));
    final double p1 = 0.5 + sig1 * 0.5 * Math.sqrt(1. - Math.exp(-coef1 * coef1 * (nSteps + 1. / 6.)));
    final double p2 = 0.5 + sig2 * 0.5 * Math.sqrt(1. - Math.exp(-coef2 * coef2 * (nSteps + 1. / 6.)));
    final double rr = Math.exp(interestRate * dt);
    final double upFactor = rr * p1 / p2;
    final double downFactor = (rr - p2 * upFactor) / (1 - p2);

    return new double[] {upFactor, downFactor, p2, 1 - p2 };
  }
}
