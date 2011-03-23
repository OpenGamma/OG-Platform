/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class CouponIborSpreadDefinitionTest {
  private static final Tenor TENOR = new Tenor(Period.ofMonths(3));
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final double NOTIONAL = 1000000; //1m
  private static final double SPREAD = -0.001; // -10 bps
  // Coupon with standard payment and accrual dates.
  private static final CouponIborDefinition IBOR_COUPON = CouponIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX);
  private static final CouponIborSpreadDefinition IBOR_COUPON_SPREAD = CouponIborSpreadDefinition.from(IBOR_COUPON, SPREAD);

  //  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 12, 27); //For conversion to derivative

  @Test
  public void testGetter() {
    assertEquals(IBOR_COUPON_SPREAD.getNotional(), NOTIONAL, 1E-2);
    assertEquals(IBOR_COUPON_SPREAD.getFixingDate(), FIXING_DATE);
    assertEquals(IBOR_COUPON_SPREAD.isFixed(), false);
    assertEquals(IBOR_COUPON_SPREAD.getSpread(), SPREAD, 1E-10);
    assertEquals(IBOR_COUPON_SPREAD.getSpreadAmount(), SPREAD * NOTIONAL * IBOR_COUPON_SPREAD.getPaymentYearFraction(), 1E-2);
  }

}
