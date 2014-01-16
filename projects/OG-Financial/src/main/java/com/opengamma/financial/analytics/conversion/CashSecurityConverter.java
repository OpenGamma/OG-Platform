/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts {@link CashSecurity} to the equivalent OG-Analytics object ({@link CashDefinition}).
 */
public class CashSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;

  /**
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   */
  public CashSecurityConverter(final HolidaySource holidaySource, final RegionSource regionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _regionSource = regionSource;
  }

  @Override
  public CashDefinition visitCashSecurity(final CashSecurity security) {
    ArgumentChecker.notNull(security, "cash security");
    final Currency currency = security.getCurrency();
    // TODO: Do we need to adjust the dates to a good business day?
    final ZonedDateTime startDate = security.getStart();
    final ZonedDateTime endDate = security.getMaturity();
    final ExternalId regionId = security.getRegionId();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final double accrualFactor = security.getDayCount().getDayCountFraction(startDate, endDate, calendar);
    return new CashDefinition(currency, startDate, endDate, security.getAmount(), security.getRate(), accrualFactor);
  }

}
