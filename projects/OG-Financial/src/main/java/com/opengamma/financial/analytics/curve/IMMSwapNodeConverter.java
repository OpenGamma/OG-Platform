/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.ircurve.strips.IMMSwapNode;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IMMSwapConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class IMMSwapNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** TODO this should not be hard-coded */
  private static final TemporalAdjuster THIRD_MONDAY_ADJUSTER = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY);
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
  public IMMSwapNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
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
  public InstrumentDefinition<?> visitIMMSwapNode(final IMMSwapNode immSwapNode) {
    final IMMSwapConvention swapConvention = _conventionSource.getConvention(IMMSwapConvention.class, immSwapNode.getSwapConvention());
    if (swapConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + immSwapNode.getSwapConvention() + " was null");
    }
    final Convention payLegConvention = _conventionSource.getConvention(swapConvention.getPayLegConvention());
    if (payLegConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + swapConvention.getPayLegConvention() + " was null");
    }
    final Convention receiveLegConvention = _conventionSource.getConvention(swapConvention.getReceiveLegConvention());
    if (receiveLegConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + swapConvention.getReceiveLegConvention() + " was null");
    }
    //TODO should use date adjuster convention from IMMSwapConvention
    final ZonedDateTime unadjustedStartDate = _valuationTime.plus(immSwapNode.getStartTenor().getPeriod());
    final ZonedDateTime startDate = unadjustedStartDate.plusMonths(3 * immSwapNode.getImmDateStartNumber()).with(THIRD_MONDAY_ADJUSTER);
    final Period startTenor = Period.ofDays((int) _valuationTime.periodUntil(startDate, ChronoUnit.DAYS));
    final Period maturityTenor = startTenor.plusMonths(3 * immSwapNode.getImmDateEndNumber());
    return NodeConverterUtils.getSwapDefinition(payLegConvention, receiveLegConvention, startTenor, maturityTenor, _regionSource,
        _holidaySource, _conventionSource, _marketData, _dataId, _valuationTime);
  }
}
