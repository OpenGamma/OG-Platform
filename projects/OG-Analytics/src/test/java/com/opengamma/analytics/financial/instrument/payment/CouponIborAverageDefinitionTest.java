/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CouponIborAverageDefinitionTest {

  private static final Period TENOR_1 = Period.ofMonths(3);
  private static final Period TENOR_2 = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX_1 = new IborIndex(CUR, TENOR_1, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index");
  private static final IborIndex INDEX_2 = new IborIndex(CUR, TENOR_2, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index");

  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  // The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE_1 = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, TENOR_1, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final ZonedDateTime FIXING_END_DATE_2 = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, TENOR_2, BUSINESS_DAY, CALENDAR, IS_EOM);

  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365;
  private static final double ACCRUAL_FACTOR = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double ACCRUAL_FACTOR_FIXING_1 = DAY_COUNT_INDEX.getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE_1);
  private static final double ACCRUAL_FACTOR_FIXING_2 = DAY_COUNT_INDEX.getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE_2);
  private static final double NOTIONAL = 1000000; //1m
  private static final double WEIGHT_1 = 23;
  private static final double WEIGHT_2 = -.03;

  private static final CouponIborAverageIndexDefinition IBOR_AVERAGE_COUPON_DEFINITION_1 = new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR,
      NOTIONAL, FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  private static final CouponIborAverageIndexDefinition IBOR_AVERAGE_COUPON_DEFINITION_2 = CouponIborAverageIndexDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR,
      NOTIONAL, FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  private static final double FIXING_RATE = .005;
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {FIXING_DATE }, new double[] {FIXING_RATE });
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27); //For conversion to derivative

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentCurrencies() {
    new CouponIborAverageIndexDefinition(Currency.AUD, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
        FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentCurrenciesBetweenIndices() {
    new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
        FIXING_DATE, new IborIndex(Currency.AUD, TENOR_1, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index"), INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex1() {
    new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, null,
        INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex2() {
    new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_1,
        null, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualStartDate() {
    CouponIborAverageIndexDefinition.from(PAYMENT_DATE, null, ACCRUAL_END_DATE, ACCRUAL_FACTOR,
        NOTIONAL, FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualEndDate1() {
    CouponIborAverageIndexDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, null, ACCRUAL_FACTOR,
        NOTIONAL, FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    CouponIborAverageIndexDefinition.from(null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testfromNullIndex1again() {
    CouponIborAverageIndexDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR,
        NOTIONAL, FIXING_DATE, null, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex2again() {
    CouponIborAverageIndexDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_1, null, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFixingAfterPayment() {
    CouponIborAverageIndexDefinition.from(FIXING_DATE.minusDays(1), ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  }

  @Test
  public void test() {
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.getPaymentDate(), PAYMENT_DATE);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.getPaymentYearFraction(), ACCRUAL_FACTOR, 1E-10);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.getNotional(), NOTIONAL, 1E-2);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingDate(), FIXING_DATE);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodStartDate1(), FIXING_START_DATE);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodStartDate2(), FIXING_START_DATE);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodEndDate1(), FIXING_END_DATE_1);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodEndDate2(), FIXING_END_DATE_2);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodAccrualFactor1(), ACCRUAL_FACTOR_FIXING_1, 1E-10);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodAccrualFactor2(), ACCRUAL_FACTOR_FIXING_2, 1E-10);

    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_2.getPaymentDate(), PAYMENT_DATE);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_2.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_2.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_2.getPaymentYearFraction(), ACCRUAL_FACTOR, 1E-10);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_2.getNotional(), NOTIONAL, 1E-2);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_2.getFixingDate(), FIXING_DATE);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_2.getFixingPeriodStartDate1(), FIXING_START_DATE);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_2.getFixingPeriodStartDate2(), FIXING_START_DATE);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_2.getFixingPeriodEndDate1(), FIXING_END_DATE_1);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_2.getFixingPeriodEndDate2(), FIXING_END_DATE_2);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_2.getFixingPeriodAccrualFactor1(), ACCRUAL_FACTOR_FIXING_1, 1E-10);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_2.getFixingPeriodAccrualFactor2(), ACCRUAL_FACTOR_FIXING_2, 1E-10);
  }

  @Test
  public void testEqualHash() {
    CouponIborAverageIndexDefinition other = new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1,
        WEIGHT_2, CALENDAR, CALENDAR);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1, other);
    assertEquals(IBOR_AVERAGE_COUPON_DEFINITION_1.hashCode(), other.hashCode());
    other = new CouponIborAverageIndexDefinition(Currency.AUD, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, new IborIndex(Currency.AUD, TENOR_1,
        SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index1"), new IborIndex(Currency.AUD, TENOR_2, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index2"), WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
    assertFalse(IBOR_AVERAGE_COUPON_DEFINITION_1.equals(other));
    other = new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE.plusDays(1), ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
    assertFalse(IBOR_AVERAGE_COUPON_DEFINITION_1.equals(other));
    other = new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE.plusDays(1), ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
    assertFalse(IBOR_AVERAGE_COUPON_DEFINITION_1.equals(other));
    other = new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE.plusDays(1), ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
    assertFalse(IBOR_AVERAGE_COUPON_DEFINITION_1.equals(other));
    other = new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR + 0.01, NOTIONAL, FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
    assertFalse(IBOR_AVERAGE_COUPON_DEFINITION_1.equals(other));
    other = new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL + 100, FIXING_DATE, INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
    assertFalse(IBOR_AVERAGE_COUPON_DEFINITION_1.equals(other));
    other = new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE.plusDays(1), INDEX_1, INDEX_2, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
    assertFalse(IBOR_AVERAGE_COUPON_DEFINITION_1.equals(other));
    other = new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, new IborIndex(CUR, TENOR_1, SETTLEMENT_DAYS + 1,
        DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index1"), new IborIndex(CUR, TENOR_2, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index2"), WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
    assertFalse(IBOR_AVERAGE_COUPON_DEFINITION_1.equals(other));
    other = new CouponIborAverageIndexDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, new IborIndex(CUR, TENOR_1, SETTLEMENT_DAYS,
        DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index1"), new IborIndex(CUR, TENOR_2, SETTLEMENT_DAYS + 1, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index2"), WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
    assertFalse(IBOR_AVERAGE_COUPON_DEFINITION_1.equals(other));

  }

  @Test
  public void testToDerivativeBeforeFixing() {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final double paymentTime = actAct.getDayCountFraction(REFERENCE_DATE, PAYMENT_DATE);
    final double fixingTime = actAct.getDayCountFraction(REFERENCE_DATE, FIXING_DATE);
    final double fixingPeriodStartTime1 = actAct.getDayCountFraction(REFERENCE_DATE, IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodStartDate1());
    final double fixingPeriodEndTime1 = actAct.getDayCountFraction(REFERENCE_DATE, IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodEndDate1());
    final double fixingPeriodStartTime2 = actAct.getDayCountFraction(REFERENCE_DATE, IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodStartDate2());
    final double fixingPeriodEndTime2 = actAct.getDayCountFraction(REFERENCE_DATE, IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodEndDate2());
    final CouponIborAverage couponIborAverage = new CouponIborAverage(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, fixingTime, INDEX_1, fixingPeriodStartTime1, fixingPeriodEndTime1,
        ACCRUAL_FACTOR_FIXING_1,
        INDEX_2, fixingPeriodStartTime2, fixingPeriodEndTime2, ACCRUAL_FACTOR_FIXING_2, WEIGHT_1, WEIGHT_2);
    CouponIborAverage convertedDefinition = (CouponIborAverage) IBOR_AVERAGE_COUPON_DEFINITION_1.toDerivative(REFERENCE_DATE);
    assertEquals(couponIborAverage, convertedDefinition);
    convertedDefinition = (CouponIborAverage) IBOR_AVERAGE_COUPON_DEFINITION_1.toDerivative(REFERENCE_DATE, FIXING_TS);
    assertEquals(couponIborAverage, convertedDefinition);
  }

  @Test
  /**
   * Tests the toDerivative method where the fixing date before the current date.
   */
  public void testToDerivativeAfterFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 1, 10, 12, 0);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, IBOR_AVERAGE_COUPON_DEFINITION_1.getPaymentDate());
    final CouponFixed coupon = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, FIXING_RATE);
    final Payment couponConverted = IBOR_AVERAGE_COUPON_DEFINITION_1.toDerivative(referenceDate, FIXING_TS);
    assertEquals("CouponIborAverageDefinition: toDerivative", coupon, couponConverted);
  }

  @Test
  /**
   * Tests the toDerivative method where the fixing date is equal to the current date.
   */
  public void testToDerivativeOnFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 1, 3, 11, 11);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, IBOR_AVERAGE_COUPON_DEFINITION_1.getPaymentDate());
    final double fixingTime = TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE);
    final double fixingPeriodStartTime1 = TimeCalculator.getTimeBetween(referenceDate, IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodStartDate1());
    final double fixingPeriodEndTime1 = TimeCalculator.getTimeBetween(referenceDate, IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodEndDate1());
    final double fixingPeriodStartTime2 = TimeCalculator.getTimeBetween(referenceDate, IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodStartDate2());
    final double fixingPeriodEndTime2 = TimeCalculator.getTimeBetween(referenceDate, IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodEndDate2());
    // The fixing is known
    final CouponFixed coupon = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, FIXING_RATE);
    final Payment couponConverted = IBOR_AVERAGE_COUPON_DEFINITION_1.toDerivative(referenceDate, FIXING_TS);
    assertEquals(coupon, couponConverted);
    // The fixing is not known
    final DoubleTimeSeries<ZonedDateTime> fixingTS2 = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {ScheduleCalculator.getAdjustedDate(FIXING_DATE, -1, CALENDAR) },
        new double[] {FIXING_RATE });
    final CouponIborAverage coupon2 = new CouponIborAverage(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, fixingTime, INDEX_1, fixingPeriodStartTime1, fixingPeriodEndTime1,
        IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodAccrualFactor1(), INDEX_2, fixingPeriodStartTime2, fixingPeriodEndTime2,
        IBOR_AVERAGE_COUPON_DEFINITION_1.getFixingPeriodAccrualFactor2(), WEIGHT_1, WEIGHT_2);
    final Payment couponConverted2 = IBOR_AVERAGE_COUPON_DEFINITION_1.toDerivative(referenceDate, fixingTS2);
    assertEquals("CouponIborAverageDefinition: toDerivative", coupon2, couponConverted2);
    final Payment couponConverted3 = IBOR_AVERAGE_COUPON_DEFINITION_1.toDerivative(referenceDate);
    assertEquals("CouponIborAverageDefinition: toDerivative", coupon2, couponConverted3);
  }
}
