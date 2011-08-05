/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.capfloor;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.analytics.fixedincome.CalendarUtil;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurityVisitor;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CapFloorCMSSpreadSecurityConverter implements CapFloorCMSSpreadSecurityVisitor<FixedIncomeInstrumentConverter<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;

  public CapFloorCMSSpreadSecurityConverter(HolidaySource holidaySource, ConventionBundleSource conventionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }
  
  //TODO too much is being done in this method - should be in the constructor
  @Override
  public FixedIncomeInstrumentConverter<?> visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity capFloorCMSSpreadSecurity) {
    Validate.notNull(capFloorCMSSpreadSecurity, "cap/floor security");
    double notional = capFloorCMSSpreadSecurity.getNotional();
    ZonedDateTime paymentDate = capFloorCMSSpreadSecurity.getMaturityDate(); //TODO check this
    ZonedDateTime accrualStartDate = capFloorCMSSpreadSecurity.getStartDate(); //TODO check this 
    ZonedDateTime accrualEndDate = capFloorCMSSpreadSecurity.getMaturityDate(); //TODO check this
    double accrualFactor = capFloorCMSSpreadSecurity.getDayCount().getDayCountFraction(accrualStartDate, accrualEndDate); 
    double strike = capFloorCMSSpreadSecurity.getStrike();
    boolean isCap = capFloorCMSSpreadSecurity.getIsCap();
    ExternalId longId = capFloorCMSSpreadSecurity.getLongIdentifier();
    ExternalId shortId = capFloorCMSSpreadSecurity.getShortIdentifier();
    Currency currency = capFloorCMSSpreadSecurity.getCurrency();
    Frequency tenor = capFloorCMSSpreadSecurity.getFrequency();
    final Calendar calendar = CalendarUtil.getCalendar(_holidaySource, currency);
    final ConventionBundle longConvention = _conventionSource.getConventionBundle(longId);
    if (longConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + longId);
    }
    final IborIndex iborIndex1 = new IborIndex(currency, getTenor(tenor), longConvention.getSettlementDays(), calendar,
        longConvention.getDayCount(), longConvention.getBusinessDayConvention(), longConvention.isEOMConvention());
    CMSIndex cmsIndex1 = new CMSIndex(longConvention.getPeriod(), longConvention.getDayCount(), iborIndex1, getTenor(tenor));
    final ConventionBundle shortConvention = _conventionSource.getConventionBundle(longId);
    if (shortConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + shortId);
    }
    final IborIndex iborIndex2 = new IborIndex(currency, getTenor(tenor), shortConvention.getSettlementDays(), calendar,
        shortConvention.getDayCount(), shortConvention.getBusinessDayConvention(), shortConvention.isEOMConvention());
    CMSIndex cmsIndex2 = new CMSIndex(shortConvention.getPeriod(), shortConvention.getDayCount(), iborIndex2, getTenor(tenor));
    return CapFloorCMSSpreadDefinition.from(paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, cmsIndex1, cmsIndex2, strike, isCap);
  }
  
  // FIXME: convert frequency to period in a better way
  private Period getTenor(final Frequency freq) {
    Period tenor;
    if (freq.getConventionName() == Frequency.ANNUAL_NAME) {
      tenor = Period.ofMonths(12);
    } else if (freq.getConventionName() == Frequency.SEMI_ANNUAL_NAME) {
      tenor = Period.ofMonths(6);
    } else if (freq.getConventionName() == Frequency.QUARTERLY_NAME) {
      tenor = Period.ofMonths(3);
    } else if (freq.getConventionName() == Frequency.MONTHLY_NAME) {
      tenor = Period.ofMonths(1);
    } else {
      throw new OpenGammaRuntimeException(
          "Can only handle annual, semi-annual, quarterly and monthly frequencies for cap/floor CMS spreads");
    }
    return tenor;
  }
}
