/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * Compute price and Greeks of digital (cash-or-nothing) option
 * Cash-or-nothing call option pays 0 if S <= K and K if S > K, whereas cash-or-nothing put option pays 1. if S < K and 0 if S >= K,
 * where S is asset price at expiry.
 */
public class DigitalOptionFunction {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Compute <b>spot</b> price of cash-or-nothing option
   * @param spot The spot
   * @param strike The strike
   * @param timeToExpiry The time to expiry
   * @param lognormalVol The log-normal volatility
   * @param interestRate The interest rate
   * @param costOfCarry The cost-of-carry
   * @param isCall True for calls, false for puts
   * @return The option price
   */
  public static double price(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate, final double costOfCarry, final boolean isCall) {
    ArgumentChecker.isTrue(spot > 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike > 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry > 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol > 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double d = (Math.log(spot / strike) + (costOfCarry - 0.5 * lognormalVol * lognormalVol) * timeToExpiry) / lognormalVol / Math.sqrt(timeToExpiry);
    final double sign = isCall ? 1. : -1.;
    return Math.exp(-interestRate * timeToExpiry) * NORMAL.getCDF(sign * d);
  }

  /**
   * Compute delta of cash-or-nothing option
   * @param spot The spot
   * @param strike The strike
   * @param timeToExpiry The time to expiry
   * @param lognormalVol The log-normal volatility
   * @param interestRate The interest rate
   * @param costOfCarry The cost-of-carry
   * @param isCall True for calls, false for puts
   * @return The option price
   */
  public static double delta(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate,
      final double costOfCarry, final boolean isCall) {
    ArgumentChecker.isTrue(spot > 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike > 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry > 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol > 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double sigmaRootT = lognormalVol * Math.sqrt(timeToExpiry);
    final double d = (Math.log(spot / strike) + (costOfCarry - 0.5 * lognormalVol * lognormalVol) * timeToExpiry) / sigmaRootT;
    final double sign = isCall ? 1. : -1.;
    return Math.exp(-interestRate * timeToExpiry) * (sign * NORMAL.getPDF(d) / spot / sigmaRootT);
  }

  /**
   * Compute gamma of cash-or-nothing option
   * @param spot The spot
   * @param strike The strike
   * @param timeToExpiry The time to expiry
   * @param lognormalVol The log-normal volatility
   * @param interestRate The interest rate
   * @param costOfCarry The cost-of-carry
   * @param isCall True for calls, false for puts
   * @return The option price
   */
  public static double gamma(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate,
      final double costOfCarry, final boolean isCall) {
    ArgumentChecker.isTrue(spot > 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike > 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry > 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol > 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double sigmaRootT = lognormalVol * Math.sqrt(timeToExpiry);
    final double d = (Math.log(spot / strike) + (costOfCarry - 0.5 * lognormalVol * lognormalVol) * timeToExpiry) / sigmaRootT;
    final double sign = isCall ? 1. : -1.;
    return -sign * (Math.exp(-interestRate * timeToExpiry) * NORMAL.getPDF(d) * (1. + d / sigmaRootT) / spot / spot / sigmaRootT);
  }

  /**
   * Compute theta price of cash-or-nothing option
   * @param spot The spot
   * @param strike The strike
   * @param timeToExpiry The time to expiry
   * @param lognormalVol The log-normal volatility
   * @param interestRate The interest rate
   * @param costOfCarry The cost-of-carry
   * @param isCall True for calls, false for puts
   * @return The option price
   */
  public static double theta(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate,
      final double costOfCarry, final boolean isCall) {
    ArgumentChecker.isTrue(spot > 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike > 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry > 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol > 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double d = (Math.log(spot / strike) + (costOfCarry - 0.5 * lognormalVol * lognormalVol) * timeToExpiry) / lognormalVol / Math.sqrt(timeToExpiry);
    final double sign = isCall ? 1. : -1.;
    final double div = 0.5 * (-Math.log(spot / strike) / Math.pow(timeToExpiry, 1.5) + (costOfCarry - 0.5 * lognormalVol * lognormalVol) / Math.pow(timeToExpiry, 0.5)) / lognormalVol;
    return interestRate * Math.exp(-interestRate * timeToExpiry) * NORMAL.getCDF(sign * d) - sign * Math.exp(-interestRate * timeToExpiry) * NORMAL.getPDF(d) * div;
  }

  /**
   * Compute driftless (forward) theta price of cash-or-nothing option
   * @param forward The forward
   * @param strike The strike
   * @param timeToExpiry The time to expiry
   * @param lognormalVol The log-normal volatility
   * @param isCall True for calls, false for puts
   * @return The option price
   */
  public static double driftlessTheta(final double forward, final double strike, final double timeToExpiry, final double lognormalVol, final boolean isCall) {
    ArgumentChecker.isTrue(forward > 0.0, "negative/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(strike > 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry > 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol > 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);

    final double sigmaRootT = lognormalVol * Math.sqrt(timeToExpiry);
    final double d = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final double sign = isCall ? 1. : -1.;
    final double div = 0.5 * (-Math.log(forward / strike) / Math.pow(timeToExpiry, 1.5) / lognormalVol - 0.5 * lognormalVol / Math.pow(timeToExpiry, 0.5));
    return -sign * NORMAL.getPDF(d) * div;
  }

  /**
   * Compute vega price of cash-or-nothing option
   * @param spot The spot
   * @param strike The strike
   * @param timeToExpiry The time to expiry
   * @param lognormalVol The log-normal volatility
   * @param interestRate The interest rate
   * @param costOfCarry The cost-of-carry
   * @param isCall True for calls, false for puts
   * @return The option price
   */
  public static double vega(final double spot, final double strike, final double timeToExpiry, final double lognormalVol, final double interestRate,
      final double costOfCarry, final boolean isCall) {
    ArgumentChecker.isTrue(spot > 0.0, "negative/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike > 0.0, "negative/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry > 0.0, "negative/NaN timeToExpiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(lognormalVol > 0.0, "negative/NaN lognormalVol; have {}", lognormalVol);
    ArgumentChecker.isFalse(Double.isNaN(interestRate), "interestRate is NaN");
    ArgumentChecker.isFalse(Double.isNaN(costOfCarry), "costOfCarry is NaN");

    final double rootT = Math.sqrt(timeToExpiry);
    final double d = (Math.log(spot / strike) + (costOfCarry - 0.5 * lognormalVol * lognormalVol) * timeToExpiry) / lognormalVol / rootT;
    final double sign = isCall ? 1. : -1.;
    final double div = -(Math.log(spot / strike) + costOfCarry * timeToExpiry) / lognormalVol / lognormalVol / rootT - 0.5 * rootT;
    return sign * Math.exp(-interestRate * timeToExpiry) * NORMAL.getPDF(d) * div;
  }
}
