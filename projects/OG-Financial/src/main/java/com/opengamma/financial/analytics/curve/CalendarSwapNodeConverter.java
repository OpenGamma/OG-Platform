/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.DataNotFoundException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.DateSet;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.CalendarSwapNode;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CalendarSwapNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
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
  /** The config query */
  private final ConfigSourceQuery<DateSet> _calendarQuery;

  /**
   * @param securitySource The security source, not null
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   * @param calendarQuery The calendar config query, not null.
   */
  public CalendarSwapNodeConverter(final SecuritySource securitySource, final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime, final ConfigSourceQuery<DateSet> calendarQuery) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    // ArgumentChecker.notNull(calendarQuery, "config source");
    _securitySource = securitySource;
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _marketData = marketData;
    _dataId = dataId;
    _valuationTime = valuationTime;
    _calendarQuery = calendarQuery;
  }

  @Override
  public InstrumentDefinition<?> visitCalendarSwapNode(final CalendarSwapNode calendarSwapNode) {
    final SwapConvention swapConvention = _conventionSource.getSingle(calendarSwapNode.getSwapConvention(), SwapConvention.class);
    final FinancialConvention payLegConvention = _conventionSource.getSingle(swapConvention.getPayLegConvention(), FinancialConvention.class);
    final FinancialConvention receiveLegConvention = _conventionSource.getSingle(swapConvention.getReceiveLegConvention(), FinancialConvention.class);
    final ZonedDateTime unadjustedStartDate = _valuationTime.plus(calendarSwapNode.getStartTenor().getPeriod());
    final DateSet calendar = _calendarQuery.get(calendarSwapNode.getDateSetName());
    if (calendar == null) {
      throw new DataNotFoundException("DateSet not found: " + calendarSwapNode.getDateSetName());
    }
    return NodeConverterUtils.getSwapCalendarDefinition(payLegConvention, receiveLegConvention, unadjustedStartDate, calendarSwapNode.getStartDateNumber(),
        calendarSwapNode.getEndDateNumber(), calendar,
        _regionSource, _holidaySource,
        _marketData, _dataId, _valuationTime);
  }
  
}
