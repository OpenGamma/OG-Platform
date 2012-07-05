/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is constant on the rate (continuously compounded).
 */
public class GeneratorCurveYieldConstant implements GeneratorCurve {

  @Override
  public int getNumberOfParameter() {
    return 1;
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, double[] x) {
    ArgumentChecker.isTrue(x.length == 1, "Constant curve should have one parameter");
    return new YieldCurve(name, new ConstantDoublesCurve(x[0]));
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, YieldCurveBundle bundle, double[] parameters) {
    return generateCurve(name, parameters);
  }

}
