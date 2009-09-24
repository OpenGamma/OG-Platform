/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.PoweredOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * Analytic pricing model for powered options. This model is only valid for
 * options with an integer power.
 * 
 * @author emcleod
 */
public class PoweredOptionModel extends AnalyticOptionModel<PoweredOptionDefinition, StandardOptionDataBundle> {
  final ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final PoweredOptionDefinition definition) {
    Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      /**
       * 
       * @throws IllegalArgumentException
       *           If the power is not an integer.
       */
      @Override
      public Double evaluate(StandardOptionDataBundle data) {
        if (Math.abs(definition.getPower() - Math.round(definition.getPower())) > 1e-15) {
          throw new OptionPricingException("Analytic powered option pricing model can only be used when then power is an integer");
        }
        double s = data.getSpot();
        double k = definition.getStrike();
        double t = definition.getTimeToExpiry(data.getDate());
        double b = data.getCostOfCarry();
        double r = data.getInterestRate(t);
        double sigma = data.getVolatility(t, k);
        long power = Math.round(definition.getPower());
        int sign = definition.isCall() ? 1 : -1;
        double sigmaSq = sigma * sigma;
        double sigmaT = sigma * Math.sqrt(t);
        double x = (Math.log(s / k) + t * (b - 0.5 * sigma * sigma)) / sigmaT;
        long diff;
        double price = 0;
        for (int i = 0; i <= power; i++) {
          diff = power - i;
          price += getCombinatorial(power, i) * Math.pow(sign * s, diff) * Math.pow(-sign * k, i) * Math.exp((diff - 1) * (r + diff * sigmaSq / 2.) * t - diff * (r - b) * t)
              * _normalProbabilityDistribution.getCDF(sign * getD(x, diff, sigmaT, sigmaSq, t));
        }
        return price;
      }
    };
    return pricingFunction;
  }

  long getFactorial(long i) {
    if (i == 0)
      return 1;
    if (i <= 2)
      return i;
    long result = 2;
    for (int j = 3; j <= i; j++) {
      result *= j;
    }
    return result;
  }

  long getCombinatorial(long i, long j) {
    return getFactorial(i) / (getFactorial(j) * (getFactorial(i - j)));
  }

  double getD(double x, double diff, double sigmaT, double sigmaSq, double t) {
    return x + diff * sigmaSq * t / sigmaT;
  }
}
