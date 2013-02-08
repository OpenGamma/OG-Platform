/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.SimpleChooserOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.time.DateUtils;

/**
 * Pricing model for capped power options (see {@link com.opengamma.analytics.financial.model.option.definition.CappedPowerOptionDefinition}).
 * <p>
 * The price of this option is given by:
 * $$
 * \begin{align*}
 * p = Se^{(b-r)T_2}N(d_1) - Ke^{-rT_2}N(d_1 - \sigma\sqrt{T_2}) - Se^{(b-r)T_2}N(-d_2) + Ke^{-rT_2}N(-d_2 + \sigma\sqrt{T_1})
 * \end{align*}
 * $$
 * where $T_1$ is the time to make the choice and
 * $$
 * \begin{align*}
 * d_1 &= \frac{\ln(\frac{S}{K}) + (b + \frac{\sigma^2}{2})T_2}{\sigma\sqrt{T_1}}\\
 * d_2 &= \frac{\ln(\frac{S}{K}) + bT_2 + \frac{\sigma^2 T_1}{2}}{\sigma\sqrt{T_1}}
 * \end{align*}
 * $$
 */
public class SimpleChooserOptionModel extends AnalyticOptionModel<SimpleChooserOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final SimpleChooserOptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data);
        final double s = data.getSpot();
        final double k = definition.getUnderlyingStrike();
        final double t1 = definition.getTimeToExpiry(data.getDate());
        final double t2 = DateUtils.getDifferenceInYears(data.getDate(), definition.getUnderlyingExpiry().getExpiry());
        final double b = data.getCostOfCarry();
        final double r = data.getInterestRate(t1);
        final double sigma = data.getVolatility(t1, k);
        final double sigmaT1 = sigma * Math.sqrt(t1);
        final double sigmaT2 = sigma * Math.sqrt(t2);
        final double sigmaSq = sigma * sigma / 2.;
        final double logSK = Math.log(s / k);
        final double bT2 = b * t2;
        final double d = getD(logSK, bT2, sigmaSq * t2, sigmaT2);
        final double y = getD(logSK, bT2, sigmaSq * t1, sigmaT1);
        final double df1 = getDF(r, b, t2);
        final double df2 = getDF(r, 0, t2);
        return s * df1 * (NORMAL.getCDF(d) - NORMAL.getCDF(-y)) - k * df2 * (NORMAL.getCDF(d - sigmaT2) - NORMAL.getCDF(-y + sigmaT1));
      }

    };
    return pricingFunction;
  }

  double getD(final double x, final double y, final double z, final double sigmaT) {
    return (x + y + z) / sigmaT;
  }
}
