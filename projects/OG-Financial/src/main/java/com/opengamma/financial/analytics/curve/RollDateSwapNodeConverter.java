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
import com.opengamma.core.link.ConventionLink;
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
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   */
  public RollDateSwapNodeConverter(HolidaySource holidaySource,
                                   RegionSource regionSource,
                                   SnapshotDataBundle marketData,
                                   ExternalId dataId,
                                   ZonedDateTime valuationTime) {
    _holidaySource = ArgumentChecker.notNull(holidaySource, "holidaySource");
    _regionSource = ArgumentChecker.notNull(regionSource, "regionSource");
    _marketData = ArgumentChecker.notNull(marketData, "marketData");
    _dataId = ArgumentChecker.notNull(dataId, "dataId");
    _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
  }

  /**
   * @param securitySource The security source, not required
   * @param conventionSource The convention source, not required
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   * @deprecated use constructor without securitySource and conventionSource
   */
  public RollDateSwapNodeConverter(SecuritySource securitySource, ConventionSource conventionSource,
                                   HolidaySource holidaySource, RegionSource regionSource,
      SnapshotDataBundle marketData, ExternalId dataId, ZonedDateTime valuationTime) {
    this(holidaySource, regionSource, marketData, dataId, valuationTime);
  }

  @Override
  public InstrumentDefinition<?> visitRollDateSwapNode(RollDateSwapNode rollDateSwapNode) {

    RollDateSwapConvention swapConvention =
        ConventionLink.resolvable(rollDateSwapNode.getRollDateSwapConvention(), RollDateSwapConvention.class).resolve();
    FinancialConvention payLegConvention =
        ConventionLink.resolvable(swapConvention.getPayLegConvention(), FinancialConvention.class).resolve();
    FinancialConvention receiveLegConvention =
        ConventionLink.resolvable(swapConvention.getReceiveLegConvention(), FinancialConvention.class).resolve();
    RollDateAdjuster adjuster = RollDateAdjusterFactory.getAdjuster(swapConvention.getRollDateConvention().getValue());
    ZonedDateTime unadjustedStartDate = _valuationTime.plus(rollDateSwapNode.getStartTenor().getPeriod());
    return NodeConverterUtils.getSwapRollDateDefinition(
        payLegConvention, receiveLegConvention, unadjustedStartDate, rollDateSwapNode.getRollDateStartNumber(),
        rollDateSwapNode.getRollDateEndNumber(), adjuster, _regionSource, _holidaySource, _marketData,
        _dataId, _valuationTime);
  }
  
}
