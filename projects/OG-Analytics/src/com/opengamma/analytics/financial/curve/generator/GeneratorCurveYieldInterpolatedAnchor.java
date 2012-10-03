/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.generator;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.market.description.IMarketBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is interpolated on the rate (continuously compounded). 
 * One extra node with value zero is added at the mid point between the first and second point. This extra anchor is required when two translation invariant curves descriptions
 * are added in a spread curve (two translations would create a singular system).
 * TODO Change to have the anchor point flexible.
 * Only the lastTimeCalculator is stored. The node are computed from the instruments.
 */
public class GeneratorCurveYieldInterpolatedAnchor extends GeneratorYDCurve {

  /**
   * Calculator of the node associated to instruments.
   */
  private final AbstractInstrumentDerivativeVisitor<Object, Double> _nodeTimeCalculator;
  /**
   * The interpolator used for the curve.
   */
  private final Interpolator1D _interpolator;

  /**
   * Constructor.
   * @param nodeTimeCalculator Calculator of the node associated to instruments.
   * @param interpolator The interpolator used for the curve.
   */
  public GeneratorCurveYieldInterpolatedAnchor(AbstractInstrumentDerivativeVisitor<Object, Double> nodeTimeCalculator, Interpolator1D interpolator) {
    _nodeTimeCalculator = nodeTimeCalculator;
    _interpolator = interpolator;
  }

  @Override
  public int getNumberOfParameter() {
    throw new UnsupportedOperationException("Cannot return the number of parameter for a GeneratorCurveYieldInterpolated");
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolatedAnchor");
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, YieldCurveBundle bundle, double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolatedAnchor");
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, IMarketBundle bundle, double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolatedAnchor");
  }

  /**
   * The data passed should be one instrument for the anchor then one instrument for each of the nodes.
   * @param data The array of instruments.
   * @return The final generator.
   */
  @Override
  public GeneratorYDCurve finalGenerator(Object data) {
    ArgumentChecker.isTrue(data instanceof InstrumentDerivative[], "data should be an array of InstrumentDerivative");
    InstrumentDerivative[] instruments = (InstrumentDerivative[]) data;
    double[] node = new double[instruments.length - 1];
    for (int loopins = 0; loopins < instruments.length - 1; loopins++) {
      node[loopins] = _nodeTimeCalculator.visit(instruments[loopins + 1]);
    }
    double anchor = _nodeTimeCalculator.visit(instruments[0]);
    return new GeneratorCurveYieldInterpolatedAnchorNode(node, anchor, _interpolator);
  }

}
