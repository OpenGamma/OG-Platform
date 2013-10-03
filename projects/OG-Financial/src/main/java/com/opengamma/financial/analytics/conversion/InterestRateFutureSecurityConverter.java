/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.QUARTERLY;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.STIR_FUTURES;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getConventionName;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts interest rate future securities into the definition form used by the analytics library
 */
public class InterestRateFutureSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
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
  public InterestRateFutureSecurityConverter(final HolidaySource holidaySource, final ConventionSource conventionSource, final RegionSource regionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public InterestRateFutureSecurityDefinition visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ZonedDateTime lastTradeDate = security.getExpiry().getExpiry();
    final Currency currency = security.getCurrency();
    final String conventionName = getConventionName(currency, STIR_FUTURES + QUARTERLY);
    final InterestRateFutureConvention convention = _conventionSource.getConvention(InterestRateFutureConvention.class, ExternalId.of(SCHEME_NAME, conventionName)); // PLAT-4532
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get interest rate future convention with id " + ExternalId.of(SCHEME_NAME, conventionName));
    }
    final IborIndexConvention iborIndexConvention = _conventionSource.getConvention(IborIndexConvention.class, convention.getIndexConvention());
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, convention.getExchangeCalendar());
    final Period period = Period.ofMonths(3); //TODO
    final double paymentAccrualFactor = getAccrualFactor(period);
    final int spotLag = iborIndexConvention.getSettlementDays();
    final IborIndex iborIndex = new IborIndex(currency, period, spotLag, iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
    final double notional = security.getUnitAmount() / paymentAccrualFactor;
    return new InterestRateFutureSecurityDefinition(lastTradeDate, iborIndex, notional, paymentAccrualFactor, security.getName(), calendar);
  }

  /**
   * Returns the conventional accrual factor for a given period. For 3 months, the factor is 0.25; for 1 month, the factor is 1/12.
   * @param period The period.
   * @return The accrual factor.
   */
  private static double getAccrualFactor(final Period period) {
    final long nMonths = period.toTotalMonths();
    return nMonths / 12.0d;
  }

}
