/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.example.curveconstruction;

// @export "imports"
import java.io.PrintStream;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;

/**
 * Example for yield curve
 */
public class YieldCurveExample {

  // @export constantYieldCurveDemo
  //CSOFF
  public static double y = 0.02;
  //CSON

  public static void constantYieldCurveDemo(PrintStream out) {
    DoublesCurve curve = new ConstantDoublesCurve(y);
    YieldCurve yieldCurve = YieldCurve.from(curve);

    out.println(yieldCurve.getInterestRate(1.0));
    out.println(yieldCurve.getInterestRate(2.0));
    out.println(yieldCurve.getInterestRate(10.0));

    out.println(yieldCurve.getDiscountFactor(1.0));
    out.println(yieldCurve.getDiscountFactor(2.0));
    out.println(yieldCurve.getDiscountFactor(10.0));
  }

  // @export yieldCurveBundleDemo
  public static void yieldCurveBundleDemo(PrintStream out) {
    DoublesCurve curve = new ConstantDoublesCurve(y);
    YieldCurve yieldCurve = YieldCurve.from(curve);

    YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve("Constant 2% Yield Curve", yieldCurve);

    out.println(bundle.getAllNames());

    assert bundle.containsName("Constant 2% Yield Curve");
    assert bundle.getCurve("Constant 2% Yield Curve").equals(yieldCurve);
  }
}
