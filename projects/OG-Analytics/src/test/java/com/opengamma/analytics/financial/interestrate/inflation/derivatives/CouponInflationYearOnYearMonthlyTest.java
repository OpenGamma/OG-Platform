/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.derivatives;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CouponInflationYearOnYearMonthlyTest {
  private static final String NAME = "Euro HICP x";
  private static final Currency CUR = Currency.EUR;
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME, CUR);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final ZonedDateTime REFERENCE_START_DATE = PAYMENT_DATE.minusMonths(MONTH_LAG - 12).withDayOfMonth(1);
  private static final ZonedDateTime REFERENCE_END_DATE = PAYMENT_DATE.minusMonths(MONTH_LAG).withDayOfMonth(1);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 29);
  private static final double PAYMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, PAYMENT_DATE);
  private static final double NATURAL_PAYMENT_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, PAYMENT_DATE.minusMonths(12));
  private static final double NATURAL_PAYMENT_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, PAYMENT_DATE);
  private static final double REFERENCE_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, REFERENCE_START_DATE);
  private static final double REFERENCE_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, REFERENCE_END_DATE);
  private static final CouponInflationYearOnYearMonthly YoY_COUPON = new CouponInflationYearOnYearMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, REFERENCE_START_TIME,
      NATURAL_PAYMENT_START_TIME,
      REFERENCE_END_TIME, NATURAL_PAYMENT_END_TIME, false);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponInflationYearOnYearMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, null, REFERENCE_START_TIME, NATURAL_PAYMENT_START_TIME, REFERENCE_END_TIME, NATURAL_PAYMENT_END_TIME, false);
  }

  @Test
  /**
   * Tests the class getter.
   */
  public void getter() {
    assertEquals("Inflation Zero-coupon: getter", PRICE_INDEX, YoY_COUPON.getPriceIndex());
    assertEquals("Inflation Zero-coupon: getter", REFERENCE_START_TIME, YoY_COUPON.getReferenceStartTime());
    assertEquals("Inflation Zero-coupon: getter", REFERENCE_END_TIME, YoY_COUPON.getReferenceEndTime());
    assertEquals("Inflation Year on Year coupon: getter", NATURAL_PAYMENT_START_TIME, YoY_COUPON.getNaturalPaymentStartTime());
    assertEquals("Inflation Year on Year coupon: getter", NATURAL_PAYMENT_END_TIME, YoY_COUPON.getNaturalPaymentEndTime());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals(YoY_COUPON, YoY_COUPON);

    CouponInflationYearOnYearMonthly couponDuplicate = new CouponInflationYearOnYearMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, REFERENCE_START_TIME, NATURAL_PAYMENT_START_TIME,
        REFERENCE_END_TIME, NATURAL_PAYMENT_END_TIME, false);
    assertEquals(YoY_COUPON, couponDuplicate);
    assertEquals(YoY_COUPON.hashCode(), couponDuplicate.hashCode());
    CouponInflationYearOnYearMonthly modified;
    modified = new CouponInflationYearOnYearMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, REFERENCE_START_TIME + 0.1, NATURAL_PAYMENT_START_TIME,
        REFERENCE_END_TIME, NATURAL_PAYMENT_END_TIME, false);
    assertFalse(YoY_COUPON.equals(modified));
    modified = new CouponInflationYearOnYearMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, REFERENCE_START_TIME, NATURAL_PAYMENT_START_TIME,
        REFERENCE_END_TIME + 0.1, NATURAL_PAYMENT_END_TIME, false);
    assertFalse(YoY_COUPON.equals(modified));
    final double modifiedNaturalPaymentStartTime = NATURAL_PAYMENT_START_TIME + .01;
    modified = new CouponInflationYearOnYearMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, REFERENCE_START_TIME,
        modifiedNaturalPaymentStartTime, REFERENCE_END_TIME, NATURAL_PAYMENT_END_TIME, false);
    assertFalse(YoY_COUPON.equals(modified));
    final double modifiedNaturalPaymentEndTime = NATURAL_PAYMENT_END_TIME + .01;
    modified = new CouponInflationYearOnYearMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, REFERENCE_START_TIME,
        NATURAL_PAYMENT_START_TIME, REFERENCE_END_TIME, modifiedNaturalPaymentEndTime, false);
    assertFalse(YoY_COUPON.equals(modified));
  }
}
