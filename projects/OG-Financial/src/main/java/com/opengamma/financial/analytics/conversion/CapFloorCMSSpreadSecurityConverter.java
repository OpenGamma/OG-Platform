/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.apache.commons.lang.Validate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CapFloorCMSSpreadSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public CapFloorCMSSpreadSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public InstrumentDefinition<?> visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity capFloorCMSSpreadSecurity) {
    Validate.notNull(capFloorCMSSpreadSecurity, "cap/floor security");
    final ZonedDateTime startDate = capFloorCMSSpreadSecurity.getStartDate();
    final ZonedDateTime endDate = capFloorCMSSpreadSecurity.getMaturityDate();
    final double notional = capFloorCMSSpreadSecurity.getNotional();
    final Currency currency = capFloorCMSSpreadSecurity.getCurrency();
    final Frequency payFreq = capFloorCMSSpreadSecurity.getFrequency();
    // FIXME: convert frequency to period in a better way
    final Period tenorPayment = getTenor(payFreq);
    final ExternalId[] swapIndexId = new ExternalId[2];
    swapIndexId[0] = capFloorCMSSpreadSecurity.getLongId();
    swapIndexId[1] = capFloorCMSSpreadSecurity.getShortId();
    final ConventionBundle[] swapIndexConvention = new ConventionBundle[2];
    final ConventionBundle[] iborIndexConvention = new ConventionBundle[2];
    for (int loopindex = 0; loopindex < 2; loopindex++) {
      swapIndexConvention[loopindex] = _conventionSource.getConventionBundle(swapIndexId[loopindex]);
      if (swapIndexConvention[loopindex] == null) {
        throw new OpenGammaRuntimeException("Could not get swap index convention for " + swapIndexId[loopindex].toString());
      }
      iborIndexConvention[loopindex] = _conventionSource.getConventionBundle(swapIndexConvention[loopindex].getSwapFloatingLegInitialRate());
      if (iborIndexConvention[loopindex] == null) {
        throw new OpenGammaRuntimeException("Could not get ibor index convention for " + swapIndexConvention[loopindex].getSwapFloatingLegInitialRate());
      }
    }
    final ExternalId regionId = swapIndexConvention[0].getSwapFloatingLegRegion();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final IborIndex[] iborIndex = new IborIndex[2];
    final IndexSwap[] swapIndex = new IndexSwap[2];
    for (int loopindex = 0; loopindex < 2; loopindex++) {
      iborIndex[loopindex] = new IborIndex(currency, tenorPayment, iborIndexConvention[loopindex].getSettlementDays(), iborIndexConvention[loopindex].getDayCount(),
          iborIndexConvention[loopindex].getBusinessDayConvention(), iborIndexConvention[loopindex].isEOMConvention());
      final Period fixedLegPaymentPeriod = getTenor(swapIndexConvention[loopindex].getSwapFixedLegFrequency());
      swapIndex[loopindex] = new IndexSwap(fixedLegPaymentPeriod, swapIndexConvention[loopindex].getSwapFixedLegDayCount(), iborIndex[loopindex], swapIndexConvention[loopindex].getPeriod(), calendar);
    }
    return AnnuityCapFloorCMSSpreadDefinition.from(startDate, endDate, notional, swapIndex[0], swapIndex[1], tenorPayment, capFloorCMSSpreadSecurity.getDayCount(),
        capFloorCMSSpreadSecurity.isPayer(), capFloorCMSSpreadSecurity.getStrike(), capFloorCMSSpreadSecurity.isCap(), calendar, calendar);
  }

  // FIXME: convert frequency to period in a better way
  private Period getTenor(final Frequency freq) {
    Period tenor;
    if (Frequency.ANNUAL_NAME.equals(freq.getName())) {
      tenor = Period.ofMonths(12);
    } else if (Frequency.SEMI_ANNUAL_NAME.equals(freq.getName())) {
      tenor = Period.ofMonths(6);
    } else if (Frequency.QUARTERLY_NAME.equals(freq.getName())) {
      tenor = Period.ofMonths(3);
    } else if (Frequency.MONTHLY_NAME.equals(freq.getName())) {
      tenor = Period.ofMonths(1);
    } else {
      throw new OpenGammaRuntimeException("Can only handle annual, semi-annual, quarterly and monthly frequencies for cap/floor CMS spreads");
    }
    return tenor;
  }

}
