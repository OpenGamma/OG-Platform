/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.FRASecurityVisitor;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class FRASecurityConverter implements FRASecurityVisitor<FixedIncomeInstrumentConverter<?>> {
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final ConventionBundleSource _conventionSource;

  public FRASecurityConverter(final HolidaySource holidaySource, final RegionSource regionSource, final ConventionBundleSource conventionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(regionSource, "region source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _conventionSource = conventionSource;
  }

  @Override
  public ForwardRateAgreementDefinition visitFRASecurity(final FRASecurity security) {
    Validate.notNull(security, "security");
    final Currency currency = security.getCurrency();
    final String currencyCode = currency.getCode();
    final ConventionBundle fraConvention = _conventionSource.getConventionBundle(Identifier.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currencyCode + "_FRA"));
    //ConventionBundle iborConvention; //TODO use this once there's a reference to the index in the security definition
    final ZonedDateTime accrualStartDate = security.getStartDate();
    final ZonedDateTime accrualEndDate = security.getEndDate();
    final double notional = security.getAmount();
    final Calendar calendar = CalendarUtil.getCalendar(_regionSource, _holidaySource, RegionUtils.currencyRegionId(currency)); //TODO exchange region?
    final IborIndex iborIndex = new IborIndex(currency, getIndexTenor(accrualStartDate, accrualEndDate), fraConvention.getSettlementDays(),
        calendar, fraConvention.getDayCount(), fraConvention.getBusinessDayConvention(),
        fraConvention.isEOMConvention());
    final double rate = security.getRate() / 100; //TODO should not be done here
    return ForwardRateAgreementDefinition.from(accrualStartDate, accrualEndDate, notional, iborIndex, rate);
  }

  //TODO this is horrible but will have to do until we get proper information about the underlying index  
  private Period getIndexTenor(final ZonedDateTime startDate, final ZonedDateTime endDate) {
    final double period = DateUtil.getDifferenceInYears(startDate, endDate);
    if (period < 0.1) {
      return Period.ofMonths(1);
    } else if (period < 0.29) {
      return Period.ofMonths(3);
    } else if (period < 0.6) {
      return Period.ofMonths(6);
    } else if (period < 0.8) {
      return Period.ofMonths(9);
    } else if (period < 1.2) {
      return Period.ofMonths(12);
    }
    throw new OpenGammaRuntimeException("Haven't implemented for FRAs longer than 1 year (e.g. 24x48)");
  }
}
