/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.UnderlyingMarketPriceSTIRFutureOptionCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * InstrumentDerivativeVisitor that calculates the underlying future price.
 * @deprecated [@link YieldCurveBundle} is deprecated. Use {@link UnderlyingMarketPriceSTIRFutureOptionCalculator}.
 */
@Deprecated
public class UnderlyingMarketPriceCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {
  /** A static instance */
  private static final UnderlyingMarketPriceCalculator INSTANCE = new UnderlyingMarketPriceCalculator();

  /**
   * Gets a static instance.
   * @return An instance
   */
  public static UnderlyingMarketPriceCalculator getInstance() {
    return INSTANCE;
  }

  /** Calculator for margined interest rate future options */
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod MARGINED_IR_FUTURE_OPTION = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();
  /** Calculator for premium interest rate future options */
  private static final InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod PREMIUM_IR_FUTURE_OPTION = InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    final double underlyingPrice = MARGINED_IR_FUTURE_OPTION.underlyingFuturePrice(security, curves);
    return underlyingPrice;
  }

  @Override
  public Double visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    final double underlyingPrice = PREMIUM_IR_FUTURE_OPTION.underlyingFuturePrice(security, curves);
    return underlyingPrice;
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    return MARGINED_IR_FUTURE_OPTION.underlyingFuturePrice(security.getUnderlyingSecurity(), curves);
  }

  @Override
  public Double visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    return PREMIUM_IR_FUTURE_OPTION.underlyingFuturePrice(security.getUnderlyingOption(), curves);
  }
}
