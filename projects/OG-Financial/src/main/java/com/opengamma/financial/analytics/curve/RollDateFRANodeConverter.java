/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterFactory;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class RollDateFRANodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The security source */
  private final SecuritySource _securitySource;
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
   * @param securitySource The security source, not null
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   */
  public RollDateFRANodeConverter(final SecuritySource securitySource, final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    _securitySource = securitySource;
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _marketData = marketData;
    _dataId = dataId;
    _valuationTime = valuationTime;
  }

  @Override
  public InstrumentDefinition<?> visitRollDateFRANode(final RollDateFRANode rollDateFRANode) {
    final Double rate = _marketData.getDataPoint(_dataId);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    final RollDateFRAConvention convention = _conventionSource.getSingle(rollDateFRANode.getRollDateFRAConvention(), RollDateFRAConvention.class);
    final com.opengamma.financial.security.index.IborIndex indexSecurity = 
        (com.opengamma.financial.security.index.IborIndex) _securitySource.getSingle(convention.getIndexConvention().toBundle()); 
    final IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + convention.getIndexConvention() + " was null");
    }
    final IborIndex index = ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
    final Calendar fixingCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
    final RollDateAdjuster adjuster = RollDateAdjusterFactory.getAdjuster(convention.getRollDateConvention().getValue());
    final ZonedDateTime adjustedStartDate = ScheduleCalculator.getAdjustedDate(_valuationTime.plus(rollDateFRANode.getStartTenor().getPeriod()), 0, fixingCalendar);
    // Implementation note: Date adjustment to following
    ZonedDateTime immStartDate = RollDateAdjusterUtils.nthDate(adjustedStartDate, adjuster, rollDateFRANode.getRollDateStartNumber());
    ZonedDateTime immEndDate = RollDateAdjusterUtils.nthDate(immStartDate.plusDays(1), adjuster, rollDateFRANode.getRollDateEndNumber() - rollDateFRANode.getRollDateStartNumber());
    immStartDate = ScheduleCalculator.getAdjustedDate(immStartDate, 0, fixingCalendar);
    immEndDate = ScheduleCalculator.getAdjustedDate(immEndDate, 0, fixingCalendar);
    final Currency currency = indexConvention.getCurrency();
    final DayCount dayCount = indexConvention.getDayCount();
    final double accrualFactor = dayCount.getDayCountFraction(immStartDate, immEndDate);
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(immStartDate, -index.getSpotLag(), fixingCalendar);
    return new ForwardRateAgreementDefinition(currency, immStartDate, immStartDate, immEndDate, accrualFactor, 1, fixingDate, immStartDate, immEndDate, index, rate, fixingCalendar);
  }
  
}
