/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AnnuityCouponFixedDefinitionTest {
  //Semi-annual 2Y
  private static final Currency CUR = Currency.EUR;
  private static final PeriodFrequency PAYMENT_FREQUENCY = PeriodFrequency.SEMI_ANNUAL;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 17);
  private static final double NOTIONAL = 1000000;
  private static final double RATE = 0.0325;
  private static final boolean IS_PAYER = true;

  private static final ZonedDateTime MATURITY_DATE = SETTLEMENT_DATE.plus(ANNUITY_TENOR);
  private static final ZonedDateTime[] PAYMENT_DATES_UNADJUSTED = ScheduleCalculator.getUnadjustedDateSchedule(SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_FREQUENCY);
  private static final ZonedDateTime[] PAYMENT_DATES = ScheduleCalculator.getAdjustedDateSchedule(PAYMENT_DATES_UNADJUSTED, BUSINESS_DAY, CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 3, 15); //For conversion to derivative

  @Test
  public void test() {
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[PAYMENT_DATES.length];
    //First coupon uses settlement date
    final double sign = IS_PAYER ? -1.0 : 1.0;
    coupons[0] = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), sign * NOTIONAL, RATE);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), sign * NOTIONAL, RATE);
    }
    final AnnuityCouponFixedDefinition fixedAnnuity = new AnnuityCouponFixedDefinition(coupons, CALENDAR);

    assertEquals(fixedAnnuity.isPayer(), IS_PAYER);
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      assertEquals(fixedAnnuity.getNthPayment(loopcpn), coupons[loopcpn]);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPayments() {
    new AnnuityCouponFixedDefinition(null, null);
  }

  @Test
  public void testEqualHash() {
    final double sign = IS_PAYER ? -1.0 : 1.0;
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[PAYMENT_DATES.length];
    coupons[0] = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), sign * NOTIONAL, RATE);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), sign * NOTIONAL, RATE);
    }
    final AnnuityCouponFixedDefinition fixedAnnuity = new AnnuityCouponFixedDefinition(coupons, CALENDAR);
    final AnnuityCouponFixedDefinition fixedAnnuity2 = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE,
        IS_PAYER);
    assertEquals(fixedAnnuity, fixedAnnuity2);
    assertEquals(fixedAnnuity.hashCode(), fixedAnnuity2.hashCode());
    final AnnuityCouponFixedDefinition modifiedFixedAnnuity1 = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM,
        NOTIONAL, RATE, !IS_PAYER);
    assertFalse(fixedAnnuity.equals(modifiedFixedAnnuity1));
    final AnnuityCouponFixedDefinition modifiedFixedAnnuity2 = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM,
        NOTIONAL, RATE, IS_PAYER);
    assertFalse(modifiedFixedAnnuity2.equals(modifiedFixedAnnuity1));
    final AnnuityCouponFixedDefinition bond1 = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_TENOR, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY,
        IS_EOM, NOTIONAL, RATE, !IS_PAYER);
    AnnuityCouponFixedDefinition bond2 = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_TENOR, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM,
        NOTIONAL, RATE, IS_PAYER);
    assertFalse(bond1.equals(bond2));
    bond2 = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_TENOR, 2, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
    assertFalse(bond1.equals(bond2));
    bond2 = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_TENOR, 2, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, !IS_PAYER);
    assertEquals(bond1, bond2);
    final CouponFixedDefinition[] payments = bond2.getPayments();
    bond2 = new AnnuityCouponFixedDefinition(payments, CALENDAR);
    assertEquals(bond1, bond2);
  }

  @Test
  public void testPaymentDates() {
    AnnuityCouponFixedDefinition fixedAnnuity;
    ZonedDateTime[] expectedPaymentDate;
    // End date is modified
    fixedAnnuity = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
    expectedPaymentDate = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 19), DateUtils.getUTCDate(2012, 3, 19), DateUtils.getUTCDate(2012, 9, 17), DateUtils.getUTCDate(2013, 3, 18) };
    for (int loopcpn = 0; loopcpn < expectedPaymentDate.length; loopcpn++) {
      assertEquals(expectedPaymentDate[loopcpn], fixedAnnuity.getNthPayment(loopcpn).getPaymentDate());
    }
    // Check modified in modified following.
    final ZonedDateTime settlementDateModified = DateUtils.getUTCDate(2011, 3, 31);
    fixedAnnuity = AnnuityCouponFixedDefinition.from(CUR, settlementDateModified, ANNUITY_TENOR, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
    expectedPaymentDate = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 30), DateUtils.getUTCDate(2012, 3, 30), DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2013, 3, 29) };
    for (int loopcpn = 0; loopcpn < expectedPaymentDate.length; loopcpn++) {
      assertEquals(expectedPaymentDate[loopcpn], fixedAnnuity.getNthPayment(loopcpn).getPaymentDate());
    }
    // End-of-month
    final ZonedDateTime settlementDateEOM = DateUtils.getUTCDate(2011, 2, 28);
    fixedAnnuity = AnnuityCouponFixedDefinition.from(CUR, settlementDateEOM, ANNUITY_TENOR, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
    expectedPaymentDate = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 8, 31), DateUtils.getUTCDate(2012, 2, 29), DateUtils.getUTCDate(2012, 8, 31), DateUtils.getUTCDate(2013, 2, 28) };
    for (int loopcpn = 0; loopcpn < expectedPaymentDate.length; loopcpn++) {
      assertEquals(expectedPaymentDate[loopcpn], fixedAnnuity.getNthPayment(loopcpn).getPaymentDate());
    }

  }

  @SuppressWarnings("deprecation")
  @Test
  public void testToDerivativeDeprecated() {
    final double sign = IS_PAYER ? -1.0 : 1.0;
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[PAYMENT_DATES.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), sign * NOTIONAL, RATE);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), sign * NOTIONAL, RATE);
    }
    final AnnuityCouponFixedDefinition fixedAnnuity = new AnnuityCouponFixedDefinition(coupons, CALENDAR);
    final String fundingCurve = "Funding";
    final CouponFixed[] couponFixedConverted = new CouponFixed[PAYMENT_DATES.length];
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      couponFixedConverted[loopcpn] = fixedAnnuity.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, fundingCurve);
    }
    final AnnuityCouponFixed referenceAnnuity = new AnnuityCouponFixed(couponFixedConverted);
    final AnnuityCouponFixed convertedDefinition = fixedAnnuity.toDerivative(REFERENCE_DATE, fundingCurve);
    assertEquals(referenceAnnuity, convertedDefinition);
  }

  @Test
  public void testToDerivative() {
    final double sign = IS_PAYER ? -1.0 : 1.0;
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[PAYMENT_DATES.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), sign * NOTIONAL, RATE);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), sign * NOTIONAL, RATE);
    }
    final AnnuityCouponFixedDefinition fixedAnnuity = new AnnuityCouponFixedDefinition(coupons, CALENDAR);
    final CouponFixed[] couponFixedConverted = new CouponFixed[PAYMENT_DATES.length];
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      couponFixedConverted[loopcpn] = fixedAnnuity.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE);
    }
    final AnnuityCouponFixed referenceAnnuity = new AnnuityCouponFixed(couponFixedConverted);
    final AnnuityCouponFixed convertedDefinition = fixedAnnuity.toDerivative(REFERENCE_DATE);
    assertEquals(referenceAnnuity, convertedDefinition);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullCurrency1() {
    AnnuityCouponFixedDefinition.from(null, SETTLEMENT_DATE, PAYMENT_TENOR, ANNUITY_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullSettlementDate1() {
    AnnuityCouponFixedDefinition.from(CUR, null, PAYMENT_TENOR, ANNUITY_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullTenorPeriod() {
    AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, (Period) null, ANNUITY_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullPaymentTenor1() {
    AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, PAYMENT_TENOR, null, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullCalendar1() {
    AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, PAYMENT_TENOR, ANNUITY_TENOR, null, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullDayCount1() {
    AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, PAYMENT_TENOR, ANNUITY_TENOR, CALENDAR, null, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullBusinessDay1() {
    AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, PAYMENT_TENOR, ANNUITY_TENOR, CALENDAR, DAY_COUNT, null, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullCurrency2() {
    AnnuityCouponFixedDefinition.from(null, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_FREQUENCY, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullSettlementDate2() {
    AnnuityCouponFixedDefinition.from(CUR, null, MATURITY_DATE, PAYMENT_FREQUENCY, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullMaturityDate() {
    AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, (ZonedDateTime) null, PAYMENT_FREQUENCY, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullPaymentFrequency() {
    AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, MATURITY_DATE, (PeriodFrequency) null, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullCalendar2() {
    AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_FREQUENCY, null, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullDayCount2() {
    AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_FREQUENCY, CALENDAR, null, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullBusinessDay2() {
    AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_FREQUENCY, CALENDAR, DAY_COUNT, null, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullCurrency3() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(null, SETTLEMENT_DATE, MATURITY_DATE, ANNUITY_TENOR, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullSettlementDate3() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, null, MATURITY_DATE, ANNUITY_TENOR, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullMaturityDate2() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, null, ANNUITY_TENOR, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullPaymentTenor2() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, null, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullCalendar3() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, ANNUITY_TENOR, true, true, null, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullDayCount3() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, ANNUITY_TENOR, true, true, CALENDAR, null, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullBusinessDay3() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, ANNUITY_TENOR, true, true, CALENDAR, DAY_COUNT, null, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullCurrency4() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(null, SETTLEMENT_DATE, MATURITY_DATE, ANNUITY_TENOR, 2, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullSettlementDate4() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, null, MATURITY_DATE, ANNUITY_TENOR, 2, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullMaturityDate3() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, null, ANNUITY_TENOR, 2, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullPaymentTenor3() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, null, 2, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionPaymentsPerYear() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, ANNUITY_TENOR, -2, true, true, null, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullCalendar4() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, ANNUITY_TENOR, 2, true, true, null, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullDayCount4() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, ANNUITY_TENOR, 2, true, true, CALENDAR, null, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullBusinessDay4() {
    AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, ANNUITY_TENOR, 2, true, true, CALENDAR, DAY_COUNT, null, IS_EOM, NOTIONAL, RATE, IS_PAYER);
  }

  @Test
  public void testStaticConstruction() {
    AnnuityCouponFixedDefinition definition1 = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE,
        IS_PAYER);
    AnnuityCouponFixedDefinition definition2 = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_FREQUENCY, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE,
        IS_PAYER);
    assertEquals(definition1, definition2);
    assertEquals(IS_PAYER, definition1.isPayer());
    definition2 = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_FREQUENCY, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, !IS_PAYER);
    assertFalse(definition1.equals(definition2));
    assertEquals(!IS_PAYER, definition2.isPayer());
    definition1 = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_TENOR, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE,
        IS_PAYER);
    definition2 = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_TENOR, 2, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE,
        IS_PAYER);
    assertEquals(definition1, definition2);
    definition2 = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_TENOR, 2, true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE,
        !IS_PAYER);
    assertFalse(definition1.equals(definition2));
  }
}
