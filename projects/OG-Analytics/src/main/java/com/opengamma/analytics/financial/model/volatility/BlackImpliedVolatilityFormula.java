/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the implied volatility in a log-normally (Black) distributed asset price world.
 */
public class BlackImpliedVolatilityFormula {

  /** Limit defining "close of ATM forward" to avoid the formula singularity. **/
  private static final double ATM_LIMIT = 1.0E-3;
  private static final double ROOT_ACCURACY = 1.0E-7;
  private static final NewtonRaphsonSingleRootFinder ROOT_FINDER = new NewtonRaphsonSingleRootFinder(ROOT_ACCURACY);
 
  /**
   * Computes the implied volatility from the price in a log-normally distributed asset price world.
   * @param data The model data. The data volatility is not used.
   * @param option The option.
   * @param optionPrice The option price.
   * @return The implied volatility.
   */
  public double getImpliedVolatility(
      BlackFunctionData data, 
      EuropeanVanillaOption option, 
      double optionPrice) {
    Validate.notNull(data, "null data");
    Validate.notNull(option, "null option");
    final double discountFactor = data.getDiscountFactor();
    final boolean isCall = option.isCall();
    final double f = data.getForward();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double fwdPrice = optionPrice / discountFactor;
    return BlackFormulaRepository.impliedVolatility(fwdPrice, f, k, t, isCall);
  }
  
  /**
   * Compute the implied volatility from a normal volatility using an approximate initial guess and a root-finder.
   * <p>
   * The forward and the strike must be positive.
   * <p>
   * Reference: Hagan, P. S. Volatility conversion calculator. Technical report, Bloomberg.
   * 
   * @param forward  the forward rate/price
   * @param strike  the option strike
   * @param timeToExpiry  the option time to maturity
   * @param normalVolatility  the Black implied volatility
   * @return the implied volatility
   */
  public static double impliedVolatilityFromNormalApproximated(
      final double forward,
      final double strike,
      final double timeToExpiry,
      final double normalVolatility) {
    ArgumentChecker.isTrue(strike > 0, "strike must be strictly positive");
    ArgumentChecker.isTrue(forward > 0, "strike must be strictly positive");
    // initial guess
    double guess = impliedVolatilityFromNormalApproximated2(forward, strike, timeToExpiry, normalVolatility);
    // Newton-Raphson method
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      private static final long serialVersionUID = 1L;
      @Override
      public Double evaluate(Double volatility) {
        return NormalImpliedVolatilityFormula
            .impliedVolatilityFromBlackApproximated(forward, strike, timeToExpiry, volatility) - normalVolatility;
      }
    };
    return ROOT_FINDER.getRoot(func, guess);
  }
  
  /**
   * Compute the implied volatility from a normal volatility using an approximate explicit. 
   * <p>
   * The formula is usually not good enough to be used as such, but provide a good initial guess for a 
   * root-finding procedure. Use {@link BlackImpliedVolatilityFormula#impliedVolatilityFromNormalApproximated} for
   * more precision.
   * <p>
   * The forward and the strike must be positive.
   * <p>
   * Reference: Hagan, P. S. Volatility conversion calculator. Technical report, Bloomberg.
   * 
   * @param forward  the forward rate/price
   * @param strike  the option strike
   * @param timeToExpiry  the option time to maturity
   * @param normalVolatility  the Black implied volatility
   * @return the implied volatility
   */
  public static double impliedVolatilityFromNormalApproximated2(
      double forward,
      double strike,
      double timeToExpiry,
      double normalVolatility) {
    ArgumentChecker.isTrue(strike > 0, "strike must be strctly positive");
    ArgumentChecker.isTrue(forward > 0, "strike must be strctly positive");
    double lnFK = Math.log(forward / strike);
    double s2t = normalVolatility * normalVolatility * timeToExpiry;
    if (Math.abs((forward - strike) / strike) < ATM_LIMIT) {
      double factor1 = 1.0d / Math.sqrt(forward * strike);
      double factor2 = (1.0d + s2t / (24.0d * forward * strike)) / (1.0d + lnFK * lnFK / 24.0d);
      return normalVolatility * factor1 * factor2;
    }
    double factor1 = lnFK / (forward - strike);
    double factor2 = (1.0d + (1.0d - lnFK * lnFK / 120.0d) * s2t / (24.0d * forward * strike));
    return normalVolatility * factor1 * factor2;
  }

}
