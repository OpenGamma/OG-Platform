/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureOptionPremiumTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * InstrumentDerivativeVisitor that calculates position delta, the delta of a Transaction, Trade or Position.<p>
 * See also {@link PresentValueBlackDeltaForSecurityCalculator}
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class PresentValueBlackDeltaForTransactionCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {
  private static final PresentValueBlackDeltaForTransactionCalculator INSTANCE = new PresentValueBlackDeltaForTransactionCalculator();
  private static final BondFutureOptionPremiumTransactionBlackSurfaceMethod PREMIUM_BOND_FUTURE_OPTION = BondFutureOptionPremiumTransactionBlackSurfaceMethod.getInstance();
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod IR_FUTURE_OPTION = InterestRateFutureOptionMarginTransactionBlackSurfaceMethod.getInstance();
  public static PresentValueBlackDeltaForTransactionCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public Double visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction transaction, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return PREMIUM_BOND_FUTURE_OPTION.presentValueDelta(transaction, (YieldCurveWithBlackCubeBundle) curves);
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction transaction, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return IR_FUTURE_OPTION.deltaWrtFuturesPrice(transaction, (YieldCurveWithBlackCubeBundle) curves);
  }

}
