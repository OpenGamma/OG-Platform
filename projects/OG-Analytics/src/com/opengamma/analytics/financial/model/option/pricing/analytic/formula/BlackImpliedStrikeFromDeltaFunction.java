/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Function to compute the strike from a delta in the Black function. The delta is the delta with respect to the forward.
 */
public class BlackImpliedStrikeFromDeltaFunction {

  /**
   * The normal distribution implementation sued in the computations.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Computes the implied strike from delta and volatility in the Black formula.
   * @param delta The option delta
   * @param isCall The call (true) / put (false) flag.
   * @param forward The forward.
   * @param time The time to expiration.
   * @param volatility The volatility.
   * @return The strike.
   */
  public static double impliedStrike(final double delta, final boolean isCall, final double forward, final double time, final double volatility) {
    Validate.isTrue(isCall ^ (delta < 0), "Delta incompatible with call/put: " + isCall + ", " + delta);
    Validate.isTrue(forward > 0, "Forward negative");
    final double omega = (isCall ? 1.0 : -1.0);
    final double strike = forward * Math.exp(-volatility * Math.sqrt(time) * omega * NORMAL.getInverseCDF(omega * delta) + volatility * volatility * time / 2);
    return strike;
  }

}
