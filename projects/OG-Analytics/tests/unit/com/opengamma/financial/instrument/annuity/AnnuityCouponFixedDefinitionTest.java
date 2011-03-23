/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Tenor;

public class AnnuityCouponFixedDefinitionTest {
  //Semi-annual 2Y
  private static final Currency CUR = Currency.USD;
  private static final PeriodFrequency PAYMENT_FREQUENCY = PeriodFrequency.SEMI_ANNUAL;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Tenor ANNUITY_TENOR = new Tenor(Period.ofYears(2));
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 3, 17);
  private static final double NOTIONAL = 1000000;
  private static final double RATE = 0.0325;
  private static final boolean IS_PAYER = true;

  private static final ZonedDateTime MATURITY_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, IS_EOM, ANNUITY_TENOR);
  private static final ZonedDateTime[] PAYMENT_DATES_UNADJUSTED = ScheduleCalculator.getUnadjustedDateSchedule(SETTLEMENT_DATE, MATURITY_DATE, PAYMENT_FREQUENCY);
  private static final ZonedDateTime[] PAYMENT_DATES = ScheduleCalculator.getAdjustedDateSchedule(PAYMENT_DATES_UNADJUSTED, BUSINESS_DAY, CALENDAR);

  private static final LocalDate REFERENCE_DATE = LocalDate.of(2011, 3, 15); //For conversion to derivative

  @Test
  public void test() {
    CouponFixedDefinition[] coupons = new CouponFixedDefinition[PAYMENT_DATES.length];
    //First coupon uses settlement date
    double sign = IS_PAYER ? -1.0 : 1.0;
    coupons[0] = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), sign * NOTIONAL, RATE);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), sign * NOTIONAL, RATE);
    }
    AnnuityCouponFixedDefinition fixedAnnuity = new AnnuityCouponFixedDefinition(coupons);

    assertEquals(fixedAnnuity.isPayer(), IS_PAYER);
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      assertEquals(fixedAnnuity.getNthPayment(loopcpn), coupons[loopcpn]);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPayments() {
    new AnnuityCouponFixedDefinition(null);
  }

  @Test
  public void testEqualHash() {
    double sign = IS_PAYER ? -1.0 : 1.0;
    CouponFixedDefinition[] coupons = new CouponFixedDefinition[PAYMENT_DATES.length];
    coupons[0] = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), sign * NOTIONAL, RATE);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), sign * NOTIONAL, RATE);
    }
    AnnuityCouponFixedDefinition fixedAnnuity = new AnnuityCouponFixedDefinition(coupons);
    AnnuityCouponFixedDefinition fixedAnnuity2 = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE,
        IS_PAYER);
    assertEquals(fixedAnnuity, fixedAnnuity2);
    assertEquals(fixedAnnuity.hashCode(), fixedAnnuity2.hashCode());
    AnnuityCouponFixedDefinition modifiedFixedAnnuity = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL,
        RATE, !IS_PAYER);
    assertFalse(fixedAnnuity.equals(modifiedFixedAnnuity));
  }

  @Test
  public void testPaymentDates() {
    AnnuityCouponFixedDefinition fixedAnnuity;
    ZonedDateTime[] expectedPaymentDate;
    // End date is modified
    fixedAnnuity = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
    expectedPaymentDate = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 9, 19), DateUtil.getUTCDate(2012, 3, 19), DateUtil.getUTCDate(2012, 9, 17), DateUtil.getUTCDate(2013, 3, 18)};
    for (int loopcpn = 0; loopcpn < expectedPaymentDate.length; loopcpn++) {
      assertEquals(expectedPaymentDate[loopcpn], fixedAnnuity.getNthPayment(loopcpn).getPaymentDate());
    }
    // Check modified in modified following.
    ZonedDateTime settlementDateModified = DateUtil.getUTCDate(2011, 3, 31);
    fixedAnnuity = AnnuityCouponFixedDefinition.from(CUR, settlementDateModified, ANNUITY_TENOR, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
    expectedPaymentDate = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 9, 30), DateUtil.getUTCDate(2012, 3, 30), DateUtil.getUTCDate(2012, 9, 28), DateUtil.getUTCDate(2013, 3, 29)};
    for (int loopcpn = 0; loopcpn < expectedPaymentDate.length; loopcpn++) {
      assertEquals(expectedPaymentDate[loopcpn], fixedAnnuity.getNthPayment(loopcpn).getPaymentDate());
    }
    // End-of-month
    ZonedDateTime settlementDateEOM = DateUtil.getUTCDate(2011, 2, 28);
    fixedAnnuity = AnnuityCouponFixedDefinition.from(CUR, settlementDateEOM, ANNUITY_TENOR, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, IS_PAYER);
    expectedPaymentDate = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 8, 31), DateUtil.getUTCDate(2012, 2, 29), DateUtil.getUTCDate(2012, 8, 31), DateUtil.getUTCDate(2013, 2, 28)};
    for (int loopcpn = 0; loopcpn < expectedPaymentDate.length; loopcpn++) {
      assertEquals(expectedPaymentDate[loopcpn], fixedAnnuity.getNthPayment(loopcpn).getPaymentDate());
    }

  }

  @Test
  public void testToDerivative() {
    double sign = IS_PAYER ? -1.0 : 1.0;
    CouponFixedDefinition[] coupons = new CouponFixedDefinition[PAYMENT_DATES.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), sign * NOTIONAL, RATE);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), sign * NOTIONAL, RATE);
    }
    AnnuityCouponFixedDefinition fixedAnnuity = new AnnuityCouponFixedDefinition(coupons);

    //    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    //    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
    String fundingCurve = "Funding";
    CouponFixed[] couponFixedConverted = new CouponFixed[PAYMENT_DATES.length];
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      couponFixedConverted[loopcpn] = fixedAnnuity.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, fundingCurve);
    }
    AnnuityCouponFixed referenceAnnuity = new AnnuityCouponFixed(couponFixedConverted);
    AnnuityCouponFixed convertedDefinition = fixedAnnuity.toDerivative(REFERENCE_DATE, fundingCurve);
    assertEquals(referenceAnnuity, convertedDefinition);
  }

}
