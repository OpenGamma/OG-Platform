/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.strips;

/**
 * 
 */
public interface CurveNodeVisitor<T> {

  T visitCashNode(CashNode node);

  T visitContinuouslyCompoundedRateNode(ContinuouslyCompoundedRateNode node);

  T visitCreditSpreadNode(CreditSpreadNode node);

  T visitDiscountFactorNode(DiscountFactorNode node);

  T visitFRANode(FRANode node);

  T visitRateFutureNode(RateFutureNode node);

  T visitSwapNode(SwapNode node);
}
