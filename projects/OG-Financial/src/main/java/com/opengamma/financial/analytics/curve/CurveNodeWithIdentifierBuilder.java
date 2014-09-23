/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurvePointsInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CalendarSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.FXSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.PeriodicallyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ThreeLegBasisSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Class that constructs a {@link CurveNodeWithIdentifier} given a curve node and node id mapper.
 */
//TODO make every node type work like FXForwardNode
public class CurveNodeWithIdentifierBuilder implements CurveNodeVisitor<CurveNodeWithIdentifier> {
  /** The curve construction date */
  private final LocalDate _curveDate;
  /** The curve node id mapper */
  private final CurveNodeIdMapper _nodeIdMapper;

  /**
   * @param curveDate The curve construction date, not null
   * @param nodeIdMapper The curve node id mapper, not null
   */
  public CurveNodeWithIdentifierBuilder(final LocalDate curveDate, final CurveNodeIdMapper nodeIdMapper) {
    ArgumentChecker.notNull(curveDate, "curve date");
    ArgumentChecker.notNull(nodeIdMapper, "node id mapper");
    _curveDate = curveDate;
    _nodeIdMapper = nodeIdMapper;
  }

  /**
   * Gets the curve date.
   * @return The curve date
   */
  public LocalDate getCurveDate() {
    return _curveDate;
  }

  /**
   * Gets the curve node id mapper.
   * @return The curve node id mapper
   */
  public CurveNodeIdMapper getCurveNodeIdMapper() {
    return _nodeIdMapper;
  }

  @Override
  public CurveNodeWithIdentifier visitBillNode(final BillNode node) {
    final Tenor tenor = node.getMaturityTenor();
    final ExternalId identifier = _nodeIdMapper.getBillNodeId(_curveDate, tenor);
    final String dataField = _nodeIdMapper.getBillNodeDataField(tenor);
    final DataFieldType fieldType = _nodeIdMapper.getBillNodeDataFieldType(tenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitBondNode(final BondNode node) {
    final Tenor tenor = node.getMaturityTenor();
    final ExternalId identifier = _nodeIdMapper.getBondNodeId(_curveDate, tenor);
    final String dataField = _nodeIdMapper.getBondNodeDataField(tenor);
    final DataFieldType fieldType = _nodeIdMapper.getBondNodeDataFieldType(tenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitCalendarSwapNode(final CalendarSwapNode node) {
    final Tenor startTenor = node.getStartTenor();
    final ExternalId identifier = _nodeIdMapper.getCalendarSwapNodeId(_curveDate, startTenor, node.getStartDateNumber(), node.getEndDateNumber());
    final String dataField = _nodeIdMapper.getCalendarSwapNodeDataField(startTenor);
    final DataFieldType fieldType = _nodeIdMapper.getCalendarSwapNodeDataFieldType(startTenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitCashNode(final CashNode node) {
    final Tenor tenor = node.getMaturityTenor();
    final ExternalId identifier = _nodeIdMapper.getCashNodeId(_curveDate, tenor);
    final String dataField = _nodeIdMapper.getCashNodeDataField(tenor);
    final DataFieldType fieldType = _nodeIdMapper.getCashNodeDataFieldType(tenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node) {
    final Tenor tenor = node.getTenor();
    final ExternalId identifier = _nodeIdMapper.getContinuouslyCompoundedRateNodeId(_curveDate, tenor);
    final String dataField = _nodeIdMapper.getContinuouslyCompoundedRateNodeDataField(tenor);
    final DataFieldType fieldType = _nodeIdMapper.getContinuouslyCompoundedRateDataFieldType(tenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitPeriodicallyCompoundedRateNode(final PeriodicallyCompoundedRateNode node) {
    final Tenor tenor = node.getTenor();
    final ExternalId identifier = _nodeIdMapper.getPeriodicallyCompoundedRateNodeId(_curveDate, tenor);
    final String dataField = _nodeIdMapper.getPeriodicallyCompoundedRateNodeDataField(tenor);
    final DataFieldType fieldType = _nodeIdMapper.getPeriodicallyCompoundedRateDataFieldType(tenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitCreditSpreadNode(final CreditSpreadNode node) {
    final Tenor tenor = node.getTenor();
    final ExternalId identifier = _nodeIdMapper.getCreditSpreadNodeId(_curveDate, tenor);
    final String dataField = _nodeIdMapper.getCreditSpreadNodeDataField(tenor);
    final DataFieldType fieldType = _nodeIdMapper.getCreditSpreadNodeDataFieldType(tenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitDeliverableSwapFutureNode(final DeliverableSwapFutureNode node) {
    final Tenor startTenor = node.getStartTenor();
    final ExternalId identifier = _nodeIdMapper.getDeliverableSwapFutureNodeId(_curveDate,
                                                                               startTenor,
                                                                               node.getFutureTenor(),
                                                                               node.getFutureNumber());
    final String dataField = _nodeIdMapper.getDeliverableSwapFutureNodeDataField(startTenor);
    final DataFieldType fieldType = _nodeIdMapper.getDeliverableSwapFutureNodeDataFieldType(startTenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitDiscountFactorNode(final DiscountFactorNode node) {
    final Tenor tenor = node.getTenor();
    final ExternalId identifier = _nodeIdMapper.getDiscountFactorNodeId(_curveDate, tenor);
    final String dataField = _nodeIdMapper.getDiscountFactorNodeDataField(tenor);
    final DataFieldType fieldType = _nodeIdMapper.getDiscountFactorNodeDataFieldType(tenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitFRANode(final FRANode node) {
    final Tenor tenor = node.getFixingEnd();
    final ExternalId identifier = _nodeIdMapper.getFRANodeId(_curveDate, tenor);
    final String dataField = _nodeIdMapper.getFRANodeDataField(tenor);
    final DataFieldType fieldType = _nodeIdMapper.getFRANodeDataFieldType(tenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitFXForwardNode(final FXForwardNode node) {
    final Map<Tenor, CurveInstrumentProvider> ids = _nodeIdMapper.getFXForwardNodeIds();
    final Tenor tenor = node.getMaturityTenor();
    CurveInstrumentProvider curveInstrumentProvider = ids.get(tenor);
    if (curveInstrumentProvider instanceof StaticCurvePointsInstrumentProvider) {
      return createCurveNodeWithIdentifier(curveInstrumentProvider, tenor, node);
    } else {
      final ExternalId identifier = _nodeIdMapper.getFXForwardNodeId(_curveDate, tenor);
      final String dataField = _nodeIdMapper.getFXForwardNodeDataField(tenor);
      final DataFieldType fieldType = _nodeIdMapper.getFXForwardNodeDataFieldType(tenor);
      return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
    }
  }

  @Override
  public CurveNodeWithIdentifier visitFXSwapNode(final FXSwapNode node) {
    final Map<Tenor, CurveInstrumentProvider> ids = _nodeIdMapper.getFXSwapNodeIds();
    final Tenor tenor = node.getMaturityTenor();
    CurveInstrumentProvider curveInstrumentProvider = ids.get(tenor);
    if (curveInstrumentProvider instanceof StaticCurvePointsInstrumentProvider) {
      return createCurveNodeWithIdentifier(curveInstrumentProvider, tenor, node);
    } else {
      final ExternalId identifier = _nodeIdMapper.getFXSwapNodeId(_curveDate, tenor);
      final String dataField = _nodeIdMapper.getFXSwapNodeDataField(tenor);
      final DataFieldType fieldType = _nodeIdMapper.getFXSwapNodeDataFieldType(tenor);
      return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
    }
  }

  private CurveNodeWithIdentifier createCurveNodeWithIdentifier(CurveInstrumentProvider provider,
                                                                Tenor tenor, CurveNode node) {
    // Cast is safe as already validated by caller
    StaticCurvePointsInstrumentProvider pointsInstrumentProvider = (StaticCurvePointsInstrumentProvider) provider;
    ExternalId identifier = pointsInstrumentProvider.getInstrument(_curveDate, tenor);
    String dataField = pointsInstrumentProvider.getMarketDataField();
    ExternalId underlyingId = pointsInstrumentProvider.getUnderlyingInstrument();
    String underlyingField = pointsInstrumentProvider.getUnderlyingMarketDataField();
    return new PointsCurveNodeWithIdentifier(
        node, identifier, dataField, DataFieldType.POINTS, underlyingId, underlyingField);
  }

  @Override
  public CurveNodeWithIdentifier visitRollDateFRANode(final RollDateFRANode node) {
    final Tenor startTenor = node.getStartTenor();
    final ExternalId identifier = _nodeIdMapper.getIMMFRANodeId(_curveDate, startTenor, node.getRollDateStartNumber(), node.getRollDateEndNumber());
    final String dataField = _nodeIdMapper.getIMMFRANodeDataField(startTenor);
    final DataFieldType fieldType = _nodeIdMapper.getIMMFRANodeDataFieldType(startTenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitRollDateSwapNode(final RollDateSwapNode node) {
    final Tenor startTenor = node.getStartTenor();
    final ExternalId identifier = _nodeIdMapper.getIMMSwapNodeId(_curveDate, startTenor, node.getRollDateStartNumber(), node.getRollDateEndNumber());
    final String dataField = _nodeIdMapper.getIMMSwapNodeDataField(startTenor);
    final DataFieldType fieldType = _nodeIdMapper.getIMMSwapNodeDataFieldType(startTenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitRateFutureNode(final RateFutureNode node) {
    final Tenor startTenor = node.getStartTenor();
    final ExternalId identifier = _nodeIdMapper.getRateFutureNodeId(_curveDate, startTenor, node.getFutureTenor(), node.getFutureNumber());
    final String dataField = _nodeIdMapper.getRateFutureNodeDataField(startTenor);
    final DataFieldType fieldType = _nodeIdMapper.getRateFutureNodeDataFieldType(startTenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitSwapNode(final SwapNode node) {
    final Tenor tenor = node.getMaturityTenor();
    final ExternalId identifier = _nodeIdMapper.getSwapNodeId(_curveDate, tenor);
    final String dataField = _nodeIdMapper.getSwapNodeDataField(tenor);
    final DataFieldType fieldType = _nodeIdMapper.getSwapNodeDataFieldType(tenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitThreeLegBasisSwapNode(final ThreeLegBasisSwapNode node) {
    final Tenor tenor = node.getMaturityTenor();
    final ExternalId identifier = _nodeIdMapper.getThreeLegBasisSwapNodeId(_curveDate, tenor);
    final String dataField = _nodeIdMapper.getThreeLegBasisSwapNodeDataField(tenor);
    final DataFieldType fieldType = _nodeIdMapper.getThreeLegBasisSwapNodeDataFieldType(tenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

  @Override
  public CurveNodeWithIdentifier visitZeroCouponInflationNode(final ZeroCouponInflationNode node) {
    final Tenor tenor = node.getTenor();
    final ExternalId identifier = _nodeIdMapper.getZeroCouponInflationNodeId(_curveDate, tenor);
    final String dataField = _nodeIdMapper.getZeroCouponInflationNodeDataField(tenor);
    final DataFieldType fieldType = _nodeIdMapper.getZeroCouponInflationNodeDataFieldType(tenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }

}
