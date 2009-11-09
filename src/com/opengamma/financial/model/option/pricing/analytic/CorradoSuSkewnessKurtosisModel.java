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
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 * 
 */

public class CorradoSuSkewnessKurtosisModel extends AnalyticOptionModel<EuropeanVanillaOptionDefinition, SkewKurtosisOptionDataBundle> {
  protected BlackScholesMertonModel _bsm;
  protected ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

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
          final double skew = data.getOnePeriodSkew();
          final double kurtosis = data.getOnePeriodKurtosis();
          EuropeanVanillaOptionDefinition callDefinition = definition;
          if (!definition.isCall()) {
            callDefinition = new EuropeanVanillaOptionDefinition(callDefinition.getStrike(), callDefinition.getExpiry(), true);
          }
          final Function1D<StandardOptionDataBundle, Double> bsm = _bsm.getPricingFunction(callDefinition);
          final double bsmCall = bsm.evaluate(data);
          final double w = getW(sigma, t, skew, kurtosis);
          final double d = getD(s, k, sigma, t, b, w);
          final double call = bsmCall + skew * getQ3(s, sigma, t, d, w) + (kurtosis - 3) * getQ4(s, sigma, t, d, w);
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

  double getW(final double sigma, final double t, final double skew, final double kurtosis) {
    final double sigma3 = sigma * sigma * sigma;
    return skew * sigma3 * Math.pow(t, 1.5) / 6. + kurtosis * sigma * sigma3 * t * t / 24.;
  }

  double getD(final double s, final double k, final double sigma, final double t, final double b, final double w) {
    return (Math.log(s / k) + t * (b + sigma * sigma / 2.) - Math.log(1 + w)) / (sigma * Math.sqrt(t));
  }

  double getQ3(final double s, final double sigma, final double t, final double d, final double w) {
    final double sigmaT = sigma * Math.sqrt(t);
    return s * sigmaT * (2 * sigmaT - d) * _normalProbabilityDistribution.getPDF(d) / (24 * (1 + w));
  }

  double getQ4(final double s, final double sigma, final double t, final double d, final double w) {
    final double sigmaT = sigma * Math.sqrt(t);
    return s * sigmaT * (d * d - 3 * d * sigmaT + 3 * sigmaT * sigmaT - 1) * _normalProbabilityDistribution.getPDF(d) / (24 * (1 + w));
  }
}
