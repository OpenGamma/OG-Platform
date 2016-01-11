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
public class NormalPriceFunction implements OptionPriceFunction<NormalFunctionData> {

  /**
   * The normal distribution implementation.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<NormalFunctionData, Double> getPriceFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double strike = option.getStrike();
    final double t = option.getTimeToExpiry();
    return new Function1D<NormalFunctionData, Double>() {
      private static final long serialVersionUID = 1L;
      @SuppressWarnings("synthetic-access")
      @Override
      public final Double evaluate(final NormalFunctionData data) {
        Validate.notNull(data, "data");
        final double forward = data.getForward();
        final double numeraire = data.getNumeraire();
        final double sigma = data.getNormalVolatility();
        final double sigmaRootT = sigma * Math.sqrt(t);
        final int sign = option.isCall() ? 1 : -1;
        if (sigmaRootT < 1e-16) {
          final double x = sign * (forward - strike);
          return (x > 0 ? numeraire * x : 0.0);
        }
        final double arg = sign * (forward - strike) / sigmaRootT;
        return numeraire * (sign * (forward - strike) * NORMAL.getCDF(arg) + sigmaRootT * NORMAL.getPDF(arg));
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
  public double getPriceAdjoint(final EuropeanVanillaOption option, final NormalFunctionData data, double[] priceDerivative) {
    Validate.notNull(option, "option");
    Validate.notNull(data, "data");
    Validate.notNull(priceDerivative, "derivatives");
    Validate.isTrue(priceDerivative.length == 3, "array size");
    final double strike = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double forward = data.getForward();
    final double numeraire = data.getNumeraire();
    final double sigma = data.getNormalVolatility();
    final int sign = option.isCall() ? 1 : -1;
    double price;
    double nCDF = 0.0;
    double nPDF = 0.0;
    double arg = 0.0;
    double x = 0.0;
    // Implementation Note: Forward sweep.
    final double sigmaRootT = sigma * Math.sqrt(t);
    if (sigmaRootT < 1e-16) {
      x = sign * (forward - strike);
      price = (x > 0 ? numeraire * x : 0.0);
    } else {
      arg = sign * (forward - strike) / sigmaRootT;
      nCDF = NORMAL.getCDF(arg);
      nPDF = NORMAL.getPDF(arg);
      price = numeraire * (sign * (forward - strike) * nCDF + sigmaRootT * nPDF);
    }
    // Implementation Note: Backward sweep.
    double priceBar = 1.0;
    if (sigmaRootT < 1e-16) {
      double xBar = (x > 0 ? numeraire : 0.0);
      priceDerivative[0] = sign * xBar;
      priceDerivative[2] = -priceDerivative[0];
      priceDerivative[1] = 0.0;
    } else {
      double nCDFBar = numeraire * (sign * (forward - strike)) * priceBar;
      double nPDFBar = numeraire * sigmaRootT * priceBar;
      double argBar = nPDF * nCDFBar - nPDF * arg * nPDFBar;
      priceDerivative[0] = numeraire * sign * nCDF * priceBar + sign / sigmaRootT * argBar;
      priceDerivative[2] = -priceDerivative[0];
      double sigmaRootTBar = -arg / sigmaRootT * argBar + numeraire * nPDF * priceBar;
      priceDerivative[1] = Math.sqrt(t) * sigmaRootTBar;
    }
    return price;
  }

  /**
   * Computes forward delta of an option in the normally distributed assets hypothesis (Bachelier model). 
   * @param option The option description.
   * @param data The model data.
   * @return The Delta.
   */
  public double getDelta(EuropeanVanillaOption option, NormalFunctionData data) {
    Validate.notNull(option, "option");
    Validate.notNull(data, "data");
    double strike = option.getStrike();
    double t = option.getTimeToExpiry();
    double forward = data.getForward();
    double numeraire = data.getNumeraire();
    double sigma = data.getNormalVolatility();
    int sign = option.isCall() ? 1 : -1;
    double sigmaRootT = sigma * Math.sqrt(t);
    if (sigmaRootT < 1e-16) {
      double x = sign * (forward - strike);
      if (Math.abs(x) <= 1e-16) {
        return sign * 0.5 * numeraire; // ambiguous if x and sigmaRootT are tiny, then reference number is returned
      }
      return x > 0 ? sign * numeraire : 0.0;
    }
    double arg = sign * (forward - strike) / sigmaRootT;
    double nCDF = NORMAL.getCDF(arg);
    return numeraire * sign * nCDF;
  }

  /**
   * Computes forward gamma of an option in the normally distributed assets hypothesis (Bachelier model). 
   * @param option The option description.
   * @param data The model data.
   * @return The gamma.
   */
  public double getGamma(EuropeanVanillaOption option, NormalFunctionData data) {
    Validate.notNull(option, "option");
    Validate.notNull(data, "data");
    double strike = option.getStrike();
    double t = option.getTimeToExpiry();
    double forward = data.getForward();
    double numeraire = data.getNumeraire();
    double sigma = data.getNormalVolatility();
    int sign = option.isCall() ? 1 : -1;
    double sigmaRootT = sigma * Math.sqrt(t);
    if (sigmaRootT < 1e-16) {
      double x = sign * (forward - strike);
      // ambiguous (tend to be infinite) if x and sigmaRootT are tiny, then reference number is returned
      return Math.abs(x) > 1e-16 ? 0.0 : numeraire / Math.sqrt(2.0 * Math.PI) / sigmaRootT;
    }
    double arg = (forward - strike) / sigmaRootT;
    double nPDF = NORMAL.getPDF(arg);
    return numeraire * nPDF / sigmaRootT;
  }

  /**
   * Computes vega of an option in the normally distributed assets hypothesis (Bachelier model). 
   * @param option The option description.
   * @param data The model data.
   * @return The vega.
   */
  public double getVega(EuropeanVanillaOption option, NormalFunctionData data) {
    Validate.notNull(option, "option");
    Validate.notNull(data, "data");
    double strike = option.getStrike();
    double t = option.getTimeToExpiry();
    double forward = data.getForward();
    double numeraire = data.getNumeraire();
    double sigma = data.getNormalVolatility();
    int sign = option.isCall() ? 1 : -1;
    double rootT = Math.sqrt(t);
    double sigmaRootT = sigma * rootT;
    if (sigmaRootT < 1e-16) {
      double x = sign * (forward - strike);
      // ambiguous if x and sigmaRootT are tiny, then reference number is returned
      return Math.abs(x) > 1e-16 ? 0.0 : numeraire * rootT / Math.sqrt(2.0 * Math.PI);
    }
    double arg = (forward - strike) / sigmaRootT;
    double nPDF = NORMAL.getPDF(arg);
    return numeraire * nPDF * rootT;
  }

  /**
   * Computes theta of an option in the normally distributed assets hypothesis (Bachelier model). 
   * @param option The option description.
   * @param data The model data.
   * @return The theta.
   */
  public double getTheta(EuropeanVanillaOption option, NormalFunctionData data) {
    Validate.notNull(option, "option");
    Validate.notNull(data, "data");
    double strike = option.getStrike();
    double t = option.getTimeToExpiry();
    double forward = data.getForward();
    double numeraire = data.getNumeraire();
    double sigma = data.getNormalVolatility();
    int sign = option.isCall() ? 1 : -1;
    double rootT = Math.sqrt(t);
    double sigmaRootT = sigma * rootT;
    if (sigmaRootT < 1e-16) {
      double x = sign * (forward - strike);
      // ambiguous if x and sigmaRootT are tiny, then reference number is returned
      return Math.abs(x) > 1e-16 ? 0.0 : -0.5 * numeraire * sigma / rootT / Math.sqrt(2.0 * Math.PI);
    }
    double arg = (forward - strike) / sigmaRootT;
    double nPDF = NORMAL.getPDF(arg);
    return -0.5 * numeraire * nPDF * sigma / rootT;
  }
}
