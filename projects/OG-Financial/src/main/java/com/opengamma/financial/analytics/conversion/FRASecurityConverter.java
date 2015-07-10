/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.ConverterUtils;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.calendar.Calendar;
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
  /** A security source. Used to retrieve Ibor index. */
  private final SecuritySource _securitySource;
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final ConventionSource _conventionSource;

  public FRASecurityConverter(final SecuritySource securitySource, final HolidaySource holidaySource, final RegionSource regionSource, final ConventionSource conventionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    _securitySource = securitySource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _conventionSource = conventionSource;
  }

  @Override
  public ForwardRateAgreementDefinition visitFRASecurity(final FRASecurity security) {
    ArgumentChecker.notNull(security, "security");
    final Security sec = _securitySource.getSingle(security.getUnderlyingId().toBundle()); 
    if (sec == null) {
      throw new OpenGammaRuntimeException("Ibor index with id " + security.getUnderlyingId() + " was null");
    }
    final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
    final IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
    final IborIndex iborIndex = ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
    final Currency currency = security.getCurrency();
    final ZonedDateTime accrualStartDate = security.getStartDate();
    final ZonedDateTime accrualEndDate = security.getEndDate();
    final double notional = security.getAmount();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, ExternalSchemes.currencyRegionId(currency)); //TODO exchange region?
    return ForwardRateAgreementDefinition.from(accrualStartDate, accrualEndDate, notional, iborIndex, security.getRate(), calendar);
  }

  @Override
  public ForwardRateAgreementDefinition visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ZonedDateTime accrualStartDate = security.getStartDate().atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime accrualEndDate = security.getEndDate().atStartOfDay(ZoneOffset.UTC);
    final double notional = security.getAmount();
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCalendars().toArray(new ExternalId[security.getCalendars().size()]));
    final Calendar paymentCalendar = security.getPaymentCalendars() != null ?
        new HolidaySourceCalendarAdapter(_holidaySource, security.getPaymentCalendars().toArray(new ExternalId[security.getPaymentCalendars().size()]))
        : calendar;

    ArgumentChecker.notNull(security, "security");
    final Security sec = _securitySource.getSingle(security.getUnderlyingId().toBundle());
    if (sec == null) {
      throw new OpenGammaRuntimeException("Ibor index with id " + security.getUnderlyingId() + " was null");
    }
    final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
    final IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(),
        IborIndexConvention.class);
    final IborIndex iborIndex = ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention,
        indexSecurity.getTenor());

    final LocalDate fixingDate = security.getFixingDate();

    if (fixingDate == null) {
      return ForwardRateAgreementDefinition.from(accrualStartDate, 
          accrualEndDate, 
          notional, 
          iborIndex,
          security.getRate(),
          calendar, 
          paymentCalendar);
    } else {
      return ForwardRateAgreementDefinition.from(accrualStartDate, 
          accrualEndDate, 
          notional,
          fixingDate.atStartOfDay(ZoneOffset.UTC),
          iborIndex,
          security.getRate(),
          calendar, 
          paymentCalendar);
    }
  }
}
