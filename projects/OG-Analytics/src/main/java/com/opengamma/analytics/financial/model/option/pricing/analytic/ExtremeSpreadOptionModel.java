/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.definition.ExtremeSpreadOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionWithSpotTimeSeriesDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class ExtremeSpreadOptionModel extends AnalyticOptionModel<ExtremeSpreadOptionDefinition, StandardOptionWithSpotTimeSeriesDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<StandardOptionWithSpotTimeSeriesDataBundle, Double> getPricingFunction(final ExtremeSpreadOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardOptionWithSpotTimeSeriesDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionWithSpotTimeSeriesDataBundle data) {
        Validate.notNull(data, "data");
        final double s = data.getSpot();
        final double b = data.getCostOfCarry();
        final ZonedDateTime date = data.getDate();
        final double t1 = -definition.getTimeFromPeriodEnd(date);
        final double t2 = definition.getTimeToExpiry(date);
        final double r = data.getInterestRate(t2);
        final DoubleTimeSeries<?> ts = data.getSpotTimeSeries();
        final int eta = definition.isCall() ? 1 : -1;
        final int phi = definition.isReverse() ? -1 : 1;
        final double x = eta * phi == 1 ? ts.maxValue() : ts.minValue();
        final double sigma = data.getVolatility(t2, x); //REVIEW emcleod 21-7-10 works for flat vol surfaces
        final double m = Math.log(x / s);
        final double mu1 = b - sigma * sigma / 2;
        final double mu2 = b + sigma * sigma / 2;
        final double df1 = Math.exp(t2 * (b - r));
        final double df2 = Math.exp(b * (t1 - t2));
        final double df3 = Math.exp(-r * t2);
        final double sigmaT2 = sigma * Math.sqrt(t2);
        final double sigmaT1 = sigma * Math.sqrt(t1);
        final double sigmaSq = sigma * sigma;
        final double y = sigmaSq / 2 / b;
        if (definition.isReverse()) {
          final double dt = t2 - t1;
          final double sigmaDT = sigma * Math.sqrt(dt);
          final double z1 = s * df1 * (1 + y) * NORMAL.getCDF(eta * (m - mu2 * t2) / sigmaT2);
          final double z2 = df3 * x * NORMAL.getCDF(eta * (mu1 * t2 - m) / sigmaT2);
          final double z3 = -df3 * x * y * Math.exp(2 * mu1 * m / sigmaSq) * NORMAL.getCDF(eta * (m + mu1 * t2) / sigmaT2);
          final double z4 = -s * df1 * (1 + y) * NORMAL.getCDF(-eta * mu2 * dt / sigmaDT);
          final double z5 = -df2 * df1 * s * (1 - y) * NORMAL.getCDF(eta * mu1 * dt / sigmaDT);
          return -eta * (z1 + z2 + z3 + z4 + z5);
        }
        final double z1 = s * df1 * (1 + y) * NORMAL.getCDF(eta * (mu2 * t2 - m) / sigmaT2);
        final double z2 = -df2 * df1 * s * (1 + y) * NORMAL.getCDF(eta * (mu2 * t1 - m) / sigmaT1);
        final double z3 = df3 * x * NORMAL.getCDF(eta * (m - mu1 * t2) / sigmaT2);
        final double z4 = -df3 * x * y * Math.exp(2 * mu1 * m / sigmaSq) * NORMAL.getCDF(-eta * (m + mu1 * t2) / sigmaT2);
        final double z5 = -df3 * x * NORMAL.getCDF(eta * (m - mu1 * t1) / sigmaT1);
        final double z6 = df3 * x * y * Math.exp(2 * mu1 * m / sigmaSq) * NORMAL.getCDF(-eta * (m + mu1 * t1) / sigmaT1);
        return eta * (z1 + z2 + z3 + z4 + z5 + z6);
      }

    };
  }
}
