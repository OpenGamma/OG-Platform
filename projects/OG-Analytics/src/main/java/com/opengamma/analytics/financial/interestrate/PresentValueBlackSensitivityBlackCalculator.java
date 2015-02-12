/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;

/**
 * Present value sensitivity to SABR parameters calculator for interest rate instruments using SABR volatility formula.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class PresentValueBlackSensitivityBlackCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, SurfaceValue> {

  /**
   * The method unique instance.
   */
  private static final PresentValueBlackSensitivityBlackCalculator INSTANCE = new PresentValueBlackSensitivityBlackCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PresentValueBlackSensitivityBlackCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  PresentValueBlackSensitivityBlackCalculator() {
  }

  /**
   * The methods used in the calculator.
   */
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod METHOD_OPTIONFUTURESMARGIN_BLACK = InterestRateFutureOptionMarginTransactionBlackSurfaceMethod.getInstance();

  @Override
  public SurfaceValue visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction transaction, final YieldCurveBundle curves) {
    Validate.notNull(transaction);
    Validate.notNull(curves);
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      final YieldCurveWithBlackCubeBundle curvesBlack = (YieldCurveWithBlackCubeBundle) curves;
      return METHOD_OPTIONFUTURESMARGIN_BLACK.presentValueBlackSensitivity(transaction, curvesBlack);
    }
    throw new UnsupportedOperationException("The PresentValueBlackSensitivityBlackCalculator visitor visitInterestRateFutureOptionMarginTransaction requires a YieldCurveWithBlackCubeBundle as data.");
  }

  @Override
  public SurfaceValue visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(option, "option");
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      final InterestRateFutureOptionPremiumSecurity underlyingOption = option.getUnderlyingSecurity();
      final InterestRateFutureOptionMarginSecurity underlyingMarginedOption = new InterestRateFutureOptionMarginSecurity(underlyingOption.getUnderlyingFuture(), underlyingOption.getExpirationTime(),
          underlyingOption.getStrike(), underlyingOption.isCall());
      final InterestRateFutureOptionMarginTransaction margined = new InterestRateFutureOptionMarginTransaction(underlyingMarginedOption, option.getQuantity(), option.getReferencePrice());
      return METHOD_OPTIONFUTURESMARGIN_BLACK.presentValueBlackSensitivity(margined, (YieldCurveWithBlackCubeBundle) curves);
    }
    throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitInterestRateFutureOptionPremiumTransaction requires a YieldCurveWithBlackCubeBundle as data.");
  }

}
