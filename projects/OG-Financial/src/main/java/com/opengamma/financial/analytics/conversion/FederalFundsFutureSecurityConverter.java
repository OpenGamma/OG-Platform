/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.FED_FUNDS_FUTURE;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.ConverterUtils;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts Federal funds future securities into the definition form used by the analytics library.
 */
public class FederalFundsFutureSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The security source */
  private final SecuritySource _securitySource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The convention bundle source */
  private final ConventionSource _conventionSource;
  /** The region source */
  private final RegionSource _regionSource;

  /**
   * @param securitySource The security source, not null
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param regionSource The region source, not null
   */
  public FederalFundsFutureSecurityConverter(final SecuritySource securitySource, final HolidaySource holidaySource, final ConventionSource conventionSource, final RegionSource regionSource) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _securitySource = securitySource;
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  /**
   * @param security The security.
   * @return Security definition.
   * @deprecated Use InterestRateFutureSecurityConverter.
   */
  @Override
  @Deprecated
  // [PLAT-5535] This method will be removed soon.
  public FederalFundsFutureSecurityDefinition visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ZonedDateTime lastTradeDate = security.getExpiry().getExpiry();
    final FederalFundsFutureConvention convention = _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, FED_FUNDS_FUTURE), FederalFundsFutureConvention.class);
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, convention.getExchangeCalendar());
    final OvernightIndex index = (OvernightIndex) _securitySource.getSingle(convention.getIndexConvention().toBundle());
    final OvernightIndexConvention indexConvention = _conventionSource.getSingle(index.getConventionId(), OvernightIndexConvention.class);
    final IndexON indexON = ConverterUtils.indexON(index.getName(), indexConvention);
    final double paymentAccrualFactor = 1 / 12.; //TODO should not be hard-coded
    final double notional = security.getUnitAmount() / paymentAccrualFactor;
    return FederalFundsFutureSecurityDefinition.from(lastTradeDate, indexON, notional, paymentAccrualFactor, security.getName(), calendar);
  }

  @Override
  public FederalFundsFutureSecurityDefinition visitFederalFundsFutureSecurity(FederalFundsFutureSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ZonedDateTime lastTradeDate = security.getExpiry().getExpiry().withHour(0);
    final FederalFundsFutureConvention convention = _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, FED_FUNDS_FUTURE), FederalFundsFutureConvention.class);
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, convention.getExchangeCalendar());
    final OvernightIndex index = (OvernightIndex) _securitySource.getSingle(convention.getIndexConvention().toBundle());
    final OvernightIndexConvention indexConvention = _conventionSource.getSingle(index.getConventionId(), OvernightIndexConvention.class);
    final IndexON indexON = ConverterUtils.indexON(index.getName(), indexConvention);
    final double paymentAccrualFactor = 1 / 12.; //TODO should not be hard-coded
    final double notional = security.getUnitAmount() / paymentAccrualFactor;
    return FederalFundsFutureSecurityDefinition.from(lastTradeDate, indexON, notional, paymentAccrualFactor, security.getName(), calendar);
  }

}
