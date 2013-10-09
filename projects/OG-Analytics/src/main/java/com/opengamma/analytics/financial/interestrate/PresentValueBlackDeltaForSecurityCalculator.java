/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureOptionPremiumSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
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
  private static final PresentValueBlackDeltaForSecurityCalculator INSTANCE = new PresentValueBlackDeltaForSecurityCalculator();
  private static final BondFutureOptionPremiumSecurityBlackSurfaceMethod PREMIUM_BOND_FUTURE_OPTION = BondFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod IR_FUTURE_OPTION = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();
  public static PresentValueBlackDeltaForSecurityCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public Double visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double delta = PREMIUM_BOND_FUTURE_OPTION.optionPriceDelta(security, (YieldCurveWithBlackCubeBundle) curves);
    final double unitNotional = security.getUnderlyingFuture().getNotional();
    return delta * unitNotional;
  }

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double delta = IR_FUTURE_OPTION.optionPriceDelta(security, (YieldCurveWithBlackCubeBundle) curves);
    return delta;
  }

}
