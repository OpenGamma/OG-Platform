/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts interest rate future securities into the definition form used by the analytics library
 * 
 * @deprecated Use the converter that does not reference a {@link ConventionBundleSource}
 */
@Deprecated
public class InterestRateFutureSecurityConverterDeprecated extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The convention bundle source */
  private final ConventionBundleSource _conventionSource;
  /** The region source */
  private final RegionSource _regionSource;

  /** The version/correction timestamp */

  /**
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param regionSource The region source, not null
   */
  public InterestRateFutureSecurityConverterDeprecated(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource) {
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
    ConventionBundle iborConvention = _conventionSource.getConventionBundle(security.getUnderlyingId());
    if (iborConvention == null) {
      iborConvention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_IR_FUTURE"));
      if (iborConvention == null) {
        throw new OpenGammaRuntimeException("Could not get ibor convention for " + currency.getCode());
      }
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, ExternalSchemes.currencyRegionId(currency));
    final double paymentAccrualFactor = getAccrualFactor(iborConvention.getPeriod());
    final IborIndex iborIndex = new IborIndex(currency, iborConvention.getPeriod(), iborConvention.getSettlementDays(), iborConvention.getDayCount(),
        iborConvention.getBusinessDayConvention(), iborConvention.isEOMConvention());
    final double notional = security.getUnitAmount() / paymentAccrualFactor;
    return new InterestRateFutureSecurityDefinition(lastTradeDate, iborIndex, notional, paymentAccrualFactor, security.getName(), calendar);
  }

  /**
   * Returns the conventional accrual factor for a given period. For 3 months, the factor is 0.25; for 1 month, the factor is 1/12.
   * 
   * @param period The period.
   * @return The accrual factor.
   */
  private static double getAccrualFactor(final Period period) {
    final long nMonths = period.toTotalMonths();
    return nMonths / 12.0d;
  }

}
