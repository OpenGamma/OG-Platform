/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import java.util.Arrays;

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
   * The factor is called {@latex.inline $\\gamma$} in the article and is given by
   * {@latex.ilb %preamble{\\usepackage{amsmath}}
   * \\begin{equation*}
   * \\gamma(t) = \\exp\\left(\\int_t^{t_0} \\nu(s,t_2) (\\nu(s,t_2)-\\nu(s,t_1)) ds \\right). 
   * \\end{equation*}
   * }
   * Reference: Henrard, M. The Irony in the derivatives discounting Part II: the crisis. Wilmott Journal, 2010, 2, 301-316
   * @param data The Hull-White model parameters.
   * @param t0 The expiry time.
   * @param t1 The first reference time.
   * @param t2 The second reference time.
   * @return The factor.
   */
  public double futureConvexityFactor(final HullWhiteOneFactorPiecewiseConstantParameters data, double t0, double t1, double t2) {
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
   * Compute the common part of the exercise boundary of European swaptions forward. Used in particular for Bermudan swaption first step of the pricing.
   * <p>Reference: Henrard, M. Bermudan Swaptions in Gaussian HJM One-Factor Model: Analytical and Numerical Approaches. SSRN, October 2008. Available at SSRN: http://ssrn.com/abstract=1287982
   * @param discountedCashFlow The swap discounted cash flows.
   * @param alpha2 The {@latex.inline $\\alpha^2$} parameters.
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
   * @return The volatility.
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
   * Compute the swap rate for a given value of the standard normal random variable in the {@latex.inline $P(.,\\theta)$} numeraire.
   * @param x The random variable value.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg.
   * @param alphaFixed The zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate.
   */
  public double swapRate(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor) {
    double numerator = 0.0;
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      numerator += discountedCashFlowIbor[loopcf] * Math.exp(-(x + alphaIbor[loopcf]) * (x + alphaIbor[loopcf]) / 2.0);
    }
    double denominator = 0.0;
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      denominator += discountedCashFlowFixed[loopcf] * Math.exp(-(x + alphaFixed[loopcf]) * (x + alphaFixed[loopcf]) / 2.0);
    }
    return -numerator / denominator;
  }

  /**
   * Compute the first order derivative of the swap rate with respect to the value of the standard normal random variable in the {@latex.inline $P(.,\\theta)$} numeraire.
   * @param x The random variable value.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg.
   * @param alphaFixed The zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate.
   */
  public double swapRateD1(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor) {
    double f = 0.0;
    double df = 0.0;
    double term;
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      term = discountedCashFlowIbor[loopcf] * Math.exp(-(x + alphaIbor[loopcf]) * (x + alphaIbor[loopcf]) / 2.0);
      f += term;
      df += -alphaIbor[loopcf] * term;
    }
    double g = 0.0;
    double dg = 0.0;
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      term = discountedCashFlowFixed[loopcf] * Math.exp(-(x + alphaFixed[loopcf]) * (x + alphaFixed[loopcf]) / 2.0);
      g += term;
      dg += -alphaFixed[loopcf] * term;
    }
    return -(df * g - f * dg) / (g * g);
  }

  /**
   * Computes the second order derivative of the swap rate with respect to the value of the standard normal random variable in the {@latex.inline $P(.,\\theta)$} numeraire.
   * @param x The random variable value.
   * @param discountedCashFlowFixed The discounted cash flows equivalent of the swap fixed leg.
   * @param alphaFixed The zero-coupon bond volatilities for the swap fixed leg.
   * @param discountedCashFlowIbor The discounted cash flows equivalent of the swap Ibor leg.
   * @param alphaIbor The zero-coupon bond volatilities for the swap Ibor leg.
   * @return The swap rate.
   */
  public double swapRateD2(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor) {
    double f = 0.0;
    double df = 0.0;
    double df2 = 0.0;
    double term;
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      term = discountedCashFlowIbor[loopcf] * Math.exp(-(x + alphaIbor[loopcf]) * (x + alphaIbor[loopcf]) / 2.0);
      f += term;
      df += -alphaIbor[loopcf] * term;
      df2 += alphaIbor[loopcf] * alphaIbor[loopcf] * term;
    }
    double g = 0.0;
    double dg = 0.0;
    double dg2 = 0.0;
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      term = discountedCashFlowFixed[loopcf] * Math.exp(-(x + alphaFixed[loopcf]) * (x + alphaFixed[loopcf]) / 2.0);
      g += term;
      dg += -alphaFixed[loopcf] * term;
      dg2 += alphaFixed[loopcf] * alphaFixed[loopcf] * term;
    }
    return -((df2 * g - f * dg2) / (g * g) - (df * g - f * dg) * 2 * dg / (g * g * g));
  }

}
