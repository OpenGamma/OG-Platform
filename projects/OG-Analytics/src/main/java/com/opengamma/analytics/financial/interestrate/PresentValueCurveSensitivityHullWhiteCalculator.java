/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureHullWhiteMethod;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Present value curve sensitivity calculator for interest rate instruments using Hull-White (extended Vasicek) one factor model.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class PresentValueCurveSensitivityHullWhiteCalculator extends PresentValueCurveSensitivityCalculator {

  /**
   * The instance of the calculator.
   */
  private static final PresentValueCurveSensitivityHullWhiteCalculator INSTANCE = new PresentValueCurveSensitivityHullWhiteCalculator();

  /**
   * Return the instance of the calculator.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private PresentValueCurveSensitivityHullWhiteCalculator() {
  }

  /**
   * The methods.
   */
  private static final BondFutureHullWhiteMethod METHOD_HW = BondFutureHullWhiteMethod.getInstance();

  @Override
  public Map<String, List<DoublesPair>> visitBondFuture(final BondFuture bondFuture, final YieldCurveBundle curves) {
    return METHOD_HW.presentValueCurveSensitivity(bondFuture, curves).getSensitivities();
  }

}
