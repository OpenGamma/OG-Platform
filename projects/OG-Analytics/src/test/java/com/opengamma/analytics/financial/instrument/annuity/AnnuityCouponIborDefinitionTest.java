/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AnnuityCouponIborDefinitionTest {
  //Libor3m
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final PeriodFrequency INDEX_FREQUENCY = PeriodFrequency.QUARTERLY;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  //Annuity description
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 17);
  private static final boolean IS_PAYER = true;
  private static final double NOTIONAL = 1000000;

  private static final ZonedDateTime MATURITY_DATE = SETTLEMENT_DATE.plus(ANNUITY_TENOR);
  private static final ZonedDateTime[] PAYMENT_DATES_UNADJUSTED = ScheduleCalculator.getUnadjustedDateSchedule(SETTLEMENT_DATE, MATURITY_DATE, INDEX_FREQUENCY);
  private static final ZonedDateTime[] PAYMENT_DATES = ScheduleCalculator.getAdjustedDateSchedule(PAYMENT_DATES_UNADJUSTED, BUSINESS_DAY, CALENDAR, false);

  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, IS_PAYER, CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 3, 15); //For conversion to derivative
  private static final double FIXING_RATE = 0.05;
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS;

  static {
    FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.of(REFERENCE_DATE, FIXING_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConversionDate() {
    IBOR_ANNUITY.toDerivative(null, FIXING_TS);
  }

  @Test
  public void test() {
    final CouponIborDefinition[] coupons = new CouponIborDefinition[PAYMENT_DATES.length];
    final double sign = IS_PAYER ? -1.0 : 1.0;
    //First coupon uses settlement date
    CouponFixedDefinition coupon = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), sign
        * NOTIONAL, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, -SETTLEMENT_DAYS, CALENDAR);
    coupons[0] = CouponIborDefinition.from(coupon, fixingDate, INDEX, CALENDAR);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupon = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), sign * NOTIONAL, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(PAYMENT_DATES[loopcpn - 1], -SETTLEMENT_DAYS, CALENDAR);
      coupons[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, INDEX, CALENDAR);
    }
    final AnnuityCouponIborDefinition iborAnnuity = new AnnuityCouponIborDefinition(coupons, INDEX, CALENDAR);
    //    assertEquals(iborAnnuity.getPayments(), coupons);
    assertEquals(iborAnnuity.isPayer(), IS_PAYER);
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      assertEquals(iborAnnuity.getNthPayment(loopcpn), coupons[loopcpn]);
      assertEquals(iborAnnuity.getPayments()[loopcpn], coupons[loopcpn]);
    }
    final AnnuityCouponIborDefinition iborAnnuity2 = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, IS_PAYER, CALENDAR);
    assertEquals(iborAnnuity, iborAnnuity2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPayments() {
    new AnnuityCouponIborDefinition(null, INDEX, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new AnnuityCouponIborDefinition(IBOR_ANNUITY.getPayments(), null, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOneNullPayment() {
    final CouponIborDefinition[] coupons = new CouponIborDefinition[PAYMENT_DATES.length];
    //First coupon uses settlement date
    CouponFixedDefinition coupon = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), NOTIONAL, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, -SETTLEMENT_DAYS, CALENDAR);
    coupons[0] = null;
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupon = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), NOTIONAL, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(PAYMENT_DATES[loopcpn - 1], -SETTLEMENT_DAYS, CALENDAR);
      coupons[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, INDEX, CALENDAR);
    }
    new AnnuityCouponIborDefinition(coupons, INDEX, CALENDAR);
  }

  @Test
  public void testFrom() {
    final ZonedDateTime settleDate = DateUtils.getUTCDate(2014, 3, 20);
    final Period indexTenor = Period.ofMonths(3);
    final DayCount dayCount = DayCounts.ACT_360;
    final IborIndex index = new IborIndex(CUR, indexTenor, SETTLEMENT_DAYS, dayCount, BUSINESS_DAY, IS_EOM, "Ibor");
    final AnnuityCouponIborDefinition iborAnnuity = AnnuityCouponIborDefinition.from(settleDate, Period.ofYears(1), NOTIONAL, index, IS_PAYER, CALENDAR);
    final ZonedDateTime[] paymentDates = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 6, 20), DateUtils.getUTCDate(2014, 9, 22), DateUtils.getUTCDate(2014, 12, 22),
        DateUtils.getUTCDate(2015, 03, 20) };
    final ZonedDateTime[] fixingDates = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 3, 18), DateUtils.getUTCDate(2014, 6, 18), DateUtils.getUTCDate(2014, 9, 18),
        DateUtils.getUTCDate(2014, 12, 18) };
    final ZonedDateTime[] startPeriodDates = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 3, 20), DateUtils.getUTCDate(2014, 6, 20), DateUtils.getUTCDate(2014, 9, 22),
        DateUtils.getUTCDate(2014, 12, 22) };
    final ZonedDateTime[] endPeriodDates = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 6, 20), DateUtils.getUTCDate(2014, 9, 22), DateUtils.getUTCDate(2014, 12, 22),
        DateUtils.getUTCDate(2015, 03, 23) };
    for (int loopcpn = 0; loopcpn < iborAnnuity.getPayments().length; loopcpn++) {
      assertEquals(paymentDates[loopcpn], iborAnnuity.getNthPayment(loopcpn).getPaymentDate());
      assertEquals(fixingDates[loopcpn], iborAnnuity.getNthPayment(loopcpn).getFixingDate());
      assertEquals(startPeriodDates[loopcpn], iborAnnuity.getNthPayment(loopcpn).getFixingPeriodStartDate());
      assertEquals(endPeriodDates[loopcpn], iborAnnuity.getNthPayment(loopcpn).getFixingPeriodEndDate());
    }
  }

  @Test
  public void testEqualHash() {
    final CouponIborDefinition[] coupons = new CouponIborDefinition[PAYMENT_DATES.length];
    //First coupon uses settlement date
    CouponFixedDefinition coupon = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]), NOTIONAL, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, -SETTLEMENT_DAYS, CALENDAR);
    coupons[0] = CouponIborDefinition.from(coupon, fixingDate, INDEX, CALENDAR);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      coupon = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), NOTIONAL, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(PAYMENT_DATES[loopcpn - 1], -SETTLEMENT_DAYS, CALENDAR);
      coupons[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, INDEX, CALENDAR);
    }
    final AnnuityCouponIborDefinition iborAnnuity = new AnnuityCouponIborDefinition(coupons, INDEX, CALENDAR);
    final AnnuityCouponIborDefinition iborAnnuity2 = new AnnuityCouponIborDefinition(coupons, INDEX, CALENDAR);
    assertEquals(iborAnnuity, iborAnnuity2);
    assertEquals(iborAnnuity.hashCode(), iborAnnuity2.hashCode());
    AnnuityCouponIborDefinition modifiedIborAnnuity = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, IS_PAYER, CALENDAR);
    assertFalse(iborAnnuity.equals(modifiedIborAnnuity));
    final CouponIborDefinition[] couponsModified = new CouponIborDefinition[PAYMENT_DATES.length];
    CouponFixedDefinition couponModified = new CouponFixedDefinition(CUR, PAYMENT_DATES[0], SETTLEMENT_DATE, PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, PAYMENT_DATES[0]),
        NOTIONAL, 0.0);
    fixingDate = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, -SETTLEMENT_DAYS, CALENDAR);
    couponsModified[0] = CouponIborDefinition.from(couponModified, fixingDate, INDEX, CALENDAR);
    for (int loopcpn = 1; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      couponModified = new CouponFixedDefinition(CUR, PAYMENT_DATES[loopcpn], PAYMENT_DATES[loopcpn - 1], PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(PAYMENT_DATES[loopcpn - 1],
          PAYMENT_DATES[loopcpn]), NOTIONAL + 5.0, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(PAYMENT_DATES[loopcpn - 1], -SETTLEMENT_DAYS, CALENDAR);
      couponsModified[loopcpn] = CouponIborDefinition.from(couponModified, fixingDate, INDEX, CALENDAR);
    }
    modifiedIborAnnuity = new AnnuityCouponIborDefinition(couponsModified, INDEX, CALENDAR);
    assertFalse(iborAnnuity.equals(modifiedIborAnnuity));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testToDerivativeAfterFixingDeprecated() {
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve };
    final Payment[] couponIborConverted = new Payment[PAYMENT_DATES.length];
    ZonedDateTime date = REFERENCE_DATE.plusMonths(1);
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      couponIborConverted[loopcpn] = IBOR_ANNUITY.getNthPayment(loopcpn).toDerivative(date, FIXING_TS, curves);
    }
    Annuity<Payment> referenceAnnuity = new Annuity<>(couponIborConverted);
    Annuity<? extends Payment> convertedDefinition = IBOR_ANNUITY.toDerivative(date, FIXING_TS, curves);
    assertEquals(referenceAnnuity, convertedDefinition);
    assertTrue(convertedDefinition.getNthPayment(0) instanceof CouponFixed);
    assertEquals(((CouponFixed) convertedDefinition.getNthPayment(0)).getFixedRate(), FIXING_RATE, 0);
    for (int i = 1; i < PAYMENT_DATES.length; i++) {
      assertTrue(convertedDefinition.getNthPayment(i) instanceof CouponIbor);
    }
    date = REFERENCE_DATE;
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      couponIborConverted[loopcpn] = IBOR_ANNUITY.getNthPayment(loopcpn).toDerivative(date, FIXING_TS, curves);
    }
    referenceAnnuity = new Annuity<>(couponIborConverted);
    convertedDefinition = IBOR_ANNUITY.toDerivative(date, FIXING_TS, curves);
    assertEquals(referenceAnnuity, convertedDefinition);
    assertTrue(convertedDefinition.getNthPayment(0) instanceof CouponFixed);
    assertEquals(((CouponFixed) convertedDefinition.getNthPayment(0)).getFixedRate(), FIXING_RATE, 0);
    for (int i = 1; i < PAYMENT_DATES.length; i++) {
      assertTrue(convertedDefinition.getNthPayment(i) instanceof CouponIbor);
    }
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testToDerivativeBeforeFixingDeprecated() {
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve };
    final Payment[] couponIborConverted = new Payment[PAYMENT_DATES.length];
    final ZonedDateTime date = REFERENCE_DATE.minusDays(1);
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      couponIborConverted[loopcpn] = IBOR_ANNUITY.getNthPayment(loopcpn).toDerivative(date, FIXING_TS, curves);
    }
    final Annuity<Payment> referenceAnnuity = new Annuity<>(couponIborConverted);
    final Annuity<? extends Payment> convertedDefinition = IBOR_ANNUITY.toDerivative(date, FIXING_TS, curves);
    assertEquals(referenceAnnuity, convertedDefinition);
    for (int i = 0; i < PAYMENT_DATES.length; i++) {
      assertTrue(convertedDefinition.getNthPayment(i) instanceof CouponIbor);
    }
  }

  @Test
  public void testToDerivativeAfterFixing() {
    final Payment[] couponIborConverted = new Payment[PAYMENT_DATES.length];
    ZonedDateTime date = REFERENCE_DATE.plusMonths(1);
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      couponIborConverted[loopcpn] = IBOR_ANNUITY.getNthPayment(loopcpn).toDerivative(date, FIXING_TS);
    }
    Annuity<Payment> referenceAnnuity = new Annuity<>(couponIborConverted);
    Annuity<? extends Payment> convertedDefinition = IBOR_ANNUITY.toDerivative(date, FIXING_TS);
    assertEquals(referenceAnnuity, convertedDefinition);
    assertTrue(convertedDefinition.getNthPayment(0) instanceof CouponFixed);
    assertEquals(((CouponFixed) convertedDefinition.getNthPayment(0)).getFixedRate(), FIXING_RATE, 0);
    for (int i = 1; i < PAYMENT_DATES.length; i++) {
      assertTrue(convertedDefinition.getNthPayment(i) instanceof CouponIbor);
    }
    date = REFERENCE_DATE;
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      couponIborConverted[loopcpn] = IBOR_ANNUITY.getNthPayment(loopcpn).toDerivative(date, FIXING_TS);
    }
    referenceAnnuity = new Annuity<>(couponIborConverted);
    convertedDefinition = IBOR_ANNUITY.toDerivative(date, FIXING_TS);
    assertEquals(referenceAnnuity, convertedDefinition);
    assertTrue(convertedDefinition.getNthPayment(0) instanceof CouponFixed);
    assertEquals(((CouponFixed) convertedDefinition.getNthPayment(0)).getFixedRate(), FIXING_RATE, 0);
    for (int i = 1; i < PAYMENT_DATES.length; i++) {
      assertTrue(convertedDefinition.getNthPayment(i) instanceof CouponIbor);
    }
  }

  @Test
  public void testToDerivativeBeforeFixing() {
    final Payment[] couponIborConverted = new Payment[PAYMENT_DATES.length];
    final ZonedDateTime date = REFERENCE_DATE.minusDays(1);
    for (int loopcpn = 0; loopcpn < PAYMENT_DATES.length; loopcpn++) {
      couponIborConverted[loopcpn] = IBOR_ANNUITY.getNthPayment(loopcpn).toDerivative(date, FIXING_TS);
    }
    final Annuity<Payment> referenceAnnuity = new Annuity<>(couponIborConverted);
    final Annuity<? extends Payment> convertedDefinition = IBOR_ANNUITY.toDerivative(date, FIXING_TS);
    assertEquals(referenceAnnuity, convertedDefinition);
    for (int i = 0; i < PAYMENT_DATES.length; i++) {
      assertTrue(convertedDefinition.getNthPayment(i) instanceof CouponIbor);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullSettlementDate1() {
    AnnuityCouponIborDefinition.from(null, ANNUITY_TENOR, NOTIONAL, INDEX, IS_PAYER, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullSettlementDate2() {
    AnnuityCouponIborDefinition.from(null, MATURITY_DATE, NOTIONAL, INDEX, IS_PAYER, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullSettlementDate3() {
    AnnuityCouponIborDefinition.fromAccrualUnadjusted(null, MATURITY_DATE, NOTIONAL, INDEX, IS_PAYER, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullPeriod() {
    AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, (Period) null, NOTIONAL, INDEX, IS_PAYER, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullMaturityDate1() {
    AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, (ZonedDateTime) null, NOTIONAL, INDEX, IS_PAYER, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullMaturityDate2() {
    AnnuityCouponIborDefinition.fromAccrualUnadjusted(SETTLEMENT_DATE, null, NOTIONAL, INDEX, IS_PAYER, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNegativeNotional1() {
    AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, -NOTIONAL, INDEX, IS_PAYER, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNegativeNotional2() {
    AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, -NOTIONAL, INDEX, IS_PAYER, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNegativeNotional3() {
    AnnuityCouponIborDefinition.fromAccrualUnadjusted(SETTLEMENT_DATE, MATURITY_DATE, -NOTIONAL, INDEX, IS_PAYER, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullIndex1() {
    AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, null, IS_PAYER, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullIndex2() {
    AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, null, IS_PAYER, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullIndex3() {
    AnnuityCouponIborDefinition.fromAccrualUnadjusted(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, null, IS_PAYER, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStaticConstructionNullAnnuity() {
    AnnuityCouponIborDefinition.from(null);
  }

  @Test
  public void testStaticConstruction() {
    AnnuityCouponIborDefinition definition1 = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, IS_PAYER, CALENDAR);
    AnnuityCouponIborDefinition definition2 = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, INDEX, IS_PAYER, CALENDAR);
    assertEquals(definition1, definition2);
    assertEquals(IS_PAYER, definition1.isPayer());
    definition2 = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, !IS_PAYER, CALENDAR);
    assertFalse(definition1.equals(definition2));
    definition2 = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, INDEX, !IS_PAYER, CALENDAR);
    assertFalse(definition1.equals(definition2));
    definition1 = AnnuityCouponIborDefinition.fromAccrualUnadjusted(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, INDEX, IS_PAYER, CALENDAR);
    definition2 = AnnuityCouponIborDefinition.fromAccrualUnadjusted(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, INDEX, !IS_PAYER, CALENDAR);
    assertFalse(definition1.equals(definition2));
  }

  @Test
  public void testNoSpread() {
    final AnnuityCouponIborDefinition definition = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, INDEX, IS_PAYER, CALENDAR);
    final CouponIborDefinition[] noSpreadCoupons = definition.getPayments();
    final int n = noSpreadCoupons.length;
    final double spread = 0.01;
    final CouponIborSpreadDefinition[] spreadCoupons = new CouponIborSpreadDefinition[n];
    for (int i = 0; i < n; i++) {
      final CouponIborDefinition coupon = noSpreadCoupons[i];
      spreadCoupons[i] = new CouponIborSpreadDefinition(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(),
          coupon.getNotional(), coupon.getFixingDate(), coupon.getIndex(), spread, coupon.getCalendar());
    }
    assertEquals(definition, AnnuityCouponIborDefinition.from(new AnnuityCouponIborSpreadDefinition(spreadCoupons, CALENDAR)));
  }

}
