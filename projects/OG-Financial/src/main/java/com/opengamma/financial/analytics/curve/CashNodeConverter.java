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
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
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
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Convert a cash node into an Instrument definition.
 * The dates of the deposits are computed in the following way: 
 * - The spot date is computed from the valuation date adding the "Settlement Days" (i.e. the number of business days) of the convention.
 * - The start date is computed from the spot date adding the "StartTenor" of the node and using the business-day-convention, calendar and EOM of the convention.
 * - The end date is computed from the start date adding the "MaturityTenor" of the node and using the business-day-convention, calendar and EOM of the convention.
 * The deposit notional is 1.
 */
public class CashNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The market data */
  private final SnapshotDataBundle _marketData;
  /** The market data id */
  private final ExternalId _dataId;
  /** The valuation time */
  private final ZonedDateTime _valuationTime;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   */
  public CashNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _marketData = marketData;
    _dataId = dataId;
    _valuationTime = valuationTime;
  }

  @Override
  public InstrumentDefinition<?> visitCashNode(final CashNode cashNode) {
    final Convention convention = _conventionSource.getConvention(cashNode.getConvention());
    if (convention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + cashNode.getConvention() + " was null");
    }
    final Double rate = _marketData.getDataPoint(_dataId);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
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
      final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_valuationTime, settlementDays, calendar);
      final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDate, startPeriod, businessDayConvention, calendar, isEOM);
      final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, maturityPeriod, businessDayConvention, calendar, isEOM);
      final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
      return new CashDefinition(currency, startDate, endDate, 1, rate, accrualFactor);
    } else if (convention instanceof IborIndexConvention) {
      final IborIndexConvention iborConvention = (IborIndexConvention) convention;
      final Currency currency = iborConvention.getCurrency();
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, iborConvention.getRegionCalendar());
      final BusinessDayConvention businessDayConvention = iborConvention.getBusinessDayConvention();
      final boolean isEOM = iborConvention.isIsEOM();
      final DayCount dayCount = iborConvention.getDayCount();
      final int settlementDays = iborConvention.getSettlementDays();
      final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_valuationTime, settlementDays, calendar);
      final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDate, startPeriod, businessDayConvention, calendar, isEOM);
      final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, maturityPeriod, businessDayConvention, calendar, isEOM);
      final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
      final int spotLag = iborConvention.getSettlementDays();
      final boolean eom = iborConvention.isIsEOM();
      final long months = maturityPeriod.toTotalMonths() - startPeriod.toTotalMonths();
      final Period indexTenor = Period.ofMonths((int) months);
      final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eom, convention.getName());
      return new DepositIborDefinition(currency, startDate, endDate, 1, rate, accrualFactor, iborIndex);
    } else {
      throw new OpenGammaRuntimeException("Could not handle convention of type " + convention.getClass());
    }
  }
}
