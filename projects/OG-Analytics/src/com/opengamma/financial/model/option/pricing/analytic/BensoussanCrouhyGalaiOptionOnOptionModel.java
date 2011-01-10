/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.option.definition.EuropeanOptionOnEuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class BensoussanCrouhyGalaiOptionOnOptionModel extends AnalyticOptionModel<EuropeanOptionOnEuropeanVanillaOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();
  private static final Set<Greek> REQUIRED_GREEKS = Sets.newHashSet(Greek.FAIR_PRICE, Greek.DELTA);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final EuropeanOptionOnEuropeanVanillaOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data, "data");
        final double s = data.getSpot();
        final OptionDefinition underlying = definition.getUnderlyingOption();
        final double k1 = definition.getStrike();
        final double k2 = underlying.getStrike();
        final ZonedDateTime date = data.getDate();
        final double t1 = definition.getTimeToExpiry(date);
        final double sigma = data.getVolatility(t1, k1);
        final double r = data.getInterestRate(t1);
        final double b = data.getCostOfCarry();
        final OptionDefinition callDefinition = underlying.isCall() ? underlying : new EuropeanVanillaOptionDefinition(k2, underlying.getExpiry(), true);
        final GreekResultCollection result = BSM.getGreeks(callDefinition, data, REQUIRED_GREEKS);
        final double callBSM = result.get(Greek.FAIR_PRICE);
        final double callDelta = result.get(Greek.DELTA);
        final double underlyingSigma = sigma * Math.abs(callDelta) * s / callBSM;
        final double d1 = getD1(callBSM, k1, t1, underlyingSigma, b);
        final double d2 = getD2(d1, underlyingSigma, t1);
        final int sign = definition.isCall() ? 1 : -1;
        return sign * (callBSM * NORMAL.getCDF(sign * d1) - k1 * Math.exp(-r * t1) * NORMAL.getCDF(sign * d2));
      }

    };
  }
}
