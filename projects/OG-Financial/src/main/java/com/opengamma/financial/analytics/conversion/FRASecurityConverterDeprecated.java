/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * @deprecated Use {@link FRASecurityConverter}. {@link ConventionBundleSource} should not be used,
 * as the conventions are not typed.
 */
@Deprecated
public class FRASecurityConverterDeprecated extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final ConventionBundleSource _conventionSource;

  public FRASecurityConverterDeprecated(final HolidaySource holidaySource, final RegionSource regionSource, final ConventionBundleSource conventionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _conventionSource = conventionSource;
  }

  @Override
  public ForwardRateAgreementDefinition visitFRASecurity(final FRASecurity security) {
    ArgumentChecker.notNull(security, "security");
    final Currency currency = security.getCurrency();
    final ConventionBundle fraConvention = _conventionSource.getConventionBundle(security.getUnderlyingId());
    if (fraConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + security.getUnderlyingId());
    }
    final ZonedDateTime accrualStartDate = security.getStartDate();
    final ZonedDateTime accrualEndDate = security.getEndDate();
    final double notional = security.getAmount();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, ExternalSchemes.currencyRegionId(currency)); //TODO exchange region?
    final IborIndex iborIndex = new IborIndex(currency, fraConvention.getPeriod(), fraConvention.getSettlementDays(), fraConvention.getDayCount(), fraConvention.getBusinessDayConvention(),
        fraConvention.isEOMConvention());
    return ForwardRateAgreementDefinition.from(accrualStartDate, accrualEndDate, notional, iborIndex, security.getRate(), calendar);
  }

  @Override
  public ForwardRateAgreementDefinition visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final Currency currency = security.getCurrency();
    final Period period = PeriodFrequency.of(security.getIndexFrequency().getName()).getPeriod();
    final ZonedDateTime accrualStartDate = security.getStartDate().atStartOfDay(ZoneId.systemDefault());
    final ZonedDateTime accrualEndDate = security.getEndDate().atStartOfDay(ZoneId.systemDefault());
    final double notional = security.getAmount();
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCalendars().toArray(new ExternalId[security.getCalendars().size()]));
    final IborIndex iborIndex = new IborIndex(currency, period, security.getFixingLag(), security.getDayCount(), security.getFixingBusinessDayConvention(), false);
    return ForwardRateAgreementDefinition.from(accrualStartDate, accrualEndDate, notional, iborIndex, security.getRate(), calendar);
  }

}
