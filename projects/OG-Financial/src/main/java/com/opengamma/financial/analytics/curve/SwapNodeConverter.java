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
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Convert a swap node into an Instrument definition.
 * The dates of the swap are computed in the following way:
 * - The spot date is computed from the valuation date adding the "Settlement Days" (i.e. the number of business days) of the convention.
 * - The start date is computed from the spot date adding the "StartTenor" of the node and using the business-day-convention, calendar and EOM of the convention.
 * - The end date is computed from the start date adding the "MaturityTenor" of the node and using Annuity constructor.
 * The swap notional for each leg is 1.
 * A fixed leg always has the market quote as fixed rate.
 * If both legs are floating (VanillaIborLegConvention or OISLegConvention), the receive leg has a spread equal to the market quote.
 */
public class SwapNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
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
  public SwapNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
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
  public InstrumentDefinition<?> visitSwapNode(final SwapNode swapNode) {
    final FinancialConvention payLegConvention = _conventionSource.getConvention(FinancialConvention.class, swapNode.getPayLegConvention());
    if (payLegConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + swapNode.getPayLegConvention() + " was null");
    }
    final FinancialConvention receiveLegConvention =  _conventionSource.getConvention(FinancialConvention.class, swapNode.getReceiveLegConvention());
    if (receiveLegConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + swapNode.getReceiveLegConvention() + " was null");
    }
    final Period startTenor = swapNode.getStartTenor().getPeriod();
    final Period maturityTenor = swapNode.getMaturityTenor().getPeriod();
    return NodeConverterUtils.getSwapDefinition(payLegConvention, receiveLegConvention, startTenor, maturityTenor, _regionSource,
        _holidaySource, _conventionSource, _marketData, _dataId, _valuationTime);
  }
  
}
