/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;

/**
 * 
 */
public class SubtractCurveSpreadFunction implements CurveSpreadFunction {
  private static final String NAME = "-";

  @Override
  public Function<Double, Double> evaluate(final Curve<Double, Double>... curves) {
    Validate.notNull(curves, "x");
    Validate.notEmpty(curves, "curves");
    return new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... x) {
        Validate.notNull(x, "x");
        Validate.notEmpty(x, "x");
        double x0 = x[0];
        double y = curves[0].getYValue(x0);
        for (int i = 1; i < curves.length; i++) {
          y -= curves[i].getYValue(x0);
        }
        return y;
      }

    };
  }

  public String getOperationName() {
    return NAME;
  }
}
