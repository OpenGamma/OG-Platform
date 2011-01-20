/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.BondOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class BlackBondOptionModel extends AnalyticOptionModel<BondOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final BondOptionDefinition definition) {
    Validate.notNull(definition);
    return new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data);
        final ZonedDateTime date = data.getDate();
        final double k = definition.getStrike();
        final double tOption = definition.getTimeToExpiry(date);
        final double tBond = definition.getTimeToBondMaturity(date);
        final double sigma = data.getVolatility(tOption, k);
        final double rOption = data.getInterestRate(tOption);
        final double rBond = data.getInterestRate(tBond);
        final double p1 = Math.exp(-rOption * tOption);
        final double p2 = Math.exp(-rBond * tBond);
        final double forwardBondPrice = p1 / p2;
        final double d1 = getD1(forwardBondPrice, k, tOption, sigma, 0);
        final double d2 = getD2(d1, sigma, tOption);
        final int sign = definition.isCall() ? 1 : -1;
        return sign * p1 * (forwardBondPrice * NORMAL.getCDF(sign * d1) - k * NORMAL.getCDF(sign * d2));
      }

    };
  }
}
