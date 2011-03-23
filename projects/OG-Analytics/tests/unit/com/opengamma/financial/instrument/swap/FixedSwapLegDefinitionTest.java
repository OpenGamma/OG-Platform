/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import static org.junit.Assert.assertArrayEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class FixedSwapLegDefinitionTest {
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final boolean IS_EOM = true;
  private static final String NAME = "CONVENTION";
  private static final SwapConvention CONVENTION = new SwapConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, IS_EOM, NAME);
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2011, 8, 1);
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime[] NOMINAL_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 1, 3), DateUtil.getUTCDate(2011, 7, 3), DateUtil.getUTCDate(2012, 1, 3),
      DateUtil.getUTCDate(2012, 7, 3), DateUtil.getUTCDate(2013, 1, 3), DateUtil.getUTCDate(2013, 7, 3), DateUtil.getUTCDate(2014, 1, 3), DateUtil.getUTCDate(2014, 7, 3),
      DateUtil.getUTCDate(2015, 1, 3), DateUtil.getUTCDate(2015, 7, 3)};
  private static final ZonedDateTime[] SETTLEMENT_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 1, 3), DateUtil.getUTCDate(2011, 7, 4), DateUtil.getUTCDate(2012, 1, 3),
      DateUtil.getUTCDate(2012, 7, 3), DateUtil.getUTCDate(2013, 1, 3), DateUtil.getUTCDate(2013, 7, 3), DateUtil.getUTCDate(2014, 1, 3), DateUtil.getUTCDate(2014, 7, 3),
      DateUtil.getUTCDate(2015, 1, 5), DateUtil.getUTCDate(2015, 7, 3)};
  private static final double NOTIONAL = 1000000;
  private static final double RATE = 0.05;
  private static final FixedSwapLegDefinition DEFINITION = new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, CONVENTION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEffectiveDate() {
    new FixedSwapLegDefinition(null, NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominalDates() {
    new FixedSwapLegDefinition(EFFECTIVE_DATE, null, SETTLEMENT_DATES, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDates() {
    new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, null, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConvention() {
    new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeRate() {
    new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, -RATE, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDatesLength() {
    new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, new ZonedDateTime[] {}, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    DEFINITION.toDerivative(null, new String[] {"B"});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNames() {
    DEFINITION.toDerivative(LocalDate.of(2011, 2, 1), (String[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyNames() {
    DEFINITION.toDerivative(LocalDate.of(2011, 2, 1), new String[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAfterLastPayment() {
    DEFINITION.toDerivative(LocalDate.of(2100, 1, 1), new String[] {"S"});
  }

  @Test
  public void test() {
    assertEquals(DEFINITION.getConvention(), CONVENTION);
    assertEquals(DEFINITION.getEffectiveDate(), EFFECTIVE_DATE);
    assertArrayEquals(DEFINITION.getNominalDates(), NOMINAL_DATES);
    assertEquals(DEFINITION.getNotional(), NOTIONAL, 0);
    assertEquals(DEFINITION.getRate(), RATE, 0);
    assertArrayEquals(DEFINITION.getSettlementDates(), SETTLEMENT_DATES);
    FixedSwapLegDefinition other = new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, CONVENTION);
    assertEquals(other, DEFINITION);
    assertEquals(other.hashCode(), DEFINITION.hashCode());
    other = new FixedSwapLegDefinition(EFFECTIVE_DATE.plusDays(1), NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FixedSwapLegDefinition(EFFECTIVE_DATE, SETTLEMENT_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, NOMINAL_DATES, NOTIONAL, RATE, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL + 1, RATE, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE + 1, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL, RATE, new SwapConvention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, !IS_EOM, NAME));
    assertFalse(other.equals(DEFINITION));
  }

  @Test
  public void testConversion() {
    final String yieldCurveName = "R";
    final GenericAnnuity<CouponFixed> annuity = DEFINITION.toDerivative(DATE.toLocalDate(), yieldCurveName);
    final int n = annuity.getNumberOfPayments();
    final int offset = 2;
    assertEquals(n, SETTLEMENT_DATES.length - offset);
    for (int i = 0; i < n; i++) {
      final CouponFixed nthPayment = annuity.getNthPayment(i);
      assertEquals(NOTIONAL, nthPayment.getNotional(), 0);
      assertEquals(nthPayment.getFundingCurveName(), yieldCurveName);
      final double paymentTime = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA").getDayCountFraction(DATE, SETTLEMENT_DATES[i + offset]);
      assertEquals(nthPayment.getPaymentTime(), paymentTime, 0);
      final double yearFraction = DAY_COUNT.getDayCountFraction(SETTLEMENT_DATES[i + offset - 1], SETTLEMENT_DATES[i + offset]);
      assertEquals(nthPayment.getPaymentYearFraction(), yearFraction, 0);
      assertEquals(nthPayment.getFixedRate(), RATE, 0);
    }
  }
}
