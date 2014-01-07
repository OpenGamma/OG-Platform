/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityIRSCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class BondFutureOptionPremiumTransactionBlackSurfaceMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFutureOptionPremiumTransactionBlackSurfaceMethod INSTANCE = new BondFutureOptionPremiumTransactionBlackSurfaceMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFutureOptionPremiumTransactionBlackSurfaceMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFutureOptionPremiumTransactionBlackSurfaceMethod() {
  }

  /**
   * The methods and calculators used.
   */
  private static final BondFutureOptionPremiumSecurityBlackSurfaceMethod METHOD_SECURITY = BondFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();
  private static final PresentValueMCACalculator PVC = PresentValueMCACalculator.getInstance();
  private static final PresentValueCurveSensitivityIRSCalculator PVCSC = PresentValueCurveSensitivityIRSCalculator.getInstance();

  /**
   * Compute the present value of a bond future option transaction from the quoted option price.
   * @param option The future option, not null
   * @param curves The curves, not null
   * @param price The quoted price
   * @return The present value.
   */
  public CurrencyAmount presentValueFromPrice(final BondFutureOptionPremiumTransaction option, final YieldCurveBundle curves, final double price) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(curves, "curves");
    final Currency ccy = option.getUnderlyingOption().getCurrency();
    final CurrencyAmount premiumPV = option.getPremium().accept(PVC, curves).getCurrencyAmount(ccy);
    final double optionPV = price * option.getQuantity() * option.getUnderlyingOption().getUnderlyingFuture().getNotional();
    return premiumPV.plus(optionPV);
  }

  /**
   * Computes the present value of a bond future option. The option security price is computed according to the data available in the bundle.
   * If the underlying future price is available it is used, if not it is computed from the curves.
   * @param transaction The option transaction.
   * @param curves The curves bundle.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final BondFutureOptionPremiumTransaction transaction, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(curves, "curves");
    final double priceSecurity = METHOD_SECURITY.optionPrice(transaction.getUnderlyingOption(), curves);
    final CurrencyAmount pvTransaction = presentValueFromPrice(transaction, curves, priceSecurity);
    return pvTransaction;
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
    final InterestRateCurveSensitivity premiumSensitivity = PVCSC.visit(transaction.getPremium(), curves);
    final InterestRateCurveSensitivity securitySensitivity = METHOD_SECURITY.priceCurveSensitivity(transaction.getUnderlyingOption(), curves);
    return premiumSensitivity.plus(securitySensitivity.multipliedBy(transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()));
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
    final double securityGamma = METHOD_SECURITY.optionPriceGamma(transaction.getUnderlyingOption(), blackData);
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
    final double securityDelta = METHOD_SECURITY.optionPriceDelta(transaction.getUnderlyingOption(), blackData);
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
    final double securityVega = METHOD_SECURITY.optionPriceVega(transaction.getUnderlyingOption(), blackData);
    final double txnVega = securityVega * transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional();
    return txnVega;
  }

}
