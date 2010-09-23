/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fra;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.world.holiday.HolidaySource;
import com.opengamma.id.Identifier;
/**
 * 
 */
public class FRASecuityToForwardRateAgreementConverter {
  
  private HolidaySource _holidaySource;
  private ConventionBundleSource _conventionSource;

  public FRASecuityToForwardRateAgreementConverter(HolidaySource holidaySource, ConventionBundleSource conventionSource) {
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  public ForwardRateAgreement getFRA(FRASecurity security, String fundingCurveName, String indexCurveName, double marketRate, ZonedDateTime now){
    String currency = security.getCurrency().getISOCode();
    ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_FRA"));
    Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCurrency()); // TODO: check we've got the right holiday calendar.
    
    BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    ZonedDateTime fixingDate = businessDayConvention.adjustDate(calendar, security.getStartDate().toZonedDateTime()); // just in case
    
    ZonedDateTime settlementDate = businessDayConvention.adjustDate(calendar, security.getStartDate().toZonedDateTime().plusDays(conventions.getSettlementDays()));
    ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, security.getEndDate().toZonedDateTime()); // just in case
    
    DayCount dayCount = conventions.getDayCount();
    
    // all times on discount/yield/forward curves are measured ACT/ACT
    double fixingTime = DayCountFactory.INSTANCE.getDayCount("Actual/Actual").getDayCountFraction(now, fixingDate);
    double settlementTime = DayCountFactory.INSTANCE.getDayCount("Actual/Actual").getDayCountFraction(now, settlementDate);
    double maturityTime = DayCountFactory.INSTANCE.getDayCount("Actual/Actual").getDayCountFraction(now, maturityDate);
    
    double forwardYearFraction = dayCount.getDayCountFraction(fixingDate, maturityDate);
    double discountingYearFraction = dayCount.getDayCountFraction(settlementDate, maturityDate);
    
    return new ForwardRateAgreement(settlementTime, maturityTime, fixingTime, forwardYearFraction, discountingYearFraction, marketRate, fundingCurveName, indexCurveName);
  }
  
}
