/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.google.common.base.Function;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 *
 */
public class CurveManipulator {

  public CurveManipulator parallelShift(double shift) {
    return this;
  }

  public CurveManipulator transform(Function<YieldAndDiscountCurve, YieldAndDiscountCurve> fn) {
    return this;
  }
}
