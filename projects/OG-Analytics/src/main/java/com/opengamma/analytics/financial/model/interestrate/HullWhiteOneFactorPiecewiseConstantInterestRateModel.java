/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import java.util.Arrays;

import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Methods related to the Hull-White one factor (extended Vasicek) model with piecewise constant volatility.
 */
public class HullWhiteOneFactorPiecewiseConstantInterestRateModel {

  /**
   * Computes the future convexity factor used in future pricing.  The factor
   * is called $\gamma$ in the article and is given by
   * $$
   * \begin{equation*}
   * \gamma(t) = \exp\left(\int_t^{t_0} \nu(s,t_2) (\nu(s,t_2)-\nu(s,t_1)) ds \right). 
   * \end{equation*}
   * $$
   * <p>
   * Reference: Henrard, M. The Irony in the derivatives discounting Part II: the crisis. Wilmott Journal, 2010, 2, 301-316
   * @param data The Hull-White model parameters.
   * @param t0 The first expiry time.
   * @param t1 The first reference time.
   * @param t2 The second reference time.
   * @return The factor.
   */
  public double futuresConvexityFactor(final HullWhiteOneFactorPiecewiseConstantParameters data, double t0, double t1, double t2) {
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
   * Computes the future convexity factor used in future pricing. Computes also the derivatives of the factor with respect to the model volatilities.
   * The factor is called $\gamma$ in the article and is given by
   * $$
   * \begin{equation*}
   * \gamma(t) = \exp\left(\int_t^{t_0} \nu(s,t_2) (\nu(s,t_2)-\nu(s,t_1)) ds \right). 
   * \end{equation*}
   * $$
   * <p>
   * Reference: Henrard, M. The Irony in the derivatives discounting Part II: the crisis. Wilmott Journal, 2010, 2, 301-316
   * @param data The Hull-White model parameters.
   * @param t0 The expiry time.
   * @param t1 The first reference time.
   * @param t2 The second reference time.
   * @param derivatives Array used for return the derivatives with respect to the input. The array is changed by the method. The derivatives of the function alpha
   * with respect to the piecewise constant volatilities.
   * @return The factor.
   */
  public double futuresConvexityFactor(final HullWhiteOneFactorPiecewiseConstantParameters data, final double t0, final double t1, final double t2, final double[] derivatives) {
    final int nbSigma = data.getVolatility().length;
    ArgumentChecker.isTrue(derivatives.length == nbSigma, "derivatives vector of incorrect size");
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
    double[] factorExp = new double[indexT0];
    for (int loopperiod = 0; loopperiod < indexT0; loopperiod++) {
      factorExp[loopperiod] = (Math.exp(data.getMeanReversion() * s[loopperiod + 1]) - Math.exp(data.getMeanReversion() * s[loopperiod]))
          * (2 - Math.exp(-data.getMeanReversion() * (t2 - s[loopperiod + 1])) - Math.exp(-data.getMeanReversion() * (t2 - s[loopperiod])));
      factor2 += data.getVolatility()[loopperiod] * data.getVolatility()[loopperiod] * factorExp[loopperiod];
    }
    double factor = Math.exp(factor1 / numerator * factor2);
    // Backward sweep 
    double factorBar = 1.0;
    double factor2Bar = factor1 / numerator * factor * factorBar;
    for (int loopperiod = 0; loopperiod < indexT0; loopperiod++) {
      derivatives[loopperiod] = 2 * data.getVolatility()[loopperiod] * factorExp[loopperiod] * factor2Bar;
    }
    return factor;
  }

  /**
   * Computes the payment delay convexity factor used in coupons with mismatched dates pricing.  The factor
   * is called $\zeta$ in the note and is given by
   * $$
   * \begin{equation*}
   * \zeta = \exp\left(\int_{\theta_0}^{\theta_1} (\nu(s,v)-\nu(s,t_p)) (\nu(s,v)-\nu(s,u)) ds \right). 
   * \end{equation*}
   * $$
   * <p>
   * Reference: Henrard, M. xxx
   * @param parameters The Hull-White model parameters.
   * @param startExpiry The start expiry time.
   * @param endExpiry The end expiry time.
   * @param u The fixing period start time.
   * @param v The fixing period end time.
   * @param tp The payment time.
   * @return The factor.
   */
  public double paymentDelayConvexityFactor(final HullWhiteOneFactorPiecewiseConstantParameters parameters, final double startExpiry, final double endExpiry,
      final double u, final double v, final double tp) {
    final double a = parameters.getMeanReversion();
    final double factor1 = (Math.exp(-a * v) - Math.exp(-a * tp)) * (Math.exp(-a * v) - Math.exp(-a * u));
    final double numerator = 2 * a * a * a;
    int indexStart = Math.abs(Arrays.binarySearch(parameters.getVolatilityTime(), startExpiry) + 1);
    // Period in which the time startExpiry is; _volatilityTime[i-1] <= startExpiry < _volatilityTime[i];
    int indexEnd = Math.abs(Arrays.binarySearch(parameters.getVolatilityTime(), endExpiry) + 1);
    // Period in which the time endExpiry is; _volatilityTime[i-1] <= endExpiry < _volatilityTime[i];
    int sLen = indexEnd - indexStart + 1;
    double[] s = new double[sLen + 1];
    s[0] = startExpiry;
    System.arraycopy(parameters.getVolatilityTime(), indexStart, s, 1, sLen - 1);
    s[sLen] = endExpiry;
    double factor2 = 0.0;
    double[] exp2as = new double[sLen + 1];
    for (int loopperiod = 0; loopperiod < sLen + 1; loopperiod++) {
      exp2as[loopperiod] = Math.exp(2 * a * s[loopperiod]);
    }
    for (int loopperiod = 0; loopperiod < sLen; loopperiod++) {
      factor2 += parameters.getVolatility()[loopperiod + indexStart - 1] * parameters.getVolatility()[loopperiod + indexStart - 1] * (exp2as[loopperiod + 1] - exp2as[loopperiod]);
    }
    return Math.exp(factor1 * factor2 / numerator);
  }

  /**
   * Computes the (zero-coupon) bond volatility divided by a bond numeraire for a given period. 
   * @param data Hull-White model data.
   * @param startExpiry Start time of the expiry period.
   * @param endExpiry End time of the expiry period.
   * @param numeraireTime Time to maturity for the bond numeraire.
   * @param bondMaturity Time to maturity for the bond.
   * @return The re-based bond volatility.
   */
  public double alpha(final HullWhiteOneFactorPiecewiseConstantParameters data, final double startExpiry, final double endExpiry, final double numeraireTime, final double bondMaturity) {
    double factor1 = Math.exp(-data.getMeanReversion() * numeraireTime) - Math.exp(-data.getMeanReversion() * bondMaturity);
    double numerator = 2 * data.getMeanReversion() * data.getMeanReversion() * data.getMeanReversion();
    int indexStart = Math.abs(Arrays.binarySearch(data.getVolatilityTime(), startExpiry) + 1);
    // Period in which the time startExpiry is; _volatilityTime[i-1] <= startExpiry < _volatilityTime[i];
    int indexEnd = Math.abs(Arrays.binarySearch(data.getVolatilityTime(), endExpiry) + 1);
    // Period in which the time endExpiry is; _volatilityTime[i-1] <= endExpiry < _volatilityTime[i];
    int sLen = indexEnd - indexStart + 1;
    double[] s = new double[sLen + 1];
    s[0] = startExpiry;
    System.arraycopy(data.getVolatilityTime(), indexStart, s, 1, sLen - 1);
    s[sLen] = endExpiry;
    double factor2 = 0.0;
    double[] exp2as = new double[sLen + 1];
    for (int loopperiod = 0; loopperiod < sLen + 1; loopperiod++) {
      exp2as[loopperiod] = Math.exp(2 * data.getMeanReversion() * s[loopperiod]);
    }
    for (int loopperiod = 0; loopperiod < sLen; loopperiod++) {
      factor2 += data.getVolatility()[loopperiod + indexStart - 1] * data.getVolatility()[loopperiod + indexStart - 1] * (exp2as[loopperiod + 1] - exp2as[loopperiod]);
    }
    return factor1 * Math.sqrt(factor2 / numerator);
  }

  /**
   * The adjoint version of the method. Computes the (zero-coupon) bond volatility divided by a bond numeraire for a given period ant its derivatives. 
   * @param data Hull-White model data.
   * @param startExpiry Start time of the expiry period.
   * @param endExpiry End time of the expiry period.
   * @param numeraireTime Time to maturity for the bond numeraire.
   * @param bondMaturity Time to maturity for the bond.
   * @param derivatives Array used for return the derivatives with respect to the input. The array is changed by the method. The derivatives of the function alpha
   * with respect to the piecewise constant volatilities.
   * @return The re-based bond volatility.
   */
  public double alpha(final HullWhiteOneFactorPiecewiseConstantParameters data, final double startExpiry, final double endExpiry, final double numeraireTime, final double bondMaturity,
      double[] derivatives) {
    int nbSigma = data.getVolatility().length;
    for (int loopperiod = 0; loopperiod < nbSigma; loopperiod++) { // To clean derivatives
      derivatives[loopperiod] = 0.0;
    }
    // Forward sweep
    double factor1 = Math.exp(-data.getMeanReversion() * numeraireTime) - Math.exp(-data.getMeanReversion() * bondMaturity);
    double numerator = 2 * data.getMeanReversion() * data.getMeanReversion() * data.getMeanReversion();
    int indexStart = Math.abs(Arrays.binarySearch(data.getVolatilityTime(), startExpiry) + 1);
    // Period in which the time startExpiry is; _volatilityTime[i-1] <= startExpiry < _volatilityTime[i];
    int indexEnd = Math.abs(Arrays.binarySearch(data.getVolatilityTime(), endExpiry) + 1);
    // Period in which the time endExpiry is; _volatilityTime[i-1] <= endExpiry < _volatilityTime[i];
    int sLen = indexEnd - indexStart + 1;
    double[] s = new double[sLen + 1];
    s[0] = startExpiry;
    System.arraycopy(data.getVolatilityTime(), indexStart, s, 1, sLen - 1);
    s[sLen] = endExpiry;
    double factor2 = 0.0;
    double[] exp2as = new double[sLen + 1];
    for (int loopperiod = 0; loopperiod < sLen + 1; loopperiod++) {
      exp2as[loopperiod] = Math.exp(2 * data.getMeanReversion() * s[loopperiod]);
    }
    for (int loopperiod = 0; loopperiod < sLen; loopperiod++) {
      factor2 += data.getVolatility()[loopperiod + indexStart - 1] * data.getVolatility()[loopperiod + indexStart - 1] * (exp2as[loopperiod + 1] - exp2as[loopperiod]);
    }
    double sqrtFactor2Num = Math.sqrt(factor2 / numerator);
    double alpha = factor1 * sqrtFactor2Num;
    // Backward sweep 
    double alphaBar = 1.0;
    double factor2Bar = factor1 / sqrtFactor2Num / 2.0 / numerator * alphaBar;
    for (int loopperiod = 0; loopperiod < sLen; loopperiod++) {
      derivatives[loopperiod + indexStart - 1] = 2 * data.getVolatility()[loopperiod + indexStart - 1] * (exp2as[loopperiod + 1] - exp2as[loopperiod]) * factor2Bar;
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

  public double beta(final HullWhiteOneFactorPiecewiseConstantParameters data, final double startExpiry, final double endExpiry) {
    double numerator = 2 * data.getMeanReversion();
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
    double denominator = 0.0;
    for (int loopperiod = 0; loopperiod < sLen; loopperiod++) {
      denominator += data.getVolatility()[loopperiod + indexStart - 1] * data.getVolatility()[loopperiod + indexStart - 1]
          * (Math.exp(2 * data.getMeanReversion() * s[loopperiod + 1]) - Math.exp(2 * data.getMeanReversion() * s[loopperiod]));
    }
    return Math.sqrt(denominator / numerator);
  }

  /**
   * Compute the common part of the exercise boundary of European swaptions
   * forward. Used in particular for Bermudan swaption first step of the
   * pricing.
   * <p>
   * Reference: Henrard, M. Bermudan Swaptions in Gaussian HJM One-Factor
   * Model: Analytical and Numerical Approaches. SSRN, October 2008. Available
   * at SSRN: http://ssrn.com/abstract=1287982
   * @param discountedCashFlow The swap discounted cash flows.
   * @param alpha2 The $\alpha^2$ parameters.
   * @param hwH The H factors.
   * @return The exercise boundary.
   */
  public double lambda(final double[] discountedCashFlow, final double[] alpha2, final double[] hwH) {
    final Function1D<Double, Double> swapValue = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        double value = 0.0;
        for (int loopcf = 0; loopcf < alpha2.length; loopcf++) {
          value += discountedCashFlow[loopcf] * Math.exp(-0.5 * alpha2[loopcf] - hwH[loopcf] * x);
        }
        return value;
      }
    };
    final BracketRoot bracketer = new BracketRoot();
    double accuracy = 1.0E-8;
    final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(accuracy);
    final double[] range = bracketer.getBracketedPoints(swapValue, -2.0, 2.0);
    return rootFinder.getRoot(swapValue, range[0], range[1]);
  }

  /**
   * The maturity dependent part of the volatility (function called H in the implementation note).
   * @param hwParameters The model parameters.
   * @param u The start time.
   * @param v The end times.
   * @return The volatility. Same dimension as v.
   */
  public double[][] volatilityMaturityPart(final HullWhiteOneFactorPiecewiseConstantParameters hwParameters, double u, double[][] v) {
    double a = hwParameters.getMeanReversion();
    double[][] result = new double[v.length][];
    double expau = Math.exp(-a * u);
    for (int loopcf1 = 0; loopcf1 < v.length; loopcf1++) {
      result[loopcf1] = new double[v[loopcf1].length];
      for (int loopcf2 = 0; loopcf2 < v[loopcf1].length; loopcf2++) {
        result[loopcf1][loopcf2] = (expau - Math.exp(-a * v[loopcf1][loopcf2])) / a;
      }
    }
    return result;
  }

  /**
   * The expiry time dependent part of the volatility.
   * @param hwParameters The model parameters.
   * @param theta0 The start expiry time.
   * @param theta1 The end expiry time.
   * @return The volatility.
   */
  public double gamma(final HullWhiteOneFactorPiecewiseConstantParameters hwParameters, double theta0, double theta1) {
    double a = hwParameters.getMeanReversion();
    double[] sigma = hwParameters.getVolatility();
    int indexStart = 1; // Period in which the time startExpiry is; _volatilityTime[i-1] <= startExpiry < _volatilityTime[i];
    while (theta0 > hwParameters.getVolatilityTime()[indexStart]) {
      indexStart++;
    }
    int indexEnd = indexStart; // Period in which the time endExpiry is; _volatilityTime[i-1] <= endExpiry < _volatilityTime[i];
    while (theta1 > hwParameters.getVolatilityTime()[indexEnd]) {
      indexEnd++;
    }
    int sLen = indexEnd - indexStart + 2;
    double[] s = new double[sLen];
    s[0] = theta0;
    System.arraycopy(hwParameters.getVolatilityTime(), indexStart, s, 1, sLen - 2);
    s[sLen - 1] = theta1;

    double gamma = 0.0;
    double[] exp2as = new double[sLen];
    for (int loopindex = 0; loopindex < sLen; loopindex++) {
      exp2as[loopindex] = Math.exp(2 * a * s[loopindex]);
    }
    for (int loopindex = 0; loopindex < sLen - 1; loopindex++) {
      gamma += sigma[indexStart - 1 + loopindex] * sigma[indexStart - 1 + loopindex] * (exp2as[loopindex + 1] - exp2as[loopindex]);
    }
    return gamma;
  }

  /**
   * Compute the swap rate for a given value of the standard normal random variable in the $P(.,\theta)$ numeraire.
   * @param x The random variable value.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg.
   * @param alphaFixed The zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate.
   */
  public double swapRate(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor) {
    ArgumentChecker.isTrue(discountedCashFlowFixed.length == alphaFixed.length, "Length shouyld be equal");
    double numerator = 0.0;
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      numerator += discountedCashFlowIbor[loopcf] * Math.exp(-alphaIbor[loopcf] * x - 0.5 * alphaIbor[loopcf] * alphaIbor[loopcf]);
    }
    double denominator = 0.0;
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      denominator += discountedCashFlowFixed[loopcf] * Math.exp(-alphaFixed[loopcf] * x - 0.5 * alphaFixed[loopcf] * alphaFixed[loopcf]);
    }
    return -numerator / denominator;
  }

  /**
   * Compute the first order derivative of the swap rate with respect to the value of the standard normal random variable in the $P(.,\theta)$ numeraire.
   * @param x The random variable value.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg.
   * @param alphaFixed The zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate.
   */
  public double swapRateDx1(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor) {
    double f = 0.0;
    double df = 0.0;
    double term;
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      term = discountedCashFlowIbor[loopcf] * Math.exp(-alphaIbor[loopcf] * x - 0.5 * alphaIbor[loopcf] * alphaIbor[loopcf]);
      f += term;
      df += -alphaIbor[loopcf] * term;
    }
    double g = 0.0;
    double dg = 0.0;
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      term = discountedCashFlowFixed[loopcf] * Math.exp(-alphaFixed[loopcf] * x - 0.5 * alphaFixed[loopcf] * alphaFixed[loopcf]);
      g += term;
      dg += -alphaFixed[loopcf] * term;
    }
    return -(df * g - f * dg) / (g * g);
  }

  /**
   * Computes the second order derivative of the swap rate with respect to the value of the standard normal random variable in the $P(.,\theta)$ numeraire.
   * @param x The random variable value.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg.
   * @param alphaFixed The zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate.
   */
  public double swapRateDx2(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor) {
    double f = 0.0;
    double df = 0.0;
    double df2 = 0.0;
    double term;
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      term = discountedCashFlowIbor[loopcf] * Math.exp(-alphaIbor[loopcf] * x - 0.5 * alphaIbor[loopcf] * alphaIbor[loopcf]);
      f += term;
      df += -alphaIbor[loopcf] * term;
      df2 += alphaIbor[loopcf] * alphaIbor[loopcf] * term;
    }
    double g = 0.0;
    double dg = 0.0;
    double dg2 = 0.0;
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      term = discountedCashFlowFixed[loopcf] * Math.exp(-alphaFixed[loopcf] * x - 0.5 * alphaFixed[loopcf] * alphaFixed[loopcf]);
      g += term;
      dg += -alphaFixed[loopcf] * term;
      dg2 += alphaFixed[loopcf] * alphaFixed[loopcf] * term;
    }
    double g2 = g * g;
    double g3 = g * g2;

    return -df2 / g + (2 * df * dg + f * dg2) / g2 - 2 * f * dg * dg / g3;
  }

  /**
   * Compute the first order derivative of the swap rate with respect to the discountedCashFlowIbor in the $P(.,\theta)$ numeraire.
   * @param x The random variable value.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg.
   * @param alphaFixed The zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate derivative.
   */
  public double[] swapRateDdcfi1(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor) {
    final int nbDcfi = discountedCashFlowIbor.length;
    final int nbDcff = discountedCashFlowFixed.length;
    final double[] swapRateDdcfi1 = new double[nbDcfi];
    double denominator = 0.0;
    for (int loopcf = 0; loopcf < nbDcff; loopcf++) {
      denominator += discountedCashFlowFixed[loopcf] * Math.exp(-alphaFixed[loopcf] * x - 0.5 * alphaFixed[loopcf] * alphaFixed[loopcf]);
    }
    for (int loopcf = 0; loopcf < nbDcfi; loopcf++) {
      swapRateDdcfi1[loopcf] = -Math.exp(-alphaIbor[loopcf] * x - 0.5 * alphaIbor[loopcf] * alphaIbor[loopcf]) / denominator;
    }
    return swapRateDdcfi1;
  }

  /**
   * Compute the first order derivative of the swap rate with respect to the discountedCashFlowFixed in the $P(.,\theta)$ numeraire.
   * @param x The random variable value.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg.
   * @param alphaFixed The zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate derivative.
   */
  public double[] swapRateDdcff1(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor) {
    final int nbDcff = discountedCashFlowFixed.length;
    final int nbDcfi = discountedCashFlowIbor.length;
    final double[] expD = new double[nbDcfi];
    double numerator = 0.0;
    for (int loopcf = 0; loopcf < nbDcfi; loopcf++) {
      numerator += discountedCashFlowIbor[loopcf] * Math.exp(-alphaIbor[loopcf] * x - 0.5 * alphaIbor[loopcf] * alphaIbor[loopcf]);
    }
    double denominator = 0.0;
    for (int loopcf = 0; loopcf < nbDcff; loopcf++) {
      expD[loopcf] = Math.exp(-alphaFixed[loopcf] * x - 0.5 * alphaFixed[loopcf] * alphaFixed[loopcf]);
      denominator += discountedCashFlowFixed[loopcf] * expD[loopcf];
    }
    final double ratio = numerator / (denominator * denominator);
    final double[] swapRateDdcff1 = new double[nbDcff];
    for (int loopcf = 0; loopcf < nbDcff; loopcf++) {
      swapRateDdcff1[loopcf] = ratio * expD[loopcf];
    }
    return swapRateDdcff1;
  }

  /**
   * Compute the first order derivative of the swap rate with respect to the alphaIbor in the $P(.,\theta)$ numeraire.
   * @param x The random variable value.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg.
   * @param alphaFixed The zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate derivatives.
   */
  public double[] swapRateDai1(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor) {
    final int nbDcfi = discountedCashFlowIbor.length;
    final int nbDcff = discountedCashFlowFixed.length;
    final double[] swapRateDai1 = new double[nbDcfi];
    double denominator = 0.0;
    for (int loopcf = 0; loopcf < nbDcff; loopcf++) {
      denominator += discountedCashFlowFixed[loopcf] * Math.exp(-alphaFixed[loopcf] * x - 0.5 * alphaFixed[loopcf] * alphaFixed[loopcf]);
    }
    for (int loopcf = 0; loopcf < nbDcfi; loopcf++) {
      swapRateDai1[loopcf] = discountedCashFlowIbor[loopcf] * Math.exp(-alphaIbor[loopcf] * x - 0.5 * alphaIbor[loopcf] * alphaIbor[loopcf]) * (x + alphaIbor[loopcf]) / denominator;
    }
    return swapRateDai1;
  }

  /**
   * Compute the first order derivative of the swap rate with respect to the alphaFixed in the $P(.,\theta)$ numeraire.
   * @param x The random variable value.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg.
   * @param alphaFixed The zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate derivatives.
   */
  public double[] swapRateDaf1(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor) {
    final int nbDcff = discountedCashFlowFixed.length;
    final int nbDcfi = discountedCashFlowIbor.length;
    final double[] expD = new double[nbDcfi];
    double numerator = 0.0;
    for (int loopcf = 0; loopcf < nbDcfi; loopcf++) {
      numerator += discountedCashFlowIbor[loopcf] * Math.exp(-alphaIbor[loopcf] * x - 0.5 * alphaIbor[loopcf] * alphaIbor[loopcf]);
    }
    double denominator = 0.0;
    for (int loopcf = 0; loopcf < nbDcff; loopcf++) {
      expD[loopcf] = discountedCashFlowFixed[loopcf] * Math.exp(-alphaFixed[loopcf] * x - 0.5 * alphaFixed[loopcf] * alphaFixed[loopcf]);
      denominator += expD[loopcf];
    }
    final double ratio = numerator / (denominator * denominator);
    final double[] swapRateDaf1 = new double[nbDcff];
    for (int loopcf = 0; loopcf < nbDcff; loopcf++) {
      swapRateDaf1[loopcf] = ratio * expD[loopcf] * (-x - alphaFixed[loopcf]);
    }
    return swapRateDaf1;
  }

  /**
   * Compute the first order derivative with respect to the discountedCashFlowFixed and to the discountedCashFlowIbor of the of swap rate second derivative with respect 
   * to the random variable x in the $P(.,\theta)$ numeraire.
   * @param x The random variable value.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg.
   * @param alphaFixed The zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate derivative. Made of a pair of arrays. The first one is the derivative wrt discountedCashFlowFixed and the second one wrt discountedCashFlowIbor.
   */
  public Pair<double[], double[]> swapRateDx2Ddcf1(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor) {
    final int nbDcff = discountedCashFlowFixed.length;
    final int nbDcfi = discountedCashFlowIbor.length;
    double f = 0.0;
    double df = 0.0;
    double df2 = 0.0;
    double[] termIbor = new double[nbDcfi];
    double[] expIbor = new double[nbDcfi];
    for (int loopcf = 0; loopcf < nbDcfi; loopcf++) {
      expIbor[loopcf] = Math.exp(-alphaIbor[loopcf] * x - 0.5 * alphaIbor[loopcf] * alphaIbor[loopcf]);
      termIbor[loopcf] = discountedCashFlowIbor[loopcf] * expIbor[loopcf];
      f += termIbor[loopcf];
      df += -alphaIbor[loopcf] * termIbor[loopcf];
      df2 += alphaIbor[loopcf] * alphaIbor[loopcf] * termIbor[loopcf];
    }
    double g = 0.0;
    double dg = 0.0;
    double dg2 = 0.0;
    double[] termFixed = new double[nbDcff];
    double[] expFixed = new double[nbDcff];
    for (int loopcf = 0; loopcf < nbDcff; loopcf++) {
      expFixed[loopcf] = Math.exp(-alphaFixed[loopcf] * x - 0.5 * alphaFixed[loopcf] * alphaFixed[loopcf]);
      termFixed[loopcf] = discountedCashFlowFixed[loopcf] * expFixed[loopcf];
      g += termFixed[loopcf];
      dg += -alphaFixed[loopcf] * termFixed[loopcf];
      dg2 += alphaFixed[loopcf] * alphaFixed[loopcf] * termFixed[loopcf];
    }
    double g2 = g * g;
    double g3 = g * g2;
    double g4 = g * g3;
    //    double dx2 = -((df2 * g - f * dg2) / g2 - (df * g - f * dg) * 2 * dg / g3);
    // Backward sweep
    double dx2Bar = 1.0;
    double gBar = (df2 / g2 - 2 * f * dg2 / g3 - 4 * df * dg / g3 + 6 * dg * dg * f / g4) * dx2Bar;
    double dgBar = (2 * df / g2 - 4 * f * dg / g3) * dx2Bar;
    double dg2Bar = f / g2 * dx2Bar;
    double fBar = (dg2 / g2 - 2 * dg * dg / g3) * dx2Bar;
    double dfBar = 2 * dg / g2 * dx2Bar;
    double df2Bar = -1.0 / g * dx2Bar;

    final double[] discountedCashFlowFixedBar = new double[nbDcff];
    final double[] termFixedBar = new double[nbDcff];
    for (int loopcf = 0; loopcf < nbDcff; loopcf++) {
      termFixedBar[loopcf] = gBar - alphaFixed[loopcf] * dgBar + alphaFixed[loopcf] * alphaFixed[loopcf] * dg2Bar;
      discountedCashFlowFixedBar[loopcf] = expFixed[loopcf] * termFixedBar[loopcf];
    }
    final double[] discountedCashFlowIborBar = new double[nbDcfi];
    final double[] termIborBar = new double[nbDcfi];
    for (int loopcf = 0; loopcf < nbDcfi; loopcf++) {
      termIborBar[loopcf] = fBar - alphaIbor[loopcf] * dfBar + alphaIbor[loopcf] * alphaIbor[loopcf] * df2Bar;
      discountedCashFlowIborBar[loopcf] = expIbor[loopcf] * termIborBar[loopcf];
    }
    return ObjectsPair.of(discountedCashFlowFixedBar, discountedCashFlowIborBar);
  }

  /**
   * Compute the first order derivative with respect to the alphaFixed and to the alphaIbor of the of swap rate second derivative with respect 
   * to the random variable x in the $P(.,\theta)$ numeraire.
   * @param x The random variable value.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg.
   * @param alphaFixed The zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate derivatives. Made of a pair of arrays. The first one is the derivative wrt alphaFixed and the second one wrt alphaIbor.
   */
  public Pair<double[], double[]> swapRateDx2Da1(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor) {
    final int nbDcff = discountedCashFlowFixed.length;
    final int nbDcfi = discountedCashFlowIbor.length;
    double f = 0.0;
    double df = 0.0;
    double df2 = 0.0;
    double[] termIbor = new double[nbDcfi];
    double[] expIbor = new double[nbDcfi];
    for (int loopcf = 0; loopcf < nbDcfi; loopcf++) {
      expIbor[loopcf] = Math.exp(-alphaIbor[loopcf] * x - 0.5 * alphaIbor[loopcf] * alphaIbor[loopcf]);
      termIbor[loopcf] = discountedCashFlowIbor[loopcf] * expIbor[loopcf];
      f += termIbor[loopcf];
      df += -alphaIbor[loopcf] * termIbor[loopcf];
      df2 += alphaIbor[loopcf] * alphaIbor[loopcf] * termIbor[loopcf];
    }
    double g = 0.0;
    double dg = 0.0;
    double dg2 = 0.0;
    double[] termFixed = new double[nbDcff];
    double[] expFixed = new double[nbDcff];
    for (int loopcf = 0; loopcf < nbDcff; loopcf++) {
      expFixed[loopcf] = Math.exp(-alphaFixed[loopcf] * x - 0.5 * alphaFixed[loopcf] * alphaFixed[loopcf]);
      termFixed[loopcf] = discountedCashFlowFixed[loopcf] * expFixed[loopcf];
      g += termFixed[loopcf];
      dg += -alphaFixed[loopcf] * termFixed[loopcf];
      dg2 += alphaFixed[loopcf] * alphaFixed[loopcf] * termFixed[loopcf];
    }
    double g2 = g * g;
    double g3 = g * g2;
    double g4 = g * g3;
    //    double dx2 = -((df2 * g - f * dg2) / g2 - (df * g - f * dg) * 2 * dg / g3);
    // Backward sweep
    double dx2Bar = 1.0;
    double gBar = (df2 / g2 - 2 * f * dg2 / g3 - 4 * df * dg / g3 + 6 * dg * dg * f / g4) * dx2Bar;
    double dgBar = (2 * df / g2 - 4 * f * dg / g3) * dx2Bar;
    double dg2Bar = f / g2 * dx2Bar;
    double fBar = (dg2 / g2 - 2 * dg * dg / g3) * dx2Bar;
    double dfBar = 2 * dg / g2 * dx2Bar;
    double df2Bar = -1.0 / g * dx2Bar;

    final double[] alphaFixedBar = new double[nbDcff];
    final double[] termFixedBar = new double[nbDcff];
    for (int loopcf = 0; loopcf < nbDcff; loopcf++) {
      termFixedBar[loopcf] = gBar - alphaFixed[loopcf] * dgBar + alphaFixed[loopcf] * alphaFixed[loopcf] * dg2Bar;
      alphaFixedBar[loopcf] = termFixed[loopcf] * (-x - alphaFixed[loopcf]) * termFixedBar[loopcf] - termFixed[loopcf] * dgBar + 2 * alphaFixed[loopcf] * termFixed[loopcf] * dg2Bar;
    }
    final double[] alphaIborBar = new double[nbDcfi];
    final double[] termIborBar = new double[nbDcfi];
    for (int loopcf = 0; loopcf < nbDcfi; loopcf++) {
      termIborBar[loopcf] = fBar - alphaIbor[loopcf] * dfBar + alphaIbor[loopcf] * alphaIbor[loopcf] * df2Bar;
      alphaIborBar[loopcf] = termIbor[loopcf] * (-x - alphaIbor[loopcf]) * termIborBar[loopcf] - termIbor[loopcf] * dfBar + 2 * alphaIbor[loopcf] * termIbor[loopcf] * df2Bar;
    }
    return ObjectsPair.of(alphaFixedBar, alphaIborBar);
  }

}
