/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.curve.BillNodeConverter;
import com.opengamma.financial.analytics.curve.BondNodeConverter;
import com.opengamma.financial.analytics.curve.CashNodeConverter;
import com.opengamma.financial.analytics.curve.CurveNodeVisitorAdapter;
import com.opengamma.financial.analytics.curve.FRANodeConverter;
import com.opengamma.financial.analytics.curve.RateFutureNodeConverter;
import com.opengamma.financial.analytics.curve.RollDateFRANodeConverter;
import com.opengamma.financial.analytics.curve.RollDateSwapNodeConverter;
import com.opengamma.financial.analytics.curve.SwapNodeConverter;
import com.opengamma.financial.analytics.curve.ThreeLegBasisSwapNodeConverter;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.FXSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ThreeLegBasisSwapNode;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Helper class to create instrument definitions for curve nodes. Internally this uses
 * the visitor pattern as that is what was used in the previous code. However, this
 * leads to the odd situation where the visitor being created already has knowledge of
 * the node it will be visited by. This suggests there may be a better approach.
 */
public class CurveNodeInstrumentDefinitionFactory {

  private final SecuritySource _securitySource;
  private final ConventionSource _conventionSource;
  private final ConventionBundleSource _conventionBundleSource;
  private final LegalEntitySource _legalEntitySource;
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;

  /**
   * Create the factory.
   *
   * @param securitySource the securitySource
   * @param conventionSource the convention source
   * @param holidaySource the holiday source
   * @param regionSource the region source
   * @param conventionBundleSource the convention bundle source
   * @param legalEntitySource the legal entity source
   */
  public CurveNodeInstrumentDefinitionFactory(SecuritySource securitySource, ConventionSource conventionSource,
                                              HolidaySource holidaySource, RegionSource regionSource,
                                              ConventionBundleSource conventionBundleSource,
                                              LegalEntitySource legalEntitySource) {
    _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
    _conventionSource = ArgumentChecker.notNull(conventionSource, "conventionSource");
    _holidaySource = ArgumentChecker.notNull(holidaySource, "holidaySource");
    _regionSource = ArgumentChecker.notNull(regionSource, "regionSource");
    _conventionBundleSource = ArgumentChecker.notNull(conventionBundleSource, "conventionBundleSource");
    _legalEntitySource = ArgumentChecker.notNull(legalEntitySource, "legalEntitySource");
  }

  /**
   * Creates an instrument definition for the specified node using the
   * supplied market data.
   *
   * @param nodeWithId the node to generate an instrument definition for
   * @param marketData snapshot containing the market data for the nodes
   * @param valuationTime valuation time for the calibration
   * @param fxMatrix fx matrix containing fx rates
   * @return an instrument definition for the node
   */
  public InstrumentDefinition<?> createInstrumentDefinition(CurveNodeWithIdentifier nodeWithId,
                                                            SnapshotDataBundle marketData,
                                                            ZonedDateTime valuationTime,
                                                            FXMatrix fxMatrix) {

    CurveNodeVisitor<InstrumentDefinition<?>> curveNodeVisitor =
        createCurveNodeVisitor(nodeWithId, marketData, valuationTime, fxMatrix);
    return nodeWithId.getCurveNode().accept(curveNodeVisitor);
  }

  private CurveNodeVisitor<InstrumentDefinition<?>> createCurveNodeVisitor(final CurveNodeWithIdentifier nodeWithId,
                                                                           final SnapshotDataBundle marketData,
                                                                           final ZonedDateTime valuationTime,
                                                                           final FXMatrix fxMatrix) {
    final ExternalId nodeDataId = nodeWithId.getIdentifier();

    return new CurveNodeVisitorAdapter<InstrumentDefinition<?>>() {

      @Override
      public InstrumentDefinition<?> visitFXForwardNode(FXForwardNode node) {

        PointsCurveNodeWithIdentifier pointsNodeWithId = (PointsCurveNodeWithIdentifier) nodeWithId;
        double forward = getMarketData(pointsNodeWithId.getIdentifier());
        double spot = getMarketData(pointsNodeWithId.getUnderlyingIdentifier());

        ExternalId conventionId = node.getFxForwardConvention();
        FXForwardAndSwapConvention forwardConvention =
            ConventionLink.resolvable(conventionId, FXForwardAndSwapConvention.class).resolve();

        ExternalId underlyingConventionId = forwardConvention.getSpotConvention();
        FXSpotConvention underlyingConvention =
            ConventionLink.resolvable(underlyingConventionId, FXSpotConvention.class).resolve();

        Currency payCurrency = node.getPayCurrency();
        Currency receiveCurrency = node.getReceiveCurrency();
        Tenor forwardTenor = node.getMaturityTenor();
        double payAmount = 1;
        double receiveAmount = forward + spot;
        int settlementDays = underlyingConvention.getSettlementDays();

        Calendar settlementCalendar =
            getCalendar(forwardConvention.getSettlementRegion());
        ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(valuationTime, settlementDays, settlementCalendar);
        ZonedDateTime exchangeDate = ScheduleCalculator.getAdjustedDate(
            spotDate, forwardTenor.getPeriod(), forwardConvention.getBusinessDayConvention(),
            settlementCalendar, forwardConvention.isIsEOM());

        return ForexDefinition.fromAmounts(payCurrency, receiveCurrency, exchangeDate, payAmount, -receiveAmount);
      }

      @Override
      public InstrumentDefinition<?> visitFXSwapNode(FXSwapNode node) {

        PointsCurveNodeWithIdentifier pointsNodeWithId = (PointsCurveNodeWithIdentifier) nodeWithId;
        double forward = getMarketData(pointsNodeWithId.getIdentifier());
        double spot = getMarketData(pointsNodeWithId.getUnderlyingIdentifier());

        ExternalId conventionId = node.getFxSwapConvention();
        FXForwardAndSwapConvention forwardConvention =
            ConventionLink.resolvable(conventionId, FXForwardAndSwapConvention.class).resolve();

        ExternalId underlyingConventionId = forwardConvention.getSpotConvention();
        FXSpotConvention underlyingConvention =
            ConventionLink.resolvable(underlyingConventionId, FXSpotConvention.class).resolve();

        Currency payCurrency = node.getPayCurrency();
        Currency receiveCurrency = node.getReceiveCurrency();
        Tenor forwardTenor = node.getMaturityTenor();
        int settlementDays = underlyingConvention.getSettlementDays();

        Calendar settlementCalendar = getCalendar(forwardConvention.getSettlementRegion());
        ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(valuationTime, settlementDays, settlementCalendar);
        ZonedDateTime exchangeDate = ScheduleCalculator.getAdjustedDate(
            spotDate, forwardTenor.getPeriod(), forwardConvention.getBusinessDayConvention(),
            settlementCalendar, forwardConvention.isIsEOM());

        return new ForexSwapDefinition(payCurrency, receiveCurrency, spotDate, exchangeDate, 1, spot, forward);
      }

      @Override
      public InstrumentDefinition<?> visitCashNode(CashNode node) {
        CashNodeConverter nodeConverter = new CashNodeConverter(
            _securitySource, _conventionSource, _holidaySource,
            _regionSource, marketData, nodeDataId, valuationTime);
        return nodeConverter.visitCashNode(node);
      }

      @Override
      public InstrumentDefinition<?> visitFRANode(FRANode node) {
        FRANodeConverter nodeConverter = new FRANodeConverter(
            _securitySource, _conventionSource, _holidaySource,
            _regionSource, marketData, nodeDataId, valuationTime);
        return nodeConverter.visitFRANode(node);
      }

      @Override
      public InstrumentDefinition<?> visitRollDateFRANode(RollDateFRANode node) {
        RollDateFRANodeConverter nodeConverter = new RollDateFRANodeConverter(
            _securitySource, _conventionSource, _holidaySource,
            _regionSource, marketData, nodeDataId, valuationTime);
        return nodeConverter.visitRollDateFRANode(node);
      }

      @Override
      public InstrumentDefinition<?> visitRollDateSwapNode(RollDateSwapNode node) {
        RollDateSwapNodeConverter nodeConverter = new RollDateSwapNodeConverter(
            _securitySource, _conventionSource, _holidaySource,
            _regionSource, marketData, nodeDataId, valuationTime);
        return nodeConverter.visitRollDateSwapNode(node);
      }

      @Override
      public InstrumentDefinition<?> visitRateFutureNode(RateFutureNode node) {
        RateFutureNodeConverter nodeConverter = new RateFutureNodeConverter(
            _securitySource, _conventionSource, _holidaySource,
            _regionSource, marketData, nodeDataId, valuationTime);
        return nodeConverter.visitRateFutureNode(node);
      }

      @Override
      public InstrumentDefinition<?> visitThreeLegBasisSwapNode(ThreeLegBasisSwapNode node) {
        ThreeLegBasisSwapNodeConverter nodeConverter = new ThreeLegBasisSwapNodeConverter(
            _securitySource, _conventionSource, _holidaySource, _regionSource,
            marketData, nodeDataId, valuationTime);
        return nodeConverter.visitThreeLegBasisSwapNode(node);
      }

      @Override
      public InstrumentDefinition<?> visitSwapNode(SwapNode node) {
        SwapNodeConverter nodeConverter = new SwapNodeConverter(
            _securitySource, _conventionSource, _holidaySource, _regionSource,
            marketData, nodeDataId, valuationTime, fxMatrix);
        return nodeConverter.visitSwapNode(node);
      }

      @Override
      public InstrumentDefinition<?> visitBillNode(BillNode node) {
        BillNodeConverter nodeConverter = new BillNodeConverter(
            _holidaySource, _regionSource, _securitySource, _legalEntitySource,
            marketData, nodeDataId, valuationTime);
        return nodeConverter.visitBillNode(node);
      }

      @Override
      public InstrumentDefinition<?> visitBondNode(BondNode node) {
        BondNodeConverter nodeConverter = new BondNodeConverter(
            _conventionBundleSource, _holidaySource, _regionSource, _securitySource,
            marketData, nodeDataId, valuationTime);
        return nodeConverter.visitBondNode(node);
      }

      private Calendar getCalendar(ExternalId calendarId) {
        return CalendarUtils.getCalendar(_regionSource, _holidaySource, calendarId);
      }

      private double getMarketData(ExternalId identifier) {
        Double forward = marketData.getDataPoint(identifier);
        if (forward != null) {
          return forward;
        } else {
          throw new OpenGammaRuntimeException("Could not get market data for " + identifier);
        }
      }
    };
  }

}
