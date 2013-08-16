/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionCashFixedIborBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBlackMethod;

/**
 * Interpolates, for interest rate instruments using Black model, and returns the implied volatility required.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class ImpliedVolatilityBlackCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The method unique instance.
   */
  private static final ImpliedVolatilityBlackCalculator INSTANCE = new ImpliedVolatilityBlackCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ImpliedVolatilityBlackCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  ImpliedVolatilityBlackCalculator() {
  }

  /**
   * The methods used in the calculator.
   */
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWAPTION_PHYSICAL = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  private static final SwaptionCashFixedIborBlackMethod METHOD_SWAPTION_CASH = SwaptionCashFixedIborBlackMethod.getInstance();
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod METHOD_IR_FUTURE_OPTION_TXN = InterestRateFutureOptionMarginTransactionBlackSurfaceMethod.getInstance();

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    return METHOD_SWAPTION_PHYSICAL.impliedVolatility(swaption, curves);
  }

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    return METHOD_SWAPTION_CASH.impliedVolatility(swaption, curves);
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction transaction, final YieldCurveBundle curves) {
    return METHOD_IR_FUTURE_OPTION_TXN.impliedVolatility(transaction, curves);
  }
}
