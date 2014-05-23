/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapMultilegDefinition;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.ThreeLegBasisSwapNode;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Convert a three-leg basis swap node into an Instrument definition.
 * The dates of the swap are computed in the following way:
 * <p>
 * - The spot date is computed from the valuation date adding the "Settlement Days" (i.e. the number of business days) of the convention.<br>
 * - The start date is computed from the spot date adding the "StartTenor" of the node and using the business-day-convention, calendar and EOM of the convention.<br>
 * - The end date is computed from the start date adding the "MaturityTenor" of the node and using Annuity constructor.<br>
 * The swap notional for each leg is 1.
 */
public class ThreeLegBasisSwapNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
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
  public ThreeLegBasisSwapNodeConverter(final SecuritySource securitySource, final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
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
  public InstrumentDefinition<?> visitThreeLegBasisSwapNode(final ThreeLegBasisSwapNode threeLegBasisSwapNode) {
    final FinancialConvention payLegConvention = _conventionSource.getSingle(threeLegBasisSwapNode.getPayLegConvention(), FinancialConvention.class);
    final FinancialConvention receiveLegConvention = _conventionSource.getSingle(threeLegBasisSwapNode.getReceiveLegConvention(), FinancialConvention.class);
    final FinancialConvention spreadLegConvention = _conventionSource.getSingle(threeLegBasisSwapNode.getSpreadLegConvention(), FinancialConvention.class);
    final Period startTenor = threeLegBasisSwapNode.getStartTenor().getPeriod();
    final Period maturityTenor = threeLegBasisSwapNode.getMaturityTenor().getPeriod();
    final AnnuityDefinition<?>[] legs = new AnnuityDefinition[3];
    legs[0] = NodeConverterUtils.getSwapLeg(spreadLegConvention, startTenor, maturityTenor, _securitySource, _regionSource, _holidaySource, _conventionSource, 
        _marketData, _dataId, _valuationTime, true, false, false, 1.0); // Spread leg
    legs[1] = NodeConverterUtils.getSwapLeg(payLegConvention, startTenor, maturityTenor, _securitySource, _regionSource, _holidaySource, _conventionSource, 
        _marketData, _dataId, _valuationTime, true, false, false, 1.0); // Leg associated to the spread (same pay/receive)
    legs[2] = NodeConverterUtils.getSwapLeg(receiveLegConvention, startTenor, maturityTenor, _securitySource, _regionSource, _holidaySource, _conventionSource, 
        _marketData, _dataId, _valuationTime, false, false, false, 1.0); // Other leg
    return new SwapMultilegDefinition(legs);
  }

}
