/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class BlackPriceFunction implements OptionPriceFunction<BlackFunctionData> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<BlackFunctionData, Double> getPriceFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double eps = 1E-16;
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    return new Function1D<BlackFunctionData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final BlackFunctionData data) {
        Validate.notNull(data, "data");
        final double f = data.getForward();
        final double discountFactor = data.getDiscountFactor();
        if (k < eps) {
          return option.isCall() ? (discountFactor * f) : 0.0;
        }
        final double sigma = data.getBlackVolatility();
        final int sign = option.isCall() ? 1 : -1;
        final double sigmaRootT = sigma * Math.sqrt(t);
        if (f == k) {
          return discountFactor * f * (2 * NORMAL.getCDF(sigmaRootT / 2) - 1);
        }
        if (sigmaRootT < eps) {
          final double x = sign * (f - k);
          return (x > 0 ? discountFactor * x : 0.0);
        }
        final double d1 = getD1(f, k, sigmaRootT);
        final double d2 = d1 - sigmaRootT;

        return sign * discountFactor * (f * NORMAL.getCDF(sign * d1) - k * NORMAL.getCDF(sign * d2));
      }
    };
  }

  /**
   * Return the Black price and its derivatives.
   * @param option The option.
   * @param data The Black data.
   * @return An array with [0] the price, [1] the derivative with respect to the forward, [2] the derivative with respect to the volatility and 
   * [3] the derivative with respect to the strike.
   */
  public double[] getPriceAdjoint(final EuropeanVanillaOption option, final BlackFunctionData data) {
    /**
     * The array storing the price and derivatives.
     */
    double[] priceAdjoint = new double[4];
    /**
     * The cut-off for small time and strike.
     */
    final double eps = 1E-16;

    final double strike = option.getStrike();
    final double timeToExpiry = option.getTimeToExpiry();
    final double vol = data.getBlackVolatility();
    final double forward = data.getForward();
    final boolean isCall = option.isCall();
    final double discountFactor = data.getDiscountFactor();
    double sqrttheta = Math.sqrt(timeToExpiry);
    double omega = isCall ? 1 : -1;
    // Note Implementation: Forward sweep.
    double volblack = 0, kappa = 0, d1 = 0, d2 = 0;
    double x = 0;
    if (strike < eps | sqrttheta < eps) {
      x = omega * (forward - strike);
      priceAdjoint[0] = (x > 0 ? discountFactor * x : 0.0);
    } else {
      volblack = vol * sqrttheta;
      kappa = Math.log(forward / strike) / volblack - 0.5 * volblack;
      d1 = NORMAL.getCDF(omega * (kappa + volblack));
      d2 = NORMAL.getCDF(omega * kappa);
      priceAdjoint[0] = discountFactor * omega * (forward * d1 - strike * d2);
    }
    // Note Implementation: Backward sweep.
    double pBar = 1.0;
    double forwardBar = 0, strikeBar = 0, volblackBar = 0, volatilityBar = 0;
    if (strike < eps | sqrttheta < eps) {
      forwardBar = (x > 0 ? discountFactor * omega : 0.0);
      strikeBar = (x > 0 ? -discountFactor * omega : 0.0);
    } else {
      double d1Bar = discountFactor * omega * forward * pBar;
      double density1 = NORMAL.getPDF(omega * (kappa + volblack));
      // Note Implementation: kappa_bar = 0; no need to implement it.
      // Note Methodology: kappa_bar is optimal exercise boundary. The
      // derivative at the optimal point is 0.
      forwardBar = discountFactor * omega * d1 * pBar;
      strikeBar = -discountFactor * omega * d2 * pBar;
      volblackBar = density1 * omega * d1Bar;
      volatilityBar = sqrttheta * volblackBar;
    }
    priceAdjoint[1] = forwardBar;
    priceAdjoint[2] = volatilityBar;
    priceAdjoint[3] = strikeBar;
    return priceAdjoint;
  }

  public Function1D<BlackFunctionData, Double> getVegaFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    return new Function1D<BlackFunctionData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final BlackFunctionData data) {
        Validate.notNull(data, "data");
        final double sigma = data.getBlackVolatility();
        final double f = data.getForward();
        final double discountFactor = data.getDiscountFactor();
        final double rootT = Math.sqrt(t);
        final double d1 = getD1(f, k, sigma * rootT);
        return f * rootT * discountFactor * NORMAL.getPDF(d1);
      }

    };
  }

  private static double getD1(final double f, final double k, final double sigmaRootT) {
    final double numerator = (Math.log(f / k) + sigmaRootT * sigmaRootT / 2);
    if (CompareUtils.closeEquals(numerator, 0, 1e-16)) {
      return 0;
    }
    return numerator / sigmaRootT;
  }

}
