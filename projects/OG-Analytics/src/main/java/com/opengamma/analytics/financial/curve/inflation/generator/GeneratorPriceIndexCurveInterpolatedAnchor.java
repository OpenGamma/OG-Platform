/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is interpolated on the price index.
 * One extra node with a given value is added at the given anchor node.
 * This extra anchor is required when two translation invariant curves descriptions
 * are added in a spread curve (two translations would create a singular system).
 * Only the nodeTimeCalculator is stored. The node are computed from the instruments.
 */
public class GeneratorPriceIndexCurveInterpolatedAnchor extends GeneratorPriceIndexCurve {

  /** Calculator of the node associated to instruments. */
  private final InstrumentDerivativeVisitorAdapter<Object, Double> _nodeTimeCalculator;
  /** The interpolator used for the curve. */
  private final Interpolator1D _interpolator;
  /** The anchor node, i.e. the X value of the node used to anchor the curve. */
  private final double _anchorNode;
  /** The anchor value, i.e. the Y value at the node used to anchor the curve. */
  private final double _anchorValue;

  /**
   * Constructor.
   * @param nodeTimeCalculator Calculator of the node associated to instruments.
   * @param interpolator The interpolator used for the curve.
   * @param anchorNode The anchor node.
   * @param anchorValue The anchor value.
   */
  public GeneratorPriceIndexCurveInterpolatedAnchor(
      final InstrumentDerivativeVisitorAdapter<Object, Double> nodeTimeCalculator, final Interpolator1D interpolator,
      final double anchorNode, final double anchorValue) {
    ArgumentChecker.notNull(nodeTimeCalculator, "node time calculator");
    ArgumentChecker.notNull(interpolator, "interpolator");
    _nodeTimeCalculator = nodeTimeCalculator;
    _interpolator = interpolator;
    _anchorNode = anchorNode;
    _anchorValue = anchorValue;
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
  public int getNumberOfParameter() {
    throw new UnsupportedOperationException("Cannot return the number of parameter for a GeneratorPriceIndexInterpolatedAnchor");
  }

  @Override
  public PriceIndexCurve generateCurve(final String name, final double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorPriceIndexInterpolatedAnchor");
  }

  @Override
  public PriceIndexCurve generateCurve(final String name, final InflationProviderInterface multicurve, final double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolatedAnchor");
  }

  /**
   * The data passed should be one instrument for the anchor then one instrument for each of the nodes.
   * @param data The array of instruments.
   * @return The final generator.
   */
  @Override
  public GeneratorPriceIndexCurve finalGenerator(final Object data) {
    ArgumentChecker.isTrue(data instanceof InstrumentDerivative[], "data should be an array of InstrumentDerivative");
    final InstrumentDerivative[] instruments = (InstrumentDerivative[]) data;
    final double[] node = new double[instruments.length];
    for (int loopins = 0; loopins < instruments.length; loopins++) {
      node[loopins] = instruments[loopins].accept(_nodeTimeCalculator);
    }
    return new GeneratorPriceIndexCurveInterpolatedAnchorNode(node, _interpolator, _anchorNode, _anchorValue);
  }

}
