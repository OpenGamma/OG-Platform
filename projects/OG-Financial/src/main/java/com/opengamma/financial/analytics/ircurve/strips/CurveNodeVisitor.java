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
   * @param node A cash node
   * @return The return value
   */
  T visitCashNode(CashNode node);

  /**
   * @param node A continuously compounded rate node
   * @return The return value
   */
  T visitContinuouslyCompoundedRateNode(ContinuouslyCompoundedRateNode node);

  /**
   * @param node A credit spread node
   * @return The return value
   */
  T visitCreditSpreadNode(CreditSpreadNode node);

  /**
   * @param node A discount factor node
   * @return The return value
   */
  T visitDiscountFactorNode(DiscountFactorNode node);

  /**
   * @param node A FRA node
   * @return The return value
   */
  T visitFRANode(FRANode node);

  /**
   * @param node A FX forward node
   * @return The return value
   */
  T visitFXForwardNode(FXForwardNode node);

  /**
   * @param node A rate future node
   * @return The return value
   */
  T visitRateFutureNode(RateFutureNode node);

  /**
   * @param node A swap node
   * @return The return value
   */
  T visitSwapNode(SwapNode node);
}
