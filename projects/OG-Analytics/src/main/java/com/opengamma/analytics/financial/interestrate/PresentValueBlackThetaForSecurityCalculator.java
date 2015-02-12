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
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * InstrumentDerivativeVisitor that calculates theta, the first derivative of the price with respect to time.
 * <p>
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class PresentValueBlackThetaForSecurityCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {
  private static final PresentValueBlackThetaForSecurityCalculator INSTANCE = new PresentValueBlackThetaForSecurityCalculator();
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod MARGINED_IR_FUTURE_OPTION = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();
  private static final InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod PREMIUM_IR_FUTURE_OPTION = InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();

  public static PresentValueBlackThetaForSecurityCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double theta = MARGINED_IR_FUTURE_OPTION.optionPriceTheta(security, (YieldCurveWithBlackCubeBundle) curves);
    return theta;
  }

  @Override
  public Double visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double theta = PREMIUM_IR_FUTURE_OPTION.optionPriceTheta(security, (YieldCurveWithBlackCubeBundle) curves);
    return theta;
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double theta = MARGINED_IR_FUTURE_OPTION.optionPriceTheta(security.getUnderlyingSecurity(), (YieldCurveWithBlackCubeBundle) curves);
    return theta;
  }

  @Override
  public Double visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    final double theta = PREMIUM_IR_FUTURE_OPTION.optionPriceTheta(security.getUnderlyingSecurity(), (YieldCurveWithBlackCubeBundle) curves);
    return theta;
  }
}
