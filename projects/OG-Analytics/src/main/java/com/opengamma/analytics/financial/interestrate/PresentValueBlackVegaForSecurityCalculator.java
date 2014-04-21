/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureOptionPremiumSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * InstrumentDerivativeVisitor that calculates vega, the first derivative of the price with respect to the implied vol of the underlying future.
 * <p>
 * See also {@link PresentValueBlackVegaCalculator}
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class PresentValueBlackVegaForSecurityCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {
  private static final PresentValueBlackVegaForSecurityCalculator INSTANCE = new PresentValueBlackVegaForSecurityCalculator();
  private static final BondFutureOptionPremiumSecurityBlackSurfaceMethod PREMIUM_BOND_FUTURE_OPTION = BondFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod MARGINED_IR_FUTURE_OPTION = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();
  private static final InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod PREMIUM_IR_FUTURE_OPTION = InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();
  public static PresentValueBlackVegaForSecurityCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public Double visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double vega = PREMIUM_BOND_FUTURE_OPTION.optionPriceVega(security, (YieldCurveWithBlackCubeBundle) curves);
    final double unitNotional = security.getUnderlyingFuture().getNotional();
    return vega * unitNotional;
  }

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double vega = MARGINED_IR_FUTURE_OPTION.optionPriceVega(security, (YieldCurveWithBlackCubeBundle) curves);
    return vega;
  }

  @Override
  public Double visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double vega = PREMIUM_IR_FUTURE_OPTION.optionPriceVega(security, (YieldCurveWithBlackCubeBundle) curves);
    return vega;
  }
}
