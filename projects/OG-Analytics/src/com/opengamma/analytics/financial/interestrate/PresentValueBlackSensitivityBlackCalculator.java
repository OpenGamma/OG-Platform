/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.util.surface.SurfaceValue;

/**
 * Present value sensitivity to SABR parameters calculator for interest rate instruments using SABR volatility formula.
 */
public final class PresentValueBlackSensitivityBlackCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, SurfaceValue> {

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
  public SurfaceValue visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

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

}
