/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.definition.FadeInOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionWithSpotTimeSeriesDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class FadeInOptionModel extends AnalyticOptionModel<FadeInOptionDefinition, StandardOptionWithSpotTimeSeriesDataBundle> {
  private static final ProbabilityDistribution<double[]> BIVARIATE_NORMAL = new BivariateNormalDistribution();

  @Override
  public Function1D<StandardOptionWithSpotTimeSeriesDataBundle, Double> getPricingFunction(final FadeInOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardOptionWithSpotTimeSeriesDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionWithSpotTimeSeriesDataBundle data) {
        Validate.notNull(data, "data");
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double b = data.getCostOfCarry();
        final ZonedDateTime date = data.getDate();
        final double t = definition.getTimeToExpiry(date);
        final double r = data.getInterestRate(t);
        final double sigma = data.getVolatility(t, k);
        final double l = definition.getLowerBound();
        final double u = definition.getUpperBound();
        final double n = data.getSpotTimeSeries().size();
        final double df1 = Math.exp(t * (b - r));
        final double df2 = Math.exp(-r * t);
        final int sign = definition.isCall() ? 1 : -1;
        double rho, tI, d1, d2, d3, d4, d5, d6;
        double price = 0;
        for (int i = 0; i < n; i++) {
          tI = i * t / n;
          rho = -sign * Math.sqrt(tI / t);
          d1 = getD1(s, k, t, sigma, b);
          d2 = getD2(d1, sigma, t);
          d3 = getD1(s, l, tI, sigma, b);
          d4 = getD2(d3, sigma, tI);
          d5 = getD1(s, u, tI, sigma, b);
          d6 = getD2(d5, sigma, tI);
          price += sign
              * (s * df1 * (BIVARIATE_NORMAL.getCDF(new double[] {-d5, sign * d1, rho}) - BIVARIATE_NORMAL.getCDF(new double[] {-d3, sign * d1, rho})) - k * df2
                  * (BIVARIATE_NORMAL.getCDF(new double[] {-d6, sign * d2, rho}) - BIVARIATE_NORMAL.getCDF(new double[] {-d4, sign * d2, rho})));
        }
        return price / n;
      }

    };
  }
}
