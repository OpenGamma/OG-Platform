/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.strips;

import com.opengamma.id.ExternalId;

/**
 * Visitor interface for curve nodes.
 * @param <T> The return type for the visitor
 */
public interface CurveNodeWithExternalIdVisitor<T> {

  /**
   * Visits a {@link CashNode}
   * @param node A cash node
   * @param externalId External ID
   * @return The return value
   */
  T visitCashNode(CashNode node, ExternalId externalId);

  /**
   * Visits a {@link ContinuouslyCompoundedRateNode}
   * @param node A continuously compounded rate node
   * @param externalId External ID
   * @return The return value
   */
  T visitContinuouslyCompoundedRateNode(ContinuouslyCompoundedRateNode node, ExternalId externalId);

  /**
   * Visits a {@link CreditSpreadNode}
   * @param node A credit spread node
   * @param externalId External ID
   * @return The return value
   */
  T visitCreditSpreadNode(CreditSpreadNode node, ExternalId externalId);

  /**
   * Visits a {@link DeliverableSwapFutureNode}
   * @param node A deliverable swap future node
   * @param externalId External ID
   * @return The return value
   */
  T visitDeliverableSwapFutureNode(DeliverableSwapFutureNode node, ExternalId externalId);

  /**
   * Visits a {@link DiscountFactorNode}
   * @param node A discount factor node
   * @param externalId External ID
   * @return The return value
   */
  T visitDiscountFactorNode(DiscountFactorNode node, ExternalId externalId);

  /**
   * Visits a {@link FRANode}
   * @param node A FRA node
   * @param externalId External ID
   * @return The return value
   */
  T visitFRANode(FRANode node, ExternalId externalId);

  /**
   * Visits a {@link FXForwardNode}
   * @param node A FX forward node
   * @param externalId External ID
   * @return The return value
   */
  T visitFXForwardNode(FXForwardNode node, ExternalId externalId);

  /**
   * Visits a {@link RateFutureNode}
   * @param node A rate future node
   * @param externalId External ID
   * @return The return value
   */
  T visitRateFutureNode(RateFutureNode node, ExternalId externalId);

  /**
   * Visits a {@link SwapNode}
   * @param node A swap node
   * @param externalId External ID
   * @return The return value
   */
  T visitSwapNode(SwapNode node, ExternalId externalId);

  /**
   * Visits a {@link ZeroCouponInflationNode}
   * @param node A zero-coupon inflation node
   * @param externalId External ID
   * @return The return value
   */
  T visitZeroCouponInflationNode(ZeroCouponInflationNode node, ExternalId externalId);
}
