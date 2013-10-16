/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * InstrumentDerivativeVisitor that calculates delta, the first derivative of the price with respect to the price of the underlying future.
 * <p>
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class PresentValueBlackThetaForSecurityCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {
  private static final PresentValueBlackThetaForSecurityCalculator INSTANCE = new PresentValueBlackThetaForSecurityCalculator();
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod IR_FUTURE_OPTION = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();

  public static PresentValueBlackThetaForSecurityCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double delta = IR_FUTURE_OPTION.optionPriceTheta(security, (YieldCurveWithBlackCubeBundle) curves);
    return delta;
  }

}
