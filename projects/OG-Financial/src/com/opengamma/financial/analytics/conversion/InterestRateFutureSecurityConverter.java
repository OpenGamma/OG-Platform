/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterestRateFutureSecurityConverter extends AbstractFutureSecurityVisitor<InstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public InterestRateFutureSecurityConverter(final HolidaySource holidaySource,
      final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public InstrumentDefinition<?> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    Validate.notNull(security, "security");
    final ZonedDateTime lastTradeDate = security.getExpiry().getExpiry();
    final Currency currency = security.getCurrency();
    final ConventionBundle iborConvention = _conventionSource.getConventionBundle(security.getUnderlyingId());
    if (iborConvention == null) {
      throw new OpenGammaRuntimeException("Could not get ibor convention for " + currency.getCode());
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, RegionUtils.currencyRegionId(currency)); //TODO exchange region?
    final double paymentAccrualFactor = getAccrualFactor(iborConvention.getPeriod());
    final IborIndex iborIndex = new IborIndex(currency, iborConvention.getPeriod(), iborConvention.getSettlementDays(),
        calendar, iborConvention.getDayCount(), iborConvention.getBusinessDayConvention(),
        iborConvention.isEOMConvention());
    final double notional = security.getUnitAmount();
    final double referencePrice = 0.0; // TODO CASE - Future refactor - Confirm referencePrice
    return new InterestRateFutureDefinition(lastTradeDate, iborIndex, referencePrice, notional, paymentAccrualFactor);
  }

  private double getAccrualFactor(final Period period) {
    if (period.equals(Period.ofMonths(3))) {
      return 0.25;
    } else if (period.equals(Period.ofMonths(1))) {
      return 1. / 12;
    }
    throw new OpenGammaRuntimeException("Can only handle 1M and 3M interest rate futures");
  }
}
