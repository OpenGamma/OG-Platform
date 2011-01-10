/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.PoweredOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * Analytic pricing model for powered options. This model is only valid for
 * options with an integer power.
 * 
 */
public class PoweredOptionModel extends AnalyticOptionModel<PoweredOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final PoweredOptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      /**
       * 
       * @throws IllegalArgumentException
       *           If the power is not an integer.
       */
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data);
        if (Math.abs(definition.getPower() - Math.round(definition.getPower())) > 1e-15) {
          throw new OptionPricingException("Analytic powered option pricing model can only be used when then power is an integer");
        }
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double b = data.getCostOfCarry();
        final double r = data.getInterestRate(t);
        final double sigma = data.getVolatility(t, k);
        final long power = Math.round(definition.getPower());
        final int sign = definition.isCall() ? 1 : -1;
        final double sigmaSq = sigma * sigma;
        final double sigmaT = sigma * Math.sqrt(t);
        final double x = (Math.log(s / k) + t * (b - 0.5 * sigma * sigma)) / sigmaT;
        long diff;
        double price = 0;
        for (int i = 0; i <= power; i++) {
          diff = power - i;
          price += getCombinatorial(power, i) * Math.pow(sign * s, diff) * Math.pow(-sign * k, i) * Math.exp((diff - 1) * (r + diff * sigmaSq / 2.) * t - diff * (r - b) * t)
              * NORMAL.getCDF(sign * getD(x, diff, sigmaT, sigmaSq, t));
        }
        return price;
      }
    };
    return pricingFunction;
  }

  long getFactorial(final long i) {
    if (i == 0) {
      return 1;
    }
    if (i <= 2) {
      return i;
    }
    long result = 2;
    for (int j = 3; j <= i; j++) {
      result *= j;
    }
    return result;
  }

  long getCombinatorial(final long i, final long j) {
    return getFactorial(i) / (getFactorial(j) * (getFactorial(i - j)));
  }

  double getD(final double x, final double diff, final double sigmaT, final double sigmaSq, final double t) {
    return x + diff * sigmaSq * t / sigmaT;
  }
}
