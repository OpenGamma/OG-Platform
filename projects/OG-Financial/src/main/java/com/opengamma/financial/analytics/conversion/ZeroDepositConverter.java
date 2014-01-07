/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.apache.commons.lang.NotImplementedException;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts zero deposit securities to their equivalents in the analytics library.
 */
public class ZeroDepositConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The convention source */
  private final ConventionBundleSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   */
  public ZeroDepositConverter(final ConventionBundleSource conventionSource, final HolidaySource holidaySource) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
  }

  @Override
  public InstrumentDefinition<?> visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final Currency currency = security.getCurrency();
    final ZonedDateTime startDate = security.getStartDate();
    final ZonedDateTime endDate = security.getMaturityDate();
    final ConventionBundle convention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_ZERO_DEPOSIT"));
    final DayCount daycount = convention.getDayCount();
    final InterestRate rate = new ContinuousInterestRate(security.getRate());
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, currency);
    return DepositZeroDefinition.from(currency, startDate, endDate, daycount, rate, calendar, daycount);
  }

  @Override
  public InstrumentDefinition<?> visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
    throw new NotImplementedException();
  }

  @Override
  public InstrumentDefinition<?> visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final Currency currency = security.getCurrency();
    final ZonedDateTime startDate = security.getStartDate();
    final ZonedDateTime endDate = security.getMaturityDate();
    final ConventionBundle convention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_ZERO_DEPOSIT"));
    final DayCount daycount = convention.getDayCount();
    final InterestRate rate = new PeriodicInterestRate(security.getRate(), (int) security.getCompoundingPeriodsPerYear());
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, currency);
    return DepositZeroDefinition.from(currency, startDate, endDate, daycount, rate, calendar, daycount);
  }

}
