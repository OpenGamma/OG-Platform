/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.UnderlyingMarketPriceSTIRFutureOptionCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * InstrumentDerivativeVisitor that calculates the underlying future price.
 * @deprecated [@link YieldCurveBundle} is deprecated. Use {@link UnderlyingMarketPriceSTIRFutureOptionCalculator}.
 */
@Deprecated
public class UnderlyingMarketPriceCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  private static final UnderlyingMarketPriceCalculator INSTANCE = new UnderlyingMarketPriceCalculator();

  public static UnderlyingMarketPriceCalculator getInstance() {
    return INSTANCE;
  }

  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod IR_FUTURE_OPTION = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    final double underlyingPrice = IR_FUTURE_OPTION.underlyingFuturePrice(security, curves);
    return underlyingPrice;
  }
}
