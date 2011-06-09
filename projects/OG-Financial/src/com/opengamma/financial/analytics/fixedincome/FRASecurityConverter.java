/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.instrument.Convention;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.fra.FRADefinition;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.FRASecurityVisitor;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class FRASecurityConverter implements FRASecurityVisitor<FixedIncomeInstrumentConverter<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;

  public FRASecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  @Override
  public FRADefinition visitFRASecurity(final FRASecurity security) {
    Validate.notNull(security, "security");
    final Currency currency = security.getCurrency();
    final String currencyCode = currency.getCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currencyCode + "_FRA"));
    final Calendar calendar = CalendarUtil.getCalendar(_holidaySource, currency);
    final BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    final ZonedDateTime startDate = security.getStartDate();
    final ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, security.getEndDate()); // just in case
    final Convention convention = new Convention(conventions.getSettlementDays(), conventions.getDayCount(),
        conventions.getBusinessDayConvention(), calendar, currencyCode + "_FRA_CONVENTION");
    return new FRADefinition(startDate, maturityDate, security.getRate() / 100, convention);
  }
}
