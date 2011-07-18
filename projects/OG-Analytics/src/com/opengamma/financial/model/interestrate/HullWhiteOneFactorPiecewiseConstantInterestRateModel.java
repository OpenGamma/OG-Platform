/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RidderSingleRootFinder;

/**
 * Methods related to the Hull-White one factor (extended Vasicek) model with piecewise constant volatility.
 */
public class HullWhiteOneFactorPiecewiseConstantInterestRateModel {

  /**
   * Computes the future convexity factor used in future pricing.
   * Reference: Henrard, M. The Irony in the derivatives discounting Part II: the crisis. Wilmott Journal, 2010, 2, 301-316
   * @param future The future security.
   * @param data The Hull-White model parameters.
   * @return The factor.
   */
  public double futureConvexityFactor(final InterestRateFutureSecurity future, final HullWhiteOneFactorPiecewiseConstantParameters data) {
    double t0 = future.getLastTradingTime();
    double t1 = future.getFixingPeriodStartTime();
    double t2 = future.getFixingPeriodEndTime();
    double factor1 = Math.exp(-data.getMeanReversion() * t1) - Math.exp(-data.getMeanReversion() * t2);
    double numerator = 2 * data.getMeanReversion() * data.getMeanReversion() * data.getMeanReversion();
    int indexT0 = 1; // Period in which the time t0 is; _volatilityTime[i-1] <= t0 < _volatilityTime[i];
    while (t0 > data.getVolatilityTime()[indexT0]) {
      indexT0++;
    }
    double[] s = new double[indexT0 + 1];
    System.arraycopy(data.getVolatilityTime(), 0, s, 0, indexT0);
    s[indexT0] = t0;
    double factor2 = 0.0;
    for (int loopperiod = 0; loopperiod < indexT0; loopperiod++) {
      factor2 += data.getVolatility()[loopperiod] * data.getVolatility()[loopperiod] * (Math.exp(data.getMeanReversion() * s[loopperiod + 1]) - Math.exp(data.getMeanReversion() * s[loopperiod]))
          * (2 - Math.exp(-data.getMeanReversion() * (t2 - s[loopperiod + 1])) - Math.exp(-data.getMeanReversion() * (t2 - s[loopperiod])));
    }
    return Math.exp(factor1 / numerator * factor2);
  }

  /**
   * Computes the (zero-coupon) bond volatility divided by a bond numeraire for a given period. 
   * @param startExpiry Start time of the expiry period.
   * @param endExpiry End time of the expiry period.
   * @param numeraireTime Time to maturity for the bond numeraire.
   * @param bondMaturity Time to maturity for the bond.
   * @param data Hull-White model data.
   * @return The re-based bond volatility.
   */
  public double alpha(final double startExpiry, final double endExpiry, final double numeraireTime, final double bondMaturity, final HullWhiteOneFactorPiecewiseConstantParameters data) {
    double factor1 = Math.exp(-data.getMeanReversion() * numeraireTime) - Math.exp(-data.getMeanReversion() * bondMaturity);
    double numerator = 2 * data.getMeanReversion() * data.getMeanReversion() * data.getMeanReversion();
    int indexStart = 1; // Period in which the time startExpiry is; _volatilityTime[i-1] <= startExpiry < _volatilityTime[i];
    while (startExpiry > data.getVolatilityTime()[indexStart]) {
      indexStart++;
    }
    int indexEnd = indexStart; // Period in which the time endExpiry is; _volatilityTime[i-1] <= endExpiry < _volatilityTime[i];
    while (endExpiry > data.getVolatilityTime()[indexEnd]) {
      indexEnd++;
    }
    int sLen = indexEnd - indexStart + 1;
    double[] s = new double[sLen + 1];
    s[0] = startExpiry;
    System.arraycopy(data.getVolatilityTime(), indexStart, s, 1, sLen - 1);
    s[sLen] = endExpiry;
    double factor2 = 0.0;
    for (int loopperiod = 0; loopperiod < sLen; loopperiod++) {
      factor2 += data.getVolatility()[loopperiod + indexStart - 1] * data.getVolatility()[loopperiod + indexStart - 1]
          * (Math.exp(2 * data.getMeanReversion() * s[loopperiod + 1]) - Math.exp(2 * data.getMeanReversion() * s[loopperiod]));
    }
    return factor1 * Math.sqrt(factor2 / numerator);
  }

  /**
   * The adjoint version of the method. Computes the (zero-coupon) bond volatility divided by a bond numeraire for a given period. 
   * @param startExpiry Start time of the expiry period.
   * @param endExpiry End time of the expiry period.
   * @param numeraireTime Time to maturity for the bond numeraire.
   * @param bondMaturity Time to maturity for the bond.
   * @param data Hull-White model data.
   * @param derivatives Array used for return the derivatives with respect to the input. The array is changed by the method. The derivatives of the function alpha
   * with respect to the piecewise constant volatilities.
   * @return The re-based bond volatility.
   */
  public double alpha(final double startExpiry, final double endExpiry, final double numeraireTime, final double bondMaturity, final HullWhiteOneFactorPiecewiseConstantParameters data,
      double[] derivatives) {
    // Forward sweep
    double factor1 = Math.exp(-data.getMeanReversion() * numeraireTime) - Math.exp(-data.getMeanReversion() * bondMaturity);
    double numerator = 2 * data.getMeanReversion() * data.getMeanReversion() * data.getMeanReversion();
    int indexStart = 1; // Period in which the time startExpiry is; _volatilityTime[i-1] <= startExpiry < _volatilityTime[i];
    while (startExpiry > data.getVolatilityTime()[indexStart]) {
      indexStart++;
    }
    int indexEnd = indexStart; // Period in which the time endExpiry is; _volatilityTime[i-1] <= endExpiry < _volatilityTime[i];
    while (endExpiry > data.getVolatilityTime()[indexEnd]) {
      indexEnd++;
    }
    int sLen = indexEnd - indexStart + 1;
    double[] s = new double[sLen + 1];
    s[0] = startExpiry;
    System.arraycopy(data.getVolatilityTime(), indexStart, s, 1, sLen - 1);
    s[sLen] = endExpiry;
    double factor2 = 0.0;
    for (int loopperiod = 0; loopperiod < sLen; loopperiod++) {
      factor2 += data.getVolatility()[loopperiod + indexStart - 1] * data.getVolatility()[loopperiod + indexStart - 1]
          * (Math.exp(2 * data.getMeanReversion() * s[loopperiod + 1]) - Math.exp(2 * data.getMeanReversion() * s[loopperiod]));
    }
    double alpha = factor1 * Math.sqrt(factor2 / numerator);
    // Backward sweep 
    double alphaBar = 1.0;
    double factor2Bar = factor1 / Math.sqrt(factor2 / numerator) / 2.0 / numerator * alphaBar;
    int nbSigma = data.getVolatility().length;
    for (int loopperiod = 0; loopperiod < nbSigma; loopperiod++) { // To clean derivatives
      derivatives[loopperiod] = 0.0;
    }
    for (int loopperiod = 0; loopperiod < sLen; loopperiod++) {
      derivatives[loopperiod + indexStart - 1] = 2 * data.getVolatility()[loopperiod + indexStart - 1]
          * (Math.exp(2 * data.getMeanReversion() * s[loopperiod + 1]) - Math.exp(2 * data.getMeanReversion() * s[loopperiod])) * factor2Bar;
    }
    return alpha;
  }

  /**
   * Computes the exercise boundary for swaptions.
   * Reference: Henrard, M. (2003). Explicit bond option and swaption formula in Heath-Jarrow-Morton one-factor model. International Journal of Theoretical and Applied Finance, 6(1):57--72.
   * @param discountedCashFlow The cash flow equivalent discounted to today.
   * @param alpha The zero-coupon bond volatilities.
   * @return The exercise boundary.
   */
  public double kappa(final double[] discountedCashFlow, final double[] alpha) {
    final Function1D<Double, Double> swapValue = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        double error = 0.0;
        for (int loopcf = 0; loopcf < alpha.length; loopcf++) {
          error += discountedCashFlow[loopcf] * Math.exp(-0.5 * alpha[loopcf] * alpha[loopcf] - (alpha[loopcf] - alpha[0]) * x);
        }
        return error;
      }
    };
    final BracketRoot bracketer = new BracketRoot();
    double accuracy = 1.0E-8;
    final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(accuracy);
    final double[] range = bracketer.getBracketedPoints(swapValue, -2.0, 2.0);
    return rootFinder.getRoot(swapValue, range[0], range[1]);
  }

}
