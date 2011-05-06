/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Test related to the construction of Cap/floor on Ibor.
 */
public class CapFloorIborTest {

  private static final Currency CUR = Currency.USD;
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  //  private static final boolean IS_EOM = true;
  //  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  double NOTIONAL = 1000000;
  double STRIKE = 0.04;
  boolean IS_CAP = true;
  // The dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, BUSINESS_DAY, CALENDAR, TENOR);
  private static final DayCount DAY_COUNT_COUPON = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  double PAYMENT_YEAR_FRACTION = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  double FIXING_YEAR_FRACTION = DAY_COUNT_INDEX.getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE);
  // Reference date and time.
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 12, 27); //For conversion to derivative
  final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
  double PAYMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, PAYMENT_DATE);
  double FIXING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_DATE);
  double FIXING_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_START_DATE);
  double FIXING_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_END_DATE);
  // Curves
  String FUNDING_CURVE_NAME = "Funding";
  String FORWARD_CURVE_NAME = "Forward";

  @Test
  public void testGetters() {
    CapFloorIbor cap = new CapFloorIbor(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, FIXING_START_TIME, FIXING_END_TIME, FIXING_YEAR_FRACTION,
        FORWARD_CURVE_NAME, STRIKE, IS_CAP);
    assertEquals("Getter strike", STRIKE, cap.geStrike());
    assertEquals("Getter cap flag", IS_CAP, cap.isCap());
    double fixingRate = 0.05;
    assertEquals("Pay-off", Math.max(fixingRate - STRIKE, 0), cap.payOff(fixingRate));
  }
}
