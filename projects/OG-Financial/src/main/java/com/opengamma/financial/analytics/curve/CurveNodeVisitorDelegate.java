/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.util.ArgumentChecker;

/**
 * Delegate class for curve node visitors
 *
 * @param <T> The return type of the visitor.
 */
public class CurveNodeVisitorDelegate<T> implements CurveNodeVisitor<T> {
  /** The delegate */
  private final CurveNodeVisitor<T> _delegate;

  /**
   * @param delegate The delegate, not null
   */
  public CurveNodeVisitorDelegate(final CurveNodeVisitor<T> delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public T visitCashNode(final CashNode node) {
    return _delegate.visitCashNode(node);
  }

  @Override
  public T visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node) {
    return _delegate.visitContinuouslyCompoundedRateNode(node);
  }

  @Override
  public T visitCreditSpreadNode(final CreditSpreadNode node) {
    return _delegate.visitCreditSpreadNode(node);
  }

  @Override
  public T visitDeliverableSwapFutureNode(final DeliverableSwapFutureNode node) {
    return _delegate.visitDeliverableSwapFutureNode(node);
  }

  @Override
  public T visitDiscountFactorNode(final DiscountFactorNode node) {
    return _delegate.visitDiscountFactorNode(node);
  }

  @Override
  public T visitFRANode(final FRANode node) {
    return _delegate.visitFRANode(node);
  }

  @Override
  public T visitFXForwardNode(final FXForwardNode node) {
    return _delegate.visitFXForwardNode(node);
  }

  @Override
  public T visitRateFutureNode(final RateFutureNode node) {
    return _delegate.visitRateFutureNode(node);
  }

  @Override
  public T visitSwapNode(final SwapNode node) {
    return _delegate.visitSwapNode(node);
  }

  @Override
  public T visitZeroCouponInflationNode(final ZeroCouponInflationNode node) {
    return _delegate.visitZeroCouponInflationNode(node);
  }

}
