/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.CappedPowerOptionDefinition;
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
public class CappedPowerOptionModel extends AnalyticOptionModel<CappedPowerOptionDefinition, StandardOptionDataBundle> {
  final ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final CappedPowerOptionDefinition definition) {
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        try {
          final double s = data.getSpot();
          final double k = definition.getStrike();
          final double t = definition.getTimeToExpiry(data.getDate());
          final double sigma = data.getVolatility(t, k);
          final double r = data.getInterestRate(t);
          final double b = data.getCostOfCarry();
          final double power = definition.getPower();
          final double cap = definition.getCap();
          final boolean isCall = definition.isCall();
          final double sigmaT = t * Math.sqrt(sigma);
          final double x = t * (b + sigma * sigma * (power - 0.5));
          final double d1 = getD(s / Math.pow(k, 1. / power), x, sigmaT);
          final double d2 = d1 - power * sigmaT;
          final double d3 = getD(isCall ? s / Math.pow(k + cap, 1. / power) : s / Math.pow(k - cap, 1. / power), x, sigmaT);
          final double d4 = d3 - power * sigmaT;
          final int sign = isCall ? 1 : -1;
          final double df1 = Math.exp(-r * t);
          final double df2 = Math.exp(t * ((power - 1) * (r + power * sigma * sigma * 0.5) - power * (r - b)));
          return sign
              * (Math.pow(s, power) * df2 * (_normalProbabilityDistribution.getCDF(sign * d1) - _normalProbabilityDistribution.getCDF(sign * d3)) + sign * df1
                  * (k * _normalProbabilityDistribution.getCDF(sign * d2) - (k + sign * cap) * _normalProbabilityDistribution.getCDF(sign * d4)));
        } catch (final InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }

    };
    return pricingFunction;
  }

  double getD(final double x, final double y, final double z) {
    return (Math.log(x) + y) / z;
  }
}
