/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.interestratefuture;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.convention.BusinessDayConvention;
import com.opengamma.core.convention.Calendar;
import com.opengamma.core.convention.DayCount;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.HolidaySourceCalendarAdapter;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class InterestRateFutureSecurityToInterestRateFutureConverter {

  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;

  public InterestRateFutureSecurityToInterestRateFutureConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource) {
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  public InterestRateFuture getInterestRateFuture(final InterestRateFutureSecurity security, final String indexCurveName, final double price, final ZonedDateTime now) {
    final String currency = security.getCurrency().getISOCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_IRFUTURE"));
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCurrency()); // TODO: check we've got the right holiday calendar.

    final BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    // Expiry is actually the last trade date rather than the expiry date of the future so we have to go forward in time.
    final ZonedDateTime fixingDate = businessDayConvention.adjustDate(calendar, security.getExpiry().getExpiry());
    final ZonedDateTime settlementDate = businessDayConvention.adjustDate(calendar, fixingDate.plusDays(conventions.getSettlementDays()));
    final ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, settlementDate.plusMonths(3));
    final DayCount dayCount = conventions.getDayCount();
    final double yearFraction = dayCount.getDayCountFraction(fixingDate, maturityDate); // TODO: double check this
    final double valueYearFraction = conventions.getFutureYearFraction();
    final double settlementDateFraction = dayCount.getDayCountFraction(now, settlementDate);
    final double fixingDateFraction = dayCount.getDayCountFraction(now, fixingDate);
    final double maturityDateFraction = dayCount.getDayCountFraction(now, maturityDate);
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
