/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * 
 */
public final class BondFutureOptionPremiumTransactionBlackSurfaceMethod {
  private static final BondFutureOptionPremiumTransactionBlackSurfaceMethod INSTANCE = new BondFutureOptionPremiumTransactionBlackSurfaceMethod();
  private static final BondFutureOptionPremiumSecurityBlackSurfaceMethod METHOD = BondFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();

  public static BondFutureOptionPremiumTransactionBlackSurfaceMethod getInstance() {
    return INSTANCE;
  }

  private BondFutureOptionPremiumTransactionBlackSurfaceMethod() {
  }

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param option The future option, not null
   * @param curves The curves, not null
   * @param price The quoted price
   * @return The present value.
   */
  public CurrencyAmount presentValueFromPrice(final BondFutureOptionPremiumTransaction option, final YieldCurveBundle curves, final double price) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(curves, "curves");
    final PresentValueCalculator pvc = PresentValueCalculator.getInstance();
    final double premiumPV = pvc.visit(option.getPremium(), curves);
    final double optionPV = price * option.getQuantity() * option.getUnderlyingOption().getUnderlyingFuture().getNotional();
    return CurrencyAmount.of(option.getUnderlyingOption().getCurrency(), optionPV + premiumPV);
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param curves The yield curve bundle.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final BondFutureOptionPremiumTransaction transaction, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(curves, "curves");
    final InterestRateCurveSensitivity securitySensitivity = METHOD.priceCurveSensitivity(transaction.getUnderlyingOption(), curves);
    return securitySensitivity.multiply(transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional());
  }

  public CurrencyAmount presentValue(final BondFutureOptionPremiumTransaction transaction, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(curves, "curves");
    final double priceSecurity = METHOD.optionPrice(transaction.getUnderlyingOption(), curves);
    final CurrencyAmount pvTransaction = presentValueFromPrice(transaction, curves, priceSecurity);
    return pvTransaction;
  }

  /**
   * Computes the present value gamma of a transaction.
   * This is with respect to futures rate
   * @param transaction The future option transaction.
   * @param blackData The curve and Black volatility data.
   * @return The gamma.
   */
  public double presentValueGamma(final BondFutureOptionPremiumTransaction transaction, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(blackData, "Black data");
    final double securityGamma = METHOD.optionPriceGamma(transaction.getUnderlyingOption(), blackData);
    final double txnGamma = securityGamma * transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional();
    return txnGamma;
  }

  /**
   * Computes the present value delta of a transaction.
   * This is with respect to futures rate
   * @param transaction The future option transaction.
   * @param blackData The curve and Black volatility data.
   * @return The delta.
   */
  public double presentValueDelta(final BondFutureOptionPremiumTransaction transaction, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(blackData, "Black data");
    final double securityDelta = METHOD.optionPriceDelta(transaction.getUnderlyingOption(), blackData);
    final double txnDelta = securityDelta * transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional();
    return txnDelta;
  }

  /**
   * Computes the present value vega of a transaction.
   * @param transaction The future option transaction.
   * @param blackData The curve and Black volatility data.
   * @return The delta.
   */
  public double presentValueVega(final BondFutureOptionPremiumTransaction transaction, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(blackData, "Black data");
    final double securityVega = METHOD.optionPriceVega(transaction.getUnderlyingOption(), blackData);
    final double txnVega = securityVega * transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional();
    return txnVega;
  }
}
