/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * Computes the implied volatility from the price in a normally distributed asset price world.
 */
public class NormalImpliedVolatilityFormula {

  /**
   * The function used to compute the price with normal hypothesis.
   */
  private static final NormalPriceFunction NORMAL_PRICE_FUNCTION = new NormalPriceFunction();
  /**
   * The maximal number of iterations in the root solving algorithm.
   */
  private static final int MAX_ITERATIONS = 100;
  /**
   * The solution precision.
   */
  private static final double EPS = 1e-15;

  /** Limit defining "close of ATM forward" to avoid the formula singularity. **/
  private static final double ATM_LIMIT = 1.0E-3;

  /**
   * Computes the implied volatility from the price in a normally distributed asset price world.
   * @param data The model data. The data volatility, if not zero, is used as a starting point for the volatility search.
   * @param option The option.
   * @param optionPrice The option price.
   * @return The implied volatility.
   */
  public double getImpliedVolatility(final NormalFunctionData data, final EuropeanVanillaOption option, final double optionPrice) {
    final double numeraire = data.getNumeraire();
    final boolean isCall = option.isCall();
    final double f = data.getForward();
    final double k = option.getStrike();
    final double intrinsicPrice = numeraire * Math.max(0, (isCall ? 1 : -1) * (f - k));
    Validate.isTrue(optionPrice > intrinsicPrice || CompareUtils.closeEquals(optionPrice, intrinsicPrice, 1e-6), "option price (" + optionPrice + ") less than intrinsic value (" + intrinsicPrice
        + ")");
    if (Double.doubleToLongBits(optionPrice) == Double.doubleToLongBits(intrinsicPrice)) {
      return 0.0;
    }
    double sigma = (Math.abs(data.getNormalVolatility()) < 1E-10 ? 0.3 * f : data.getNormalVolatility());
    NormalFunctionData newData = new NormalFunctionData(f, numeraire, sigma);
    final double maxChange = 0.5 * f;
    double[] priceDerivative = new double[3];
    double price = NORMAL_PRICE_FUNCTION.getPriceAdjoint(option, newData, priceDerivative);
    double vega = priceDerivative[1];
    double change = (price - optionPrice) / vega;
    double sign = Math.signum(change);
    change = sign * Math.min(maxChange, Math.abs(change));
    if (change > 0 && change > sigma) {
      change = sigma;
    }
    int count = 0;
    while (Math.abs(change) > EPS) {
      sigma -= change;
      newData = new NormalFunctionData(f, numeraire, sigma);
      price = NORMAL_PRICE_FUNCTION.getPriceAdjoint(option, newData, priceDerivative);
      vega = priceDerivative[1];
      change = (price - optionPrice) / vega;
      sign = Math.signum(change);
      change = sign * Math.min(maxChange, Math.abs(change));
      if (change > 0 && change > sigma) {
        change = sigma;
      }
      if (count++ > MAX_ITERATIONS) {
        final BracketRoot bracketer = new BracketRoot();
        final BisectionSingleRootFinder rootFinder = new BisectionSingleRootFinder(EPS);
        final Function1D<Double, Double> func = new Function1D<Double, Double>() {
          private static final long serialVersionUID = 1L;

          @SuppressWarnings({"synthetic-access" })
          @Override
          public Double evaluate(final Double volatility) {
            final NormalFunctionData myData = new NormalFunctionData(data.getForward(), data.getNumeraire(), volatility);
            return NORMAL_PRICE_FUNCTION.getPriceFunction(option).evaluate(myData) - optionPrice;
          }
        };
        final double[] range = bracketer.getBracketedPoints(func, 0.0, 10.0);
        return rootFinder.getRoot(func, range[0], range[1]);
      }
    }
    return sigma;
  }
  
  /**
   * Computes the implied volatility using an approximate explicit transformation formula.
   * <p>
   * The forward and the strike must be positive.
   * <p>
   * Reference: Hagan, P. S. Volatility conversion calculator. Technical report, Bloomberg.
   * 
   * @param forward  the forward rate/price
   * @param strike  the option strike
   * @param timeToExpiry  the option time to maturity
   * @param blackVolatility  the Black implied volatility
   * @return the implied volatility
   */
  public static double impliedVolatilityFromBlackApproximated(
      double forward,
      double strike,
      double timeToExpiry,
      double blackVolatility) {
    ArgumentChecker.isTrue(strike > 0, "strike must be strctly positive");
    ArgumentChecker.isTrue(forward > 0, "strike must be strctly positive");
    double lnFK = Math.log(forward / strike);
    double s2t = blackVolatility * blackVolatility * timeToExpiry;
    if (Math.abs((forward - strike) / strike) < ATM_LIMIT) {
      double factor1 = Math.sqrt(forward * strike);
      double factor2 = (1.0d + lnFK * lnFK / 24.0d) / (1.0d + s2t / 24.0d + s2t * s2t / 5670.0d);
      return blackVolatility * factor1 * factor2;
    }
    double factor1 = (forward - strike) / lnFK;
    double factor2 = 1.0d / (1.0d + (1.0d - lnFK * lnFK / 120.0d) / 24.0d * s2t + s2t * s2t / 5670.0d);
    return blackVolatility * factor1 * factor2;
  }
  
}
