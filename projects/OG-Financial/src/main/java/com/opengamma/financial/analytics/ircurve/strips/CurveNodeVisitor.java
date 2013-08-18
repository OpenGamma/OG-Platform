/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.strips;

/**
 * Visitor interface for curve nodes.
 * @param <T> The return type for the visitor
 */
public interface CurveNodeVisitor<T> {

  /**
   * Visits a {@link CashNode}
   * @param node A cash node
   * @return The return value
   */
  T visitCashNode(CashNode node);

  /**
   * Visits a {@link ContinuouslyCompoundedRateNode}
   * @param node A continuously compounded rate node
   * @return The return value
   */
  T visitContinuouslyCompoundedRateNode(ContinuouslyCompoundedRateNode node);

  /**
   * Visits a {@link CreditSpreadNode}
   * @param node A credit spread node
   * @return The return value
   */
  T visitCreditSpreadNode(CreditSpreadNode node);

  /**
   * Visits a {@link DeliverableSwapFutureNode}
   * @param node A deliverable swap future node
   * @return The return value
   */
  T visitDeliverableSwapFutureNode(DeliverableSwapFutureNode node);

  /**
   * Visits a {@link DiscountFactorNode}
   * @param node A discount factor node
   * @return The return value
   */
  T visitDiscountFactorNode(DiscountFactorNode node);

  /**
   * Visits a {@link FRANode}
   * @param node A FRA node
   * @return The return value
   */
  T visitFRANode(FRANode node);

  /**
   * Visits a {@link FXForwardNode}
   * @param node A FX forward node
   * @return The return value
   */
  T visitFXForwardNode(FXForwardNode node);

  /**
   * Visits a {@link RateFutureNode}
   * @param node A rate future node
   * @return The return value
   */
  T visitRateFutureNode(RateFutureNode node);

  /**
   * Visits a {@link SwapNode}
   * @param node A swap node
   * @return The return value
   */
  T visitSwapNode(SwapNode node);

  /**
   * Visits a {@link ZeroCouponInflationNode}
   * @param node A zero-coupon inflation node
   * @return The return value
   */
  T visitZeroCouponInflationNode(ZeroCouponInflationNode node);
}
