/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public final class ForwardSlideYieldCurveRolldownFunction implements RolldownFunction<YieldAndDiscountCurve> {
  private static final ForwardSlideYieldCurveRolldownFunction INSTANCE = new ForwardSlideYieldCurveRolldownFunction();

  public static ForwardSlideYieldCurveRolldownFunction getInstance() {
    return INSTANCE;
  }

  private ForwardSlideYieldCurveRolldownFunction() {
  }

  @Override
  public YieldAndDiscountCurve rollDown(final YieldAndDiscountCurve yieldCurve, final double time) {
    ArgumentChecker.notNull(yieldCurve, "yield curve");
    return yieldCurve;
  }

}
