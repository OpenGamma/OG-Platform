/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.generator;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is interpolated on the discount factor. 
 * Only the lastTimeCalculator is stored. The node are computed from the instruments.
 */
public class GeneratorCurveDiscountFactorInterpolated extends GeneratorYDCurve {

  /**
   * Calculator of the node associated to instruments.
   */
  private final AbstractInstrumentDerivativeVisitor<Object, Double> _nodeTimeCalculator;
  /**
   * The interpolator used for the discount factors.
   */
  private final Interpolator1D _interpolator;

  /**
   * Constructor.
   * @param nodeTimeCalculator Calculator of the node associated to instruments.
   * @param interpolator The interpolator used for the curve.
   */
  public GeneratorCurveDiscountFactorInterpolated(AbstractInstrumentDerivativeVisitor<Object, Double> nodeTimeCalculator, Interpolator1D interpolator) {
    _nodeTimeCalculator = nodeTimeCalculator;
    _interpolator = interpolator;
  }

  @Override
  public int getNumberOfParameter() {
    throw new UnsupportedOperationException("Cannot return the number of parameter for a GeneratorCurveYieldInterpolated");
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolated");
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, YieldCurveBundle bundle, double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolated");
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, MulticurveProviderInterface multicurve, double[] parameters) {
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveYieldInterpolated");
  }

  @Override
  public GeneratorYDCurve finalGenerator(Object data) {
    ArgumentChecker.isTrue(data instanceof InstrumentDerivative[], "data should be an array of InstrumentDerivative");
    InstrumentDerivative[] instruments = (InstrumentDerivative[]) data;
    double[] node = new double[instruments.length];
    for (int loopins = 0; loopins < instruments.length; loopins++) {
      node[loopins] = _nodeTimeCalculator.visit(instruments[loopins]);
    }
    return new GeneratorCurveDiscountFactorInterpolatedNode(node, _interpolator);
  }

}
