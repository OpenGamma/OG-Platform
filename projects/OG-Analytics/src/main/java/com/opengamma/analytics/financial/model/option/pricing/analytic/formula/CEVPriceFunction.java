/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NonCentralChiSquaredDistribution;

/**
 * 
 */
public class CEVPriceFunction implements OptionPriceFunction<CEVFunctionData> {
  private static final BlackPriceFunction BLACK_PRICE_FUNCTION = new BlackPriceFunction();
  private static final NormalPriceFunction NORMAL_PRICE_FUNCTION = new NormalPriceFunction();

  @Override
  public Function1D<CEVFunctionData, Double> getPriceFunction(final EuropeanVanillaOption option) {
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final boolean isCall = option.isCall();
    return new Function1D<CEVFunctionData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final CEVFunctionData data) {
        Validate.notNull(data, "data");
        final double forward = data.getForward();
        final double numeraire = data.getNumeraire();
        final double sigma = data.getVolatility();
        final double beta = data.getBeta();
        if (beta == 1.0) {
          final Function1D<BlackFunctionData, Double> blackFormula = BLACK_PRICE_FUNCTION.getPriceFunction(option);
          return blackFormula.evaluate(new BlackFunctionData(forward, numeraire, sigma));
        }
        if (beta == 0.0) {
          final Function1D<NormalFunctionData, Double> normalFormula = NORMAL_PRICE_FUNCTION.getPriceFunction(option);
          return normalFormula.evaluate(new NormalFunctionData(forward, numeraire, sigma));
        }
        final double b = 1.0 / (1 - beta);
        final double x = b * b / sigma / sigma / t;
        final double a = Math.pow(k, 2 * (1 - beta)) * x;
        final double c = Math.pow(forward, 2 * (1 - beta)) * x;
        if (beta < 1) {
          final NonCentralChiSquaredDistribution chiSq1 = new NonCentralChiSquaredDistribution(b + 2, c);
          final NonCentralChiSquaredDistribution chiSq2 = new NonCentralChiSquaredDistribution(b, a);
          if (isCall) {
            return numeraire * (forward * (1 - chiSq1.getCDF(a)) - k * chiSq2.getCDF(c));
          }
          return numeraire * (k * (1 - chiSq2.getCDF(c)) - forward * chiSq1.getCDF(a));
        }
        final NonCentralChiSquaredDistribution chiSq1 = new NonCentralChiSquaredDistribution(-b, a);
        final NonCentralChiSquaredDistribution chiSq2 = new NonCentralChiSquaredDistribution(2 - b, c);
        if (isCall) {
          return numeraire * (forward * (1 - chiSq1.getCDF(c)) - k * chiSq2.getCDF(a));
        }
        return numeraire * (k * (1 - chiSq2.getCDF(a)) - forward * chiSq1.getCDF(c));
      }
    };
  }
}
