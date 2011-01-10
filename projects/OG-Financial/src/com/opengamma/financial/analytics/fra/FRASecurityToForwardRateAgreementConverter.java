/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fra;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.Identifier;
/**
 * 
 */
public class FRASecurityToForwardRateAgreementConverter {

  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;

  public FRASecurityToForwardRateAgreementConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource) {
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  public ForwardRateAgreement getFRA(final FRASecurity security, final String fundingCurveName, final String indexCurveName, final double marketRate, final ZonedDateTime now) {
    final String currency = security.getCurrency().getISOCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_FRA"));
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCurrency()); // TODO: check we've got the right holiday calendar.

    final BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    final ZonedDateTime fixingDate = businessDayConvention.adjustDate(calendar, security.getStartDate().toZonedDateTime()); // just in case

    final ZonedDateTime settlementDate = businessDayConvention.adjustDate(calendar, security.getStartDate().toZonedDateTime().plusDays(conventions.getSettlementDays()));
    final ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, security.getEndDate().toZonedDateTime()); // just in case

    final DayCount dayCount = conventions.getDayCount();

    // all times on discount/yield/forward curves are measured ACT/ACT
    final double fixingTime = DayCountFactory.INSTANCE.getDayCount("Actual/Actual").getDayCountFraction(now, fixingDate);
    final double settlementTime = DayCountFactory.INSTANCE.getDayCount("Actual/Actual").getDayCountFraction(now, settlementDate);
    final double maturityTime = DayCountFactory.INSTANCE.getDayCount("Actual/Actual").getDayCountFraction(now, maturityDate);

    final double forwardYearFraction = dayCount.getDayCountFraction(fixingDate, maturityDate);
    final double discountingYearFraction = dayCount.getDayCountFraction(settlementDate, maturityDate);

    return new ForwardRateAgreement(settlementTime, maturityTime, fixingTime, forwardYearFraction, discountingYearFraction, marketRate, fundingCurveName, indexCurveName);
  }

}
