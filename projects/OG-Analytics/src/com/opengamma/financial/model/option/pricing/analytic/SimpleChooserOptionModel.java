/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.SimpleChooserOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * Pricing model for simple chooser options.
 * 
 */
public class SimpleChooserOptionModel extends AnalyticOptionModel<SimpleChooserOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

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
        final double t2 = DateUtil.getDifferenceInYears(data.getDate(), definition.getUnderlyingExpiry());
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
