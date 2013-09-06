/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.FED_FUNDS_FUTURE;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.SCHEME_NAME;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts Federal funds future securities into the definition form used by the analytics library.
 */
public class FederalFundsFutureSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The convention bundle source */
  private final ConventionSource _conventionSource;
  /** The region source */
  private final RegionSource _regionSource;

  /**
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param regionSource The region source, not null
   */
  public FederalFundsFutureSecurityConverter(final HolidaySource holidaySource, final ConventionSource conventionSource, final RegionSource regionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public FederalFundsFutureSecurityDefinition visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ZonedDateTime lastTradeDate = security.getExpiry().getExpiry();
    final Currency currency = security.getCurrency();
    final FederalFundsFutureConvention convention = _conventionSource.getConvention(FederalFundsFutureConvention.class, ExternalId.of(SCHEME_NAME, FED_FUNDS_FUTURE));
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get interest rate future convention with id " + ExternalId.of(SCHEME_NAME, FED_FUNDS_FUTURE));
    }
    final OvernightIndexConvention overnightIndexConvention = _conventionSource.getConvention(OvernightIndexConvention.class, convention.getIndexConvention());
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, convention.getExchangeCalendar());
    final IndexON index = new IndexON(overnightIndexConvention.getName(), currency, overnightIndexConvention.getDayCount(), overnightIndexConvention.getPublicationLag());
    final double paymentAccrualFactor = 1 / 12.; //TODO should not be hard-coded
    final double notional = security.getUnitAmount() / paymentAccrualFactor;
    return FederalFundsFutureSecurityDefinition.from(lastTradeDate, index, notional, paymentAccrualFactor, security.getName(), calendar);
  }
  
  @Override
  public InstrumentDefinition<?> visitFederalFundsFutureSecurity(FederalFundsFutureSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ZonedDateTime lastTradeDate = security.getExpiry().getExpiry();
    final Currency currency = security.getCurrency();
    final FederalFundsFutureConvention convention = _conventionSource.getConvention(FederalFundsFutureConvention.class, ExternalId.of(SCHEME_NAME, FED_FUNDS_FUTURE));
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get interest rate future convention with id " + ExternalId.of(SCHEME_NAME, FED_FUNDS_FUTURE));
    }
    final OvernightIndexConvention overnightIndexConvention = _conventionSource.getConvention(OvernightIndexConvention.class, convention.getIndexConvention());
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, convention.getExchangeCalendar());
    final IndexON index = new IndexON(overnightIndexConvention.getName(), currency, overnightIndexConvention.getDayCount(), overnightIndexConvention.getPublicationLag());
    final double paymentAccrualFactor = 1 / 12.; //TODO should not be hard-coded
    final double notional = security.getUnitAmount() / paymentAccrualFactor;
    return FederalFundsFutureSecurityDefinition.from(lastTradeDate, index, notional, paymentAccrualFactor, security.getName(), calendar);
  }

}
