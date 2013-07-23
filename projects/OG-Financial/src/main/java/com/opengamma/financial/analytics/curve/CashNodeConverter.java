/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class CashNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The market data */
  private final Double _marketData;
  /** The valuation time */
  private final ZonedDateTime _now;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, may be null
   * @param now The valuation time, not null
   */
  public CashNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
      final Double marketData, final ZonedDateTime now) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _marketData = marketData;
    _now = now;
  }

  @Override
  public InstrumentDefinition<?> visitCashNode(final CashNode cashNode) {
    final Convention convention = _conventionSource.getConvention(cashNode.getConvention());
    if (convention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + cashNode.getConvention() + " was null");
    }
    final Period startPeriod = cashNode.getStartTenor().getPeriod();
    final Period maturityPeriod = cashNode.getMaturityTenor().getPeriod();
    if (convention instanceof DepositConvention) {
      final DepositConvention depositConvention = (DepositConvention) convention;
      final Currency currency = depositConvention.getCurrency();
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, depositConvention.getRegionCalendar());
      final BusinessDayConvention businessDayConvention = depositConvention.getBusinessDayConvention();
      final boolean isEOM = depositConvention.isIsEOM();
      final DayCount dayCount = depositConvention.getDayCount();
      final int settlementDays = depositConvention.getSettlementDays();
      final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_now, settlementDays, calendar);
      final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDate, startPeriod, businessDayConvention, calendar);
      final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, maturityPeriod, businessDayConvention, calendar, isEOM);
      final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
      return new CashDefinition(currency, startDate, endDate, 1, _marketData, accrualFactor);
    } else if (convention instanceof IborIndexConvention) {
      final IborIndexConvention iborConvention = (IborIndexConvention) convention;
      final Currency currency = iborConvention.getCurrency();
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, iborConvention.getRegionCalendar());
      final BusinessDayConvention businessDayConvention = iborConvention.getBusinessDayConvention();
      final boolean isEOM = iborConvention.isIsEOM();
      final DayCount dayCount = iborConvention.getDayCount();
      final int settlementDays = iborConvention.getSettlementDays();
      final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(_now, startPeriod.plusDays(settlementDays), businessDayConvention, calendar);
      final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, maturityPeriod, businessDayConvention, calendar, isEOM);
      final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
      final int spotLag = 0; //TODO
      final boolean eom = iborConvention.isIsEOM();
      final long months = maturityPeriod.toTotalMonths() - startPeriod.toTotalMonths();
      final Period indexTenor = Period.ofMonths((int) months);
      final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eom, convention.getName());
      return new DepositIborDefinition(currency, startDate, endDate, 1, _marketData, accrualFactor, iborIndex);
    } else {
      throw new OpenGammaRuntimeException("Could not handle convention of type " + convention.getClass());
    }
  }
}
