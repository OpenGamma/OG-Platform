/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.DoublesCurveInterpolatedAnchor;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is interpolated on the rate (continuously compounded).
 * One extra node with value zero is added at the mid point between the first and second point. This extra anchor is required when two translation invariant curves descriptions
 * are added in a spread curve (two translations would create a singular system).
 */
public class GeneratorCurveYieldInterpolatedAnchorNode extends GeneratorCurve {

  /**
   * The nodes (times) on which the interpolated curves is constructed. Does not include the extra anchor node.
   */
  private final double[] _nodePoints;
  /**
   * The anchor point.
   */
  private final double _anchor;
  /**
   * The interpolator used for the curve.
   */
  private final Interpolator1D _interpolator;
  /**
   * The number of points (or nodes), not including the extra anchor.
   */
  private final int _nbPoints;

  /**
   * Constructor.
   * @param nodePoints The node points (X) used to define the interpolated curve. The extra anchor node (half way between first and second one) will be added.
   * @param anchor The anchor with zero value.
   * @param interpolator The interpolator.
   */
  public GeneratorCurveYieldInterpolatedAnchorNode(double[] nodePoints, double anchor, Interpolator1D interpolator) {
    ArgumentChecker.notNull(nodePoints, "Node points");
    ArgumentChecker.notNull(interpolator, "Interpolator");
    _nbPoints = nodePoints.length;
    _nodePoints = nodePoints;
    _anchor = anchor;
    _interpolator = interpolator;
  }

  @Override
  public int getNumberOfParameter() {
    return _nbPoints;
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, double[] x) {
    ArgumentChecker.isTrue(x.length == _nbPoints, "Incorrect dimension for the rates");
    return new YieldCurve(name, DoublesCurveInterpolatedAnchor.from(_nodePoints, x, _anchor, _interpolator, name));
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, YieldCurveBundle bundle, double[] parameters) {
    return generateCurve(name, parameters);
  }

}
