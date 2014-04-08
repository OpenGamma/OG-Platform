/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.volatilityswap.VolatilitySwapDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.financial.security.swap.VolatilitySwapSecurity;
import com.opengamma.financial.security.swap.VolatilitySwapType;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts {@link VolatilitySwapSecurity} classes to {@link VolatilitySwapSecurity}, which is required
 * for use in the analytics library.
 */
public class VolatilitySwapSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The holiday source */
  private final HolidaySource _holidaySource;

  /**
   * @param holidaySource The holiday source, not null
   */
  public VolatilitySwapSecurityConverter(final HolidaySource holidaySource) {
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    _holidaySource = holidaySource;
  }

  @Override
  public VolatilitySwapDefinition visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
    final Currency currency = security.getCurrency();
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, currency);
    final Frequency frequency = security.getObservationFrequency();
    final PeriodFrequency periodFrequency;
    if (frequency instanceof PeriodFrequency) {
      periodFrequency = (PeriodFrequency) frequency;
    } else if (frequency instanceof SimpleFrequency) {
      periodFrequency = ((SimpleFrequency) frequency).toPeriodFrequency();
    } else {
      throw new OpenGammaRuntimeException("Can only handle PeriodFrequency and SimpleFrequency");
    }
    final double volStrike, volNotional;
    final VolatilitySwapType volatilitySwapType = security.getVolatilitySwapType();
    switch (volatilitySwapType) {
      case VEGA:
        throw new UnsupportedOperationException("TODO");
      case VOLATILITY:
        volStrike = security.getStrike();
        volNotional = security.getNotional();
        break;
      default:
        throw new UnsupportedOperationException("Cannot handle VolatilitySwapType " + volatilitySwapType);
    }
    return new VolatilitySwapDefinition(currency, volStrike, volNotional, security.getFirstObservationDate(),
        security.getLastObservationDate(), security.getSettlementDate(), security.getMaturityDate(), periodFrequency, security.getAnnualizationFactor(), calendar);
  }
}
