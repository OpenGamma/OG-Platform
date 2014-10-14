/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.math.curve.DoublesCurveInterpolatedAnchor;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is interpolated on the rate (continuously compounded).
 * One extra node with value zero is added at the mid point between the first and second point. This extra anchor is required when two translation invariant curves descriptions
 * are added in a spread curve (two translations would create a singular system).
 */
public class GeneratorPriceIndexCurveInterpolatedAnchorNode extends GeneratorPriceIndexCurve {

  /** The nodes (times) on which the interpolated curves is constructed. Does not include the extra anchor node. */
  private final double[] _nodePoints;
  /** The anchor node point. */
  private final double _anchorNode;
  /** The curve value at the anchor node point. */
  private final double _anchorValue;
  /** The interpolator used for the curve. */
  private final Interpolator1D _interpolator;
  /** The number of points (or nodes), not including the extra anchor. */
  private final int _nbPoints;

  /**
   * Constructor.
   * @param nodePoints The node points (X) used to define the interpolated curve.
   * @param interpolator The interpolator.
   * @param anchorNode The anchor node point.
   * @param anchorValue The anchor value at the anchor node point.
   */
  public GeneratorPriceIndexCurveInterpolatedAnchorNode(final double[] nodePoints, final Interpolator1D interpolator, 
      final double anchorNode, final double anchorValue) {
    ArgumentChecker.notNull(nodePoints, "Node points");
    ArgumentChecker.notNull(interpolator, "Interpolator");
    _nbPoints = nodePoints.length;
    _nodePoints = nodePoints;
    _anchorNode = anchorNode;
    _anchorValue = anchorValue;
    _interpolator = interpolator;
  }

  @Override
  public int getNumberOfParameter() {
    return _nbPoints;
  }

  /**
   * Gets the node points.
   * @return The nodes
   */
  public double[] getNodePoints() {
    return _nodePoints;
  }

  /**
   * Gets the anchor node.
   * @return The node
   */
  public double getAnchorNode() {
    return _anchorNode;
  }

  /**
   * Gets the anchor value.
   * @return The value
   */
  public double getAnchorValue() {
    return _anchorValue;
  }

  @Override
  public PriceIndexCurveSimple generateCurve(final String name, final double[] x) {
    ArgumentChecker.isTrue(x.length == _nbPoints, "Incorrect dimension for the rates");
    ArgumentChecker.notNull(name, "name");
    return new PriceIndexCurveSimple(DoublesCurveInterpolatedAnchor.from(_nodePoints, x, _anchorNode, _anchorValue, 
        _interpolator, name));
  }

  @Override
  public PriceIndexCurve generateCurve(final String name, final InflationProviderInterface multicurve, final double[] parameters) {
    return generateCurve(name, parameters);
  }

}
