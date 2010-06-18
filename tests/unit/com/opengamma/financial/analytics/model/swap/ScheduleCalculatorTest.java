/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.Set;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeFieldContainer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.businessday.ModifiedBusinessDayConvention;
import com.opengamma.financial.convention.businessday.PrecedingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.ActualActualISDADayCount;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyDayCount;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFiveFixedDayCount;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.OneOneDayCount;
import com.opengamma.financial.convention.daycount.ThirtyEThreeSixtyDayCount;
import com.opengamma.financial.convention.daycount.ThirtyEThreeSixtyISDADayCount;
import com.opengamma.financial.convention.daycount.ThirtyThreeSixtyDayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.Region;
import com.opengamma.financial.security.RegionType;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class ScheduleCalculatorTest {
  private static final Logger s_logger = LoggerFactory.getLogger(ScheduleCalculatorTest.class);
  private static final Calendar[] CALENDAR = new Calendar[] {new MyCalendar(12), new MyCalendar(9), new MyCalendar(7),
      new MyCalendar(20), new MyCalendar(30), new MyCalendar(15)};
  private static final Frequency[] FREQUENCY = new Frequency[] {PeriodFrequency.ANNUAL, PeriodFrequency.BIMONTHLY,
      PeriodFrequency.BIWEEKLY, PeriodFrequency.MONTHLY, PeriodFrequency.QUARTERLY, PeriodFrequency.SEMI_ANNUAL,
      PeriodFrequency.WEEKLY};
  private static final DayCount[] DAY_COUNT = new DayCount[] {new ActualActualISDADayCount(),
      new ActualThreeSixtyDayCount(), new ActualThreeSixtyFiveFixedDayCount(), new OneOneDayCount(),
      new ThirtyEThreeSixtyDayCount(), new ThirtyEThreeSixtyISDADayCount(), new ThirtyThreeSixtyDayCount()};
  private static final BusinessDayConvention[] BUSINESS_DAY = new BusinessDayConvention[] {
      new FollowingBusinessDayConvention(), new ModifiedBusinessDayConvention(), new PrecedingBusinessDayConvention()};
  private static final Notional NOTIONAL = new Notional() {
  };
  private static final Region REGION = new Region() {

    @Override
    public FudgeFieldContainer getData() {
      return null;
    }

    @Override
    public FudgeFieldContainer getDataUp() {
      return null;
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public RegionType getRegionType() {
      return null;
    }

    @Override
    public Set<Region> getSubRegions() {
      return null;
    }

    @Override
    public Region getSuperRegion() {
      return null;
    }

  };
  private static final int N = 100;

  @Test
  public void testGenerate() {
    ZonedDateTime date = DateUtil.getUTCDate(2000, 1, 1);
    final ZonedDateTime[] effective = new ZonedDateTime[N];
    final ZonedDateTime[] maturity = new ZonedDateTime[N];
    BusinessDayConvention convention;
    DayCount dayCount;
    final Calendar[] calendar = new Calendar[N];
    Frequency frequency;
    final SwapLeg[] legs = new SwapLeg[N];
    for (int i = 0; i < N; i++) {
      date = date.plusDays(1);
      effective[i] = date;
      maturity[i] = date.plusYears((int) (60 * Math.random()));
      convention = BUSINESS_DAY[(int) Math.floor(Math.random() * BUSINESS_DAY.length)];
      dayCount = DAY_COUNT[(int) Math.floor(Math.random() * DAY_COUNT.length)];
      calendar[i] = CALENDAR[(int) Math.floor(Math.random() * CALENDAR.length)];
      frequency = FREQUENCY[(int) Math.floor(Math.random() * FREQUENCY.length)];
      legs[i] = new SwapLeg(dayCount, frequency, REGION, convention, NOTIONAL);
    }
    //for (int i = 0; i < N; i++) {
    //  ScheduleCalculator.getSchedule(effective[i], maturity[i], legs[i], calendar[i]);
    //}
    //final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles", N);
    //for (int i = 0; i < N; i++) {
    //System.out.println(legs[i]);
    //  ScheduleCalculator.getSchedule(effective[i], maturity[i], legs[i], calendar[i]);
    //}
    //timer.finished();

  }

  private static class MyCalendar implements Calendar {
    private final double _frequency;

    public MyCalendar(final int daysPerYear) {
      _frequency = daysPerYear / 365.;
    }

    @Override
    public String getConventionName() {
      return null;
    }

    @Override
    public boolean isWorkingDay(final LocalDate date) {
      final DayOfWeek day = date.getDayOfWeek();
      if (day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY)) {
        return false;
      }
      if (Math.random() < _frequency) {
        return false;
      }
      return true;
    }

  }
}
