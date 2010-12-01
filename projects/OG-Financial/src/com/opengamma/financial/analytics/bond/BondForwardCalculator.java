/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.bond;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.HolidaySource;
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
import com.opengamma.id.Identifier;

/**
 * 
 */
public class BondForwardCalculator {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
 
  public BondForwardCalculator(final HolidaySource holidaySource, final ConventionBundleSource conventionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  public double getForwardDirtyPrice(final BondSecurity security, final double cleanPrice, final ZonedDateTime now, final ZonedDateTime deliveryDate, double repoRate) {
    Validate.notNull(security, "security");
    Validate.notNull(now, "now");
    Validate.notNull(deliveryDate, "deliveryDate");
    final LocalDate today = now.toLocalDate();
    final LocalDate deliveryDateLD = deliveryDate.toLocalDate();
    final LocalDate maturityDate = security.getMaturity().getExpiry().toLocalDate();
    Validate.isTrue(today.isBefore(maturityDate), "The bond has expired");
    Validate.isTrue(deliveryDateLD.isBefore(maturityDate), "The bond has expired before delivery");
    
    Validate.isTrue(cleanPrice > 10, "please input clean price on price (i.e around 100) bases");
    Validate.isTrue(repoRate < 1, "please input repo rate as fraction");
    
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
    final LocalDate datedDate = security.getInterestAccrualDate().toZonedDateTime().toLocalDate();
    final LocalDate[] schedule = getBondSchedule(security, maturityDate, simpleFrequency, convention, datedDate);
    final int periodsPerYear = (int) simpleFrequency.getPeriodsPerYear();
    final double timeBetweenPeriods = 1. / periodsPerYear;
   // final LocalDate[] settlementDateSchedule = ScheduleCalculator.getSettlementDateSchedule(schedule, calendar, convention.getSettlementDays()); //TODO should be in schedule factory 
    final  LocalDate[] settlementDateSchedule = schedule;
    //TODO remove this when the definitions for USD treasuries are correct
    final DayCount daycount = currency.getISOCode().equals("USD") ? DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA") : security.getDayCountConvention(); 
    final int settlementDays = convention.getSettlementDays();
    final double coupon = security.getCouponRate();
    final boolean isEOMConvention = convention.isEOMConvention();
    final double accruedInterest = AccruedInterestCalculator.getAccruedInterest(daycount, today, schedule, coupon, periodsPerYear, isEOMConvention, convention.getExDividendDays());
    
  
    double dirtyPrice = cleanPrice + accruedInterest;
    final DayCount repoDaycount = DayCountFactory.INSTANCE.getDayCount("Actual/360");
 
    
    double repoPeriod = repoDaycount.getDayCountFraction(now, deliveryDate);
    double valueOfExpiredCoupons = 0.0;
    
    for (LocalDate couponDate : settlementDateSchedule) {
      if (couponDate.isAfter(today)) {
        if (couponDate.isAfter(deliveryDateLD)) {
          break;
        }
        double period = repoDaycount.getDayCountFraction(couponDate.atMidnight().atZone(TimeZone.UTC), deliveryDate);
        valueOfExpiredCoupons += coupon * timeBetweenPeriods * (1 + repoRate  * period);
      }
    }
    
    return dirtyPrice * (1 + repoPeriod * repoRate) - valueOfExpiredCoupons;
    
    
    
//    final List<Double> paymentTimes = new ArrayList<Double>();
//    final double accrualTime = accruedInterest / coupon;
//    double timeToNextCoupon = timeBetweenPeriods - accrualTime;
//    paymentTimes.add(timeToNextCoupon);
//    int nextCouponIndex = 0;
//    for (final LocalDate element : settlementDateSchedule) {
//      if (today.isAfter(element)) {
//        nextCouponIndex++;
//      }
//    }
//    for (int i = nextCouponIndex + 1; i < settlementDateSchedule.length; i++) {
//      timeToNextCoupon += timeBetweenPeriods;
//      paymentTimes.add(timeToNextCoupon);
//    }
//    final double[] payments = new double[paymentTimes.size()];
//    for (int i = 0; i < payments.length; i++) {
//      payments[i] = paymentTimes.get(i);
//    }
//    return new Bond(payments, coupon / 100., timeBetweenPeriods, accruedInterest / 100.0, curveName);
  }
  
  private LocalDate[] getBondSchedule(final BondSecurity security, final LocalDate maturityDate, final SimpleFrequency simpleFrequency, final ConventionBundle convention, final LocalDate datedDate) {
    LocalDate[] schedule = ScheduleFactory.getSchedule(datedDate, maturityDate, simpleFrequency, convention.isEOMConvention(), convention.calculateScheduleFromMaturity(), false);
    // front stub
    if (schedule[0].equals(security.getFirstCouponDate().toZonedDateTime().toLocalDate())) {
      final int n = schedule.length;
      final LocalDate[] temp = new LocalDate[n + 1];
      temp[0] = datedDate;
      for (int i = 1; i < n + 1; i++) {
        temp[i] = schedule[i - 1];
      }
      schedule = temp;
    }
    if (!schedule[1].toLocalDate().equals(security.getFirstCouponDate().toZonedDateTime().toLocalDate())) {
      throw new IllegalArgumentException("Security first coupon date did not match calculated first coupon date");
    }
    return schedule;
  }

  private ZonedDateTime getSettlementDate(final ZonedDateTime today, final Calendar calendar, final int settlementDays) {
    int count = 0;
    int adjusted = 0;
    final ZonedDateTime date = today;
    while (adjusted < settlementDays) {
      if (calendar.isWorkingDay(date.plusDays(count + 1).toLocalDate())) {
        count++;
        adjusted++;
      } else {
        count++;
      }
    }
    return today.plusDays(count);
  }
}
