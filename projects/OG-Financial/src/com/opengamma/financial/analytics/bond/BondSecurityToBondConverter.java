/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.bond;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.Currency;
import com.opengamma.financial.analytics.schedule.ScheduleCalculator;
import com.opengamma.financial.analytics.timeseries.ScheduleFactory;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.AccruedInterestCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.world.holiday.master.HolidaySource;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class BondSecurityToBondConverter {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;

  public BondSecurityToBondConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  public Bond getBond(final BondSecurity security, final String curveName, final ZonedDateTime now) {
    Validate.notNull(security, "security");
    Validate.notNull(curveName, "curve name");
    Validate.notNull(now, "now");
    final ZonedDateTime maturityDate = security.getMaturity().getExpiry();
    Validate.isTrue(now.isBefore(maturityDate), "The bond has expired");
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCurrency());
    final Frequency frequency = security.getCouponFrequency();
    final SimpleFrequency simpleFrequency;
    if (frequency instanceof PeriodFrequency) {
      simpleFrequency = ((PeriodFrequency) frequency).toSimpleFrequency();
    } else if (frequency instanceof SimpleFrequency) {
      simpleFrequency = (SimpleFrequency) frequency;
    } else {
      throw new IllegalArgumentException("Can only handle PeriodFrequency and SimpleFrequency");
    }
    final Currency currency = security.getCurrency();
    final Identifier id = Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_TREASURY_COUPON_DATE_CONVENTION");
    final ConventionBundle convention = _conventionSource.getConventionBundle(id);
    final ZonedDateTime datedDate = security.getInterestAccrualDate().toZonedDateTime();
    final ZonedDateTime[] schedule = ScheduleFactory.getSchedule(datedDate, maturityDate, simpleFrequency, convention.isEOMConvention(), convention.calculateScheduleFromMaturity());
    final ZonedDateTime[] settlementDateSchedule = ScheduleCalculator.getSettlementDateSchedule(schedule, calendar, convention.getSettlementDays());
    final DayCount daycount = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"); //TODO remove this when the definitions for USD treasuries are correct
    final int settlementDays = convention.getSettlementDays();
    final double coupon = security.getCouponRate();
    final double periodsPerYear = simpleFrequency.getPeriodsPerYear();
    final double accruedInterest = AccruedInterestCalculator.getAccruedInterest(daycount, getSettlementDate(now, calendar, settlementDays), schedule, coupon,
        (int) simpleFrequency.getPeriodsPerYear(), convention.isEOMConvention());
    final List<Double> paymentTimes = new ArrayList<Double>();
    for (final ZonedDateTime settlementDate : settlementDateSchedule) {
      if (now.isBefore(settlementDate)) {
        paymentTimes.add(getPaymentTime(now, settlementDate));
      }
    }
    final double[] payments = new double[paymentTimes.size()];
    for (int i = 0; i < payments.length; i++) {
      payments[i] = paymentTimes.get(i);
    }
    //TODO have to deal with negative accrual for ex-dividend bonds
    return new Bond(payments, coupon / 100., 1. / periodsPerYear, periodsPerYear * accruedInterest / coupon, curveName);
  }

  //TODO not sure if this is right
  private double getPaymentTime(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    final int y1 = firstDate.getYear();
    final int y2 = secondDate.getYear();
    if (y1 == y2) {
      final double basis = DateUtil.isLeapYear(firstDate) ? 366 : 365;
      return DateUtil.getExactDaysBetween(firstDate, secondDate) / basis;
    }
    final ZonedDateTime firstNewYear = firstDate.withDate(y1 + 1, 1, 1).withTime(0, 0);
    final ZonedDateTime secondNewYear = secondDate.withDate(y2, 1, 1).withTime(0, 0);
    final double firstBasis = DateUtil.isLeapYear(firstDate) ? 366 : 365;
    final double secondBasis = DateUtil.isLeapYear(secondDate) ? 366 : 365;
    return DateUtil.getExactDaysBetween(firstDate, firstNewYear) / firstBasis + DateUtil.getExactDaysBetween(secondNewYear, secondDate) / secondBasis + (y2 - y1 - 1);
  }

  private ZonedDateTime getSettlementDate(final ZonedDateTime now, final Calendar calendar, final int settlementDays) {
    int count = 0;
    int adjusted = 0;
    final ZonedDateTime date = now;
    while (adjusted < settlementDays) {
      if (calendar.isWorkingDay(date.plusDays(count + 1).toLocalDate())) {
        count++;
        adjusted++;
      } else {
        count++;
      }
    }
    return now.plusDays(count);
  }
}
