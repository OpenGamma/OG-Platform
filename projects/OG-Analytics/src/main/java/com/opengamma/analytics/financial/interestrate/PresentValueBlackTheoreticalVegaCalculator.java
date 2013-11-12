/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * InstrumentDerivativeVisitor that calculates theoretical vega
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class PresentValueBlackTheoreticalVegaCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {
  /** A static instance */
  private static final PresentValueBlackTheoreticalVegaCalculator INSTANCE = new PresentValueBlackTheoreticalVegaCalculator();
  /** The margined interest rate future option calculator */
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod MARGINED_IR_FUTURE_OPTION = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();
  /** The interest rate future option with premium calculator */
  private static final InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod PREMIUM_IR_FUTURE_OPTION = InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();

  /**
   * Gets an instance.
   * @return The instance
   */
  public static PresentValueBlackTheoreticalVegaCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction transaction, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return MARGINED_IR_FUTURE_OPTION.optionPriceVega(transaction.getUnderlyingOption(), (YieldCurveWithBlackCubeBundle) curves);
  }


  @Override
  public Double visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction transaction, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return PREMIUM_IR_FUTURE_OPTION.optionPriceVega(transaction.getUnderlyingOption(), (YieldCurveWithBlackCubeBundle) curves);
  }
}
