/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Class that constructs a {@link CurveNodeWithIdentifier} given a curve node and node id mapper.
 */
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

  @Override
  public CurveNodeWithIdentifier visitCashNode(final CashNode node) {
    final ExternalId identifier = _nodeIdMapper.getCashNodeId(_curveDate, node.getMaturityTenor());
    return new CurveNodeWithIdentifier(node, identifier);
  }

  @Override
  public CurveNodeWithIdentifier visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node) {
    final ExternalId identifier = _nodeIdMapper.getContinuouslyCompoundedRateNodeId(_curveDate, node.getTenor());
    return new CurveNodeWithIdentifier(node, identifier);
  }

  @Override
  public CurveNodeWithIdentifier visitCreditSpreadNode(final CreditSpreadNode node) {
    final ExternalId identifier = _nodeIdMapper.getCreditSpreadNodeId(_curveDate, node.getTenor());
    return new CurveNodeWithIdentifier(node, identifier);
  }

  @Override
  public CurveNodeWithIdentifier visitDiscountFactorNode(final DiscountFactorNode node) {
    final ExternalId identifier = _nodeIdMapper.getDiscountFactorNodeId(_curveDate, node.getTenor());
    return new CurveNodeWithIdentifier(node, identifier);
  }

  @Override
  public CurveNodeWithIdentifier visitFRANode(final FRANode node) {
    final ExternalId identifier = _nodeIdMapper.getFRANodeId(_curveDate, node.getFixingEnd());
    return new CurveNodeWithIdentifier(node, identifier);
  }

  @Override
  public CurveNodeWithIdentifier visitRateFutureNode(final RateFutureNode node) {
    final ExternalId identifier = _nodeIdMapper.getRateFutureNodeId(_curveDate, node.getStartTenor(), node.getFutureTenor(), node.getFutureNumber());
    return new CurveNodeWithIdentifier(node, identifier);
  }

  @Override
  public CurveNodeWithIdentifier visitSwapNode(final SwapNode node) {
    final ExternalId identifier = _nodeIdMapper.getSwapNodeId(_curveDate, node.getMaturityTenor());
    return new CurveNodeWithIdentifier(node, identifier);
  }

}