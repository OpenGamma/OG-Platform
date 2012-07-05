/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is interpolated on the rate (continuously compounded).
 */
public class GeneratorCurveYieldInterpolated implements GeneratorCurve {

  private final double[] _nodePoints;
  private final Interpolator1D _interpolator;
  private final int _nbPoints;

  /**
   * Constructor.
   * @param nodePoints The node points (X) used to define the interpolated curve. 
   * @param interpolator The interpolator.
   */
  public GeneratorCurveYieldInterpolated(double[] nodePoints, Interpolator1D interpolator) {
    _nodePoints = nodePoints;
    _nbPoints = _nodePoints.length;
    _interpolator = interpolator;
  }

  @Override
  public int getNumberOfParameter() {
    return _nbPoints;
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, double[] x) {
    ArgumentChecker.isTrue(x.length == _nbPoints, "Incorrect dimension for the rates");
    return new YieldCurve(name, new InterpolatedDoublesCurve(_nodePoints, x, _interpolator, true));
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, YieldCurveBundle bundle, double[] parameters) {
    return generateCurve(name, parameters);
  }

}
