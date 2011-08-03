/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.financial.interestrate.MarketBundle;
import com.opengamma.financial.interestrate.MarketDataSets;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtil;

/**
 * Tests related to the pricing and sensitivities of Ibor coupon with gearing factor and spread in the discounting method.
 */
public class CouponIborGearingDiscountingMarketMethodTest {
  private static final MarketBundle MARKET = MarketDataSets.createMarket1();
  private static final IborIndex[] IBOR_INDEXES = MARKET.getIborIndexes().toArray(new IborIndex[0]);
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Calendar CALENDAR_EUR = EURIBOR3M.getCalendar();
  private static final DayCount DAY_COUNT_COUPON = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 8, 22);
  private static final double ACCRUAL_FACTOR = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final double FACTOR = 2.0;
  private static final double SPREAD = 0.0050;
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, CALENDAR_EUR, -EURIBOR3M.getSettlementDays());
  private static final CouponIborGearingDefinition COUPON_DEFINITION = new CouponIborGearingDefinition(EURIBOR3M.getCurrency(), ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR,
      NOTIONAL, FIXING_DATE, EURIBOR3M, SPREAD, FACTOR);
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2010, 12, 27);
  private static final CouponIborGearing COUPON = COUPON_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {"A", "B"});
  private static final CouponIborGearingDiscountingMarketMethod METHOD = new CouponIborGearingDiscountingMarketMethod();

  @Test
  public void f() {
    CurrencyAmount pv = METHOD.presentValue(COUPON, MARKET);
    double df = MARKET.getDiscountingFactor(COUPON.getCurrency(), COUPON.getPaymentTime());
    double forward = MARKET.getForwardRate(EURIBOR3M, COUPON.getFixingPeriodStartTime(), COUPON.getFixingPeriodEndTime(), COUPON.getFixingAccrualFactor());
    double pvExpected = (forward * FACTOR + SPREAD) * COUPON.getPaymentYearFraction() * COUPON.getNotional() * df;
    assertEquals("Coupon Ibor Gearing: Present value by discounting", pvExpected, pv.getAmount(), 1.0E-2);
  }

}
