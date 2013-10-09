/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.generator;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is constant on the rate (continuously compounded).
 */
@SuppressWarnings("deprecation")
public class GeneratorCurveYieldConstant extends GeneratorYDCurve {

  @Override
  public int getNumberOfParameter() {
    return 1;
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final double[] x) {
    ArgumentChecker.isTrue(x.length == 1, "Constant curve should have one parameter");
    return new YieldCurve(name, new ConstantDoublesCurve(x[0], name));
  }

  /**
   * {@inheritDoc}
   * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
   */
  @Deprecated
  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final YieldCurveBundle bundle, final double[] parameters) {
    return generateCurve(name, parameters);
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final MulticurveProviderInterface multicurve, final double[] parameters) {
    return generateCurve(name, parameters);
  }

}
