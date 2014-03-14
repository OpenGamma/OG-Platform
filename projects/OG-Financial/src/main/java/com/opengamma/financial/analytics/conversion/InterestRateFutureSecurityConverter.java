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
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.ConverterUtils;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts interest rate future securities into the definition form used by the analytics library
 */
public class InterestRateFutureSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** A security source. Used for the Ibor index. */
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
  public InterestRateFutureSecurityConverter(final SecuritySource securitySource, final HolidaySource holidaySource, 
      final ConventionSource conventionSource, final RegionSource regionSource) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _securitySource = securitySource;
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public InterestRateFutureSecurityDefinition visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final Security sec = _securitySource.getSingle(security.getUnderlyingId().toBundle()); 
    if (sec == null) {
      throw new OpenGammaRuntimeException("Ibor index with id " + security.getUnderlyingId() + " was null");
    }
    final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
    final IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
    final IborIndex iborIndex = ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
    final ZonedDateTime lastTradeDate = security.getExpiry().getExpiry();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
    final Period period = indexSecurity.getTenor().getPeriod();
    final double paymentAccrualFactor = getAccrualFactor(period);
    final double notional = security.getUnitAmount() / paymentAccrualFactor;
    return new InterestRateFutureSecurityDefinition(lastTradeDate, iborIndex, notional, paymentAccrualFactor, security.getName(), calendar);
  }

  /**
   * Returns the conventional accrual factor for a given period. For 3 months, the factor is 0.25; for 1 month, the factor is 1/12.
   * @param period The period.
   * @return The accrual factor.
   */
  private static double getAccrualFactor(final Period period) {
    long nMonths = period.toTotalMonths();
    if (nMonths == 0) {
      nMonths = Math.round(period.getDays() / 30.0d);
    }
    return nMonths / 12.0d;
  }

}
