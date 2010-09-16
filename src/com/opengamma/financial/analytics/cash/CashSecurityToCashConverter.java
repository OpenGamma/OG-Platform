/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cash;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.businessday.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.world.holiday.HolidaySource;
 
public class CashSecurityToCashConverter {
  
  private HolidaySource _holidaySource;
  private ConventionBundleSource _conventionSource;
  
  public CashSecurityToCashConverter(HolidaySource holidaySource, ConventionBundleSource conventionSource) {
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }
  
  public Cash getCash(CashSecurity security, String fundingCurveName, double marketRate, ZonedDateTime now) {
    ConventionBundle conventions = _conventionSource.getConventionBundle(security.getIdentifiers());
    Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCurrency());
    ZonedDateTime startDate = conventions.getBusinessDayConvention().adjustDate(calendar, now.plusDays(conventions.getSettlementDays()));
    DayCount dayCount = conventions.getDayCount();
    double tradeTime = dayCount.getDayCountFraction(now, startDate);
    ZonedDateTime maturityDate = security.getMaturity().toZonedDateTime();
    DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual");
    double paymentTime = actAct.getDayCountFraction(now, maturityDate); 
    double yearFraction = dayCount.getDayCountFraction(startDate, maturityDate);    
    return new Cash(tradeTime, marketRate, paymentTime, yearFraction, fundingCurveName);
  }
}
