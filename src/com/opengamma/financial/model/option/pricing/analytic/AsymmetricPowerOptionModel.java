/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.AsymmetricPowerOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationException;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * Pricing model for asymmetric power options.
 * 
 * @author emcleod
 */
public class AsymmetricPowerOptionModel extends AnalyticOptionModel<AsymmetricPowerOptionDefinition, StandardOptionDataBundle> {
  final ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final AsymmetricPowerOptionDefinition definition) {
    Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(StandardOptionDataBundle data) {
        try {
          double s = data.getSpot();
          double k = definition.getStrike();
          double t = definition.getTimeToExpiry(data.getDate());
          double sigma = data.getVolatility(t, k);
          double r = data.getInterestRate(t);
          double b = data.getCostOfCarry();
          double power = definition.getPower();
          double sigmaT = sigma * Math.sqrt(t);
          double d1 = (Math.log(s / Math.pow(k, 1. / power)) + t * (b + sigma * sigma * (power - 0.5))) / sigmaT;
          double d2 = d1 - power * sigmaT;
          int sign = definition.isCall() ? 1 : -1;
          double df1 = Math.exp(((power - 1) * (r + power * sigma * sigma * 0.5) - power * (r - b)) * t);
          double df2 = Math.exp(-r * t);
          return sign * (Math.pow(s, power) * df1 * _normalProbabilityDistribution.getCDF(sign * d1) - df2 * k * _normalProbabilityDistribution.getCDF(sign * d2));
        } catch (InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }
    };
    return pricingFunction;
  }
}
