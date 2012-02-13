/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cash.CashSecurityVisitor;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CashSecurityConverter implements CashSecurityVisitor<InstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;

  public CashSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  @Override
  public CashDefinition visitCashSecurity(final CashSecurity security) {
    Validate.notNull(security, "security");
    final Currency currency = security.getCurrency();
    // TODO: Do we need to adjust the dates to a good business day?
    final ZonedDateTime startDate = security.getStart();
    final ZonedDateTime endDate = security.getMaturity();
    final double accrualFactor = security.getDayCount().getDayCountFraction(startDate, endDate);
    return new CashDefinition(currency, startDate, endDate, security.getAmount(), security.getRate(), accrualFactor);
  }

}
