/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

public class AnnuityCouponIborDefinitionTest {
  //Libor3m
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final PeriodFrequency INDEX_FREQUENCY = PeriodFrequency.QUARTERLY;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  //Annuity description
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 3, 17);
  private static final boolean IS_PAYER = true;
  private static final double NOTIONAL = 1000000;

  private static final ZonedDateTime MATURITY_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, IS_EOM, ANNUITY_TENOR);
  private static final ZonedDateTime[] PAYMENT_DATES_UNADJUSTED = ScheduleCalculator.getUnadjustedDateSchedule(SETTLEMENT_DATE, MATURITY_DATE, INDEX_FREQUENCY);
  private static final ZonedDateTime[] PAYMENT_DATES = ScheduleCalculator.getAdjustedDateSchedule(PAYMENT_DATES_UNADJUSTED, BUSINESS_DAY, CALENDAR);

  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, IS_PAYER);

  private static final LocalDate REFERENCE_DATE = LocalDate.of(2011, 3, 15); //For conversion to derivative

  @Test
  public void test() {
    CouponIborDefinition[] coupons = new CouponIborDefinition[PAYMENT_DATES.length];
    double sign = IS_PAYER ? -1.0 : 1.0;
    //First coupon uses settlement date
    CouponFixedDefinition coupon = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), sign
        * NOTIONAL, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
    coupons[0] = CouponIborDefinition.from(coupon, fixingDate, INDEX);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupon = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), sign * NOTIONAL, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(PAYMENT_DATES[loopcpn - 1], BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
      coupons[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, INDEX);
    }
    AnnuityCouponIborDefinition iborAnnuity = new AnnuityCouponIborDefinition(coupons);
    //    assertEquals(iborAnnuity.getPayments(), coupons);
    assertEquals(iborAnnuity.isPayer(), IS_PAYER);
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      assertEquals(iborAnnuity.getNthPayment(loopcpn), coupons[loopcpn]);
      assertEquals(iborAnnuity.getPayments()[loopcpn], coupons[loopcpn]);
    }
    AnnuityCouponIborDefinition iborAnnuity2 = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, IS_PAYER);
    assertEquals(iborAnnuity, iborAnnuity2);

  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPayments() {
    new AnnuityCouponIborDefinition(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOneNullPayment() {
    CouponIborDefinition[] coupons = new CouponIborDefinition[PAYMENT_DATES.length];
    //First coupon uses settlement date
    CouponFixedDefinition coupon = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), NOTIONAL, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
    coupons[0] = null;
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupon = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), NOTIONAL, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(PAYMENT_DATES[loopcpn - 1], BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
      coupons[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, INDEX);
    }
    new AnnuityCouponIborDefinition(coupons);
  }

  @Test
  public void testFrom() {
    ZonedDateTime settleDate = DateUtil.getUTCDate(2014, 3, 20);
    Period INDEX_TENOR = Period.ofMonths(3);
    DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    IborIndex INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
    AnnuityCouponIborDefinition iborAnnuity = AnnuityCouponIborDefinition.from(settleDate, Period.ofYears(1), NOTIONAL, INDEX, IS_PAYER);
    ZonedDateTime[] paymentDates = new ZonedDateTime[] {DateUtil.getUTCDate(2014, 6, 20), DateUtil.getUTCDate(2014, 9, 22), DateUtil.getUTCDate(2014, 12, 22), DateUtil.getUTCDate(2015, 03, 20)};
    ZonedDateTime[] fixingDates = new ZonedDateTime[] {DateUtil.getUTCDate(2014, 3, 18), DateUtil.getUTCDate(2014, 6, 18), DateUtil.getUTCDate(2014, 9, 18), DateUtil.getUTCDate(2014, 12, 18)};
    ZonedDateTime[] startPeriodDates = new ZonedDateTime[] {DateUtil.getUTCDate(2014, 3, 20), DateUtil.getUTCDate(2014, 6, 20), DateUtil.getUTCDate(2014, 9, 22), DateUtil.getUTCDate(2014, 12, 22)};
    ZonedDateTime[] endPeriodDates = new ZonedDateTime[] {DateUtil.getUTCDate(2014, 6, 20), DateUtil.getUTCDate(2014, 9, 22), DateUtil.getUTCDate(2014, 12, 22), DateUtil.getUTCDate(2015, 03, 23)};
    for (int loopcpn = 0; loopcpn < iborAnnuity.getPayments().length; loopcpn++) {
      assertEquals(paymentDates[loopcpn], iborAnnuity.getNthPayment(loopcpn).getPaymentDate());
      assertEquals(fixingDates[loopcpn], iborAnnuity.getNthPayment(loopcpn).getFixingDate());
      assertEquals(startPeriodDates[loopcpn], iborAnnuity.getNthPayment(loopcpn).getFixindPeriodStartDate());
      assertEquals(endPeriodDates[loopcpn], iborAnnuity.getNthPayment(loopcpn).getFixindPeriodEndDate());
    }
  }

  @Test
  public void testEqualHash() {
    CouponIborDefinition[] coupons = new CouponIborDefinition[PAYMENT_DATES.length];
    //First coupon uses settlement date
    CouponFixedDefinition coupon = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), NOTIONAL, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
    coupons[0] = CouponIborDefinition.from(coupon, fixingDate, INDEX);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupon = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), NOTIONAL, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(PAYMENT_DATES[loopcpn - 1], BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
      coupons[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, INDEX);
    }
    AnnuityCouponIborDefinition iborAnnuity = new AnnuityCouponIborDefinition(coupons);
    AnnuityCouponIborDefinition iborAnnuity2 = new AnnuityCouponIborDefinition(coupons);
    assertEquals(iborAnnuity, iborAnnuity2);
    assertEquals(iborAnnuity.hashCode(), iborAnnuity2.hashCode());
    AnnuityCouponIborDefinition modifiedIborAnnuity = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, IS_PAYER);
    assertFalse(iborAnnuity.equals(modifiedIborAnnuity));
    CouponIborDefinition[] couponsModified = new CouponIborDefinition[PAYMENT_DATES.length];
    CouponFixedDefinition couponModified = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]),
        NOTIONAL, 0.0);
    fixingDate = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
    couponsModified[0] = CouponIborDefinition.from(couponModified, fixingDate, INDEX);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      couponModified = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), NOTIONAL + 5.0, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(PAYMENT_DATES[loopcpn - 1], BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
      couponsModified[loopcpn] = CouponIborDefinition.from(couponModified, fixingDate, INDEX);
    }
    modifiedIborAnnuity = new AnnuityCouponIborDefinition(couponsModified);
    assertFalse(iborAnnuity.equals(modifiedIborAnnuity));
  }

  @Test
  public void testToDerivative() {
    String fundingCurve = "Funding";
    String forwardCurve = "Forward";
    String[] curves = {fundingCurve, forwardCurve};
    Payment[] couponIborConverted = new Payment[PAYMENT_DATES.length];
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      couponIborConverted[loopcpn] = IBOR_ANNUITY.getNthPayment(loopcpn).toDerivative(REFERENCE_DATE, curves);
    }
    GenericAnnuity<Payment> referenceAnnuity = new GenericAnnuity<Payment>(couponIborConverted);
    GenericAnnuity<? extends Payment> convertedDefinition = IBOR_ANNUITY.toDerivative(REFERENCE_DATE, curves);
    assertEquals(referenceAnnuity, convertedDefinition);
  }
}
