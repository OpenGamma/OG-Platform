/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IBOR;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.SWAP_INDEX;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.TENOR_STR_3M;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getConventionName;

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
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class CapFloorSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionSource _conventionSource;
  private final RegionSource _regionSource;

  public CapFloorSecurityConverter(final HolidaySource holidaySource, final ConventionSource conventionSource, final RegionSource regionSource) {
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
    final Period tenorPayment = getTenor(payFreq);
    final boolean isIbor = capFloorSecurity.isIbor();
    final String iborConventionName = getConventionName(currency, IBOR);
    final IborIndexConvention iborIndexConvention = _conventionSource.getConvention(IborIndexConvention.class, ExternalId.of(SCHEME_NAME, iborConventionName));
    if (iborIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get ibor convention called " + iborConventionName);
    }
    final Frequency freqIbor = capFloorSecurity.getFrequency();
    final Period iborTenor = getTenor(freqIbor);
    final int spotLag = iborIndexConvention.getSettlementDays();
    final IborIndex iborIndex = new IborIndex(currency, iborTenor, spotLag, iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
    final ExternalId regionId = iborIndexConvention.getRegionCalendar();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final IborIndex index = new IborIndex(currency, iborTenor, iborIndexConvention.getSettlementDays(), iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
    if (isIbor) { // Cap/floor on Ibor
      final String vanillaIborLegConventionName = getConventionName(Currency.USD, TENOR_STR_3M, IRS_IBOR_LEG);
      final VanillaIborLegConvention vanillaIborLegConvention =
          _conventionSource.getConvention(VanillaIborLegConvention.class, ExternalId.of(SCHEME_NAME, vanillaIborLegConventionName));
      if (vanillaIborLegConvention == null) {
        throw new OpenGammaRuntimeException("Could not get vanilla ibor leg convention called " + vanillaIborLegConventionName);
      }
      return AnnuityCapFloorIborDefinition.from(startDate, endDate, notional, index, capFloorSecurity.getDayCount(), tenorPayment, capFloorSecurity.isPayer(), capFloorSecurity.getStrike(),
          capFloorSecurity.isCap(), calendar);
    }
    // Cap/floor on CMS
    final String swapIndexConventionName = getConventionName(currency, SWAP_INDEX);
    final SwapIndexConvention swapIndexConvention = _conventionSource.getConvention(SwapIndexConvention.class, ExternalId.of(SCHEME_NAME, swapIndexConventionName));
    if (swapIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get swap index convention called " + capFloorSecurity.getUnderlyingId().toString());
    }
    final SwapConvention swapConvention = _conventionSource.getConvention(SwapConvention.class, swapIndexConvention.getSwapConvention());
    final IndexSwap swapIndex = getSwapIndex(swapConvention, iborIndex);
    return AnnuityCapFloorCMSDefinition.from(startDate, endDate, notional, swapIndex, tenorPayment, capFloorSecurity.getDayCount(), capFloorSecurity.isPayer(), capFloorSecurity.getStrike(),
        capFloorSecurity.isCap(), calendar);
  }

  // FIXME: convert frequency to period in a better way
  private static Period getTenor(final Frequency freq) {
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

  private IndexSwap getSwapIndex(final SwapConvention swapConvention, final IborIndex iborIndex) {
    SwapFixedLegConvention fixedConvention;
    final Convention payLegConvention = _conventionSource.getConvention(swapConvention.getPayLegConvention());
    final Convention receiveLegConvention = _conventionSource.getConvention(swapConvention.getReceiveLegConvention());
    if (payLegConvention instanceof SwapFixedLegConvention) {
      fixedConvention = (SwapFixedLegConvention) payLegConvention;
    } else if (receiveLegConvention instanceof SwapFixedLegConvention) {
      fixedConvention = (SwapFixedLegConvention) receiveLegConvention;
    } else {
      throw new OpenGammaRuntimeException("Could not get fixed convention");
    }
    final Period fixedLegPaymentPeriod = fixedConvention.getPaymentTenor().getPeriod();
    final ExternalId regionId = fixedConvention.getRegionCalendar();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    return new IndexSwap(fixedLegPaymentPeriod, fixedConvention.getDayCount(), iborIndex, fixedLegPaymentPeriod, calendar);
  }
}
