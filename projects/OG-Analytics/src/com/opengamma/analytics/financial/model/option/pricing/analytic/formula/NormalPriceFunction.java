/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Computes the price of an option in the normally distributed assets hypothesis (Bachelier model).
 */
public class NormalPriceFunction implements OptionPriceFunction<BlackFunctionData> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<BlackFunctionData, Double> getPriceFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    return new Function1D<BlackFunctionData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override//REVIEW R White 25/01/12 This should not be taking BlackFunctionData which confusingly implies that the volatility is log-normal when it is not
      public final Double evaluate(final BlackFunctionData data) {
        Validate.notNull(data, "data");
        final double f = data.getForward();
        final double discountFactor = data.getDiscountFactor();
        final double sigma = data.getBlackVolatility();
        final double sigmaRootT = sigma * Math.sqrt(t);
        final int sign = option.isCall() ? 1 : -1;
        if (sigmaRootT < 1e-16) {
          final double x = sign * (f - k);
          return (x > 0 ? discountFactor * x : 0.0);
        }
        final double arg = sign * (f - k) / sigmaRootT;
        return discountFactor * (sign * (f - k) * NORMAL.getCDF(arg) + sigmaRootT * NORMAL.getPDF(arg));
      }
    };
  }

  /**
   * Computes the price of an option in the normally distributed assets hypothesis (Bachelier model). The first order price derivatives are also provided.
   * @param option The option description.
   * @param data The model data.
   * @param priceDerivative Array used to output the derivative of the price with respect to [0] forward, [1] volatility, [2] strike. The length of the array should be 3.
   * @return The price.
   */
  public double getPriceAdjoint(final EuropeanVanillaOption option, final BlackFunctionData data, double[] priceDerivative) {
    Validate.notNull(option, "option");
    Validate.notNull(data, "data");
    Validate.notNull(priceDerivative, "derivatives");
    Validate.isTrue(priceDerivative.length == 3, "array size");
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double f = data.getForward();
    final double discountFactor = data.getDiscountFactor();
    final double sigma = data.getBlackVolatility();
    final int sign = option.isCall() ? 1 : -1;
    double price;
    double nCDF = 0.0;
    double nPDF = 0.0;
    double arg = 0.0;
    double x = 0.0;
    // Implementation Note: Forward sweep.
    final double sigmaRootT = sigma * Math.sqrt(t);
    if (sigmaRootT < 1e-16) {
      x = sign * (f - k);
      price = (x > 0 ? discountFactor * x : 0.0);
    } else {
      arg = sign * (f - k) / sigmaRootT;
      nCDF = NORMAL.getCDF(arg);
      nPDF = NORMAL.getPDF(arg);
      price = discountFactor * (sign * (f - k) * nCDF + sigmaRootT * nPDF);
    }
    // Implementation Note: Backward sweep.
    double priceBar = 1.0;
    if (sigmaRootT < 1e-16) {
      double xBar = (x > 0 ? discountFactor : 0.0);
      priceDerivative[0] = sign * xBar;
      priceDerivative[2] = -priceDerivative[0];
      priceDerivative[1] = 0.0;
    } else {
      double nCDFBar = discountFactor * (sign * (f - k)) * priceBar;
      double nPDFBar = discountFactor * sigmaRootT * priceBar;
      double argBar = nPDF * nCDFBar - nPDF * arg * nPDFBar;
      priceDerivative[0] = discountFactor * sign * nCDF * priceBar + sign / sigmaRootT * argBar;
      priceDerivative[2] = -priceDerivative[0];
      double sigmaRootTBar = -arg / sigmaRootT * argBar + discountFactor * nPDF * priceBar;
      priceDerivative[1] = Math.sqrt(t) * sigmaRootTBar;
    }
    return price;
  }
}
