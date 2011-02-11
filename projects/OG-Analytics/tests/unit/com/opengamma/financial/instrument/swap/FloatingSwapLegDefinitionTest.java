/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class FloatingSwapLegDefinitionTest {
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
  private static final ZonedDateTime[] RESET_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 1, 1), DateUtil.getUTCDate(2011, 7, 1), DateUtil.getUTCDate(2012, 1, 2),
      DateUtil.getUTCDate(2012, 7, 2), DateUtil.getUTCDate(2013, 1, 1), DateUtil.getUTCDate(2013, 7, 1), DateUtil.getUTCDate(2014, 1, 1), DateUtil.getUTCDate(2014, 7, 1),
      DateUtil.getUTCDate(2015, 1, 1), DateUtil.getUTCDate(2015, 7, 1)};
  private static final ZonedDateTime[] MATURITY_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 7, 4), DateUtil.getUTCDate(2012, 1, 3), DateUtil.getUTCDate(2012, 7, 3),
      DateUtil.getUTCDate(2013, 1, 3), DateUtil.getUTCDate(2013, 7, 3), DateUtil.getUTCDate(2014, 1, 3), DateUtil.getUTCDate(2014, 7, 3), DateUtil.getUTCDate(2015, 1, 5),
      DateUtil.getUTCDate(2015, 7, 3), DateUtil.getUTCDate(2016, 1, 4)};
  private static final double NOTIONAL = 1000000;
  private static final double RATE = 0.05;
  private static final double SPREAD = 0.01;
  private static final FloatingSwapLegDefinition DEFINITION = new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, SPREAD,
      CONVENTION);

  @Test(expected = IllegalArgumentException.class)
  public void testNullEffectiveDate() {
    new FloatingSwapLegDefinition(null, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullNominalDates() {
    new FloatingSwapLegDefinition(EFFECTIVE_DATE, null, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSettlementDates() {
    new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, null, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLengthSettlementDates() {
    new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, new ZonedDateTime[0], RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullResetDates() {
    new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, null, MATURITY_DATES, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLengthResetDates() {
    new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, new ZonedDateTime[0], MATURITY_DATES, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMaturityDates() {
    new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, null, NOTIONAL, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLengthMaturityDates() {
    new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, new ZonedDateTime[0], NOTIONAL, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullConvention() {
    new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDate() {
    DEFINITION.toDerivative(null, new String[] {"e", "f"});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExpired() {
    DEFINITION.toDerivative(LocalDate.of(2100, 1, 1), new String[] {"w", "de"});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullNames() {
    DEFINITION.toDerivative(EFFECTIVE_DATE.toLocalDate(), (String[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOneName() {
    DEFINITION.toDerivative(EFFECTIVE_DATE.toLocalDate(), "d");
  }

  @Test
  public void test() {
    assertEquals(DEFINITION.getConvention(), CONVENTION);
    assertEquals(DEFINITION.getEffectiveDate(), EFFECTIVE_DATE);
    assertEquals(DEFINITION.getInitialRate(), RATE, 0);
    assertArrayEquals(DEFINITION.getMaturityDates(), MATURITY_DATES);
    assertArrayEquals(DEFINITION.getNominalDates(), NOMINAL_DATES);
    assertEquals(DEFINITION.getNotional(), NOTIONAL, 0);
    assertArrayEquals(DEFINITION.getResetDates(), RESET_DATES);
    assertArrayEquals(DEFINITION.getSettlementDates(), SETTLEMENT_DATES);
    assertEquals(DEFINITION.getSpread(), SPREAD, 0);
    FloatingSwapLegDefinition other = new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, SPREAD, CONVENTION);
    assertEquals(other, DEFINITION);
    assertEquals(other.hashCode(), DEFINITION.hashCode());
    other = new FloatingSwapLegDefinition(EFFECTIVE_DATE.minusDays(1), NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, SPREAD, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FloatingSwapLegDefinition(EFFECTIVE_DATE, SETTLEMENT_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, SPREAD, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, NOMINAL_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, SPREAD, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, NOMINAL_DATES, MATURITY_DATES, NOTIONAL, RATE, SPREAD, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, NOMINAL_DATES, NOTIONAL, RATE, SPREAD, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL + 1, RATE, SPREAD, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE + 1, SPREAD, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, SPREAD + 1, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL, RATE, SPREAD, new SwapConvention(SETTLEMENT_DAYS + 1, DAY_COUNT,
        BUSINESS_DAY, CALENDAR, IS_EOM, NAME));
    assertFalse(other.equals(DEFINITION));
  }

  @Test
  public void testConversion() {
    final String[] names = new String[] {"W", "E"};
    GenericAnnuity<Payment> payments = DEFINITION.toDerivative(DATE.toLocalDate(), names);
    final int n = 2;
    assertEquals(payments.getNumberOfPayments(), NOMINAL_DATES.length - n);
    assertTrue(payments.getNthPayment(0) instanceof ForwardLiborPayment);
    ForwardLiborPayment payment = (ForwardLiborPayment) payments.getNthPayment(0);
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    assertEquals(payment.getPaymentTime(), actAct.getDayCountFraction(DATE, SETTLEMENT_DATES[n]), 0);
    assertEquals(payment.getNotional(), NOTIONAL, 0);
    assertEquals(payment.getLiborFixingTime(), actAct.getDayCountFraction(DATE, RESET_DATES[n]), 0);
    assertEquals(payment.getLiborMaturityTime(), actAct.getDayCountFraction(DATE, MATURITY_DATES[n]), 0);
    assertEquals(payment.getPaymentYearFraction(), DAY_COUNT.getDayCountFraction(SETTLEMENT_DATES[n - 1], SETTLEMENT_DATES[n]), 0);
    assertEquals(payment.getForwardYearFraction(), DAY_COUNT.getDayCountFraction(SETTLEMENT_DATES[n - 1], SETTLEMENT_DATES[n]), 0);
    assertEquals(payment.getSpread(), SPREAD, 0);
    assertEquals(payment.getFundingCurveName(), names[0]);
    assertEquals(payment.getLiborCurveName(), names[1]);
    for (int i = n + 1; i < NOMINAL_DATES.length; i++) {
      assertTrue(payments.getNthPayment(i - n) instanceof ForwardLiborPayment);
      payment = (ForwardLiborPayment) payments.getNthPayment(i - n);
      assertEquals(payment.getPaymentTime(), actAct.getDayCountFraction(DATE, SETTLEMENT_DATES[i]), 0);
      assertEquals(payment.getNotional(), NOTIONAL, 0);
      assertEquals(payment.getLiborFixingTime(), actAct.getDayCountFraction(DATE, RESET_DATES[i]), 0);
      assertEquals(payment.getLiborMaturityTime(), actAct.getDayCountFraction(DATE, MATURITY_DATES[i]), 0);
      assertEquals(payment.getPaymentYearFraction(), DAY_COUNT.getDayCountFraction(SETTLEMENT_DATES[i - 1], SETTLEMENT_DATES[i]), 0);
      assertEquals(payment.getForwardYearFraction(), DAY_COUNT.getDayCountFraction(SETTLEMENT_DATES[i - 1], SETTLEMENT_DATES[i]), 0);
      assertEquals(payment.getSpread(), SPREAD, 0);
      assertEquals(payment.getFundingCurveName(), names[0]);
      assertEquals(payment.getLiborCurveName(), names[1]);
    }
    payments = DEFINITION.toDerivative(EFFECTIVE_DATE.toLocalDate(), names);
    assertEquals(payments.getNumberOfPayments(), NOMINAL_DATES.length - 1);
    assertTrue(payments.getNthPayment(0) instanceof FixedCouponPayment);
    final FixedCouponPayment firstPayment = (FixedCouponPayment) payments.getNthPayment(0);
    assertEquals(firstPayment.getPaymentTime(), actAct.getDayCountFraction(EFFECTIVE_DATE, SETTLEMENT_DATES[1]), 0);
    assertEquals(firstPayment.getCoupon(), RATE + SPREAD, 0);
    assertEquals(firstPayment.getNotional(), NOTIONAL, 0);
    assertEquals(firstPayment.getFundingCurveName(), names[0]);
    for (int i = 1; i < payments.getNumberOfPayments(); i++) {
      assertTrue(payments.getNthPayment(i) instanceof ForwardLiborPayment);
      payment = (ForwardLiborPayment) payments.getNthPayment(i);
      assertEquals(payment.getPaymentTime(), actAct.getDayCountFraction(EFFECTIVE_DATE, SETTLEMENT_DATES[i + 1]), 0);
      assertEquals(payment.getNotional(), NOTIONAL, 0);
      assertEquals(payment.getLiborFixingTime(), actAct.getDayCountFraction(EFFECTIVE_DATE, RESET_DATES[i + 1]), 0);
      assertEquals(payment.getLiborMaturityTime(), actAct.getDayCountFraction(EFFECTIVE_DATE, MATURITY_DATES[i + 1]), 0);
      assertEquals(payment.getPaymentYearFraction(), DAY_COUNT.getDayCountFraction(SETTLEMENT_DATES[i], SETTLEMENT_DATES[i + 1]), 0);
      assertEquals(payment.getForwardYearFraction(), DAY_COUNT.getDayCountFraction(SETTLEMENT_DATES[i], SETTLEMENT_DATES[i + 1]), 0);
      assertEquals(payment.getSpread(), SPREAD, 0);
      assertEquals(payment.getFundingCurveName(), names[0]);
      assertEquals(payment.getLiborCurveName(), names[1]);
    }
  }
}
