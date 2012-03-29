/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.derivatives;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the zero-coupon inflation constructors.
 */
public class CouponInflationZeroCouponInterpolationTest {
  private static final String NAME = "Euro HICP x";
  private static final Currency CUR = Currency.EUR;
  private static final Currency REGION = Currency.EUR;
  private static final Period LAG = Period.ofDays(14);
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME, CUR, REGION, LAG);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double INDEX_APRIL_2008 = 108.23; // 3 m before Aug: May / 1 May index = May index: 108.23
  private static final ZonedDateTime[] REFERENCE_END_DATE = new ZonedDateTime[2];
  static {
    REFERENCE_END_DATE[0] = PAYMENT_DATE.minusMonths(MONTH_LAG).withDayOfMonth(1);
    REFERENCE_END_DATE[1] = PAYMENT_DATE.minusMonths(MONTH_LAG - 1).withDayOfMonth(1);
  }
  private static final ZonedDateTime FIXING_DATE = REFERENCE_END_DATE[1].plusMonths(1).withDayOfMonth(1).plusWeeks(2);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 7, 29);
  private static final double PAYMENT_TIME = ACT_ACT.getDayCountFraction(PRICING_DATE, PAYMENT_DATE);
  private static final double[] REFERENCE_END_TIME = new double[2];
  static {
    REFERENCE_END_TIME[0] = ACT_ACT.getDayCountFraction(PRICING_DATE, REFERENCE_END_DATE[0]);
    REFERENCE_END_TIME[1] = ACT_ACT.getDayCountFraction(PRICING_DATE, REFERENCE_END_DATE[1]);
  }
  private static final double FIXING_TIME = ACT_ACT.getDayCountFraction(PRICING_DATE, FIXING_DATE);
  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final double WEIGHT = 1.0 - (PAYMENT_DATE.getDayOfMonth() - 1) / PAYMENT_DATE.getMonthOfYear().getLastDayOfMonth(PAYMENT_DATE.isLeapYear());
  private static final CouponInflationZeroCouponInterpolation ZERO_COUPON = new CouponInflationZeroCouponInterpolation(CUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, 1.0, NOTIONAL, PRICE_INDEX,
      INDEX_APRIL_2008, REFERENCE_END_TIME, WEIGHT, FIXING_TIME, false);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponInflationZeroCouponInterpolation(CUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, 1.0, NOTIONAL, null, INDEX_APRIL_2008, REFERENCE_END_TIME, WEIGHT, FIXING_TIME, false);
  }

  @Test
  /**
   * Tests the class getter.
   */
  public void getter() {
    assertEquals("Inflation Zero-coupon: getter", PRICE_INDEX, ZERO_COUPON.getPriceIndex());
    assertEquals("Inflation Zero-coupon: getter", INDEX_APRIL_2008, ZERO_COUPON.getIndexStartValue());
    assertEquals("Inflation Zero-coupon: getter", REFERENCE_END_TIME, ZERO_COUPON.getReferenceEndTime());
    assertEquals("Inflation Zero-coupon: getter", FIXING_TIME, ZERO_COUPON.getFixingEndTime());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals(ZERO_COUPON, ZERO_COUPON);
    CouponInflationZeroCouponInterpolation couponDuplicate = new CouponInflationZeroCouponInterpolation(CUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, 1.0, NOTIONAL, PRICE_INDEX, INDEX_APRIL_2008,
        REFERENCE_END_TIME, WEIGHT, FIXING_TIME, false);
    assertEquals(ZERO_COUPON, couponDuplicate);
    assertEquals(ZERO_COUPON.hashCode(), couponDuplicate.hashCode());
    CouponInflationZeroCouponInterpolation modified;
    modified = new CouponInflationZeroCouponInterpolation(CUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, 1.0, NOTIONAL, PRICE_INDEX, INDEX_APRIL_2008 + 0.1, REFERENCE_END_TIME, WEIGHT, FIXING_TIME, false);
    assertFalse(ZERO_COUPON.equals(modified));
    double[] modifiedReferenceTime = new double[2];
    modifiedReferenceTime[0] = REFERENCE_END_TIME[0];
    modifiedReferenceTime[1] = REFERENCE_END_TIME[1] + 0.1;
    modified = new CouponInflationZeroCouponInterpolation(CUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, 1.0, NOTIONAL, PRICE_INDEX, INDEX_APRIL_2008, modifiedReferenceTime, WEIGHT, FIXING_TIME, false);
    assertFalse(ZERO_COUPON.equals(modified));
    modified = new CouponInflationZeroCouponInterpolation(CUR, PAYMENT_TIME, DISCOUNTING_CURVE_NAME, 1.0, NOTIONAL, PRICE_INDEX, INDEX_APRIL_2008, REFERENCE_END_TIME, WEIGHT, FIXING_TIME + 0.1, false);
    assertFalse(ZERO_COUPON.equals(modified));
  }

}
