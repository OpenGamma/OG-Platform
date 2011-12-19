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
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorCMSSpreadDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurityVisitor;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CapFloorCMSSpreadSecurityConverter implements CapFloorCMSSpreadSecurityVisitor<InstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;

  public CapFloorCMSSpreadSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  @Override
  public InstrumentDefinition<?> visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity capFloorCMSSpreadSecurity) {
    Validate.notNull(capFloorCMSSpreadSecurity, "cap/floor security");
    // Cap/Floor description
    final double notional = capFloorCMSSpreadSecurity.getNotional();
    final ZonedDateTime settlementDate = capFloorCMSSpreadSecurity.getStartDate();
    final ZonedDateTime maturityDate = capFloorCMSSpreadSecurity.getMaturityDate();
    final double strike = capFloorCMSSpreadSecurity.getStrike();
    final boolean isCap = capFloorCMSSpreadSecurity.isCap();
    final ExternalId longId = capFloorCMSSpreadSecurity.getLongId(); // First swap index (usually the longer tenor).
    final ExternalId shortId = capFloorCMSSpreadSecurity.getShortId(); // Second swap index (usually the shorter tenor).
    final Currency currency = capFloorCMSSpreadSecurity.getCurrency();
    final Period periodPayment = getTenor(capFloorCMSSpreadSecurity.getFrequency());
    final boolean isPayer = capFloorCMSSpreadSecurity.isPayer();
    final DayCount dayCount = capFloorCMSSpreadSecurity.getDayCount();
    final Calendar calendar = CalendarUtils.getCalendar(_holidaySource, currency);
    // Underlying
    final ConventionBundle swap1Convention = _conventionSource.getConventionBundle(longId);
    if (swap1Convention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + longId);
    }
    final ConventionBundle swap2Convention = _conventionSource.getConventionBundle(shortId);
    if (swap2Convention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + shortId);
    }
    final ConventionBundle ibor1Convention = _conventionSource.getConventionBundle(swap1Convention.getSwapFloatingLegInitialRate());
    final ConventionBundle ibor2Convention = _conventionSource.getConventionBundle(swap2Convention.getSwapFloatingLegInitialRate());
    final IborIndex ibor1Index = new IborIndex(currency, ibor1Convention.getPeriod(), ibor1Convention.getSettlementDays(), calendar, ibor1Convention.getDayCount(),
        ibor1Convention.getBusinessDayConvention(), ibor1Convention.isEOMConvention());
    final IborIndex ibor2Index = new IborIndex(currency, ibor2Convention.getPeriod(), ibor2Convention.getSettlementDays(), calendar, ibor2Convention.getDayCount(),
        ibor2Convention.getBusinessDayConvention(), ibor2Convention.isEOMConvention());
    final Period swap1Tenor = getUnderlyingTenor(longId);
    final DayCount swap1FixedLegDayCount = swap1Convention.getSwapFixedLegDayCount();
    final Frequency swap1FixedLegFrequency = swap1Convention.getSwapFixedLegFrequency();
    final IndexSwap swap1Index = new IndexSwap(getTenor(swap1FixedLegFrequency), swap1FixedLegDayCount, ibor1Index, swap1Tenor);
    final Period swap2Tenor = getUnderlyingTenor(shortId);
    final DayCount swap2FixedLegDayCount = swap2Convention.getSwapFixedLegDayCount();
    final Frequency swap2FixedLegFrequency = swap2Convention.getSwapFixedLegFrequency();
    final IndexSwap swap2Index = new IndexSwap(getTenor(swap2FixedLegFrequency), swap2FixedLegDayCount, ibor2Index, swap2Tenor);
    return AnnuityCapFloorCMSSpreadDefinition.from(settlementDate, maturityDate, notional, swap1Index, swap2Index, periodPayment, dayCount, isPayer, strike, isCap);
  }

  // FIXME: convert frequency to period in a better way
  private Period getTenor(final Frequency freq) {
    Period tenor;
    if (Frequency.ANNUAL_NAME.equals(freq.getConventionName())) {
      tenor = Period.ofMonths(12);
    } else if (Frequency.SEMI_ANNUAL_NAME.equals(freq.getConventionName())) {
      tenor = Period.ofMonths(6);
    } else if (Frequency.QUARTERLY_NAME.equals(freq.getConventionName())) {
      tenor = Period.ofMonths(3);
    } else if (Frequency.MONTHLY_NAME.equals(freq.getConventionName())) {
      tenor = Period.ofMonths(1);
    } else {
      throw new OpenGammaRuntimeException("Can only handle annual, semi-annual, quarterly and monthly frequencies for cap/floor CMS spreads");
    }
    return tenor;
  }

  //TODO this is horrible - we need to add fields to the security
  private Period getUnderlyingTenor(final ExternalId id) {
    if (id.getScheme().equals(SecurityUtils.BLOOMBERG_TICKER)) {
      final String bbgCode = id.getValue();
      final String[] noSuffix = bbgCode.split(" ");
      return Period.ofYears(Integer.parseInt(noSuffix[0].split("SW")[1]));
    }
    throw new OpenGammaRuntimeException("Cannot handle id");
  }
}
