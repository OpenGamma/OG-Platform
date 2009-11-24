/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.option.definition.BondOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 */
public class BlackBondOptionModel extends AnalyticOptionModel<BondOptionDefinition, StandardOptionDataBundle> {
  protected final ProbabilityDistribution<Double> _normal = new NormalProbabilityDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final BondOptionDefinition definition) {
    if (definition == null)
      throw new IllegalArgumentException("Option definition was null");
    return new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        if (data == null)
          throw new IllegalArgumentException("Data bundle was null");
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
        return sign * p1 * (forwardBondPrice * _normal.getCDF(sign * d1) - k * _normal.getCDF(sign * d2));
      }

    };
  }
}
