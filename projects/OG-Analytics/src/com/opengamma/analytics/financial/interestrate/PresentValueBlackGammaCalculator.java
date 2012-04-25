/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.ArgumentChecker;

import org.apache.commons.lang.Validate;

/**
 * Calculator of the value gamma, second order derivative of present value with respect to the futures rate, 
 * for InterestRateFutureOptions in the Black world.
 */
public class PresentValueBlackGammaCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {

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

  /**
  * The methods used in the calculator.
  */
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod METHOD_OPTIONFUTURESMARGIN_BLACK = InterestRateFutureOptionMarginTransactionBlackSurfaceMethod.getInstance();

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction transaction, final YieldCurveBundle curves) {
    Validate.notNull(transaction);
    Validate.notNull(curves);
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return METHOD_OPTIONFUTURESMARGIN_BLACK.presentValueGamma(transaction, (YieldCurveWithBlackCubeBundle) curves);
  }

}
