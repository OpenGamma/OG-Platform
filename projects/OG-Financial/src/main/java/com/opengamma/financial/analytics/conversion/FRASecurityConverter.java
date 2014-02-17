/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getConventionName;

import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class FRASecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final ConventionSource _conventionSource;

  public FRASecurityConverter(final HolidaySource holidaySource, final RegionSource regionSource, final ConventionSource conventionSource) {
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
    final ZonedDateTime accrualStartDate = security.getStartDate();
    final ZonedDateTime accrualEndDate = security.getEndDate();
    final long months = getMonths(accrualStartDate, accrualEndDate);
    final String tenorString = months + "M";
    final VanillaIborLegConvention vanillaIborLegConvention = getIborLegConvention(currency, tenorString);
    final IborIndexConvention iborIndexConvention = _conventionSource.getSingle(vanillaIborLegConvention.getIborIndexConvention(), IborIndexConvention.class);
    final double notional = security.getAmount();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, ExternalSchemes.currencyRegionId(currency)); //TODO exchange region?
    final int spotLag = iborIndexConvention.getSettlementDays();
    final Period indexTenor = Period.ofMonths((int) months);
    final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, iborIndexConvention.getDayCount(), iborIndexConvention.getBusinessDayConvention(),
        iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
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
    final IborIndex iborIndex = new IborIndex(
        currency,
        period,
        security.getFixingLag(),
        security.getDayCount(),
        security.getFixingBusinessDayConvention(),
        false, // eom - this should come from the convention source or security, PLAT-5764
        security.getUnderlyingId().getValue());
    return ForwardRateAgreementDefinition.from(accrualStartDate, accrualEndDate, notional, iborIndex, security.getRate(), calendar);
  }

  //TODO shouldn't have to get the FRA tenor this way
  private static long getMonths(final ZonedDateTime accrualStart, final ZonedDateTime accrualEnd) {
    Period diff = Period.between(accrualStart.toLocalDate(), accrualEnd.toLocalDate());
    return diff.getMonths() + (diff.getDays() > 15 ? 1 : 0);
  }

  private VanillaIborLegConvention getIborLegConvention(final Currency currency, final String tenorString) {
    String vanillaIborLegConventionName = getConventionName(currency, tenorString, IRS_IBOR_LEG);
    return _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, vanillaIborLegConventionName), VanillaIborLegConvention.class);
  }

}
