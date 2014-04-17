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
   * Visits a {@link BillNode}.
   * @param node A bill node
   * @return The return value
   */
  T visitBillNode(BillNode node);

  /**
   * Visits a {@link BondNode}.
   * @param node A bond node
   * @return The return value
   */
  T visitBondNode(BondNode node);

  /**
   * Visits a {@link CalendarSwapNode}.
   * @param node A calendar swap node
   * @return The return value
   */
  T visitCalendarSwapNode(CalendarSwapNode node);

  /**
   * Visits a {@link CashNode}.
   * @param node A cash node
   * @return The return value
   */
  T visitCashNode(CashNode node);

  /**
   * Visits a {@link ContinuouslyCompoundedRateNode}.
   * @param node A continuously compounded rate node
   * @return The return value
   */
  T visitContinuouslyCompoundedRateNode(ContinuouslyCompoundedRateNode node);

  /**
   * Visits a {@link PeriodicallyCompoundedRateNode}.
   * @param node A periodically compounded rate node
   * @return The return value
   */
  T visitPeriodicallyCompoundedRateNode(PeriodicallyCompoundedRateNode node);

  /**
   * Visits a {@link CreditSpreadNode}.
   * @param node A credit spread node
   * @return The return value
   */
  T visitCreditSpreadNode(CreditSpreadNode node);

  /**
   * Visits a {@link DeliverableSwapFutureNode}.
   * @param node A deliverable swap future node
   * @return The return value
   */
  T visitDeliverableSwapFutureNode(DeliverableSwapFutureNode node);

  /**
   * Visits a {@link DiscountFactorNode}.
   * @param node A discount factor node
   * @return The return value
   */
  T visitDiscountFactorNode(DiscountFactorNode node);

  /**
   * Visits a {@link FRANode}.
   * @param node A FRA node
   * @return The return value
   */
  T visitFRANode(FRANode node);

  /**
   * Visits a {@link FXForwardNode}.
   * @param node A FX forward node
   * @return The return value
   */
  T visitFXForwardNode(FXForwardNode node);

  /**
   * Visits a {@link RollDateFRANode}.
   * @param node An IMM FRA node
   * @return The return value
   */
  T visitRollDateFRANode(RollDateFRANode node);

  /**
   * Visits a {@link RollDateSwapNode}.
   * @param node An IMM swap node
   * @return The return value
   */
  T visitRollDateSwapNode(RollDateSwapNode node);

  /**
   * Visits a {@link RateFutureNode}.
   * @param node A rate future node
   * @return The return value
   */
  T visitRateFutureNode(RateFutureNode node);

  /**
   * Visits a {@link SwapNode}.
   * @param node A swap node
   * @return The return value
   */
  T visitSwapNode(SwapNode node);

  /**
   * Visits a {@link ThreeLegBasisSwapNode}
   * @param node A three-leg basis swap node
   * @return The return value
   */
  T visitThreeLegBasisSwapNode(ThreeLegBasisSwapNode node);

  /**
   * Visits a {@link ZeroCouponInflationNode}.
   * @param node A zero-coupon inflation node
   * @return The return value
   */
  T visitZeroCouponInflationNode(ZeroCouponInflationNode node);
  
  /**
   * Visits an {@link ISDACashNode}
   * @param node An ISDA cash node
   * @return The return value
   */
  T visitISDACashNode(ISDACashNode node);
  
  /**
   * Visits an {@link ISDASwapNode}
   * @param node An ISDA swap node
   * @return The return value
   */
  T visitISDASwapNode(ISDASwapNode node);
}
