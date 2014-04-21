/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.convention.RollDateSwapConvention;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class RollDateSwapNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
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
  public RollDateSwapNodeConverter(final SecuritySource securitySource, final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
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
  public InstrumentDefinition<?> visitRollDateSwapNode(final RollDateSwapNode rollDateSwapNode) {
    final RollDateSwapConvention swapConvention = _conventionSource.getSingle(rollDateSwapNode.getRollDateSwapConvention(), RollDateSwapConvention.class);
    final FinancialConvention payLegConvention = _conventionSource.getSingle(swapConvention.getPayLegConvention(), FinancialConvention.class);
    final FinancialConvention receiveLegConvention = _conventionSource.getSingle(swapConvention.getReceiveLegConvention(), FinancialConvention.class);
    final RollDateAdjuster adjuster = RollDateAdjusterFactory.getAdjuster(swapConvention.getRollDateConvention().getValue());
    final ZonedDateTime unadjustedStartDate = _valuationTime.plus(rollDateSwapNode.getStartTenor().getPeriod());
    return NodeConverterUtils.getSwapRollDateDefinition(payLegConvention, receiveLegConvention, unadjustedStartDate, rollDateSwapNode.getRollDateStartNumber(),
        rollDateSwapNode.getRollDateEndNumber(), adjuster, _securitySource, _regionSource, _holidaySource, _conventionSource, _marketData, _dataId, _valuationTime);
  }
  
}
