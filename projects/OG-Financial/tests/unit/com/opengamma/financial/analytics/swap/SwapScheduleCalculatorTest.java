/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.swap;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.junit.Test;

import com.opengamma.core.convention.Calendar;
import com.opengamma.core.convention.DayCount;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.businessday.ModifiedFollowingBusinessDayConvention;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.DateTimeWithZone;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.NotionalVisitor;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
@SuppressWarnings("synthetic-access")
public class SwapScheduleCalculatorTest {
  private static final ZonedDateTime EFFECTIVE = DateUtil.getUTCDate(2010, 6, 1);
  private static final ZonedDateTime MATURITY = DateUtil.getUTCDate(2020, 6, 1);
  private static final Identifier REGION_ID = RegionUtils.countryRegionId("US");
  private static final Notional NOTIONAL = new Notional() {

    @Override
    public FudgeFieldContainer toFudgeMsg(final FudgeMessageFactory fudgeContext) {
      // Okay to return NULL as we're not doing any messaging with this
      return null;
    }

    @Override
    public <T> T accept(final NotionalVisitor<T> visitor) {
      // Okay to return NULL as we're not using the visitor for this test
      return null;
    }

  };
  private static final DayCount DAY_COUNT = new DayCount() {

    @Override
    public String getConventionName() {
      return "";
    }

    @Override
    public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
      return secondDate.getYear() - firstDate.getYear() + ((double) secondDate.getMonthOfYear().getValue() - firstDate.getMonthOfYear().getValue()) / 12.;
    }

    @Override
    public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final int paymentsPerYear) {
      return 0;
    }

  };
  private static final SwapLeg PAY_LEG = new SwapLeg(DAY_COUNT, PeriodFrequency.SEMI_ANNUAL, REGION_ID, new ModifiedFollowingBusinessDayConvention(), NOTIONAL) {

    @Override
    public <T> T accept(final SwapLegVisitor<T> visitor) {
      // Okay to return NULL as we're not using the visitor for this test
      return null;
    }

    @Override
    public FudgeFieldContainer toFudgeMsg(final FudgeMessageFactory fudgeContext) {
      return null;
    }

  };
  private static final SwapLeg RECEIVE_LEG = new SwapLeg(DAY_COUNT, PeriodFrequency.SEMI_ANNUAL, REGION_ID, new ModifiedFollowingBusinessDayConvention(), NOTIONAL) {

    @Override
    public <T> T accept(final SwapLegVisitor<T> visitor) {
      // Okay to return NULL as we're not using the visitor for this test
      return null;
    }

    @Override
    public FudgeFieldContainer toFudgeMsg(final FudgeMessageFactory fudgeContext) {
      return null;
    }

  };
  private static final Calendar CALENDAR = new Calendar() {

    @Override
    public String getConventionName() {
      return "";
    }

    @Override
    public boolean isWorkingDay(final LocalDate date) {
      return true;
    }

  };
  private static final SwapSecurity SECURITY = new SwapSecurity(new DateTimeWithZone(EFFECTIVE), new DateTimeWithZone(EFFECTIVE), new DateTimeWithZone(MATURITY), "", PAY_LEG, RECEIVE_LEG);

  @Test(expected = IllegalArgumentException.class)
  public void testNullSecurity1() {
    SwapScheduleCalculator.getPayLegPaymentTimes(null, CALENDAR, EFFECTIVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalendar1() {
    SwapScheduleCalculator.getPayLegPaymentTimes(SECURITY, null, EFFECTIVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSecurity2() {
    SwapScheduleCalculator.getReceiveLegPaymentTimes(null, CALENDAR, EFFECTIVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalendar2() {
    SwapScheduleCalculator.getReceiveLegPaymentTimes(SECURITY, null, EFFECTIVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEffectiveDate() {
    SwapScheduleCalculator.getPaymentTimes(null, MATURITY, PAY_LEG, CALENDAR, EFFECTIVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullNow() {
    SwapScheduleCalculator.getPaymentTimes(EFFECTIVE, MATURITY, PAY_LEG, CALENDAR, null);
  }

  @Test
  public void test() {
    final double[] payTimes = SwapScheduleCalculator.getPayLegPaymentTimes(SECURITY, CALENDAR, EFFECTIVE);
    final double[] receiveTimes = SwapScheduleCalculator.getReceiveLegPaymentTimes(SECURITY, CALENDAR, EFFECTIVE);
    assertEquals(payTimes.length, 20);
    assertEquals(payTimes.length, receiveTimes.length);
    for (int i = 0; i < payTimes.length; i++) {
      assertEquals(payTimes[i], receiveTimes[i], 1e-15);
      assertEquals(payTimes[i], 0.5 * (i + 1), 1e-15);
    }
  }
}
