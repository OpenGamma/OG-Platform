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
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorCMSDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorIborDefinition;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurityVisitor;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CapFloorSecurityConverter implements CapFloorSecurityVisitor<InstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;

  public CapFloorSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  @Override
  public InstrumentDefinition<?> visitCapFloorSecurity(final CapFloorSecurity capFloorSecurity) {
    Validate.notNull(capFloorSecurity, "cap/floor security");
    final double notional = capFloorSecurity.getNotional();
    final ZonedDateTime settlementDate = capFloorSecurity.getStartDate(); 
    final ZonedDateTime maturityDate = capFloorSecurity.getMaturityDate();
    final double strike = capFloorSecurity.getStrike();
    final boolean isCap = capFloorSecurity.isCap();
    final ExternalId underlyingId = capFloorSecurity.getUnderlyingId();
    final Currency currency = capFloorSecurity.getCurrency();
    final Frequency tenorPayment = capFloorSecurity.getFrequency(); // Payment tenor
    final boolean isIbor = capFloorSecurity.isIbor();
    final boolean isPayer = capFloorSecurity.isPayer();
    final DayCount dayCount = capFloorSecurity.getDayCount();
    
    final ConventionBundle underlyingConvention = _conventionSource.getConventionBundle(underlyingId);
    if (underlyingConvention == null) {
      throw new OpenGammaRuntimeException("Could not get underlying convention for " + currency);
    }
    final Calendar calendar = CalendarUtils.getCalendar(_holidaySource, currency);
    if (isIbor) { // Cap/floor on Ibor
      final IborIndex index = new IborIndex(currency, getTenor(underlyingConvention.getFrequency()), underlyingConvention.getSettlementDays(), calendar,
          underlyingConvention.getDayCount(), underlyingConvention.getBusinessDayConvention(), underlyingConvention.isEOMConvention());
      return AnnuityCapFloorIborDefinition.from(settlementDate, maturityDate, notional, index, dayCount, getTenor(tenorPayment), isPayer, strike, isCap);
    } 
    // Cap/Floor on CMS    
    final ConventionBundle iborConvention = _conventionSource.getConventionBundle(underlyingConvention.getSwapFloatingLegInitialRate());
    final IborIndex iborIndex = new IborIndex(currency, iborConvention.getPeriod(), iborConvention.getSettlementDays(), calendar,
        iborConvention.getDayCount(), iborConvention.getBusinessDayConvention(), iborConvention.isEOMConvention());
    final Period tenorSwap = getUnderlyingTenor(underlyingId);
    final DayCount swapFixedLegDayCount = underlyingConvention.getSwapFixedLegDayCount();
    final Frequency swapFixedLegFrequency = underlyingConvention.getSwapFixedLegFrequency();
    final IndexSwap swapIndex = new IndexSwap(getTenor(swapFixedLegFrequency), swapFixedLegDayCount, iborIndex, tenorSwap); 
    return AnnuityCapFloorCMSDefinition.from(settlementDate, maturityDate, notional, swapIndex, getTenor(tenorPayment), dayCount, isPayer, strike, isCap);
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
          "Can only handle annual, semi-annual, quarterly and monthly frequencies for cap/floors");
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
