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
import com.opengamma.core.link.ConventionLink;
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
  public ThreeLegBasisSwapNodeConverter(HolidaySource holidaySource,
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
  @Deprecated
  public ThreeLegBasisSwapNodeConverter(SecuritySource securitySource, ConventionSource conventionSource,
                                        HolidaySource holidaySource, RegionSource regionSource,
                                        SnapshotDataBundle marketData, ExternalId dataId,
                                        ZonedDateTime valuationTime) {
    this(holidaySource, regionSource, marketData, dataId, valuationTime);
  }

  @Override
  public InstrumentDefinition<?> visitThreeLegBasisSwapNode(ThreeLegBasisSwapNode threeLegBasisSwapNode) {

    FinancialConvention payLegConvention =
        ConventionLink.resolvable(threeLegBasisSwapNode.getPayLegConvention(), FinancialConvention.class).resolve();
    FinancialConvention receiveLegConvention =
        ConventionLink.resolvable(threeLegBasisSwapNode.getReceiveLegConvention(), FinancialConvention.class).resolve();
    FinancialConvention spreadLegConvention =
        ConventionLink.resolvable(threeLegBasisSwapNode.getSpreadLegConvention(), FinancialConvention.class).resolve();

    Period startTenor = threeLegBasisSwapNode.getStartTenor().getPeriod();
    Period maturityTenor = threeLegBasisSwapNode.getMaturityTenor().getPeriod();
    AnnuityDefinition<?>[] legs = new AnnuityDefinition[3];
    legs[0] = NodeConverterUtils.getSwapLeg(
        spreadLegConvention, startTenor, maturityTenor, _regionSource, _holidaySource,
        _marketData, _dataId, _valuationTime, true, false, false, 1.0); // Spread leg
    legs[1] = NodeConverterUtils.getSwapLeg(
        payLegConvention, startTenor, maturityTenor, _regionSource, _holidaySource,
        _marketData, _dataId, _valuationTime, true, false, false, 1.0); // Leg associated to spread (same pay/receive)
    legs[2] = NodeConverterUtils.getSwapLeg(
        receiveLegConvention, startTenor, maturityTenor, _regionSource, _holidaySource,
        _marketData, _dataId, _valuationTime, false, false, false, 1.0); // Other leg
    return new SwapMultilegDefinition(legs);
  }

}
