/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.definition.FloatingStrikeLookbackOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionWithSpotTimeSeriesDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class FloatingStrikeLookbackOptionModel extends AnalyticOptionModel<FloatingStrikeLookbackOptionDefinition, StandardOptionWithSpotTimeSeriesDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<StandardOptionWithSpotTimeSeriesDataBundle, Double> getPricingFunction(final FloatingStrikeLookbackOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardOptionWithSpotTimeSeriesDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionWithSpotTimeSeriesDataBundle data) {
        Validate.notNull(data, "data");
        final DoubleTimeSeries<?> ts = data.getSpotTimeSeries();
        final double s = data.getSpot();
        final ZonedDateTime date = data.getDate();
        final double t = definition.getTimeToExpiry(date);
        final boolean isCall = definition.isCall();
        final double k = isCall ? ts.minValue() : ts.maxValue();
        final int sign = isCall ? 1 : -1;
        final double sigma = data.getVolatility(t, k);
        final double r = data.getInterestRate(t);
        final double b = data.getCostOfCarry();
        final double d1 = getD1(s, k, t, sigma, b);
        final double d2 = getD2(d1, sigma, t);
        final double df1 = Math.exp(t * (b - r));
        final double df2 = Math.exp(-r * t);
        final double cdf1 = NORMAL.getCDF(d1);
        double y;
        if (CompareUtils.closeEquals(b, 0, 1e-15)) {
          final double x = isCall ? cdf1 - 1 : cdf1;
          y = CompareUtils.closeEquals(sigma, 0, 1e-15) ? 0 : s * df2 * sigma * Math.sqrt(t) * (NORMAL.getPDF(d1) + d1 * x);
        } else {
          y = CompareUtils.closeEquals(sigma, 0, 1e-15) ? 0 : s * df2 * sigma * sigma
              * (sign * Math.pow(s / k, -2 * b / sigma / sigma) * NORMAL.getCDF(sign * (2 * b * Math.sqrt(t) / sigma - d1)) - sign * Math.exp(b * t) * NORMAL.getCDF(-sign * d1)) / 2 / b;
        }
        return sign * (s * df1 * NORMAL.getCDF(sign * d1) - k * df2 * NORMAL.getCDF(sign * d2)) + y;
      }
    };
  }
}
