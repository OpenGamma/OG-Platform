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
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
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
public class CouponInflationYearOnYearInterpolationTest {

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
  private static final ZonedDateTime[] REFERENCE_START_DATE = new ZonedDateTime[2];
  static {
    REFERENCE_START_DATE[0] = PAYMENT_DATE.minusMonths(MONTH_LAG - 12).withDayOfMonth(1);
    REFERENCE_START_DATE[1] = PAYMENT_DATE.minusMonths(MONTH_LAG - 13).withDayOfMonth(1);
  }
  private static final ZonedDateTime[] REFERENCE_END_DATE = new ZonedDateTime[2];
  static {
    REFERENCE_END_DATE[0] = PAYMENT_DATE.minusMonths(MONTH_LAG).withDayOfMonth(1);
    REFERENCE_END_DATE[1] = PAYMENT_DATE.minusMonths(MONTH_LAG - 1).withDayOfMonth(1);
  }

  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 29);
  private static final double PAYMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, PAYMENT_DATE);
  private static final double NATURAL_PAYMENT_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, PAYMENT_DATE.minusMonths(12));
  private static final double NATURAL_PAYMENT_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, PAYMENT_DATE);
  private static final double[] REFERENCE_START_TIME = new double[2];
  static {
    REFERENCE_START_TIME[0] = ACT_ACT.getDayCountFraction(REFERENCE_DATE, REFERENCE_END_DATE[0]);
    REFERENCE_START_TIME[1] = ACT_ACT.getDayCountFraction(REFERENCE_DATE, REFERENCE_END_DATE[1]);
  }
  private static final double[] REFERENCE_END_TIME = new double[2];
  static {
    REFERENCE_END_TIME[0] = ACT_ACT.getDayCountFraction(REFERENCE_DATE, REFERENCE_END_DATE[0]);
    REFERENCE_END_TIME[1] = ACT_ACT.getDayCountFraction(REFERENCE_DATE, REFERENCE_END_DATE[1]);
  }

  private static final double WEIGHT_START = 1.0 - (PAYMENT_DATE.getDayOfMonth() - 1) / PAYMENT_DATE.toLocalDate().lengthOfMonth();
  private static final double WEIGHT_END = 1.0 - (PAYMENT_DATE.getDayOfMonth() - 1) / PAYMENT_DATE.toLocalDate().lengthOfMonth();
  private static final CouponInflationYearOnYearInterpolation YoY_COUPON = new CouponInflationYearOnYearInterpolation(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, REFERENCE_START_TIME,
      NATURAL_PAYMENT_START_TIME, REFERENCE_END_TIME, NATURAL_PAYMENT_END_TIME, false, WEIGHT_START, WEIGHT_END);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponInflationYearOnYearInterpolation(CUR, PAYMENT_TIME, 1.0, NOTIONAL, null, REFERENCE_START_TIME,
        NATURAL_PAYMENT_START_TIME, REFERENCE_END_TIME, NATURAL_PAYMENT_END_TIME, false, WEIGHT_START, WEIGHT_END);
  }

  @Test
  /**
   * Tests the class getter.
   */
  public void getter() {
    assertEquals("Inflation Year on Year coupon: getter", PRICE_INDEX, YoY_COUPON.getPriceIndex());
    assertEquals("Inflation Year on Year coupon: getter", REFERENCE_START_TIME, YoY_COUPON.getReferenceStartTime());
    assertEquals("Inflation Year on Year coupon: getter", REFERENCE_END_TIME, YoY_COUPON.getReferenceEndTime());
    assertEquals("Inflation Year on Year coupon: getter", NATURAL_PAYMENT_START_TIME, YoY_COUPON.getNaturalPaymentStartTime());
    assertEquals("Inflation Year on Year coupon: getter", NATURAL_PAYMENT_END_TIME, YoY_COUPON.getNaturalPaymentEndTime());
    assertEquals("Inflation Year on Year coupon: getter", WEIGHT_START, YoY_COUPON.getWeightStart());
    assertEquals("Inflation Year on Year coupon: getter", WEIGHT_END, YoY_COUPON.getWeightEnd());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals(YoY_COUPON, YoY_COUPON);
    CouponInflationYearOnYearInterpolation couponDuplicate = new CouponInflationYearOnYearInterpolation(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, REFERENCE_START_TIME,
        NATURAL_PAYMENT_START_TIME, REFERENCE_END_TIME, NATURAL_PAYMENT_END_TIME, false, WEIGHT_START, WEIGHT_END);
    assertEquals(YoY_COUPON, couponDuplicate);
    assertEquals(YoY_COUPON.hashCode(), couponDuplicate.hashCode());
    CouponInflationYearOnYearInterpolation modified;
    final double[] modifiedReferenceStartTime = new double[2];
    modifiedReferenceStartTime[0] = REFERENCE_START_TIME[0];
    modifiedReferenceStartTime[1] = REFERENCE_START_TIME[1] + 0.1;
    modified = new CouponInflationYearOnYearInterpolation(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, modifiedReferenceStartTime,
        NATURAL_PAYMENT_START_TIME, REFERENCE_END_TIME, NATURAL_PAYMENT_END_TIME, false, WEIGHT_START, WEIGHT_END);
    assertFalse(YoY_COUPON.equals(modified));
    final double[] modifiedReferenceEndTime = new double[2];
    modifiedReferenceEndTime[0] = REFERENCE_END_TIME[0];
    modifiedReferenceEndTime[1] = REFERENCE_END_TIME[1] + 0.1;
    modified = new CouponInflationYearOnYearInterpolation(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, REFERENCE_START_TIME,
        NATURAL_PAYMENT_START_TIME, modifiedReferenceEndTime, NATURAL_PAYMENT_END_TIME, false, WEIGHT_START, WEIGHT_END);
    assertFalse(YoY_COUPON.equals(modified));
    final double modifiedNaturalPaymentStartTime = NATURAL_PAYMENT_START_TIME + .01;
    modified = new CouponInflationYearOnYearInterpolation(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, REFERENCE_START_TIME,
        modifiedNaturalPaymentStartTime, REFERENCE_END_TIME, NATURAL_PAYMENT_END_TIME, false, WEIGHT_START, WEIGHT_END);
    assertFalse(YoY_COUPON.equals(modified));
    final double modifiedNaturalPaymentEndTime = NATURAL_PAYMENT_END_TIME + .01;
    modified = new CouponInflationYearOnYearInterpolation(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, REFERENCE_START_TIME,
        NATURAL_PAYMENT_START_TIME, REFERENCE_END_TIME, modifiedNaturalPaymentEndTime, false, WEIGHT_START, WEIGHT_END);
    assertFalse(YoY_COUPON.equals(modified));

  }

}
