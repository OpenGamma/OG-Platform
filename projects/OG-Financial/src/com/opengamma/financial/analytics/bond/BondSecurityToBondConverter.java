/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.bond;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.analytics.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.world.holiday.HolidaySource;

/**
 * 
 */
public class BondSecurityToBondConverter {
  
  private final HolidaySource _holidaySource;

  public BondSecurityToBondConverter(final HolidaySource holidaySource) {
    _holidaySource = holidaySource;
  }

  public Bond getBond(final BondSecurity security, final String curveName,  final ZonedDateTime now) {
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCurrency());
    
    ZonedDateTime firstCouponDate = security.getFirstCouponDate().toZonedDateTime();
    ZonedDateTime maturityDate = security.getMaturity().getExpiry();
    
    Validate.isTrue(now.isBefore(maturityDate), "The bond has expired");
    
    Frequency frequency = security.getCouponFrequency();
    final SimpleFrequency simpleFrequency;
    if (frequency instanceof PeriodFrequency) {
      simpleFrequency = ((PeriodFrequency) frequency).toSimpleFrequency();
    } else if (frequency instanceof SimpleFrequency) {
      simpleFrequency = (SimpleFrequency) frequency;
    } else {
      throw new IllegalArgumentException("For the moment can only deal with PeriodFrequency and SimpleFrequency");
    }
  
    double paymentYearFraction = 1.0 / simpleFrequency.getPeriodsPerYear();
   
    //these are the remaining payment dates after the firstCouponDate - they could fall on non-business days 
    final ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(firstCouponDate, maturityDate, frequency);  
    final ZonedDateTime[] temp = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, security.getBusinessDayConvention(), calendar);
    int n = temp.length; 
    final ZonedDateTime[] couponDates = new ZonedDateTime[n + 1];
    couponDates[0] = firstCouponDate;
    for (int i = 0; i < n; i++) {
      couponDates[i + 1] = temp[i];
    }
    //TODO The last coupon date should be the maturity 
    
      
    //for a seasoned bond, find out the next coupon
    int index = 0;
    while (!now.isBefore(couponDates[index])) { //if now is on or after couponDates[index], assume coupon has been paid
      index++;
    }
    
    ZonedDateTime previousCouponDate;
    if (index == 0) {
      previousCouponDate = security.getInterestAccrualDate().toZonedDateTime();
    } else {
      previousCouponDate = couponDates[index - 1];
    }
    
    double periodYearFrac = security.getDayCountConvention().getDayCountFraction(previousCouponDate, couponDates[index]);
    
    ZonedDateTime exDivDate = getExDividenedDate(couponDates[index], 0); //TODO get the days from somewhere
    double accualFraction;
    if (now.isBefore(exDivDate)) {
      double accualTime = security.getDayCountConvention().getDayCountFraction(previousCouponDate, now);
      accualFraction = accualTime / periodYearFrac;
    } else {
      double accualTime = -security.getDayCountConvention().getDayCountFraction(now, couponDates[index]);
      accualFraction = accualTime / periodYearFrac;
      index++; //drop the next coupon from the bond as we are ex-dividend 
    }
    
    int nPayments = n + 1 - index; //TODO what happens when this is zero, i.e. the last coupon has go ex-dividend - does the buyer still get the principle (in which case the bond still has value)? 
    double[] paymentTimes = new double[nPayments]; 
    DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual");
    for (int i = 0; i < nPayments; i++) {
      paymentTimes[i] = actAct.getDayCountFraction(now, couponDates[i + index]);
    }
    
    return new Bond(paymentTimes, security.getCouponRate() / 100., paymentYearFraction, accualFraction, curveName);
  }
  
  private ZonedDateTime getExDividenedDate(ZonedDateTime couponDate, int daysBefore) {
    return couponDate.minusDays(daysBefore);
  }

}
