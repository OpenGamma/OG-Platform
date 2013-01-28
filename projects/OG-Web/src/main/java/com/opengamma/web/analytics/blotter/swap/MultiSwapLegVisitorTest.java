/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter.swap;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class MultiSwapLegVisitorTest {

  @Test
  public void payFixed() {
    Frequency annual = SimpleFrequency.ANNUAL;
    Frequency quarterly = SimpleFrequency.QUARTERLY;
    SwapSecurity swap = swap(fixedLeg(annual), floatingLeg(quarterly));
    Pair<Frequency, Frequency> frequencies = new FrequencyVisitor().visit(swap);
    Pair<Frequency, Frequency> expected = Pair.of(annual, quarterly);
    assertEquals(expected, frequencies);
  }

  @Test
  public void receiveFixed() {
    Frequency annual = SimpleFrequency.ANNUAL;
    Frequency quarterly = SimpleFrequency.QUARTERLY;
    SwapSecurity swap = swap(floatingLeg(quarterly), fixedLeg(annual));
    Pair<Frequency, Frequency> frequencies = new FrequencyVisitor().visit(swap);
    Pair<Frequency, Frequency> expected = Pair.of(annual, quarterly);
    assertEquals(expected, frequencies);
  }

  @Test
  public void floatFloat() {
    Frequency annual = SimpleFrequency.ANNUAL;
    Frequency quarterly = SimpleFrequency.QUARTERLY;
    SwapSecurity swap = swap(floatingLeg(annual), floatingLeg(quarterly));
    Pair<Frequency, Frequency> frequencies = new FrequencyVisitor().visit(swap);
    Pair<Frequency, Frequency> expected = Pair.of(annual, quarterly);
    assertEquals(expected, frequencies);
  }

  private static SwapSecurity swap(SwapLeg payLeg, SwapLeg receiveLeg) {
    return new SwapSecurity(ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), "cpty", payLeg, receiveLeg);
  }

  private static SwapLeg fixedLeg(Frequency frequency) {
    return new FixedInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("Actual/360"),
                                    frequency,
                                    ExternalId.of("Reg", "123"),
                                    BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"),
                                    new InterestRateNotional(Currency.USD, 1234),
                                    true,
                                    0.1);
  }

  private static SwapLeg floatingLeg(Frequency frequency) {
    return new FloatingInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("Actual/360"),
                                       frequency,
                                       ExternalId.of("Reg", "123"),
                                       BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"),
                                       new InterestRateNotional(Currency.GBP, 1234),
                                       true,
                                       ExternalId.of("Rate", "ABC"),
                                       FloatingRateType.IBOR);
  }

  private static class FrequencyVisitor extends MultiSwapLegVisitor<Frequency> {

    @Override
    Frequency visitFixedLeg(FixedInterestRateLeg leg) {
      return leg.getFrequency();
    }

    @Override
    Frequency visitFloatingPayLeg(FloatingInterestRateLeg leg) {
      return leg.getFrequency();
    }

    @Override
    Frequency visitOtherLeg(FloatingInterestRateLeg leg) {
      return leg.getFrequency();
    }
  }
}
