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
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorCMSSpreadDefinition;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.index.IborIndex;
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
    final double notional = capFloorCMSSpreadSecurity.getNotional();
    final ZonedDateTime settlementDate = capFloorCMSSpreadSecurity.getStartDate(); 
    final ZonedDateTime maturityDate = capFloorCMSSpreadSecurity.getMaturityDate(); 
    final double strike = capFloorCMSSpreadSecurity.getStrike();
    final boolean isCap = capFloorCMSSpreadSecurity.isCap();
    final ExternalId longId = capFloorCMSSpreadSecurity.getLongId();
    final ExternalId shortId = capFloorCMSSpreadSecurity.getShortId();
    final Currency currency = capFloorCMSSpreadSecurity.getCurrency();
    final Frequency tenor = capFloorCMSSpreadSecurity.getFrequency();
    final Period longLegPeriod = getUnderlyingTenor(longId);
    final Period shortLegPeriod = getUnderlyingTenor(shortId);
    final Calendar calendar = CalendarUtils.getCalendar(_holidaySource, currency);
    final ConventionBundle longConvention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_SWAP"));
    if (longConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + longId);
    }
    final IborIndex iborIndex1 = new IborIndex(currency, getTenor(tenor), longConvention.getSwapFloatingLegSettlementDays(), calendar,
        longConvention.getSwapFloatingLegDayCount(), longConvention.getSwapFloatingLegBusinessDayConvention(), longConvention.isEOMConvention());
    final Period period = getTenor(capFloorCMSSpreadSecurity.getFrequency());
    final IndexSwap cmsIndex1 = new IndexSwap(period, longConvention.getSwapFloatingLegDayCount(), iborIndex1, longLegPeriod);
    final ConventionBundle shortConvention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_SWAP"));
    if (shortConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + shortId);
    }
    final IborIndex iborIndex2 = new IborIndex(currency, getTenor(tenor), shortConvention.getSwapFloatingLegSettlementDays(), calendar,
        shortConvention.getSwapFloatingLegDayCount(), shortConvention.getSwapFloatingLegBusinessDayConvention(), shortConvention.isEOMConvention());
    final IndexSwap cmsIndex2 = new IndexSwap(period, shortConvention.getSwapFloatingLegDayCount(), iborIndex2, shortLegPeriod);
    final boolean isPayer = capFloorCMSSpreadSecurity.isPayer();
    final DayCount dayCount = capFloorCMSSpreadSecurity.getDayCount();
    final Period paymentPeriod = getTenor(capFloorCMSSpreadSecurity.getFrequency());
    return AnnuityCapFloorCMSSpreadDefinition.from(settlementDate, maturityDate, notional, cmsIndex1, cmsIndex2, paymentPeriod, dayCount, isPayer, strike, isCap);
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
      throw new OpenGammaRuntimeException(
          "Can only handle annual, semi-annual, quarterly and monthly frequencies for cap/floor CMS spreads");
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
