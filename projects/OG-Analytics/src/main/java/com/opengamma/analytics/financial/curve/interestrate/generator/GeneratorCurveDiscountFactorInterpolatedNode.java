/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.generator;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is interpolated on the discount factor.
 */
public class GeneratorCurveDiscountFactorInterpolatedNode extends GeneratorYDCurve {

  /**
   * The nodes (times) on which the interpolated curves is constructed.
   */
  private final double[] _nodePoints;
  /**
   * The interpolator used for the curve.
   */
  private final Interpolator1D _interpolator;
  /**
   * The number of points (or nodes). Is the length of _nodePoints.
   */
  private final int _nbPoints;

  /**
   * Constructor.
   * @param nodePoints The node points (X) used to define the interpolated curve. 
   * @param interpolator The interpolator.
   */
  public GeneratorCurveDiscountFactorInterpolatedNode(double[] nodePoints, Interpolator1D interpolator) {
    ArgumentChecker.notNull(nodePoints, "Node points");
    ArgumentChecker.notNull(interpolator, "Interpolator");
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
    return new DiscountCurve(name, new InterpolatedDoublesCurve(_nodePoints, x, _interpolator, true, name));
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, YieldCurveBundle bundle, double[] parameters) {
    return generateCurve(name, parameters);
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, MulticurveProviderInterface multicurve, double[] parameters) {
    return generateCurve(name, parameters);
  }

  @Override
  public double[] initialGuess(double[] rates) {
    ArgumentChecker.isTrue(rates.length == _nbPoints, "Rates of incorrect length.");
    double[] discountFactor = new double[_nbPoints];
    for (int loopnode = 0; loopnode < _nbPoints; loopnode++) {
      discountFactor[loopnode] = Math.exp(-_nodePoints[loopnode] * rates[loopnode]);
    }
    return discountFactor;
  }

}
