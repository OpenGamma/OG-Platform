/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.interestratefuture;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.world.holiday.HolidaySource;
import com.opengamma.id.Identifier;
/**
 * 
 */
public class InterestRateFutureSecurityToInterestRateFutureConverter {
  
  private HolidaySource _holidaySource;
  private ConventionBundleSource _conventionSource;

  public InterestRateFutureSecurityToInterestRateFutureConverter(HolidaySource holidaySource, ConventionBundleSource conventionSource) {
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  public InterestRateFuture getInterestRateFuture(InterestRateFutureSecurity security, String indexCurveName, double price, ZonedDateTime now) {
    String currency = security.getCurrency().getISOCode();
    ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_IRFUTURE"));
    Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCurrency()); // TODO: check we've got the right holiday calendar.
    
    BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    ZonedDateTime settlementDate = businessDayConvention.adjustDate(calendar, security.getExpiry().getExpiry());
    ZonedDateTime fixingDate = businessDayConvention.adjustDate(calendar, settlementDate.minusDays(conventions.getSettlementDays()));
    ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, fixingDate.plusMonths(3));
    DayCount dayCount = conventions.getDayCount();
    double yearFraction = dayCount.getDayCountFraction(maturityDate, fixingDate); // TODO: double check this
    double valueYearFraction = conventions.getFuturePointValue();
    double settlementDateFraction = dayCount.getDayCountFraction(now, settlementDate);
    double fixingDateFraction = dayCount.getDayCountFraction(now, fixingDate);
    double maturityDateFraction = dayCount.getDayCountFraction(now, maturityDate);
    return new InterestRateFuture(settlementDateFraction, fixingDateFraction, maturityDateFraction, yearFraction, valueYearFraction, price, indexCurveName);
    /*
     * Settlement date = from convention (2 days before fixing date)
     */
    /*
     * Fixing date = 3rd Wednesday of the expiry month (-> no need to adjust date [what happens if it is a holiday?])
     */
    /*
     * Maturity date = 3m from fixing date 
     */
    /*
     * Adjust date 
     */
    /*
     * Year count fraction (index year fraction)
     */
    /*
     * Value year fraction = 0.25 ($) = 0.125 (£) etc
     */
    /*
     * price = 100 - r * 100
     */
    /*
     * curve name
     */
  }
}
