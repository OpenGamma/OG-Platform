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
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
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
  private final RegionSource _regionSource;

  public CapFloorSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public InstrumentDefinition<?> visitCapFloorSecurity(final CapFloorSecurity capFloorSecurity) {
    Validate.notNull(capFloorSecurity, "cap/floor security");
    final ZonedDateTime startDate = capFloorSecurity.getStartDate();
    final ZonedDateTime endDate = capFloorSecurity.getMaturityDate();
    final double notional = capFloorSecurity.getNotional();
    final Currency currency = capFloorSecurity.getCurrency();
    final Frequency payFreq = capFloorSecurity.getFrequency();
    // FIXME: convert frequency to period in a better way
    final Period tenorPayment = getTenor(payFreq);
    final boolean isIbor = capFloorSecurity.isIbor();
    final ConventionBundle iborIndexConvention;
    final ExternalId regionId;
    if (isIbor) { // Cap/floor on Ibor
      iborIndexConvention = _conventionSource.getConventionBundle(capFloorSecurity.getUnderlyingId());
      if (iborIndexConvention == null) {
        throw new OpenGammaRuntimeException("Could not get ibor index convention for " + capFloorSecurity.getUnderlyingId());
      }
      regionId = iborIndexConvention.getRegion();
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
      final IborIndex index = new IborIndex(currency, iborIndexConvention.getPeriod(), iborIndexConvention.getSettlementDays(), calendar, iborIndexConvention.getDayCount(),
          iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isEOMConvention());
      return AnnuityCapFloorIborDefinition.from(startDate, endDate, notional, index, capFloorSecurity.getDayCount(), tenorPayment, capFloorSecurity.isPayer(), capFloorSecurity.getStrike(),
          capFloorSecurity.isCap());
    }
    // Cap/floor on CMS
    final ConventionBundle swapIndexConvention = _conventionSource.getConventionBundle(capFloorSecurity.getUnderlyingId());
    if (swapIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get swap index convention for " + capFloorSecurity.getUnderlyingId().toString());
    }
    iborIndexConvention = _conventionSource.getConventionBundle(swapIndexConvention.getSwapFloatingLegInitialRate());
    if (iborIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get ibor index convention for " + swapIndexConvention.getSwapFloatingLegInitialRate());
    }
    regionId = swapIndexConvention.getSwapFloatingLegRegion();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final IborIndex iborIndex = new IborIndex(currency, tenorPayment, iborIndexConvention.getSettlementDays(), calendar, iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isEOMConvention());
    final Period fixedLegPaymentPeriod = getTenor(swapIndexConvention.getSwapFixedLegFrequency());
    final IndexSwap swapIndex = new IndexSwap(fixedLegPaymentPeriod, swapIndexConvention.getSwapFixedLegDayCount(), iborIndex, swapIndexConvention.getPeriod());
    return AnnuityCapFloorCMSDefinition.from(startDate, endDate, notional, swapIndex, tenorPayment, capFloorSecurity.getDayCount(), capFloorSecurity.isPayer(), capFloorSecurity.getStrike(),
        capFloorSecurity.isCap());
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
      throw new OpenGammaRuntimeException("Can only handle annual, semi-annual, quarterly and monthly frequencies for cap/floors");
    }
    return tenor;
  }

}
