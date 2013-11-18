/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Produces a yield curve that has been shifted forward in time.
 */
public final class ForwardSlideYieldCurveRolldownFunction implements RolldownFunction<YieldAndDiscountCurve> {
  /** The singleton instance */
  private static final ForwardSlideYieldCurveRolldownFunction INSTANCE = new ForwardSlideYieldCurveRolldownFunction();

  /**
   * Gets the singleton instance.
   * @return The instance.
   */
  public static ForwardSlideYieldCurveRolldownFunction getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private ForwardSlideYieldCurveRolldownFunction() {
  }

  @Override
  public YieldAndDiscountCurve rollDown(final YieldAndDiscountCurve yieldCurve, final double time) {
    ArgumentChecker.notNull(yieldCurve, "yield curve");
    return yieldCurve;
  }

}
