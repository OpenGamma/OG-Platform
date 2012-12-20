/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public final class ConstantSpreadYieldCurveRolldownFunction implements RolldownFunction<YieldAndDiscountCurve> {
  private static final ConstantSpreadYieldCurveRolldownFunction INSTANCE = new ConstantSpreadYieldCurveRolldownFunction();

  public static ConstantSpreadYieldCurveRolldownFunction getInstance() {
    return INSTANCE;
  }

  private ConstantSpreadYieldCurveRolldownFunction() {
  }

  @Override
  public YieldAndDiscountCurve rollDown(final YieldAndDiscountCurve yieldCurve, final double time) {
    ArgumentChecker.notNull(yieldCurve, "yield curve");
    if (!(yieldCurve instanceof YieldCurve)) {
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }
    final Curve<Double, Double> curve = ((YieldCurve) yieldCurve).getCurve();
    final Function1D<Double, Double> shiftedFunction = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double t) {
        return curve.getYValue(t + time);
      }

    };
    return YieldCurve.from(FunctionalDoublesCurve.from(shiftedFunction));
  }

}
