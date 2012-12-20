/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.GapOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Class for pricing gap options (see {@link com.opengamma.analytics.financial.model.option.definition.GapOptionDefinition}).
 * <p>
 * The price is calculated using the Reiner-Rubenstein formula:
 * $$
 * \begin{align*}
 * c &= S e^{(b-r)T}N(d_1) - K_2 e^{-rT}N(d_2)\\
 * p &= K_2 e^{-rT}N(-d_2) - S e^{(b-r)T}N(-d_1)
 * \end{align*}
 * $$
 * where
 * $$
 * \begin{align*}
 * d_1 = \frac{\ln{\frac{S}{K_1}} + (b + \frac{\sigma^2}{2})T}{\sigma\sqrt{T}}
 * \end{align*}
 * $$
 * and
 * $$
 * \begin{align*}
 * d_2 = d_1 - \sigma\sqrt{T}
 * \end{align*}
 * $$
 * 
 */
public class GapOptionModel extends AnalyticOptionModel<GapOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final GapOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data, "data");
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        final double sigma = data.getVolatility(t, k);
        final double b = data.getCostOfCarry();
        final double payoffStrike = definition.getPayoffStrike();
        final int sign = definition.isCall() ? 1 : -1;
        final double d1 = getD1(s, k, t, sigma, b);
        final double d2 = getD2(d1, sigma, t);
        return sign * (s * Math.exp(t * (b - r)) * NORMAL.getCDF(sign * d1) - payoffStrike * Math.exp(-r * t) * NORMAL.getCDF(sign * d2));
      }

    };
  }

}
