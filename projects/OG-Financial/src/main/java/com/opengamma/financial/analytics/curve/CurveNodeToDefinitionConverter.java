/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts {@link CurveNode}s into {@InstrumentDefinition}s.
 */
public class CurveNodeToDefinitionConverter {
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   */
  public CurveNodeToDefinitionConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
  }

  /**
   * Converts a {@link CurveNode} into an {@link InstrumentDefinition}, which will be used in curve construction.
   * @param node The curve node, not null
   * @param marketDataId The market data id, not null
   * @param now The curve construction time, not null
   * @param marketValues The market data values for these nodes, not null
   * @param timeSeries The historical time series needed for these nodes, not null
   * @return An instrument definition for this node
   */
  public InstrumentDefinition<?> getDefinitionForNode(final CurveNode node, final ExternalId marketDataId, final ZonedDateTime now, final SnapshotDataBundle marketValues, //CSIGNORE
      final HistoricalTimeSeriesBundle timeSeries) {
    ArgumentChecker.notNull(node, "node");
    ArgumentChecker.notNull(now, "now");
    ArgumentChecker.notNull(marketValues, "market values");
    ArgumentChecker.notNull(timeSeries, "time series");
    final Double marketData = marketValues.getDataPoint(marketDataId);
    if (marketDataId != null && marketData == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + marketDataId);
    }
    final CurveNodeVisitor<InstrumentDefinition<?>> nodeVisitor = new CurveNodeConverterVisitor(_conventionSource, _holidaySource, _regionSource, marketData, now, timeSeries);
    return node.accept(nodeVisitor);
  }

}
