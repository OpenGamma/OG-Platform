/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureOptionPremiumSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * InstrumentDerivativeVisitor that calculates delta, the first derivative of the price with respect to the price of the underlying future.
 * <p>
 * See also {@link PresentValueBlackDeltaForTransactionCalculator}
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class PresentValueBlackDeltaForSecurityCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {
  /** A static instance */
  private static final PresentValueBlackDeltaForSecurityCalculator INSTANCE = new PresentValueBlackDeltaForSecurityCalculator();
  /** Bond future option with premium calculation methods */
  private static final BondFutureOptionPremiumSecurityBlackSurfaceMethod PREMIUM_BOND_FUTURE_OPTION = BondFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();
  /** Interest rate future option with margin calculation methods */
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod MARGINED_IR_FUTURE_OPTION = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();
  /** Interest rate future option with premium calculation methods */
  private static final InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod PREMIUM_IR_FUTURE_OPTION = InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();

  /**
   * Gets a static instance
   * @return An instance
   */
  public static PresentValueBlackDeltaForSecurityCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public Double visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double delta = PREMIUM_BOND_FUTURE_OPTION.optionPriceDelta(security, (YieldCurveWithBlackCubeBundle) curves);
    return delta;
  }

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double delta = MARGINED_IR_FUTURE_OPTION.optionPriceDelta(security, (YieldCurveWithBlackCubeBundle) curves);
    return delta;
  }

  @Override
  public Double visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double delta = PREMIUM_IR_FUTURE_OPTION.optionPriceDelta(security, (YieldCurveWithBlackCubeBundle) curves);
    return delta;
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return MARGINED_IR_FUTURE_OPTION.optionPriceDelta(security.getUnderlyingOption(), (YieldCurveWithBlackCubeBundle) curves);
  }

  @Override
  public Double visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return PREMIUM_IR_FUTURE_OPTION.optionPriceDelta(security.getUnderlyingOption(), (YieldCurveWithBlackCubeBundle) curves);
  }
}
