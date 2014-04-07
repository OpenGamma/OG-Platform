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
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts cap/floors from {@link CapFloorSecurity} to the {@link InstrumentDefinition}s.
 * @deprecated Replaced by {@link CapFloorSecurityConverter}, which does not use curve name information
 */
@Deprecated
public class CapFloorSecurityConverterDeprecated extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public CapFloorSecurityConverterDeprecated(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public InstrumentDefinition<?> visitCapFloorSecurity(final CapFloorSecurity capFloorSecurity) {
    ArgumentChecker.notNull(capFloorSecurity, "cap/floor security");
    final ZonedDateTime startDate = capFloorSecurity.getStartDate();
    final ZonedDateTime endDate = capFloorSecurity.getMaturityDate();
    final double notional = capFloorSecurity.getNotional();
    final Currency currency = capFloorSecurity.getCurrency();
    final Frequency payFreq = capFloorSecurity.getFrequency();
    // FIXME: convert frequency to period in a better way
    final Period tenorPayment = ConversionUtils.getTenor(payFreq);
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
      final IborIndex index = new IborIndex(currency, iborIndexConvention.getPeriod(), iborIndexConvention.getSettlementDays(), iborIndexConvention.getDayCount(),
          iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isEOMConvention(), "Ibor");
      return AnnuityCapFloorIborDefinition.from(startDate, endDate, notional, index, capFloorSecurity.getDayCount(), tenorPayment, capFloorSecurity.isPayer(), capFloorSecurity.getStrike(),
          capFloorSecurity.isCap(), calendar);
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
    final IborIndex iborIndex = new IborIndex(currency, tenorPayment, iborIndexConvention.getSettlementDays(), iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isEOMConvention());
    final Period fixedLegPaymentPeriod = ConversionUtils.getTenor(swapIndexConvention.getSwapFixedLegFrequency());
    final IndexSwap swapIndex = new IndexSwap(fixedLegPaymentPeriod, swapIndexConvention.getSwapFixedLegDayCount(), iborIndex, swapIndexConvention.getPeriod(), calendar);
    return AnnuityCapFloorCMSDefinition.from(startDate, endDate, notional, swapIndex, tenorPayment, capFloorSecurity.getDayCount(), capFloorSecurity.isPayer(), capFloorSecurity.getStrike(),
        capFloorSecurity.isCap(), calendar);
  }

}
