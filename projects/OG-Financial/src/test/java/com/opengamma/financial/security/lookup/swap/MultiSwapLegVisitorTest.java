/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MultiSwapLegVisitorTest {

  @Test
  public void payFixed() {
    final Frequency annual = SimpleFrequency.ANNUAL;
    final Frequency quarterly = SimpleFrequency.QUARTERLY;
    final SwapSecurity swap = swap(fixedLeg(annual), floatingLeg(quarterly));
    final Pair<Frequency, Frequency> frequencies = new FrequencyVisitor().visit(swap);
    final Pair<Frequency, Frequency> expected = Pairs.of(annual, quarterly);
    assertEquals(expected, frequencies);
  }

  @Test
  public void receiveFixed() {
    final Frequency annual = SimpleFrequency.ANNUAL;
    final Frequency quarterly = SimpleFrequency.QUARTERLY;
    final SwapSecurity swap = swap(floatingLeg(quarterly), fixedLeg(annual));
    final Pair<Frequency, Frequency> frequencies = new FrequencyVisitor().visit(swap);
    final Pair<Frequency, Frequency> expected = Pairs.of(annual, quarterly);
    assertEquals(expected, frequencies);
  }

  @Test
  public void floatFloat() {
    final Frequency annual = SimpleFrequency.ANNUAL;
    final Frequency quarterly = SimpleFrequency.QUARTERLY;
    final SwapSecurity swap = swap(floatingLeg(annual), floatingLeg(quarterly));
    final Pair<Frequency, Frequency> frequencies = new FrequencyVisitor().visit(swap);
    final Pair<Frequency, Frequency> expected = Pairs.of(annual, quarterly);
    assertEquals(expected, frequencies);
  }

  @Test
  public void inflationFixedFloat() {
    final Frequency annual = SimpleFrequency.ANNUAL;
    final Frequency quarterly = SimpleFrequency.QUARTERLY;
    final ZeroCouponInflationSwapSecurity swap = zciSwap(fixedInflationLeg(annual), indexInflationLeg(quarterly));
    final Pair<Frequency, Frequency> frequencies = new FrequencyVisitor().visit(swap);
    final Pair<Frequency, Frequency> expected = Pairs.of(annual, quarterly);
    assertEquals(expected, frequencies);
  }

  @Test
  public void inflationFloatFloat() {
    final Frequency annual = SimpleFrequency.ANNUAL;
    final Frequency quarterly = SimpleFrequency.QUARTERLY;
    final ZeroCouponInflationSwapSecurity swap = zciSwap(indexInflationLeg(annual), indexInflationLeg(quarterly));
    final Pair<Frequency, Frequency> frequencies = new FrequencyVisitor().visit(swap);
    final Pair<Frequency, Frequency> expected = Pairs.of(annual, quarterly);
    assertEquals(expected, frequencies);
  }

  private static SwapSecurity swap(final SwapLeg payLeg, final SwapLeg receiveLeg) {
    return new SwapSecurity(ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), "cpty", payLeg, receiveLeg);
  }

  private static ZeroCouponInflationSwapSecurity zciSwap(final SwapLeg payLeg, final SwapLeg receiveLeg) {
    return new ZeroCouponInflationSwapSecurity(ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), "cpty", payLeg, receiveLeg);
  }

  private static SwapLeg fixedLeg(final Frequency frequency) {
    return new FixedInterestRateLeg(DayCounts.ACT_360,
        frequency,
        ExternalId.of("Reg", "123"),
        BusinessDayConventions.FOLLOWING,
        new InterestRateNotional(Currency.USD, 1234),
        true,
        0.1);
  }

  private static SwapLeg floatingLeg(final Frequency frequency) {
    return new FloatingInterestRateLeg(DayCounts.ACT_360,
        frequency,
        ExternalId.of("Reg", "123"),
        BusinessDayConventions.FOLLOWING,
        new InterestRateNotional(Currency.GBP, 1234),
        true,
        ExternalId.of("Rate", "ABC"),
        FloatingRateType.IBOR);
  }

  private static SwapLeg fixedInflationLeg(final Frequency frequency) {
    return new FixedInflationSwapLeg(DayCounts.ACT_360,
        frequency,
        ExternalId.of("Reg", "123"),
        BusinessDayConventions.FOLLOWING,
        new InterestRateNotional(Currency.USD, 1234),
        true,
        0.1);
  }

  private static SwapLeg indexInflationLeg(final Frequency frequency) {
    return new InflationIndexSwapLeg(DayCounts.ACT_360,
        frequency,
        ExternalId.of("Reg", "123"),
        BusinessDayConventions.FOLLOWING,
        new InterestRateNotional(Currency.USD, 1234),
        true,
        ExternalId.of("Test", "AD"),
        2,
        3,
        InterpolationMethod.MONTH_START_LINEAR);
  }

  private static class FrequencyVisitor extends MultiSwapLegVisitor<Frequency> {

    @Override
    Frequency visitFixedLeg(final FixedInterestRateLeg leg) {
      return leg.getFrequency();
    }

    @Override
    Frequency visitFloatingPayLeg(final FloatingInterestRateLeg leg) {
      return leg.getFrequency();
    }

    @Override
    Frequency visitOtherLeg(final FloatingInterestRateLeg leg) {
      return leg.getFrequency();
    }

    @Override
    Frequency visitFixedInflationLeg(final FixedInflationSwapLeg leg) {
      return leg.getFrequency();
    }

    @Override
    Frequency visitInflationIndexPayLeg(final InflationIndexSwapLeg leg) {
      return leg.getFrequency();
    }

    @Override
    Frequency visitOtherIndexLeg(final InflationIndexSwapLeg leg) {
      return leg.getFrequency();
    }
  }
}
