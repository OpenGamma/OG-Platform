/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * Function to compute the strike from a delta in the Black function. The delta is the delta with respect to the forward.
 */
public class BlackImpliedStrikeFromDeltaFunction {

  /**
   * The normal distribution implementation sued in the computations.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  public static double impliedStrike(final double delta, final boolean isCall, final double forward, final double time, final double volatility) {
    Validate.isTrue(isCall ^ (delta < 0), "Delta incompatible with call/put");
    Validate.isTrue(forward > 0, "Forward negative");
    double omega = (isCall ? 1.0 : -1.0);
    double strike = forward * Math.exp(-volatility * Math.sqrt(time) * omega * NORMAL.getInverseCDF(omega * delta) + volatility * volatility * time / 2);
    return strike;
  }

}
