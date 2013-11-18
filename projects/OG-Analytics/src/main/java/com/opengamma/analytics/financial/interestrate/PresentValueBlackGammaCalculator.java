/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureOptionPremiumSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculator of the value gamma, second order derivative of present value with respect to the futures rate,
 * for InterestRateFutureOptions in the Black world.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class PresentValueBlackGammaCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackGammaCalculator INSTANCE = new PresentValueBlackGammaCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBlackGammaCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  PresentValueBlackGammaCalculator() {
  }

  /** Calculator for margined interest rate future option securities */
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod MARGINED_IR_FUTURE_OPTION_SEC = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();
  /** Calculation for bond future option securities with premium */
  private static final BondFutureOptionPremiumSecurityBlackSurfaceMethod PREMIUM_BOND_FUTURE_OPTION_SEC = BondFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction transaction, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return MARGINED_IR_FUTURE_OPTION_SEC.optionPriceGamma(transaction.getUnderlyingOption(), (YieldCurveWithBlackCubeBundle) curves);
  }

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return MARGINED_IR_FUTURE_OPTION_SEC.optionPriceGamma(security, (YieldCurveWithBlackCubeBundle) curves);
  }

  @Override
  public Double visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(option, "option");
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      final InterestRateFutureOptionPremiumSecurity underlyingOption = option.getUnderlyingOption();
      final InterestRateFutureOptionMarginSecurity underlyingMarginedOption = new InterestRateFutureOptionMarginSecurity(underlyingOption.getUnderlyingFuture(), underlyingOption.getExpirationTime(),
          underlyingOption.getStrike(), underlyingOption.isCall());
      final InterestRateFutureOptionMarginTransaction margined = new InterestRateFutureOptionMarginTransaction(underlyingMarginedOption, option.getQuantity(), option.getTradePrice());
      return MARGINED_IR_FUTURE_OPTION_SEC.optionPriceGamma(margined.getUnderlyingOption(), (YieldCurveWithBlackCubeBundle) curves);
    }
    throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitInterestRateFutureOptionPremiumTransaction requires a YieldCurveWithBlackCubeBundle as data.");
  }

  @Override
  public Double visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction transaction, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return PREMIUM_BOND_FUTURE_OPTION_SEC.optionPriceGamma(transaction.getUnderlyingOption(), (YieldCurveWithBlackCubeBundle) curves);
  }

}
