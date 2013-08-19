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
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Convert a FRA node into an Instrument definition.
 * The dates of the FRA are computed in the following way:
 * - The spot date is computed from the valuation date adding the "Settlement Days" (i.e. the number of business days) of the convention.
 * - The accrual start date is computed from the spot date adding the "FixingStart" of the node and using the business-day-convention, calendar and EOM of the convention.
 * - The accrual end date is computed from the spot date adding the "FixingEnd" of the node and using the business-day-convention, calendar and EOM of the convention.
 * The FRA notional is 1.
 */
public class FRANodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
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
  public FRANodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
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

  //TODO check calendars
  @Override
  public InstrumentDefinition<?> visitFRANode(final FRANode fraNode) {
    final Convention convention = _conventionSource.getConvention(fraNode.getConvention());
    final Double rate = _marketData.getDataPoint(_dataId);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    final Period startPeriod = fraNode.getFixingStart().getPeriod();
    final Period endPeriod = fraNode.getFixingEnd().getPeriod();
    //TODO probably need a specific FRA convention to hold the reset tenor
    final long months = endPeriod.toTotalMonths() - startPeriod.toTotalMonths();
    final Period indexTenor = Period.ofMonths((int) months);
    final IborIndexConvention indexConvention;
    if (convention instanceof IborIndexConvention) {
      indexConvention = (IborIndexConvention) convention;
    } else {
      if (convention == null) {
        throw new OpenGammaRuntimeException("Convention with id " + fraNode.getConvention() + " was null");
      }
      throw new OpenGammaRuntimeException("Could not handle underlying convention of type " + convention.getClass());
    }
    final Currency currency = indexConvention.getCurrency();
    final Calendar fixingCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
    final Calendar regionCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
    final int spotLag = indexConvention.getSettlementDays();
    final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
    final DayCount dayCount = indexConvention.getDayCount();
    final boolean eom = indexConvention.isIsEOM();
    final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLag, dayCount, businessDayConvention, eom, convention.getName());
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_valuationTime, spotLag, regionCalendar);
    final ZonedDateTime accrualStartDate = ScheduleCalculator.getAdjustedDate(spotDate, startPeriod, businessDayConvention, regionCalendar, eom);
    final ZonedDateTime accrualEndDate = ScheduleCalculator.getAdjustedDate(spotDate, endPeriod, businessDayConvention, regionCalendar, eom);
    return ForwardRateAgreementDefinition.from(accrualStartDate, accrualEndDate, 1, iborIndex, rate, fixingCalendar);
  }

}
