/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SupershareOptionDefinition;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Class for pricing supershare options (see {@link com.opengamma.analytics.financial.model.option.definition.SupershareOptionDefinition}).
 * <p>
 * The price is calculated using the formula:
 * $$
 * \begin{align*}
 * w = \frac{S e^{(b-r)T}}{K_L}(N(d_1) - N(d_2))
 * \end{align*}
 * $$
 * where
 * $$
 * \begin{align*}
 * d_1 &= \frac{\ln{\frac{S}{K_L}} + (b + \frac{\sigma^2}{2})T}{\sigma\sqrt{T}}\\
 * d_2 &= \frac{\ln{\frac{S}{K_H}} + (b + \frac{\sigma^2}{2})T}{\sigma\sqrt{T}}
 * \end{align*}
 * $$
 * 
 */
public class SupershareOptionModel extends AnalyticOptionModel<SupershareOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final SupershareOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data, "data");
        final double s = data.getSpot();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        final double b = data.getCostOfCarry();
        final double lower = definition.getLowerBound();
        final double upper = definition.getUpperBound();
        final double sigma = data.getVolatility(t, lower);
        final double d1 = getD1(s, lower, t, sigma, b);
        final double d2 = getD1(s, upper, t, sigma, b);
        return s * Math.exp(t * (b - r)) * (NORMAL.getCDF(d1) - NORMAL.getCDF(d2)) / lower;
      }

    };
  }
}
