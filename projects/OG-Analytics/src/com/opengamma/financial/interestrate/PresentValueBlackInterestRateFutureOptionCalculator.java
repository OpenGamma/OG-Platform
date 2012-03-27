/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Present value calculator for interest rate instruments using the Black function.
 */
public final class PresentValueBlackInterestRateFutureOptionCalculator extends PresentValueCalculator {

  /**
   * The method unique instance.
   */
  private static final PresentValueBlackInterestRateFutureOptionCalculator INSTANCE = new PresentValueBlackInterestRateFutureOptionCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PresentValueBlackInterestRateFutureOptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  PresentValueBlackInterestRateFutureOptionCalculator() {
  }

  /**
   * Methods.
   */
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod METHOD_MARGIN = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity irFutureOption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(irFutureOption, "interest rate future option");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      final YieldCurveWithBlackCubeBundle curvesBlack = (YieldCurveWithBlackCubeBundle) curves;
      return METHOD_MARGIN.optionPrice(irFutureOption, curvesBlack);
    }
    throw new UnsupportedOperationException("The PresentValueBlackSwaptionCalculator visitor visitSwaptionCashFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

}
