/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationException;

/**
 * 
 * @author emcleod
 * 
 */

public class JarrowRuddSkewnessKurtosisModel extends AnalyticOptionModel<EuropeanVanillaOptionDefinition, SkewKurtosisOptionDataBundle> {
  protected BlackScholesMertonModel _bsm;

  @Override
  public Function1D<SkewKurtosisOptionDataBundle, Double> getPricingFunction(final EuropeanVanillaOptionDefinition definition) {
    final Function1D<SkewKurtosisOptionDataBundle, Double> pricingFunction = new Function1D<SkewKurtosisOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final SkewKurtosisOptionDataBundle data) {
        try {
          final double s = data.getSpot();
          final double k = definition.getStrike();
          final double t = definition.getTimeToExpiry(data.getDate());
          final double sigma = data.getVolatility(t, k);
          final double r = data.getInterestRate(t);
          final double b = data.getCostOfCarry();
          final double skew = data.getSkew();
          final double kurtosis = data.getKurtosis();
          EuropeanVanillaOptionDefinition callDefinition = definition;
          if (!definition.isCall()) {
            callDefinition = new EuropeanVanillaOptionDefinition(callDefinition.getStrike(), callDefinition.getExpiry(), true);
          }
          final Function1D<StandardOptionDataBundle, Double> bsm = _bsm.getPricingFunction(callDefinition);
          final double bsmCall = bsm.evaluate(data);
          final double d2 = getD2(getD1(s, k, t, sigma, b), sigma, t);
          final double a = getA(d2, k, sigma, t);
          final double call = bsmCall + getLambda1(sigma, t, skew) * getQ3(s, k, sigma, t, r, a, d2) + getLambda2(sigma, t, kurtosis) * getQ4(s, k, sigma, t, r, a, d2);
          if (!definition.isCall())
            return call - s * Math.exp((b - r) * t) + k * Math.exp(-r * t);
          return call;
        } catch (final InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }

    };
    return pricingFunction;
  }

  double getA(final double d2, final double k, final double sigma, final double t) {
    return Math.exp(-d2 * d2 / 2.) / (k * sigma * 2 * Math.PI * Math.sqrt(t));
  }

  double getLambda1(final double sigma, final double t, final double skew) {
    final double q = Math.sqrt(Math.exp(sigma * sigma * t) - 1);
    final double skewDistribution = q * (3 + q * q);
    return skew - skewDistribution;
  }

  double getLambda2(final double sigma, final double t, final double kurtosis) {
    final double q = Math.sqrt(Math.exp(sigma * sigma * t) - 1);
    final double q2 = q * q;
    final double q4 = q2 * q2;
    final double q6 = q4 * q2;
    final double q8 = q6 * q2;
    final double kurtosisDistribution = 16 * q2 + 15 * q4 + 6 * q6 + q8 + 3;
    return kurtosis - kurtosisDistribution;
  }

  double getQ3(final double s, final double k, final double sigma, final double t, final double r, final double a, final double d2) {
    final double sigmaT = sigma * Math.sqrt(t);
    final double da = a * (d2 - sigmaT) / (k * sigmaT);
    final double df = Math.exp(-r * t);
    return -Math.pow(s * df, 3) * Math.pow(Math.exp(sigmaT * sigmaT - 1), 1.5) * df * da / 6.;
  }

  double getQ4(final double s, final double k, final double sigma, final double t, final double r, final double a, final double d2) {
    final double sigmaT = sigma * Math.sqrt(t);
    final double sub = d2 - sigmaT;
    final double da2 = a * (sub * sub - sigmaT * sub - 1) / (k * k * sigmaT * sigmaT);
    final double df = Math.exp(-r * t);
    return Math.pow(s * df, 4) * Math.pow(Math.exp(sigmaT * sigmaT) - 1, 2) * df * da2 / 24.;
  }
}
