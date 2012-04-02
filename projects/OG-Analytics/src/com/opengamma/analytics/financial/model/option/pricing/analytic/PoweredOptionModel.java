/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.PoweredOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.OptionPricingException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Analytic pricing model for powered options. *This model is only valid for options with an integer power.*
 * <p>
 * The price of a powered option is:
 * $$
 * \begin{align*}
 * c &= \sum_{j=0}^i \frac{i!}{j!(i-j)!}S^{i-j}(-K)^j e^{(i-j-1)(r + (i-j)\frac{\sigma^2}{2})T - (i-j)(r-b)T}N(d_{i,j})\\
 * p &= \sum_{j=0}^i \frac{i!}{j!(i-j)!}(-S)^{i-j}K^j e^{(i-j-1)(r + (i-j)\frac{\sigma^2}{2})T - (i-j)(r-b)T}N(-d_{i,j})\\
 * \end{align*}
 * $$
 * where
 * $$
 * \begin{align*}
 * d_{i,j} = \frac{\ln(\frac{S}{K}) + (b + (i - j - \frac{1}{2})\sigma^2)T}{\sigma\sqrt{T}}
 * \end{align*}
 * $$
 * 
 */
public class PoweredOptionModel extends AnalyticOptionModel<PoweredOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final PoweredOptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      /**
       * @throws OptionPricingException If the power is not an integer.
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
